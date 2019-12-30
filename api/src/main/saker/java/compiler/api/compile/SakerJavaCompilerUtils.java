package saker.java.compiler.api.compile;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.compile.AbiVersionKeyTaskOutputChangeDetector;
import saker.java.compiler.impl.compile.CompilationClassPathTaskOutputChangeDetector;
import saker.java.compiler.impl.compile.CompilationModulePathTaskOutputChangeDetector;
import saker.java.compiler.impl.compile.ImplementationVersionKeyTaskOutputChangeDetector;
import saker.java.compiler.impl.sdk.CurrentJavaSDKReferenceEnvironmentProperty;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;

/**
 * Utility class contains functions related to Java compilation and configuration.
 */
public final class SakerJavaCompilerUtils {
	/**
	 * The name of the Java compiler task provided by this package.
	 */
	public static final String TASK_NAME_SAKER_JAVA_COMPILE = "saker.java.compile";

	/**
	 * The name of the output directory that contains the compiled class files.
	 */
	public static final String DIR_CLASS_OUTPUT = "bin";
	/**
	 * The name of the output directory that contains the processor generated source files.
	 */
	public static final String DIR_SOURCE_OUTPUT = "gen";
	/**
	 * The name of the output directory that contains the generated native header files
	 */
	public static final String DIR_NATIVE_HEADER_OUTPUT = "nativeh";
	/**
	 * The name of the output directory that contains the processor generated resource files.
	 */
	public static final String DIR_RESOURCE_OUTPUT = "res";

	/**
	 * The default SDK name that the Java compiler task uses.
	 * <p>
	 * The associated SDK determines the JDK that will be used for the compilation.
	 */
	public static final String DEFAULT_SDK_NAME = "Java";

	/**
	 * Java SDK path identifier for the install location.
	 * <p>
	 * Points to the install directory. E.g. <code>c:\Program Files\Java\jdk1.8.0_221</code>
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INSTALL_LOCATION = "install.location";
	/**
	 * Java SDK path identifier for the <code>java</code> executable.
	 * <p>
	 * E.g. <code>c:\Program Files\Java\jdk1.8.0_221\bin\java.exe</code>
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_JAVA_EXE = "exe.java";

	/**
	 * Java SDK path identifier for the include directory that is present in a JDK.
	 * <p>
	 * Points to the root include directory. E.g. <code>c:\Program Files\Java\jdk1.8.0_221\include</code>
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INCLUDE = "include";
	/**
	 * Java SDK path identifier for the <code>win32</code> platform include directory in the JDK.
	 * <p>
	 * E.g. <code>c:\Program Files\Java\jdk1.8.0_221\include\win32</code>
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INCLUDE_WIN32 = "include.win32";
	/**
	 * Java SDK path identifier for the <code>darwin</code> platform include directory in the JDK.
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INCLUDE_DARWIN = "include.darwin";
	/**
	 * Java SDK path identifier for the <code>linux</code> platform include directory in the JDK.
	 * <p>
	 * E.g. <code>/usr/lib/jvm/java-8-openjdk-amd64/jre/include/linux</code>
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INCLUDE_LINUX = "include.linux";
	/**
	 * Java SDK path identifier for the <code>solaris</code> platform include directory in the JDK.
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INCLUDE_SOLARIS = "include.solaris";
	/**
	 * Java SDK path identifier for the platform include directory if it can be determined.
	 * <p>
	 * This SDK path identifier points to the only platform include directory that is present in the root directory.
	 * Usually JDK installations have a single platform include directory. Using this identifier will automatically
	 * return the appropriate one. E.g. if the JDK is installed on Windows, it will return the value for
	 * {@link #JAVASDK_PATH_INCLUDE_WIN32}.
	 * <p>
	 * Note that if the platform include directory cannot be determined in a non-ambiguous way, then the path for this
	 * identifier won't be returned.
	 * 
	 * @see SDKReference#getPath(String)
	 */
	public static final String JAVASDK_PATH_INCLUDE_PLATFORM = "include.platform";

	/**
	 * Java SDK property identifier for the <code>java.version</code> system property.
	 * 
	 * @see SDKReference#getProperty(String)
	 */
	public static final String JAVASDK_PROPERTY_JAVA_VERSION = "java.version";
	/**
	 * Java SDK property identifier for the major version of the associated JDK.
	 * 
	 * @see SDKReference#getProperty(String)
	 */
	public static final String JAVASDK_PROPERTY_JAVA_MAJOR = "java.major";

	private static final Set<String> ALL_UNNAMED_SINGLETON_SET = ImmutableUtils.singletonSet("ALL-UNNAMED");

	private static final EnvironmentSDKDescription DEFAULT_JAVA_SDK = EnvironmentSDKDescription
			.create(CurrentJavaSDKReferenceEnvironmentProperty.INSTANCE);

