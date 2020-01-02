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
package saker.java.compiler.impl.compile.handler.info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public class CompiledFileData extends BasicFileData {
	private static final long serialVersionUID = 1L;
	protected SourceFileData sourceFile;
	protected String classBinaryName;
	protected byte[] implementationHash;
	protected byte[] abiHash;

	/**
	 * For {@link Externalizable}.
	 */
	public CompiledFileData() {
	}

	public CompiledFileData(SakerPath path, ContentDescriptor contentdescriptor, SourceFileData sourceFile,
			String classBinaryName, byte[] abiHash, byte[] implementationHash) {
		super(path, contentdescriptor);
		this.sourceFile = sourceFile;
		this.classBinaryName = classBinaryName;
		this.abiHash = abiHash;
		this.implementationHash = implementationHash;
	}

	@Override
	public FileDataKind getKind() {
		return FileDataKind.CLASS;
	}

	public SourceFileData getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(SourceFileData sourceFile) {
		this.sourceFile = sourceFile;
	}

	public String getClassBinaryName() {
		return classBinaryName;
	}

	public void setClassBinaryName(String classBinaryName) {
		this.classBinaryName = classBinaryName;
	}

	public byte[] getAbiHash() {
		return abiHash;
	}

	public byte[] getImplementationHash() {
		return implementationHash;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[sourceFile=" + sourceFile + ", classBinaryName=" + classBinaryName
				+ ", toString()=" + super.toString() + "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(sourceFile);
		out.writeUTF(classBinaryName);
		out.writeObject(abiHash);
		out.writeObject(implementationHash);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		sourceFile = (SourceFileData) in.readObject();
		classBinaryName = in.readUTF();
		abiHash = (byte[]) in.readObject();
		implementationHash = (byte[]) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((classBinaryName == null) ? 0 : classBinaryName.hashCode());
		result = prime * result + ((sourceFile == null) ? 0 : sourceFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompiledFileData other = (CompiledFileData) obj;
		if (classBinaryName == null) {
			if (other.classBinaryName != null)
				return false;
		} else if (!classBinaryName.equals(other.classBinaryName))
			return false;
		if (sourceFile == null) {
			if (other.sourceFile != null)
				return false;
		} else if (!sourceFile.equals(other.sourceFile))
			return false;
		return true;
	}

}