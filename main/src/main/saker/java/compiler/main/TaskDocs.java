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
package saker.java.compiler.main;

import saker.build.file.path.SakerPath;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.java.compiler.main.compile.option.JavaCompilerOptions;
import saker.java.compiler.main.processor.ClassPathProcessorTaskFactory;
import saker.java.compiler.main.sdk.JavaSDKTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.sdk.support.main.TaskDocs.DocSDKDescription;

public class TaskDocs {
	public static final String COMPILE_SOURCE_DIRECTORY = "Input source directories for the compilation.\n"
			+ "It is used to specify which files should be compiled. "
			+ "It can accept a single path to the source directory containing .java files.";

	public static final String COMPILE_CLASS_PATH = "Input class path for the compilation.\n"
			+ "The class files in the specified class path will be added to the available classes during Java compilation.\n"
			+ "The value may be paths to JARs or class directories, wildcards to specify multiple one easily, "
			+ "outputs of previous compilations, paths based on SDKs, or class paths from other tasks.\n"
			+ "Class paths of compilation outputs will be transitive added. Any Class-Path or similar attributes in JAR file "
			+ "manifests are ignored.";

	public static final String COMPILE_BOOT_CLASS_PATH = "Input boot class path for the compilation.\n"
			+ "The class files in the specified class path will used as the boot class path during Java compilation.\n"
			+ "The value may be paths to JARs or class directories, wildcards to specify multiple one easily, "
			+ "outputs of previous compilations, paths based on SDKs, or class paths from other tasks.\n"
			+ "Class paths of compilation outputs will be transitive added. Any Class-Path or similar attributes in JAR file "
			+ "manifests are ignored.";

	public static final String COMPILE_MODULE_PATH = "Input module path for the compilation.\n"
			+ "The specified JARs and class directories will be used as the module path durin Java compilation.\n"
			+ "The value may be paths to JARs or class directories, wildcards to specify multiple one easily, "
			+ "outputs of previous compilations, paths based on SDKs, or module paths from other tasks.\n"
			+ "Module paths of compilation outputs will be transitive added.\n"
			+ "This option only has relevance if the compilation is done for JDK9 or later.";

	public static final String COMPILE_SOURCE_VERSION = "Specifies the accepted source version.\n"
			+ "The compiled Java sources should comply to the specified source version number. "
			+ "The value can be a positive integer representing the Java major release number, "
			+ "or in the format of RELEASE_<num>.";
	public static final String COMPILE_TARGET_VERSION = "Specifies the target version of the class files.\n"
			+ "The generated class files will be used to target the Java VM with the specified version. "
			+ "The value can be a positive integer representing the Java major release number, "
			+ "or in the format of RELEASE_<num>.";

	public static final String COMPILE_PARAMETERS = "Specifies the command line parameters that should be passed to javac.\n"
			+ "The specified parameters will be passed to the Java compiler backend with minimal modifications by the compiler task.\n"
			+ "The -processorpath and -processor parameters are ignored. Use the annotation processor options for the compiler task.\n"
			+ "The -source and -target parameters are ignored. Use SourceVersion and TargetVersion parameters for the compiler task.\n"
			+ "The -sourcepath parameter is ignored. Use SourceDirectories parameter to specify source inputs.\n"
			+ "The -cp, -classpath, --class-path parameters are merged with the ClassPath parameter. Developers should use the ClassPath "
			+ "parameter instead of these options.\n"
			+ "The -p and --module-path are merged with the ModulePath option. Developers should use ModulePath instead.\n"
			+ "The -bootclasspath and --boot-class-path parameters are merged with BootClassPath. Developers should use BootClassPath instead.\n"
			+ "The -A parameters are stripped and merged with the AnnotationProcessorOptions option.";

	public static final String COMPILE_ANNOTATION_PROCESSOR_OPTIONS = "Specifies the global key-value options that should be passed to all annotation processors.\n"
			+ "All the entries in this map will be added without modification to the options which are used for the processors.";

	public static final String COMPILE_ANNOTATION_PROCESSORS = "Specifies the annotation processors that should be used during compilation.\n"
			+ "Annotation processors are plugins into the compilation. They can examine the compiled source code "
			+ "and generate various sources, classes, and resources based on that.\n"
			+ "Annotation processors can be constructed by other tasks in the build system. See "
			+ ClassPathProcessorTaskFactory.TASK_NAME + "() "
			+ "or the use-case documentation for your processor of interest.\n"
			+ "Global options can be specified for the processors using AnnotationProcessorOptions. "
			+ "Input locations can be specified using ProcessorInputLocations.";
	public static final String COMPILE_PROCESSOR_INPUT_LOCATIONS = "Specifies a read-only input location for annotation processors.\n"
			+ "The value is a string-path map which contains the names of locations and the directory paths from where processors can "
			+ "read resources from.\n"
			+ "The processors can use the Filer API to read resources, by passing StandardLocation.locationFor(name) as the location argument.";

