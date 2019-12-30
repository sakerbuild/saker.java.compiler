package saker.java.compiler.impl.compile.handler.info;

import java.util.NavigableMap;

import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface RealizedSignatureHolder {
	public NavigableMap<String, ? extends ClassSignature> getRealizedClasses();

	public PackageSignature getRealizedPackageSignature();

	public ModuleSignature getRealizedModuleSignature();

}