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

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public class ClassFileData extends CompiledFileData {
	private static final long serialVersionUID = 1L;

	/**
	 * For Externalizable implementation only.
	 */
	public ClassFileData() {
	}

	public ClassFileData(SakerPath path, ContentDescriptor contentdescriptor, SourceFileData sourceFile,
			String classBinaryName, byte[] abiHash, byte[] implementationHash) {
		super(path, contentdescriptor, sourceFile, classBinaryName, abiHash, implementationHash);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[lastModified=" + fileContent + ", "
				+ (sourceFile != null ? "sourceFile=" + sourceFile : "") + "]";
	}

}