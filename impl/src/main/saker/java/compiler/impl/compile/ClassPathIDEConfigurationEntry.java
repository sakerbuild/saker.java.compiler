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

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.ClassPathEntryInputFile;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.std.api.file.location.FileLocation;

public class ClassPathIDEConfigurationEntry implements Externalizable {
	private static final long serialVersionUID = 1L;

	private ClassPathEntryInputFile inputFile;
	private Collection<? extends JavaSourceDirectory> sourceDirectories;
	private StructuredTaskResult sourceAttachment;
	private StructuredTaskResult docAttachment;
	private SakerPath sourceGenDirectory;
	private String displayName;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassPathIDEConfigurationEntry() {
	}

	public ClassPathIDEConfigurationEntry(FileLocation fileLocation,
			Collection<? extends JavaSourceDirectory> sourceDirectories, StructuredTaskResult sourceAttachment,
			StructuredTaskResult docAttachment) {
		this.inputFile = ClassPathEntryInputFile.create(fileLocation);
		this.sourceDirectories = sourceDirectories;
		this.sourceAttachment = sourceAttachment;
		this.docAttachment = docAttachment;
	}

	public ClassPathIDEConfigurationEntry(ClassPathEntryInputFile inputFile,
			Collection<? extends JavaSourceDirectory> sourceDirectories, StructuredTaskResult sourceAttachment,
			StructuredTaskResult docAttachment) {
		this.inputFile = inputFile;
		this.sourceDirectories = sourceDirectories;
		this.sourceAttachment = sourceAttachment;
		this.docAttachment = docAttachment;
	}

	public ClassPathIDEConfigurationEntry(FileLocation fileLocation) {
		this.inputFile = ClassPathEntryInputFile.create(fileLocation);
	}

	public ClassPathEntryInputFile getInputFile() {
		return inputFile;
	}

	public Collection<? extends JavaSourceDirectory> getSourceDirectories() {
		return sourceDirectories;
	}

	public StructuredTaskResult getSourceAttachment() {
		return sourceAttachment;
	}

	public StructuredTaskResult getDocumentationAttachment() {
		return docAttachment;
	}

	public void setSourceGenDirectory(SakerPath sourceGenDirectory) {
		this.sourceGenDirectory = sourceGenDirectory;
	}

	public SakerPath getSourceGenDirectory() {
		return sourceGenDirectory;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(inputFile);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
		out.writeObject(sourceAttachment);
		out.writeObject(docAttachment);
		out.writeObject(sourceGenDirectory);
		out.writeObject(displayName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputFile = (ClassPathEntryInputFile) in.readObject();
		sourceDirectories = SerialUtils.readExternalImmutableLinkedHashSet(in);
		sourceAttachment = (StructuredTaskResult) in.readObject();
		docAttachment = (StructuredTaskResult) in.readObject();
		sourceGenDirectory = (SakerPath) in.readObject();
		displayName = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((docAttachment == null) ? 0 : docAttachment.hashCode());
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((sourceAttachment == null) ? 0 : sourceAttachment.hashCode());
		result = prime * result + ((sourceDirectories == null) ? 0 : sourceDirectories.hashCode());
		result = prime * result + ((sourceGenDirectory == null) ? 0 : sourceGenDirectory.hashCode());
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
		ClassPathIDEConfigurationEntry other = (ClassPathIDEConfigurationEntry) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (docAttachment == null) {
			if (other.docAttachment != null)
				return false;
		} else if (!docAttachment.equals(other.docAttachment))
			return false;
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
		if (sourceGenDirectory == null) {
			if (other.sourceGenDirectory != null)
				return false;
		} else if (!sourceGenDirectory.equals(other.sourceGenDirectory))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (inputFile != null ? "fileLocation=" + inputFile + ", " : "")
				+ (sourceDirectories != null ? "sourceDirectories=" + sourceDirectories + ", " : "")
				+ (sourceAttachment != null ? "sourceAttachment=" + sourceAttachment + ", " : "")
				+ (docAttachment != null ? "docAttachment=" + docAttachment : "") + "]";
	}

}