	public static final String COMPILE_GENERATE_NATIVE_HEADERS = "Specifies whether or not native headers should be generated for the compiled source files.\n"
			+ "When set to true, the task will create C language bindings for any native declarations in the files. \n"
			+ "The created header files can be included by C/C++ and other language compilers if appropriate. "
			+ "Use the HeaderDirectory field of the task output to get a path to the generated headers.";

	public static final String COMPILE_ADD_EXPORTS = "Specifies the additional module export definitions when compiling for Java 9 or later.\n"
			+ "Specifying this option will add the appropriate --add-exports options to the Java compiler backend.\n"
			+ "The also option accepts string values in the format of module/package=other-module(,other-module)*. "
			+ "If the target module is omitted, ALL-UNNAMED is used.";

	public static final String COMPILE_ADD_READS = "Specifies the additional module read definitions when compiling for Java 9 or later.\n"
			+ "Specifying this option will add the appropriate --add-reads options to the Java compiler backend.\n"
			+ "The also option accepts string values in the format of module=other-module(,other-module)*.";

	public static final String COMPILE_SUPPRESS_WARNINGS = "Specifies the warning types which should be suppressed by the compiler task.";

	public static final String PARAM_NAME_COMPILER_OPTIONS = "CompilerOptions";

	public static final String COMPILE_COMPILER_OPTIONS = "Specifies one or more option specifications that are merged into the "
			+ "compilation options of the Java compilation if applicable.\n"
			+ "The parameter can be used to indirectly specify various compilation arguments independent of the actual task "
			+ "invocation. This is generally useful when common options need to be specified to multiple Java compilations.\n"
			+ "When the compilation arguments are determined, each option specification will be merged into the used arguments "
			+ "if applicable. An option is considered to be applicable for merging if all of the Identifier parts are contained "
			+ "in the compilation task identifier, and the Language qualifier argument can be matched.\n"
			+ "In case of option conflicts, the task will throw an appropriate exception.";
	/**
	 * For {@link JavaCompilerOptions}.
	 */
	public static final String COMPILE_OPTION_IDENTIFIER = "Specifies the Identifier that serves as a qualifier for compiler option merging.\n"
			+ "The default value is null. The compiler options will only be merged to the target compilation task options if the "
			+ "task Identifier parameter contains all of the parts declared in this Identifier.\n"
			+ "An identifier constists of dash separated parts of character sequences of a-z, A-Z, 0-9, _, ., (), [], @.\n"
			+ "If no Identifier is specified, the option merging is not constrained by the Identifier, and the options will be "
			+ "merged without checking it.";
	public static final String COMPILE_IDENTIFIER = "The Identifier of the Java compilation.\n"
			+ "Each Java compilation task has an identifier that uniquely identifies it during a build execution. "
			+ "The identifer is used to determine the output directory of the compilation. It is also used "
			+ "to merge the appropriate options specified in " + PARAM_NAME_COMPILER_OPTIONS + " parameter.\n"
			+ "An identifier constists of dash separated parts of character sequences of a-z, A-Z, 0-9, _, ., (), [], @.\n"
			+ "An option specification in the " + PARAM_NAME_COMPILER_OPTIONS + " can be merged if "
			+ "the compilation identifier contains all parts of the option Identifier.\n"
			+ "The default value is automatically generated based on the SourceDirectories argument. Auto-generated identifiers are "
			+ "not subject to option merging.";
	/**
	 * For {@link JavaCompilerOptions}.
	 */
	public static final String COMPILE_OPTION_LANGUAGE = "Specifies the Language this options applies to.\n"
			+ "The default value is null. The Language serves as a qualifier for merging options into the compilation task.\n"
			+ "If the target language doesn't match the language declaration in this options, the options won't be merged into "
			+ "the compilation task.\n"
			+ "If the Language is null, then option merging is not constrained by the Language, and the options will be merged "
			+ "without checking it.";
	public static final String COMPILE_LANGUAGE = "Serves only as a qualifier for " + PARAM_NAME_COMPILER_OPTIONS
			+ " merging. Specifying this argument " + "doesn't have any effect on the Java source compilation.\n"
			+ "The default value is Java. When " + PARAM_NAME_COMPILER_OPTIONS
			+ " are merged, the languages are checked for compatibility. "
			+ "If the specified compiler option declares a different language than this one, then its options won't be merged "
			+ "into the compilation task.";

