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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class GeneratedClassFileData extends ClassFileData implements ProcessorGeneratedClassHoldingFileData {
	private static final long serialVersionUID = 1L;

	protected GeneratedFileOrigin origins;
	protected Signature signature;

	public GeneratedClassFileData() {
		super();
	}

	public GeneratedClassFileData(SakerPath path, ContentDescriptor contentdescriptor, SourceFileData sourceFile,
			String classBinaryName, byte[] abiHash, byte[] implementationHash, Signature signature) {
		super(path, contentdescriptor, sourceFile, classBinaryName, abiHash, implementationHash);
		this.signature = signature;
	}

	@Override
	public NavigableMap<SakerPath, ClassFileData> getGeneratedClassDatas() {
		return Collections.emptyNavigableMap();
	}

	@Override
	public ImportScope getImportScope() {
		return sourceFile.getImportScope();
	}

	@Override
	public GeneratedFileOrigin getOrigin() {
		return origins;
	}

	public void setOrigin(GeneratedFileOrigin origins) {
		this.origins = origins;
	}

	public Signature getSignature() {
		return signature;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}

	@Override
	public NavigableMap<String, ? extends ClassSignature> getClasses() {
		if (signature instanceof ClassSignature) {
			TreeMap<String, ClassSignature> result = new TreeMap<>();
			result.put(((ClassSignature) signature).getBinaryName(), ((ClassSignature) signature));
			return result;
		}
		return Collections.emptyNavigableMap();
	}

	@Override
	public NavigableMap<String, ? extends ClassSignature> getRealizedClasses() {
		return getClasses();
	}

	@Override
	public PackageSignature getPackageSignature() {
		if (signature instanceof PackageSignature) {
			return (PackageSignature) signature;
		}
		return null;
	}

	@Override
	public PackageSignature getRealizedPackageSignature() {
		return getPackageSignature();
	}

	@Override
	public ModuleSignature getModuleSignature() {
		if (signature instanceof ModuleSignature) {
			return (ModuleSignature) signature;
		}
		return null;
	}

	@Override
	public ModuleSignature getRealizedModuleSignature() {
		return getModuleSignature();
	}

	@Override
	public TopLevelAbiUsage getABIUsage() {
		//no abi usage for a class file
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(signature);
		out.writeObject(origins);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		origins = (GeneratedFileOrigin) in.readObject();
		signature = (Signature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((origins == null) ? 0 : origins.hashCode());
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
		GeneratedClassFileData other = (GeneratedClassFileData) obj;
		if (origins == null) {
			if (other.origins != null)
				return false;
		} else if (!origins.equals(other.origins))
			return false;
		return true;
	}

}
