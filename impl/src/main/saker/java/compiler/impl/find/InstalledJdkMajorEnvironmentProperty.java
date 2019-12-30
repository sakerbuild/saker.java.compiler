package saker.java.compiler.impl.find;

import java.io.Externalizable;

import saker.build.file.path.SakerPath;

public class InstalledJdkMajorEnvironmentProperty extends EchoJavaEnvironmentProperty<Integer>
		implements Externalizable {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public InstalledJdkMajorEnvironmentProperty() {
	}

	private InstalledJdkMajorEnvironmentProperty(SakerPath jdkPath) {
		super(jdkPath);
	}

	public static InstalledJdkMajorEnvironmentProperty forJavaInstallLocation(SakerPath jdkPath) {
		return new InstalledJdkMajorEnvironmentProperty(jdkPath);
	}

	@Override
	protected Integer parseOutput(String result) {
		return Integer.parseInt(result);
	}

	@Override
	protected String getMainClassName() {
		return JAVA_MAJOR_ECHO_CLASS_NAME;
	}
}