	public static final String COMPILE_SDKS = "Specifies the SDKs (Software Development Kits) used by the compilation task.\n"
			+ "SDKs represent development kits that are available in the build environment and to the task. For Java compilation, "
			+ "they can be used to resolve class and module paths against, or to specify a different JDK to use for compilation.\n"
			+ "SDKs are referenced by simple string names in a case-insensitive way. The \"Java\" SDK is used by the compilation task "
			+ "to determine which JDK to use. If a different JDK is specified than the one the build is running on, then the task "
			+ "will spawn a new Java process that it delegates the compilation to. (i.e. forks a compiler process)\n"
			+ "The " + JavaSDKTaskFactory.TASK_NAME
			+ "() task can be used to find JDKs with a specific version during build.";

	public static final String COMPILE_MODULE_MAIN_CLASS = "Specifies a Java class binary name that should be injected in the generated module-info.class file.\n"
			+ "When the compilation targets Java 9 or later, and uses modules (module-info.java), specifying this option will cause "
			+ "the compiler task to inject the main class attribute in the generated module-info.class class file.\n"
			+ "The task will not verify if the specified class exists or not.";

	public static final String COMPILE_MODULE_VERSION = "Specifies a version string that should be injected in the generated module-info.class file.\n"
			+ "When the compilation targets Java 9 or later, and uses modules (module-info.java), specifying this option will cause "
			+ "the compiler task to inject the version attribute in the generated module-info.class class file.\n"
			+ "The task will not check if the specified version is syntactically correct.";

	public static final String COMPILE_PARAMETER_NAMES = "Specifies whether or not the parameter names of methods and constructors should be included in the generated class files.\n"
			+ "If so, the parameter names will be available through the Reflection APIs. The default is true.\n"
			+ "Corresponds to the -parameters command line option for javac.";
	public static final String COMPILE_DEBUG_INFO = "Specifies the debugging informations to be included in the generated class files.\n"
			+ "The default is all. May specify empty collection to signal no information to be included.\n"
			+ "Corresponds to the -parameters command line option for javac.";

	public static final String COMPILE_PARALLEL_PROCESSING = "Specifies if the compiler task may run the annotation processors in a parallel manner.\n"
			+ "The default value is true. Running annotation processors in parallel may result in performance increase, as they can generate "
			+ "their classes and resources without waiting on each other. In general, parallel processing shouldn't distrupt the operation "
			+ "of annotation processors, however, if developers notice any unexpected processor outputs, they may check if turning parallel "
			+ "processing off helps.\n"
			+ "When parallel processing is on, the result of the Processor.process() function is ignored by the compiler.";

	public static final String COMPILE_BUILD_INCREMENTAL = "Specifies if incremental Java compilation should be used or not.\n"
			+ "The default value is true. Setting this to false will cause the task to use legacy compilation, and will call the "
			+ "javac backend with the least modification to the input parameters as possible. Using it will result in the compile Parameters "
			+ "being passed directly to the backend, and annotation processors will be called by the javac backed instead of the task implementation.\n"
			+ "This option should only be used when there are issues with the incremental Java compilation implementation. If you ever need "
			+ "to turn this feature off, make sure to file an issue about your use-case as this is considered to be a bug in "
			+ "the incremental compiler. Issues at: https://github.com/sakerbuild/saker.java.compiler/issues";

	public static final String COMPILE_ALLOW_TARGET_RELEASE_MISMATCH = "Sets if mismatching TargetVersion, SourceVersion and --release values are allowed to be used.\n"
			+ "WARNING: Using this flag may cause your class files to be binary incompatible with the platform you intend to "
			+ "run it on. This flag uses undocumented javac API to trick it into generating bytecode for different versions than "
			+ "it's used to. Using this may cause crashes in your application in unexpected ways.\n"
			+ "WARNING: The specified TargetVersion shouldn't be greater than the --release version. Compilation may result in error if you target "
			+ "a more recent release than the specified --release.\n"
			+ "WARNING: Other general incompatibility errors may occurr if you intend to use bytecode instructions on older Java releases where they "
			+ "don't exist.\n"
			+ "By setting this to true, the compile task will directly set the specified TargetVersion, SourceVersion and --release parameter values "
			+ "as they are specified as inputs. No compatibility validation will be performed for these.\n"
			+ "This generally can be used to generate bytecode for older releases while sometimes using newer language features. This is "
			+ "a completely undocumented and unreliable feature of this build task and we don't recommend using it without thorough testing or in general.";
	public static final String COMPILE_PATCH_ENABLE_PREVIEW = "Sets if the --enable-preview requirement on the generated class files should be patched.\n"
			+ "If this flag is set to true, then the compile task will modify the minor version of the class files from 0xFFFF to 0x0000.\n"
			+ "As a result of this, they can be run using javac without specifying the --enable-preview flag for it.\n"
			+ "Note that this feature may break on future versions of Java.";

