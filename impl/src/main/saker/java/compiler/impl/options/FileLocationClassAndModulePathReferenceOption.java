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
package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.api.classpath.ClassPathVisitor;
import saker.java.compiler.api.classpath.FileClassPath;
import saker.java.compiler.api.modulepath.FileModulePath;
import saker.java.compiler.api.modulepath.ModulePathVisitor;
import saker.std.api.file.location.FileLocation;

public class FileLocationClassAndModulePathReferenceOption
		implements ClassPathReferenceOption, FileClassPath, ModulePathReferenceOption, FileModulePath, Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation fileLocation;

	/**
	 * For {@link Externalizable}.
	 */
	public FileLocationClassAndModulePathReferenceOption() {
	}

	public FileLocationClassAndModulePathReferenceOption(FileLocation fileLocation) {
		Objects.requireNonNull(fileLocation, "file location");
		this.fileLocation = fileLocation;
	}

	@Override
	public void accept(ClassPathVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(ModulePathVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public FileLocation getFileLocation() {
		return fileLocation;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileLocation);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		fileLocation = (FileLocation) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
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
		FileLocationClassAndModulePathReferenceOption other = (FileLocationClassAndModulePathReferenceOption) obj;
		if (fileLocation == null) {
			if (other.fileLocation != null)
				return false;
		} else if (!fileLocation.equals(other.fileLocation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + fileLocation + "]";
	}

}
