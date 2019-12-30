package saker.java.compiler.impl.compile;

import java.util.Collection;

import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface InternalJavaCompilerOutput extends JavaCompilerWorkerTaskOutput {
	public Collection<ClassSignature> getClassSignatures();

	public Collection<PackageSignature> getPackageSignatures();

	public ModuleSignature getModuleSignature();
	
	public boolean hadAnnotationProcessors();
}
