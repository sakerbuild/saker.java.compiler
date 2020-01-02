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
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.regex.Pattern;

import saker.build.file.path.SakerPath;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.util.property.UserParameterEnvironmentProperty;

public final class JavaSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<JavaSDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String INSTALL_LOCATIONS_ENV_PARAMETER_NAME = "saker.java.jre.install.locations";

	private static final UserParameterEnvironmentProperty INSTALL_LOCATIONS_USER_PARAMETER_ENVIRONMENT_PROPERTY = new UserParameterEnvironmentProperty(
			INSTALL_LOCATIONS_ENV_PARAMETER_NAME);
	private static final Pattern SEMICOLON_SPLIT_PATTERN = Pattern.compile("[;]+");

	private NavigableSet<String> suitableVersions;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaSDKReferenceEnvironmentProperty() {
	}

	public JavaSDKReferenceEnvironmentProperty(Set<String> suitableVersions) {
		if (ObjectUtils.isNullOrEmpty(suitableVersions)) {
			throw new IllegalArgumentException("no suitable versions specified: " + suitableVersions);
		}
		this.suitableVersions = ImmutableUtils.makeImmutableNavigableSet(suitableVersions);
	}

	@Override
	public JavaSDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		JavaSDKReference current = environment
				.getEnvironmentPropertyCurrentValue(CurrentJavaSDKReferenceEnvironmentProperty.INSTANCE);
		if (suitableVersions.contains(current.getJavaVersion()) || suitableVersions.contains(current.getJavaMajor())) {
			return current;
		}
		String envprop = environment
				.getEnvironmentPropertyCurrentValue(INSTALL_LOCATIONS_USER_PARAMETER_ENVIRONMENT_PROPERTY);
		List<Exception> causes = new ArrayList<>();
		if (envprop != null) {
			String[] split = SEMICOLON_SPLIT_PATTERN.split(envprop);
			for (String s : split) {
				if (ObjectUtils.isNullOrEmpty(s)) {
					continue;
				}
				try {
					SakerPath installpath = SakerPath.valueOf(s);
					JavaSDKReference sdkref = environment.getEnvironmentPropertyCurrentValue(
							InstalledJavaSDKReferenceEnvironmentProperty.forJavaInstallLocation(installpath));
					if (suitableVersions.contains(sdkref.getJavaVersion())
							|| suitableVersions.contains(sdkref.getJavaMajor())) {
						return sdkref;
					}
					causes.add(new IllegalArgumentException("Unsuitable SDK: " + sdkref));
				} catch (Exception e) {
					causes.add(e);
				}
			}
		}
		IOException exc = new IOException("JDK not found with suitable versions: " + suitableVersions);
		for (Exception c : causes) {
			exc.addSuppressed(c);
		}
		throw exc;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, suitableVersions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		suitableVersions = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((suitableVersions == null) ? 0 : suitableVersions.hashCode());
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
		JavaSDKReferenceEnvironmentProperty other = (JavaSDKReferenceEnvironmentProperty) obj;
		if (suitableVersions == null) {
			if (other.suitableVersions != null)
				return false;
		} else if (!suitableVersions.equals(other.suitableVersions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (suitableVersions != null ? "suitableVersions=" + suitableVersions : "") + "]";
	}

}
