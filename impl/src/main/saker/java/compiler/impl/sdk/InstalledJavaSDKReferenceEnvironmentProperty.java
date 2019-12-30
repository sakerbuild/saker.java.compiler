package saker.java.compiler.impl.sdk;

import java.io.Externalizable;
import java.nio.file.Files;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.find.EchoJavaEnvironmentProperty;

public class InstalledJavaSDKReferenceEnvironmentProperty extends EchoJavaEnvironmentProperty<JavaSDKReference>
		implements Externalizable {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public InstalledJavaSDKReferenceEnvironmentProperty() {
	}

	private InstalledJavaSDKReferenceEnvironmentProperty(SakerPath jdkPath) {
		super(jdkPath);
	}

	public static InstalledJavaSDKReferenceEnvironmentProperty forJavaInstallLocation(SakerPath jdkPath) {
		return new InstalledJavaSDKReferenceEnvironmentProperty(jdkPath);
	}

	@Override
	protected JavaSDKReference parseOutput(String result) throws Exception {
		if (ObjectUtils.isNullOrEmpty(result)) {
			throw new RuntimeException("Failed to parse Java version output. (no output)");
		}
		int eolidx = result.indexOf('\n');
		if (eolidx < 0) {
			throw new RuntimeException("Failed to parse Java version output. (missing EOL)");
		}
		String javaversion = result.substring(0, eolidx).trim();
		if (ObjectUtils.isNullOrEmpty(javaversion)) {
			throw new RuntimeException("Failed to parse Java version output. (missing version)");
		}
		String majorv = result.substring(eolidx + 1).trim();
		SakerPath relativeExePath;
		if (Files.isExecutable(LocalFileProvider.toRealPath(jdkPath.resolve("bin/java.exe")))) {
			relativeExePath = SakerPath.valueOf("bin/java.exe");
		} else {
			relativeExePath = SakerPath.valueOf("bin/java");
		}
		return new JavaSDKReference(javaversion, majorv, jdkPath, relativeExePath,
				JavaUtil.getDefaultPlatformIncludeDirectory(jdkPath));
	}

	@Override
	protected String getMainClassName() {
		return JAVA_VERSION_AND_MAJOR_ECHO_CLASS_NAME;
	}

}
