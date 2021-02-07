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
package saker.java.compiler.util9.impl.model;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.ModuleElement.UsesDirective;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.ImmutableElementTypeSet;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonExecutableElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingTypeMirrorBase;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingFileData;
import saker.java.compiler.impl.compile.handler.invoker.CompilationContextInformation;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.util8.impl.model.IncrementalElementsTypes8;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingDirectiveBase;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingExportsDirective;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingModuleElement;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingOpensDirective;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingProvidesDirective;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingRequiresDirective;
import saker.java.compiler.util9.impl.forwarded.elem.ForwardingUsesDirective;
import saker.java.compiler.util9.impl.forwarded.mirror.ForwardedModuleType;
import saker.java.compiler.util9.impl.model.elem.CommonModuleElement;
import saker.java.compiler.util9.impl.model.elem.IncrementalModuleElement;
import saker.java.compiler.util9.impl.model.elem.IncrementalUnnamedModuleElement;
import saker.java.compiler.util9.impl.model.elem.ModifiableModuleElement;
import saker.java.compiler.util9.impl.model.mirror.CommonModuleType;

public class IncrementalElementsTypes9 extends IncrementalElementsTypes8 {
	public static final ImmutableElementTypeSet ELEMENT_TYPE_MODULE = ImmutableElementTypeSet.of(ElementType.MODULE);

	private IncrementalUnnamedModuleElement unnamedModule;
	private ModifiableModuleElement currentModule;

	private final DirectiveForwarderVisitor directiveForwarder = new DirectiveForwarderVisitor();

	/**
	 * Guarded by {@link #javacSync}.
	 */
	private final Map<Directive, Directive> forwardedDirectives = new IdentityHashMap<>(128);
	/**
	 * Guarded by {@link #javacSync}.
	 */
	private final ConcurrentHashMap<String, ForwardingModuleElement> forwardedModuleElements = new ConcurrentHashMap<>(
			DEFAULT_FORWARDING_HASHMAP_SIZE);

	private Set<? extends ModuleElement> allModuleElements = null;

	public IncrementalElementsTypes9(Elements realelements, Object javacsync, ParserCache cache,
			CompilationContextInformation context) {
		super(realelements, javacsync, cache, context);

		unnamedModule = new IncrementalUnnamedModuleElement(this, realelements.getModuleElement(""));
	}

	@Override
	public void initCompilationModule(ClassHoldingFileData fd) {
		ModuleSignature sig = fd.getModuleSignature();
		currentModule = new IncrementalModuleElement(this, sig);
		elementsToFilesMap.put(currentModule, fd);

		finishModuleInit();
	}

	@Override
	public void initCompilationModuleNotSpecified() {
		currentModule = unnamedModule;

		finishModuleInit();
	}

	private void finishModuleInit() {
		super.packageTypesContainer = currentModule;
	}

	@Override
	public ModuleElement getCurrentModuleElement() {
		return currentModule;
	}

	@Override
	public String getCurrentModuleName() {
		return currentModule == unnamedModule ? null : currentModule.getQualifiedName().toString();
	}

	public PackageElement getCurrentModulePackageElement(String name) {
		return getPackageElement(currentModule, name);
	}

	public TypeElement getCurrentModuleTypeElement(TypeSignature sig) {
		String cname = getCanonicalName(sig, currentModule);
		if (cname == null) {
			return null;
		}
		return currentModule.getTypeElement(cname);
	}

	@Override
	public TypeElement getClassTypeElement(Class<?> clazz) {
		if (clazz.isPrimitive() || clazz.isArray()) {
			return null;
		}
		String cname = clazz.getCanonicalName();
		if (cname == null) {
			return null;
		}
		Module module = clazz.getModule();
		String modulename = module.getName();
		if (modulename == null) {
			return unnamedModule.getTypeElement(cname);
		}
		return getTypeElement(getModuleElement(modulename), cname);
	}

	@Override
	public Set<? extends ModuleElement> getAllModuleElements() {
		if (allModuleElements == null) {
			synchronized (javacSync) {
				if (allModuleElements == null) {
					Set<ModuleElement> result = new HashSet<>();
					result.add(unnamedModule);
					if (currentModule != unnamedModule) {
						result.add(currentModule);
					}
					Name currentqname = currentModule.getQualifiedName();
					synchronized (javacSync) {
						Set<? extends ModuleElement> realallmodules = realElements.getAllModuleElements();
						for (ModuleElement me : realallmodules) {
							//do not include the module with the same name from javac or the unnamed module
							if (me == unnamedModule.getJavacUnnamedModule()) {
								continue;
							}
							Name mqname = me.getQualifiedName();
							if (mqname.contentEquals(currentqname)) {
								continue;
							}
							result.add(forwardModuleElementImpl(me, mqname));
						}
					}
					allModuleElements = Collections.unmodifiableSet(result);
				}
			}
		}
		return allModuleElements;
	}

	//Default implementation is used for
	// getAllPackageElements
	// getAllTypeElements

