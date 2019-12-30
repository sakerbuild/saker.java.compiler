package saker.java.compiler.impl.find;

import java.io.Externalizable;

import saker.build.file.path.SakerPath;

public class InstalledJdkVersionEnvironmentProperty extends EchoJavaEnvironmentProperty<String>
		implements Externalizable {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public InstalledJdkVersionEnvironmentProperty() {
	}

	private InstalledJdkVersionEnvironmentProperty(SakerPath jdkPath) {
		super(jdkPath);
	}

	public static InstalledJdkVersionEnvironmentProperty forJavaInstallLocation(SakerPath jdkPath) {
		return new InstalledJdkVersionEnvironmentProperty(jdkPath);
	}

	@Override
	protected String parseOutput(String result) {
		return result;
	}

	@Override
	protected String getMainClassName() {
		return JAVA_VERSION_ECHO_CLASS_NAME;
	}
}
