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
package saker.java.compiler.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import saker.build.file.path.SakerPath;
import saker.build.util.java.JavaTools;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.sdk.support.api.SDKReference;

public class JavaSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_SDK_NAME = SakerJavaCompilerUtils.DEFAULT_SDK_NAME;

	public static final String PATH_HOME = SakerJavaCompilerUtils.JAVASDK_PATH_HOME;
	public static final String PATH_INSTALL_LOCATION = SakerJavaCompilerUtils.JAVASDK_PATH_INSTALL_LOCATION;
	public static final String PATH_JAVA_EXE = SakerJavaCompilerUtils.JAVASDK_PATH_JAVA_EXE;

	public static final String PATH_INCLUDE = SakerJavaCompilerUtils.JAVASDK_PATH_INCLUDE;
	public static final String PATH_INCLUDE_WIN32 = SakerJavaCompilerUtils.JAVASDK_PATH_INCLUDE_WIN32;
	public static final String PATH_INCLUDE_DARWIN = SakerJavaCompilerUtils.JAVASDK_PATH_INCLUDE_DARWIN;
	public static final String PATH_INCLUDE_LINUX = SakerJavaCompilerUtils.JAVASDK_PATH_INCLUDE_LINUX;
	public static final String PATH_INCLUDE_SOLARIS = SakerJavaCompilerUtils.JAVASDK_PATH_INCLUDE_SOLARIS;
	public static final String PATH_INCLUDE_PLATFORM = SakerJavaCompilerUtils.JAVASDK_PATH_INCLUDE_PLATFORM;

	public static final String PROPERTY_JAVA_VERSION = SakerJavaCompilerUtils.JAVASDK_PROPERTY_JAVA_VERSION;
	public static final String PROPERTY_JAVA_MAJOR = SakerJavaCompilerUtils.JAVASDK_PROPERTY_JAVA_MAJOR;

	private String javaVersion;
	/**
	 * Relative to the install location.
	 */
	private SakerPath relativeDefaultPlatformInclude;

	//the root location
	private transient SakerPath installLocation;
	private transient SakerPath relativeExePath;
	private transient String javaMajor;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaSDKReference() {
	}

	public String getJavaMajor() {
		return javaMajor;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public SakerPath getInstallLocation() {
		return installLocation;
	}

	public SakerPath getRelativeExePath() {
		return relativeExePath;
	}

	public SakerPath getRelativeDefaultPlatformInclude() {
		return relativeDefaultPlatformInclude;
	}

	public JavaSDKReference(String javaVersion, String javaMajor, SakerPath installLocation, SakerPath relativeExePath,
			SakerPath relativedefaultplatforminclude) {
		this.javaVersion = javaVersion;
		this.relativeDefaultPlatformInclude = relativedefaultplatforminclude;
		this.javaMajor = javaMajor;
		this.installLocation = installLocation;
		this.relativeExePath = relativeExePath;
	}

	public static JavaSDKReference getCurrent() {
		return CurrentJavaSDKReferenceHolder.INSTANCE;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case PATH_INCLUDE: {
				return installLocation.resolve("include");
			}
			case PATH_INCLUDE_DARWIN: {
				return installLocation.resolve("include", "darwin");
			}
			case PATH_INCLUDE_LINUX: {
				return installLocation.resolve("include", "linux");
			}
			case PATH_INCLUDE_WIN32: {
				return installLocation.resolve("include", "win32");
			}
			case PATH_INCLUDE_SOLARIS: {
				return installLocation.resolve("include", "solaris");
			}
			case PATH_INCLUDE_PLATFORM: {
				if (relativeDefaultPlatformInclude == null) {
					return null;
				}
				return installLocation.resolve(relativeDefaultPlatformInclude);
			}

			case PATH_HOME:
			case PATH_INSTALL_LOCATION: {
				return installLocation;
			}
			case PATH_JAVA_EXE: {
				return installLocation.resolve(relativeExePath);
			}
			default: {
				break;
			}
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case PROPERTY_JAVA_VERSION: {
				return javaVersion;
			}
			case PROPERTY_JAVA_MAJOR: {
				return javaMajor;
			}
			default: {
				break;
			}
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(javaVersion);
		out.writeObject(relativeDefaultPlatformInclude);
		out.writeObject(installLocation);
		out.writeObject(relativeExePath);
		out.writeObject(javaMajor);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		javaVersion = (String) in.readObject();
		relativeDefaultPlatformInclude = (SakerPath) in.readObject();
		installLocation = (SakerPath) in.readObject();
		relativeExePath = (SakerPath) in.readObject();
		javaMajor = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaVersion == null) ? 0 : javaVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaSDKReference other = (JavaSDKReference) obj;
		if (javaVersion == null) {
			if (other.javaVersion != null)
				return false;
		} else if (!javaVersion.equals(other.javaVersion))
			return false;
		if (relativeDefaultPlatformInclude == null) {
			if (other.relativeDefaultPlatformInclude != null)
				return false;
		} else if (!relativeDefaultPlatformInclude.equals(other.relativeDefaultPlatformInclude))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (javaVersion != null ? "javaVersion=" + javaVersion : "") + "]";
	}

	private static final class CurrentJavaSDKReferenceHolder {
		public static final JavaSDKReference INSTANCE = computeCurrentJavaSDKReference();

		private CurrentJavaSDKReferenceHolder() {
			throw new UnsupportedOperationException();
		}
	}

	private static JavaSDKReference computeCurrentJavaSDKReference() {
		Path installdir = JavaTools.getJavaInstallationDirectory();

		SakerPath relativeExePath;
		if (Files.isExecutable(installdir.resolve("bin/java.exe"))) {
			relativeExePath = SakerPath.valueOf("bin/java.exe");
		} else {
			relativeExePath = SakerPath.valueOf("bin/java");
		}
		return new JavaSDKReference(JavaTools.getCurrentJavaVersionProperty(),
				Integer.toString(JavaTools.getCurrentJavaMajorVersion()), SakerPath.valueOf(installdir),
				relativeExePath, JavaUtil.getDefaultPlatformIncludeDirectory(installdir));
	}

}
