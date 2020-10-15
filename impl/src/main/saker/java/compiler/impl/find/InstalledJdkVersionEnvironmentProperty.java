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
package saker.java.compiler.impl.find;

import java.io.Externalizable;

import saker.build.file.path.SakerPath;

public class InstalledJdkVersionEnvironmentProperty extends EchoJavaEnvironmentProperty<String> {
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