	public static final String COMPILE_OUTPUT_CLASS_DIRECTORY = "The directory path to the output class files of the Java compilation.\n"
			+ "The directory contains all class files that were compiled from the Java source files and any files which were "
			+ "generated by annotation processors to the class output directory.";
	public static final String COMPILE_OUTPUT_HEADER_DIRECTORY = "The directory path to the output native header files.\n"
			+ "The directory contains all .h header files that were generated by the compiler task and any files which were "
			+ "generated by annotation processors to the native header output directory.\n"
			+ "Set GenerateNativeHeaders option to true to generate header files.";
	public static final String COMPILE_OUTPUT_RESOURCE_DIRECTORY = "The directory path to the output resource files.\n"
			+ "The directory is the base directory under which annotation processors generate their output resource files. "
			+ "The actual resource directory should be resolved against this output directory path.\n"
			+ "E.g. if a processor generates to the output location name PROC_RESOURCES_OUTPUT, then the path "
			+ "\"{ saker.java.compile()[ResourceDirectory] }/PROC_RESOURCES_OUTPUT\" expression should be used.";
	public static final String COMPILE_OUTPUT_SOURCE_GEN_DIRECTORY = "The directory path to the output source files.\n"
			+ "The directory contains any Java source files that annotation processors generated during their invocation. "
			+ "The files have the .java extension and are under their respective package subdirectory.";

	public static final String COMPILE_OUTPUT_MODULE_NAME = "The name of the module that was compiled.\n"
			+ "This field is only applicable for Java compilations targetting version 9 or later, and containing a module-info.java "
			+ "input source file.";
	public static final String COMPILE_OUTPUT_JAVA_SDK = "The Java SDK description that was used to execute that Java compilation.\n"
			+ "It was either explicitly specified by the developer, or inferred based on the current build environment.";

	public static final String ANNOTATION_PROCESSOR_ALWAYSRUN = "Specifies whether to given processor should always run, "
			+ "even if there are no relevant source element changes.\n" + "The default value is false.";
	public static final String ANNOTATION_PROCESSOR_AGGREGATING = "Specifies whether or not the processor aggregates its input to generate classes or resources.\n"
			+ "The default value is true. A processor is considered to be Aggregating if there exists an addition-wise modification to the "
			+ "compiled Java classes that causes it to generate different classes or resources.\n"
			+ "E.g. if a processor generates a list of classes that were compiled, then it is considered to be Aggregating, as adding a new class "
			+ "will result in generating a different list.\n"
			+ "E.g. if a processor generates one resource for each one of the input Java classes, then it is NOT considered to be aggregating, as the "
			+ "previously generated resources are still the same. The only change happens is that the processor will create a new resource for the "
			+ "newly added class.\n"
			+ "In general, if your processor generates resources in a multi->one relation, then it should be Aggregating. We recommend extensive testing "
			+ "before setting a process configuration to non-Aggregating.";
	public static final String ANNOTATION_PROCESSOR_CONSISTENT = "Specifies whether or not the processor is Consistent.\n"
			+ "The default value is true. A processor is considered to be consistent if it doesn't generate different classes or resources based on "
			+ "environmental changes not visible to the Java comiler.\n"
			+ "E.g. a processor that includes the current date in the generated classes or resources is NOT consistent.";