	private SakerJavaCompilerUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the {@link SDKDescription} that is used when no SDK is specified for Java compilation with the
	 * <code>Java</code> name.
	 * <p>
	 * The default SDK uses a JDK that has the same version as the JVM that runs the current build execution.
	 * 
	 * @return The default Java SDK.
	 */
	public static SDKDescription getDefaultJavaSDK() {
		return DEFAULT_JAVA_SDK;
	}

	/**
	 * Converts the argument add-exports configuration to command line options.
	 * <p>
	 * The argument configuration will be converted to <code>module/package=target-module(,target-module)*</code>
	 * format. If the argument configuration defines a setting that consists of multiple add-export command line
	 * options, then the resulting collection will have multiple entries.
	 * <p>
	 * The result strings are not prefixed by the <code>--add-exports</code> argument.
	 * 
	 * @param addexports
	 *            The add exports configuration. (If <code>null</code>, the returned collection is empty.)
	 * @return The command line strings that the argument configuration defines.
	 */
	public static Collection<String> toAddExportsCommandLineStrings(JavaAddExports addexports) {
		if (addexports == null) {
			return Collections.emptySet();
		}
		Collection<String> packs = addexports.getPackage();
		if (ObjectUtils.isNullOrEmpty(packs)) {
			return Collections.emptySet();
		}
		Collection<String> result = new TreeSet<>();
		Collection<String> target = addexports.getTarget();
		if (ObjectUtils.isNullOrEmpty(target)) {
			target = ALL_UNNAMED_SINGLETON_SET;
		}
		String module = addexports.getModule();
		for (String pack : packs) {
			for (String targetstr : target) {
				result.add(module + "/" + pack + "=" + targetstr);
			}
		}
		return result;
	}

	/**
	 * Gets a {@link TaskOutputChangeDetector} that examines the
	 * {@link JavaCompilerWorkerTaskOutput#getAbiVersionKey()}.
	 * <p>
	 * The returned change detector will detect a change if the
	 * {@linkplain JavaCompilerWorkerTaskOutput#getAbiVersionKey() ABI version key} of the task output changes.
	 * 
	 * @param abiversionkey
	 *            The expected ABI version key.
	 * @return The task output change detector.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see JavaCompilerWorkerTaskOutput#getAbiVersionKey()
	 */
	public static TaskOutputChangeDetector getCompilerOutputAbiVersionKeyTaskOutputChangeDetector(Object abiversionkey)
			throws NullPointerException {
		return new AbiVersionKeyTaskOutputChangeDetector(abiversionkey);
	}

	/**
	 * Gets a {@link TaskOutputChangeDetector} that examines the
	 * {@link JavaCompilerWorkerTaskOutput#getImplementationVersionKey()}.
	 * <p>
	 * The returned change detector will detect a change if the
	 * {@linkplain JavaCompilerWorkerTaskOutput#getImplementationVersionKey() implementation version key} of the task
	 * output changes.
	 * 
	 * @param abiversionkey
	 *            The expected implementation version key.
	 * @return The task output change detector.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see JavaCompilerWorkerTaskOutput#getImplementationVersionKey()
	 */
	public static TaskOutputChangeDetector getCompilerOutputImplementationVersionKeyTaskOutputChangeDetector(
			Object abiversionkey) throws NullPointerException {
		return new ImplementationVersionKeyTaskOutputChangeDetector(abiversionkey);
	}

	/**
	 * Gets a {@link TaskOutputChangeDetector} that expects the compilation classpath of the task output to equal to the
	 * argument.
	 * <p>
	 * The change detector will compare the {@link JavaCompilerWorkerTaskOutput#getClassPath()} for equality with the
	 * argument.
	 * 
	 * @param classpath
	 *            The expected classpath. May be <code>null</code>.
	 * @return The task output change detector.
	 * @see JavaCompilerWorkerTaskOutput#getClassPath().
	 */
	public static TaskOutputChangeDetector getCompilerOutputClassPathTaskOutputChangeDetector(JavaClassPath classpath) {
		return new CompilationClassPathTaskOutputChangeDetector(classpath);
	}

	/**
	 * Gets a {@link TaskOutputChangeDetector} that expects the compilation modulepath of the task output to equal to
	 * the argument.
	 * <p>
	 * The change detector will compare the {@link JavaCompilerWorkerTaskOutput#getModulePath()} for equality with the
	 * argument.
	 * 
	 * @param modulepath
	 *            The expected modulepath. May be <code>null</code>.
	 * @return The task output change detector.
	 * @see JavaCompilerWorkerTaskOutput#getModulePath().
	 */
	public static TaskOutputChangeDetector getCompilerOutputModulePathTaskOutputChangeDetector(
			JavaModulePath modulepath) {
		return new CompilationModulePathTaskOutputChangeDetector(modulepath);
	}
}
