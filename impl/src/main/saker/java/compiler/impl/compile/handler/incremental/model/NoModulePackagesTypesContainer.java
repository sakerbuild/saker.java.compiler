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
package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class NoModulePackagesTypesContainer implements PackagesTypesContainer {
	private final ConcurrentHashMap<String, DualPackageElement> packageElements = new ConcurrentHashMap<>();
	//does not need to be concurrent collection as it is updated only from a single thread
	private final SortedMap<String, IncrementalTypeElement> canonicalTypeElements = new TreeMap<>();

	private IncrementalElementsTypesBase elemTypes;

	public NoModulePackagesTypesContainer(IncrementalElementsTypesBase elemTypes) {
		this.elemTypes = elemTypes;
	}

	@Override
	public TypeElement getTypeElement(String name) {
		TypeElement got = canonicalTypeElements.get(name);
		if (got != null) {
			return got;
		}
		return elemTypes.getTypeElementFromRealElements(name);
	}

	@Override
	public IncrementalTypeElement getParsedTypeElement(String name) {
		return canonicalTypeElements.get(name);
	}

	@Override
	public PackageElement getPackageElement(String name) {
		return packageElements.computeIfAbsent(name, n -> {
			PackageElement javacpackage = elemTypes.javacElements(el -> el.getPackageElement(n));
			if (javacpackage != null) {
				return new DualPackageElement(elemTypes, this, null, javacpackage, n);
			}
			return null;
		});
	}

	@Override
	public PackageElement forwardOverride(PackageElement javacpackage, String qualifiedname) {
		return packageElements.compute(qualifiedname, (k, v) -> {
			if (v == null) {
				return new DualPackageElement(elemTypes, this, null, javacpackage, k);
			}
			v.setJavacElement(javacpackage);
			return v;
		});
	}

	@Override
	public IncrementalTypeElement addParsedClass(ClassSignature c) {
		String packname = c.getPackageName();
		if (packname == null) {
			packname = "";
		}
		DualPackageElement encpackage = packageElements.computeIfAbsent(packname,
				p -> new DualPackageElement(elemTypes, this, p));
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
				return new DualPackageElement(elemTypes, this, p, elemTypes.getRealElements().getPackageElement(k), k);
			}
			v.setSignature(p);
			return v;
		});
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
	public List<? extends Element> getPackageEnclosedNonJavacElements(String packname) {
		return CompilationHandler.getPackageEnclosedElements(packname, canonicalTypeElements);
	}

	@Override
	public DualPackageElement getPresentPackageElement(String name) {
		return packageElements.get(name);
	}
}