	public static final String ANNOTATION_PROCESSOR_CLASS_PARAMETER = "Specifies the class name of the processor to be loaded.\n"
			+ "The class name must be explicitly specified in order to avoid ambiguity. The task implementation "
			+ "doesn't use ServiceLoader or other automatic discovery mechanisms.";

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AnnotationProcessorOptionName")
	@NestInformation("Annotation processor input option key.\n"
			+ "The key and associated value is passed directly to the Java compilation annotation processors.")
	public static class AnnotationProcessorOptionKey {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AnnotationProcessorOptionValue")
	@NestInformation("Annotation processor input option value.\n"
			+ "The key and associated value is passed directly to the Java compilation annotation processors.")
	public static class AnnotationProcessorOptionValue {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AddExportsModuleName")
	@NestInformation("Module name from which the given package is exported to the specified target modules for Java compilation.")
	public static class AddExportsModuleOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AddExportsPackageName")
	@NestInformation("Exported package name from a module for Java compilation.")
	public static class AddExportsPackageOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AddExportsTargetModuleName")
	@NestInformation("Target module name to which the packages are exported to.")
	public static class AddExportsTargetOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AddReadsModuleName")
	@NestInformation("Name of the module that requires/reads the associated modules.")
	public static class AddReadsModuleOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AddReadsRequiresOption")
	@NestInformation("Name of the module that is required/read.")
	public static class AddReadsRequiresOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "JavaCompilationParameter")
	@NestInformation("Java compilation parameter directly passed to the backend Java compiler.")
	public static class CompilationParameterOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ProcessorInputLocationName")
	@NestInformation("Name for the specified annotation processor input location.")
	public static class ProcessorInputLocationNameOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ModuleInfoMainAttribute")
	@NestInformation("Java class name that is injected in the compiled module-info.class for Java compilation.")
	public static class ModuleInfoInjectMainClassOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ModuleInfoVersionAttribute")
	@NestInformation("Version string that is injected in the compiled module-info.class for Java compilation.")
	public static class ModuleInfoInjectVersionOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "AnnotationProcessorClassName")
	@NestInformation("The Java class name of the annotation processor that is being instantiated.")
	public static class ProcessorClassNameOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "JavaVersionNumber")
	@NestInformation("Java version number.")
	public static class JavaVersionOption {
	}

	@NestInformation("Compilation language.")
	@NestTypeInformation(kind = TypeInformationKind.ENUM,
			enumValues = { @NestFieldInformation(value = "Java",
					info = @NestInformation("Represents the Java programming language.")), })
	public static class JavaCompilationLanguageOption {
	}

	@NestInformation("Output of Java compilation.\n"
			+ "The fields can be used to access the output paths of the compilation. The compilation output can be directly set "
			+ "to an input class path or module path to subsequent Java compilations.")
	@NestFieldInformation(value = "ClassDirectory",
			info = @NestInformation(TaskDocs.COMPILE_OUTPUT_CLASS_DIRECTORY),
			type = @NestTypeUsage(kind = TypeInformationKind.DIRECTORY_PATH, value = SakerPath.class))
	@NestFieldInformation(value = "HeaderDirectory",
			info = @NestInformation(TaskDocs.COMPILE_OUTPUT_HEADER_DIRECTORY),
			type = @NestTypeUsage(kind = TypeInformationKind.DIRECTORY_PATH, value = SakerPath.class))
	@NestFieldInformation(value = "ResourceDirectory",
			info = @NestInformation(TaskDocs.COMPILE_OUTPUT_RESOURCE_DIRECTORY),
			type = @NestTypeUsage(kind = TypeInformationKind.DIRECTORY_PATH, value = SakerPath.class))
	@NestFieldInformation(value = "SourceGenDirectory",
			info = @NestInformation(TaskDocs.COMPILE_OUTPUT_SOURCE_GEN_DIRECTORY),
			type = @NestTypeUsage(kind = TypeInformationKind.DIRECTORY_PATH, value = SakerPath.class))
	@NestFieldInformation(value = "ModuleName",
			info = @NestInformation(TaskDocs.COMPILE_OUTPUT_MODULE_NAME),
			type = @NestTypeUsage(String.class))
	@NestFieldInformation(value = "JavaSDK",
			info = @NestInformation(TaskDocs.COMPILE_OUTPUT_JAVA_SDK),
			type = @NestTypeUsage(DocSDKDescription.class))
	@NestTypeInformation(qualifiedName = "JavaCompilerOutput")
	public static class DocJavaCompilerOutput {
	}

	@NestTypeInformation(qualifiedName = "ProcessorConfiguration")
	@NestInformation("Describes how an annotation processor should be instantiated by the Java compiler task.\n"
			+ "Instances of this type is created by tasks that resolve annotation processors. E.g. " + "the "
			+ ClassPathProcessorTaskFactory.TASK_NAME + "() task.")
	public static class DocProcessorConfiguration {
	}

	@NestTypeInformation(qualifiedName = "ClassPathReference")
	@NestInformation("Classpath object for the specified saker.nest bundles.")
	public static class DocBundleClassPath {
	}
}
