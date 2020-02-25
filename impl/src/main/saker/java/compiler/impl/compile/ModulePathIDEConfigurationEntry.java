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
import java.util.Collection;

import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.ClassPathEntryInputFile;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.std.api.file.location.FileLocation;

public class ModulePathIDEConfigurationEntry implements Externalizable {
	private static final long serialVersionUID = 1L;

	private ClassPathEntryInputFile inputFile;
	private Collection<? extends JavaSourceDirectory> sourceDirectories;
	private StructuredTaskResult sourceAttachment;

	/**
	 * For {@link Externalizable}.
	 */
	public ModulePathIDEConfigurationEntry() {
	}

	public ModulePathIDEConfigurationEntry(FileLocation fileLocation) {
		this.inputFile = ClassPathEntryInputFile.create(fileLocation);
	}

	public ClassPathEntryInputFile getInputFile() {
		return inputFile;
	}

	public void setSourceAttachment(StructuredTaskResult sourceAttachment) {
		this.sourceAttachment = sourceAttachment;
	}

	public StructuredTaskResult getSourceAttachment() {
		return sourceAttachment;
	}

	public void setSourceDirectories(Collection<? extends JavaSourceDirectory> sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}

	public Collection<? extends JavaSourceDirectory> getSourceDirectories() {
		return sourceDirectories;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(inputFile);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
		out.writeObject(sourceAttachment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputFile = (ClassPathEntryInputFile) in.readObject();
		sourceDirectories = SerialUtils.readExternalImmutableLinkedHashSet(in);
		sourceAttachment = (StructuredTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((sourceAttachment == null) ? 0 : sourceAttachment.hashCode());
		result = prime * result + ((sourceDirectories == null) ? 0 : sourceDirectories.hashCode());
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
		ModulePathIDEConfigurationEntry other = (ModulePathIDEConfigurationEntry) obj;
		if (inputFile == null) {
			if (other.inputFile != null)
				return false;
		} else if (!inputFile.equals(other.inputFile))
			return false;
		if (sourceAttachment == null) {
			if (other.sourceAttachment != null)
				return false;
		} else if (!sourceAttachment.equals(other.sourceAttachment))
			return false;
		if (sourceDirectories == null) {
			if (other.sourceDirectories != null)
				return false;
		} else if (!sourceDirectories.equals(other.sourceDirectories))
			return false;
		return true;
	}

}
