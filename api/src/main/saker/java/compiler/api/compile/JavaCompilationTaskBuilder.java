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
package saker.java.compiler.api.compile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.StandardLocation;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.task.TaskDirectoryContext;
import saker.build.task.TaskFactory;
import saker.compiler.utils.api.CompilationIdentifier;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaClassPathBuilder;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.modulepath.JavaModulePathBuilder;
import saker.java.compiler.api.option.JavaAddExports;
import saker.sdk.support.api.SDKDescription;

/**
 * Builder class for configuring and constructing a Java compiler worker task.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Create a new instance using {@link #newBuilder()}.
 */
public interface JavaCompilationTaskBuilder {
	/**
	 * Sets the compilation identifier of the worker task to be automatically determined based on the configuration.
	 * <p>
	 * The task directory context is used to infer working directory related information for the compilation identifier.
	 * 
	 * @param taskdircontext
	 *            The task directory context or <code>null</code> if none.
	 */
	public void setAutomaticCompilationIdentifier(TaskDirectoryContext taskdircontext);

	/**
	 * Sets the compilation identifier for the worker task.
	 * 
	 * @param compilationIdentifier
	 *            The compilation identifier.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public void setCompilationIdentifier(CompilationIdentifier compilationIdentifier) throws NullPointerException;

	/**
	 * Sets the source directory for the compilation.
	 * <p>
	 * The sources that are the input of the compilation are determined based on the argument.
	 * 
	 * @param sourceDirectories
	 *            The source directories.
	 */
	public void setSourceDirectories(Collection<? extends JavaSourceDirectory> sourceDirectories);

	/**
	 * Sets the input compilation classpath.
	 * 
	 * @param classPath
	 *            The classpath.
	 * @see JavaClassPathBuilder
	 */
	public void setClassPath(JavaClassPath classPath);

	/**
	 * Sets the compilation boot classpath.
	 * 
	 * @param bootClassPath
	 *            The boot classpath.
	 * @see JavaClassPathBuilder
	 */
	public void setBootClassPath(JavaClassPath bootClassPath);

	/**
	 * Sets the module path for the compilation.
	 * <p>
	 * If the compilation targets Java 8 or earlier, the module path will be ignored.
	 * 
	 * @param modulePath
	 *            The module path.
	 * @see JavaModulePathBuilder
	 */
	public void setModulePath(JavaModulePath modulePath);

	/**
	 * Sets the parameters that are directly passed to the <code>javac</code> backed during compilation.
	 * <p>
	 * Some of the parameters may be specially handled by the compiler task. If available, it is recommended to
	 * configure the options using the appropriate builder method.
	 * 
	 * @param parameters
	 *            The parameters.
	 * @throws IllegalArgumentException
	 *             If the specified parameters are in conflict with other properties.
	 */
	public void setParameters(List<String> parameters) throws IllegalArgumentException;

	/**
	 * Sets the source version of the source files.
	 * 
	 * @param version
	 *            The source version major number or <code>null</code> to use the default.
	 * @throws IllegalArgumentException
	 *             If the specified argument is in conflict with other properties.
	 */
	public void setSourceVersion(Integer version) throws IllegalArgumentException;

	/**
	 * Sets the target version of the generated class files.
	 * 
	 * @param version
	 *            The target version major number or <code>null</code> to use the default.
	 * @throws IllegalArgumentException
	 *             If the specified argument is in conflict with other properties.
	 */
	public void setTargetVersion(Integer version) throws IllegalArgumentException;

	/**
	 * Sets the SDKs that should be used during the compilation.
	 * <p>
	 * The SDKs are used to determine the JVM that compiles the sources, to resolve class and module paths.
	 * <p>
	 * If an SDK is not specified with the name <code>Java</code>, the
	 * {@linkplain SakerJavaCompilerUtils#getDefaultJavaSDK() default} is included.
	 * 
	 * @param sdks
	 *            The SDKs to use.
	 */
	public void setSDKs(Map<String, SDKDescription> sdks);

	/**
	 * Sets whether or not native headers should be generated for the compiled classes.
	 * <p>
	 * The default is <code>false</code>.
	 * 
	 * @param generateNativeHeaders
	 *            <code>true</code> to generate native headers.
	 */
	public void setGenerateNativeHeaders(Boolean generateNativeHeaders);

	/**
	 * Sets if the parameter names should be included in the generated class files.
	 * <p>
	 * Corresponds to the <code>-parameters</code> <code>javac</code> option.
	 * <p>
	 * The default is <code>true</code>.
	 * 
	 * @param parameterNames
	 *            <code>true</code> to include parameter names.
	 */
	public void setParameterNames(Boolean parameterNames);

