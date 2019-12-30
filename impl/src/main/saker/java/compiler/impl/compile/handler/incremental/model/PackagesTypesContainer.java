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
