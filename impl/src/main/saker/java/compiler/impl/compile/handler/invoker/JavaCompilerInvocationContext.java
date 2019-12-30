package saker.java.compiler.impl.compile.handler.invoker;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;

import saker.build.file.SakerDirectory;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;

public interface JavaCompilerInvocationContext extends Closeable {
	public boolean isParallelProcessing();

	public Collection<String> getOptions();

	public Collection<String> getSuppressWarnings();

	public SakerDirectory getOutputClassDirectory();

	public SakerDirectory getOutputSourceDirectory();

	public SakerDirectory getOutputResourceDirectory();

	public SakerDirectory getOutputNativeHeaderDirectory();

	public Collection<SakerDirectory> getClassPathDirectories();

	public Collection<SakerDirectory> getBootClassPathDirectories();

	public Map<String, SakerDirectory> getModulePathDirectories();

	public Map<String, String> getGeneralProcessorOptions();

	public Map<ProcessorDetails, JavaAnnotationProcessor> getPassProcessorReferences();
}
