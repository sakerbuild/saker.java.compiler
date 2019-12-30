package saker.java.compiler.api.compile;

import saker.build.scripting.model.info.TypeInformationKind;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;

/**
 * Enumeration utility class containing possible warning types to suppress during Java compilation.
 * <p>
 * The warning types are interpreted in a case-insensitive manner.
 */
@NestInformation("Enumeration containing possible warning types to suppress during Java compilation.\n"
		+ "The warning types are interpreted in a case-insensitive manner.")
@NestTypeInformation(kind = TypeInformationKind.ENUM,
		enumValues = {

				@NestFieldInformation(value = JavaCompilerWarningType.LastRoundGeneration,
						info = @NestInformation("Suppress warnings about annotation processors generating classes in the last round of annotation processing.")),
				@NestFieldInformation(value = JavaCompilerWarningType.LessProcessorSourceVersion,
						info = @NestInformation("Suppress warnings if annotation processors report an older supported source version than the one used for compilation.")),
				@NestFieldInformation(value = JavaCompilerWarningType.ProcessorCallResult,
						info = @NestInformation("Suppress warnings if an annotation processor returns an unexpected or invalid value from one of its methods.")),
				@NestFieldInformation(value = JavaCompilerWarningType.NoOriginatingElements,
						info = @NestInformation("Suppress warnings if annotation processors doesn't provide any originating elements for their generated sources, classes, or resources.")),
				@NestFieldInformation(value = JavaCompilerWarningType.UnrecognizedProcessorOptions,
						info = @NestInformation("Suppress warnings emitted when an annotation processor option is passed to a processor that doesn't recognize that option name.")),
				@NestFieldInformation(value = JavaCompilerWarningType.ClientProcessorMessage,
						info = @NestInformation("Suppress warnings emitted by annotation processors.")),
				@NestFieldInformation(value = JavaCompilerWarningType.JavacCompilationWarning,
						info = @NestInformation("Suppress warnings emitted by the Java compiler backend.")),

		})
public final class JavaCompilerWarningType {
	/**
	 * Suppress warnings about annotation processors generating classes in the last round of annotation processing.
	 */
	public static final String LastRoundGeneration = "LastRoundGeneration";
	/**
	 * Suppress warnings if annotation processors report an older supported source version than the one used for
	 * compilation.
	 */
	public static final String LessProcessorSourceVersion = "LessProcessorSourceVersion";
	/**
	 * Suppress warnings if an annotation processor returns an unexpected or invalid value from one of its methods.
	 */
	public static final String ProcessorCallResult = "ProcessorCallResult";
	/**
	 * Suppress warnings if annotation processors doesn't provide any originating elements for their generated sources,
	 * classes, or resources.
	 */
	public static final String NoOriginatingElements = "NoOriginatingElements";
	/**
	 * Suppress warnings emitted when an annotation processor option is passed to a processor that doesn't recognize
	 * that option name.
	 */
	public static final String UnrecognizedProcessorOptions = "UnrecognizedProcessorOptions";
	/**
	 * Suppress warnings emitted by annotation processors.
	 */
	public static final String ClientProcessorMessage = "ClientProcessorMessage";
	/**
	 * Suppress warnings emitted by the Java compiler backend.
	 */
	public static final String JavacCompilationWarning = "JavacCompilationWarning";

	private JavaCompilerWarningType() {
		throw new UnsupportedOperationException();
	}
}