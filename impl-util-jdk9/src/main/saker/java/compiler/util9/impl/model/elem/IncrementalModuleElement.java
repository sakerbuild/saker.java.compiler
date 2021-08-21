/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.util9.impl.model.elem;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.incremental.model.DualPackageElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.DocumentedElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.ExportsDirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.OpensDirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.ProvidesDirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.RequiresDirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.UsesDirectiveSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.java.compiler.util9.impl.model.mirror.IncrementalModuleType;

public class IncrementalModuleElement extends IncrementalElement<ModuleSignature>
		implements ModifiableModuleElement, DocumentedElement<ModuleSignature> {
	private static final AtomicReferenceFieldUpdater<IncrementalModuleElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalModuleElement.class, TypeMirror.class, "asType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalModuleElement, List> ARFU_directives = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalModuleElement.class, List.class, "directives");

	private Name simpleName;
	private Name qualifiedName;

	private final ConcurrentHashMap<String, DualPackageElement> packageElements = new ConcurrentHashMap<>();
	//does not need to be concurrent collection as it is updated only from a single thread
	private final SortedMap<String, IncrementalTypeElement> canonicalTypeElements = new TreeMap<>();

	private DualPackageElement unnamedPackage;

	private volatile transient TypeMirror asType;
	private volatile transient List<Directive> directives;

	public IncrementalModuleElement(IncrementalElementsTypes9 elemTypes, ModuleSignature signature) {
		super(elemTypes, signature);
		String modulename = signature.getName();
		this.qualifiedName = new IncrementalName(modulename);
		this.simpleName = new IncrementalName(modulename.substring(modulename.indexOf('.') + 1));
	}

	@Override
	public String getDocComment() {
		return signature.getDocComment();
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitModule(this, p);
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype != null) {
			return thisastype;
		}
		thisastype = new IncrementalModuleType((IncrementalElementsTypes9) elemTypes, this);
		if (ARFU_asType.compareAndSet(this, null, thisastype)) {
			return thisastype;
		}
		return this.asType;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return ImmutableUtils.makeImmutableList(packageElements.values());
	}

	@Override
	public PackageElement getPackageElement(String name) {
		return packageElements.get(name);
	}

	@Override
	public DualPackageElement getPresentPackageElement(String name) {
		return packageElements.get(name);
	}

	@Override
	public TypeElement getTypeElement(String name) {
		return canonicalTypeElements.get(name);
	}

	@Override
	public IncrementalTypeElement getParsedTypeElement(String name) {
		return canonicalTypeElements.get(name);
	}

	@Override
	public IncrementalTypeElement getTypeElement(ClassSignature c) {
		IncrementalTypeElement result = canonicalTypeElements.get(c.getCanonicalName());
		if (result == null) {
			throw new AssertionError("Class wasn't entered: " + c.getCanonicalName());
		}
		return result;
	}

	@Override
	public IncrementalTypeElement addParsedClass(ClassSignature c) {
		DualPackageElement encpackage;
		String packname = c.getPackageName();
		if (ObjectUtils.isNullOrEmpty(packname)) {
			if (unnamedPackage == null) {
				// do not add the unnamed package to the package elements
				// the unnamed package exists, but isn't considered to be an enclososed element
				unnamedPackage = new DualPackageElement(elemTypes, this, "");
			}
			encpackage = unnamedPackage;
		} else {
			encpackage = packageElements.computeIfAbsent(packname, p -> {
				DualPackageElement result = new DualPackageElement(elemTypes, this, p);
				result.setEnclosingElement(this);
				return result;
			});
		}
		IncrementalTypeElement result = canonicalTypeElements.compute(c.getCanonicalName(), (k, v) -> {
			if (v == null) {
				return elemTypes.createIncrementalTypeElement(c);
			}
			v.setSignature(c);
			return v;
		});
		result.setEnclosingElement(encpackage);
		for (ClassSignature ec : c.getEnclosedTypes()) {
			IncrementalTypeElement ecres = addParsedClass(ec);
			ecres.setEnclosingElement(result);
		}
		return result;
	}

	@Override
	public PackageElement addParsedPackage(PackageSignature p) {
		return packageElements.compute(p.getName(), (k, v) -> {
			if (v == null) {
				DualPackageElement result = new DualPackageElement(elemTypes, this, p, null, k);
				result.setEnclosingElement(this);
				return result;
			}
			v.setSignature(p);
			return v;
		});
	}

	@Override
	public List<? extends Element> getPackageEnclosedNonJavacElements(String packname) {
		return CompilationHandler.getPackageEnclosedElements(packname, canonicalTypeElements);
	}

	@Override
	public PackageElement forwardOverrideJavacLocked(PackageElement javacpackage, String qualifiedname) {
		return null;
	}

	@Override
	public Element getEnclosingElement() {
		return null;
	}

	@Override
	public byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_MODULE;
	}
	
	@Override
	public ElementKind getKind() {
		return ElementKind.MODULE;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return Collections.emptySet();
	}

	@Override
	public Name getSimpleName() {
		return simpleName;
	}

	@Override
	public Name getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public List<? extends Directive> getDirectives() {
		List<Directive> thisdirectives = this.directives;
		if (thisdirectives != null) {
			return thisdirectives;
		}
		List<? extends DirectiveSignature> sigdirectives = signature.getDirectives();
		thisdirectives = JavaTaskUtils.cloneImmutableList(sigdirectives, this::toDirective);
		if (ARFU_directives.compareAndSet(this, null, thisdirectives)) {
			return thisdirectives;
		}
		return this.directives;
	}

	private Directive toDirective(DirectiveSignature ds) {
		IncrementalElementsTypes9 elemtypes9 = (IncrementalElementsTypes9) elemTypes;
		switch (ds.getKind()) {
			case EXPORTS:
				return new IncrementalExportsDirective(elemtypes9, (ExportsDirectiveSignature) ds, this);
			case OPENS:
				return new IncrementalOpensDirective(elemtypes9, (OpensDirectiveSignature) ds, this);
			case PROVIDES:
				return new IncrementalProvidesDirective(elemtypes9, (ProvidesDirectiveSignature) ds, this);
			case REQUIRES:
				return new IncrementalRequiresDirective(elemtypes9, (RequiresDirectiveSignature) ds, this);
			case USES:
				return new IncrementalUsesDirective(elemtypes9, (UsesDirectiveSignature) ds, this);
			default: {
				throw new IllegalArgumentException("Unknown directive signature type: " + ds.getKind());
			}
		}
	}

	@Override
	public boolean isOpen() {
		return signature.isOpen();
	}

	@Override
	public boolean isUnnamed() {
		return signature.isUnnamed();
	}

	@Override
	public String toString() {
		Name qname = getQualifiedName();
		return qname.length() == 0 ? "unnamed module" : qname.toString();
	}

}