	@Override
	public ModuleElement getModuleElement(CharSequence name) {
		if (name.length() == 0) {
			return unnamedModule;
		}
		if (currentModule.getQualifiedName().contentEquals(name)) {
			return currentModule;
		}
		ForwardingModuleElement forwarded = forwardedModuleElements.get(name.toString());
		if (forwarded != null) {
			return forwarded;
		}
		return forwardElement(() -> realElements.getModuleElement(name));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Element> E forwardElementImpl(E element, ElementKind elemkind) {
		if (elemkind == ElementKind.MODULE) {
			return (E) forwardModuleElementImpl((ModuleElement) element);
		}
		return super.forwardElementImpl(element, elemkind);
	}

	protected ModuleElement forwardModuleElementImpl(ModuleElement me) {
		return (ModuleElement) forwardedElements.computeIfAbsent(me, e -> {
			ForwardingModuleElement result = new ForwardingModuleElement(this, me);
			forwardedModuleElements.putIfAbsent(result.getQualifiedName().toString(), result);
			return result;
		});
	}

	private ModuleElement forwardModuleElementImpl(ModuleElement me, Name mqname) {
		return (ModuleElement) forwardedElements.computeIfAbsent(me, e -> {
			ForwardingModuleElement result = new ForwardingModuleElement(this, me);
			String qnamestr = mqname.toString();
			result.setQualifiedName(new IncrementalName(qnamestr));
			forwardedModuleElements.putIfAbsent(qnamestr, result);
			return result;
		});
	}

	@Override
	public ModuleElement getModuleOf(Element e) {
		return getModuleOfImpl(e);
	}

	public static ModuleElement getModuleOfImpl(Element elem) {
		while (elem.getKind() != ElementKind.MODULE) {
			Element enclosing = elem.getEnclosingElement();
			if (enclosing == null) {
				throw new IllegalStateException("Enclosing is null for: " + elem);
			}
			elem = enclosing;
		}
		return (ModuleElement) elem;
	}

	//TODO implement getOrigin methods

	@Override
	protected boolean isSameTypeUnknownKind(TypeKind kind, TypeMirror t1, TypeMirror t2) {
		switch (kind) {
			case MODULE: {
				CommonModuleType m1 = (CommonModuleType) t1;
				CommonModuleType m2 = (CommonModuleType) t2;
				return m1.getModuleElement() == m2.getModuleElement();
			}
			default: {
				return super.isSameTypeUnknownKind(kind, t1, t2);
			}
		}
	}

	@Override
	protected ForwardingTypeMirrorBase<?> forwardUnknownType(TypeKind kind, TypeMirror mirror) {
		switch (kind) {
			case MODULE: {
				return new ForwardedModuleType(this, (CommonModuleType) mirror);
			}
			default: {
				return super.forwardUnknownType(kind, mirror);
			}
		}
	}

	@Override
	public PackageElement getPackageElement(ModuleElement module, CharSequence name) {
		return ((CommonModuleElement) module).getPackageElement(name.toString());
	}

	@Override
	public TypeElement getTypeElement(ModuleElement module, CharSequence name) {
		return ((CommonModuleElement) module).getTypeElement(name.toString());
	}

	@Override
	public boolean isJavacElementBridge(ExecutableElement ee) {
		synchronized (javacSync) {
			return realElements.isBridge(ee);
		}
	}

	@Override
	public boolean isBridge(ExecutableElement e) {
		return ((CommonExecutableElement) e).isBridge();
	}

	@Override
	public PackageElement getPackageElement(CharSequence name) {
		//javadoc changed
		//    Returns:the specified package, or null if it cannot be uniquely found
		//  we return the first element we find
		String namestr = name.toString();
		PackageElement parsed = currentModule.getPresentPackageElement(namestr);
		if (parsed != null) {
			return parsed;
		}
		return forwardElementElems(el -> el.getPackageElement(namestr));
	}

	@Override
	public TypeElement getTypeElement(CharSequence name) {
		//javadoc changed
		//    Returns:the named type element, or null if it cannot be uniquely found
		//  we return the first element we find
		String namestr = name.toString();
		TypeElement parsed = currentModule.getParsedTypeElement(namestr);
		if (parsed != null) {
			return parsed;
		}
		return forwardElementElems(el -> el.getTypeElement(namestr));
	}

	public <E extends Directive> List<? extends E> forwardDirectives(Supplier<List<? extends E>> javaclistsupplier) {
		synchronized (javacSync) {
			List<? extends E> list = javaclistsupplier.get();
			int size = list.size();
			if (size == 0) {
				return Collections.emptyList();
			}
			return JavaTaskUtils.cloneImmutableList(list, this::forwardDirectiveImpl);
		}
	}

	private <E extends Directive> E forwardDirectiveImpl(E t) {
		return forwardDirectiveImpl(t, t.getKind());
	}

	@SuppressWarnings("unchecked")
	private <E extends Directive> E forwardDirectiveImpl(E t, DirectiveKind kind) {
		return (E) forwardedDirectives.computeIfAbsent(t, directive -> {
			ForwardingDirectiveBase<?> result = KindBasedDirectiveVisitor9.visit(kind, directive, directiveForwarder,
					null);
			result.setKind(kind);
			return result;
		});
	}

	private class DirectiveForwarderVisitor implements DirectiveVisitor<ForwardingDirectiveBase<?>, Void> {

		@Override
		public ForwardingDirectiveBase<?> visitExports(ExportsDirective d, Void p) {
			return new ForwardingExportsDirective(IncrementalElementsTypes9.this, d);
		}

		@Override
		public ForwardingDirectiveBase<?> visitOpens(OpensDirective d, Void p) {
			return new ForwardingOpensDirective(IncrementalElementsTypes9.this, d);
		}

		@Override
		public ForwardingDirectiveBase<?> visitProvides(ProvidesDirective d, Void p) {
			return new ForwardingProvidesDirective(IncrementalElementsTypes9.this, d);
		}

		@Override
		public ForwardingDirectiveBase<?> visitRequires(RequiresDirective d, Void p) {
			return new ForwardingRequiresDirective(IncrementalElementsTypes9.this, d);
		}

		@Override
		public ForwardingDirectiveBase<?> visitUses(UsesDirective d, Void p) {
			return new ForwardingUsesDirective(IncrementalElementsTypes9.this, d);
		}

	}
}
