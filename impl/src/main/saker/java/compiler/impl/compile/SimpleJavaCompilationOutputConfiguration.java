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
package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.compile.JavaCompilationConfigurationOutput;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKSupportUtils;

public class SimpleJavaCompilationOutputConfiguration implements JavaCompilationConfigurationOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private JavaCompilationWorkerTaskIdentifier compilationTaskId;
	private SakerPath classDirectory;
	private SakerPath headerDirectory;
	private SakerPath resourceDirectory;
	private SakerPath sourceGenDirectory;
	private String moduleName;
	private NavigableMap<String, SDKDescription> sdks;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleJavaCompilationOutputConfiguration() {
	}

	public SimpleJavaCompilationOutputConfiguration(JavaCompilationWorkerTaskIdentifier compilationTaskId,
			SakerPath classDirectory, SakerPath headerDirectory, SakerPath resourceDirectory,
			SakerPath sourceGenDirectory, String moduleName, NavigableMap<String, SDKDescription> sdks) {
		this.compilationTaskId = compilationTaskId;
		this.classDirectory = classDirectory;
		this.headerDirectory = headerDirectory;
		this.resourceDirectory = resourceDirectory;
		this.sourceGenDirectory = sourceGenDirectory;
		this.moduleName = moduleName;
		this.sdks = sdks;
	}

	@Override
	public JavaCompilationWorkerTaskIdentifier getCompilationTaskIdentifier() {
		return compilationTaskId;
	}

	@Override
	public SakerPath getClassDirectory() {
		return classDirectory;
	}

	@Override
	public SakerPath getHeaderDirectory() {
		return headerDirectory;
	}

	@Override
	public SakerPath getResourceDirectory() {
		return resourceDirectory;
	}

	@Override
	public SakerPath getSourceGenDirectory() {
		return sourceGenDirectory;
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}

	@Override
	public SDKDescription getJavaSDK() {
		return sdks.get(JavaSDKReference.DEFAULT_SDK_NAME);
	}

	@Override
	public NavigableMap<String, SDKDescription> getSDKs() {
		return sdks;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(classDirectory);
		out.writeObject(headerDirectory);
		out.writeObject(resourceDirectory);
		out.writeObject(sourceGenDirectory);
		out.writeObject(compilationTaskId);
		out.writeObject(moduleName);
		SerialUtils.writeExternalMap(out, sdks);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		classDirectory = (SakerPath) in.readObject();
		headerDirectory = (SakerPath) in.readObject();
		resourceDirectory = (SakerPath) in.readObject();
		sourceGenDirectory = (SakerPath) in.readObject();
		compilationTaskId = (JavaCompilationWorkerTaskIdentifier) in.readObject();
		moduleName = (String) in.readObject();
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compilationTaskId == null) ? 0 : compilationTaskId.hashCode());
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
		SimpleJavaCompilationOutputConfiguration other = (SimpleJavaCompilationOutputConfiguration) obj;
		if (classDirectory == null) {
			if (other.classDirectory != null)
				return false;
		} else if (!classDirectory.equals(other.classDirectory))
			return false;
		if (compilationTaskId == null) {
			if (other.compilationTaskId != null)
				return false;
		} else if (!compilationTaskId.equals(other.compilationTaskId))
			return false;
		if (headerDirectory == null) {
			if (other.headerDirectory != null)
				return false;
		} else if (!headerDirectory.equals(other.headerDirectory))
			return false;
		if (moduleName == null) {
			if (other.moduleName != null)
				return false;
		} else if (!moduleName.equals(other.moduleName))
			return false;
		if (resourceDirectory == null) {
			if (other.resourceDirectory != null)
				return false;
		} else if (!resourceDirectory.equals(other.resourceDirectory))
			return false;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		if (sourceGenDirectory == null) {
			if (other.sourceGenDirectory != null)
				return false;
		} else if (!sourceGenDirectory.equals(other.sourceGenDirectory))
			return false;
		return true;
	}

}
