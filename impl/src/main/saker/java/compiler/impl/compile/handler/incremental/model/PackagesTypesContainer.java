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

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface PackagesTypesContainer {
	public TypeElement getTypeElement(String name);

	public IncrementalTypeElement getParsedTypeElement(String name);

	public IncrementalTypeElement getTypeElement(ClassSignature c);

	public PackageElement getPackageElement(String name);

	public DualPackageElement getPresentPackageElement(String name);

	public TypeElement addParsedClass(ClassSignature c);

	public PackageElement addParsedPackage(PackageSignature p);

	public List<? extends Element> getPackageEnclosedNonJavacElements(String packname);

	public PackageElement forwardOverride(PackageElement javacpackage, String qualifiedname);
}