	/**
	 * Sets the debug informations that should be included in the generated class files.
	 * <p>
	 * Corresponds to the <code>-g</code> <code>javac</code> option.
	 * <p>
	 * The default is to include {@linkplain JavaDebugInfoType#all all} debugging information.
	 * 
	 * @param debugInfo
	 *            The collection of debug information types to include, or <code>null</code> to use the default. The
	 *            elements should be any of the contants defined in {@link JavaDebugInfoType}.
	 * @see JavaDebugInfoType
	 */
	public void setDebugInfo(Collection<String> debugInfo);

	/**
	 * Sets the add-exports configuration to pass to <code>javac</code>.
	 * 
	 * @param addExports
	 *            The add-exports configurations.
	 */
	public void setAddExports(Collection<? extends JavaAddExports> addExports);

	/**
	 * Sets the annotation processors that should be used during the compilation.
	 * 
	 * @param annotationProcessors
	 *            The processors.
	 */
	public void setAnnotationProcessors(Collection<? extends JavaAnnotationProcessor> annotationProcessors);

	/**
	 * Sets the global key-value string options that should be passed to the annotation processors.
	 * 
	 * @param annotationProcessorOptions
	 *            The options.
	 * @see ProcessingEnvironment#getOptions()
	 * @see JavaAnnotationProcessor#getOptions()
	 */
	public void setAnnotationProcessorOptions(Map<String, String> annotationProcessorOptions);

	/**
	 * Sets the read-only input locations that the annotation processors can read.
	 * <p>
	 * The processors can access the files in the specified locations by using
	 * {@link StandardLocation#locationFor(String)} when opening a resource for reading.
	 * 
	 * @param processorInputLocations
	 *            The input locations.
	 * @throws NullPointerException
	 *             If any of the key or value are <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If a specified path is not absolute.
	 */
	public void setProcessorInputLocations(Map<String, SakerPath> processorInputLocations)
			throws NullPointerException, InvalidPathFormatException;

	/**
	 * Sets whether or not the annotation processors should be run in a parallel way.
	 * <p>
	 * The incremental compiler runs the annotation processing in a parallel way to improve performance. If you
	 * experience issues with it, or need the resulf of {@link Processor#process(Set, RoundEnvironment)} taken into
	 * account, set this to <code>false</code>.
	 * <p>
	 * The default is <code>true</code>.
	 * 
	 * @param parallelProcessing
	 *            <code>false</code> to disable parallel annotation processing
	 */
	public void setParallelProcessing(Boolean parallelProcessing);

	/**
	 * Sets the global warning types that should be suppressed by the compiler task.
	 * <p>
	 * The elements should be any of the constants in {@link JavaCompilerWarningType}.
	 * 
	 * @param suppressWarnings
	 *            The warning types to suppress.
	 */
	public void setSuppressWarnings(Collection<String> suppressWarnings);

	/**
	 * Sets the module main class name to inject into the generated <code>module-info.class</code> file.
	 * <p>
	 * The compiler task will insert the specified class name as the main class into the generated module-info class
	 * file. If there's no compiled module, nothing will be done.
	 * <p>
	 * The compiler task doesn't verify if a class with the specified name exists.
	 * 
	 * @param moduleMainClass
	 *            The main class name.
	 */
	public void setModuleMainClass(String moduleMainClass);

	/**
	 * Sets the module version string to inject into the generated <code>module-info.class</code> file.
	 * <p>
	 * The compiler task will insert the specified version string as the main version into the generated module-info
	 * class file. If there's no compiled module, nothing will be done.
	 * 
	 * @param moduleVersion
	 *            The module version string.
	 */
	public void setModuleVersion(String moduleVersion);

	/**
	 * Sets whether or not to compile the sources using the incremental compiler implementation.
	 * <p>
	 * Setting this to <code>false</code> will cause the compiler task to fall back to the legacy compiler
	 * implementation that always compiles every input source file.
	 * <p>
	 * This can be used if you experience errors in the incremental compiler, or need to take advantage of legacy
	 * features. The non-incremental compilation may not be supported in some cases.
	 * <p>
	 * The default is <code>true</code>.
	 * 
	 * @param buildIncremental
	 *            <code>false</code> to not build incrementally.
	 */
	public void setBuildIncremental(Boolean buildIncremental);

	/**
	 * Builds the task identifier that should be used to start the configured task.
	 * <p>
	 * The builder can be reused after this call.
	 * 
	 * @return The task identifier.
	 * @see #buildTaskFactory()
	 */
	public JavaCompilationWorkerTaskIdentifier buildTaskIdentifier();

	/**
	 * Builds the task factory for the compilation.
	 * <p>
	 * The task works specified by the configuration in this builder. The task should be started with the
	 * {@link #buildTaskIdentifier()} as the task identifier.
	 * <p>
	 * The builder can be reused after this call.
	 * 
	 * @return The worker compiler task factory.
	 */
	public TaskFactory<? extends JavaCompilerWorkerTaskOutput> buildTaskFactory();

	/**
	 * Creates a new builder instance.
	 * 
	 * @return The builder.
	 */
	public static JavaCompilationTaskBuilder newBuilder() {
		return new JavaCompilationTaskBuilderImpl();
	}
}