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
import java.util.NavigableMap;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class GeneratedSourceFileData extends SourceFileData implements ProcessorGeneratedClassHoldingFileData {
	private static final long serialVersionUID = 1L;

	protected GeneratedFileOrigin origin;

	public GeneratedSourceFileData() {
		super();
	}

	public GeneratedSourceFileData(SourceFileData data, GeneratedFileOrigin origins) {
		super(data);
		this.origin = origins;
	}

	public GeneratedSourceFileData(SakerPath path, ContentDescriptor contentdescriptor,
			NavigableMap<String, ? extends ClassSignature> classes, TopLevelAbiUsage abiusage,
			PackageSignature packagesignature, ImportScope importscope) {
		super(path, contentdescriptor, classes, abiusage, packagesignature, importscope);
	}

	public GeneratedSourceFileData(SakerPath path, ContentDescriptor contentdescriptor,
			NavigableMap<String, ? extends ClassSignature> classes, PackageSignature packagesignature,
			ImportScope importscope) {
		super(path, contentdescriptor, classes, packagesignature, importscope);
	}

	public GeneratedSourceFileData(SakerPath path, ContentDescriptor contentdescriptor,
			NavigableMap<String, ? extends ClassSignature> classes, PackageSignature packagesignature,
			ImportScope importscope, GeneratedFileOrigin origins) {
		super(path, contentdescriptor, classes, packagesignature, importscope);
		this.origin = origins;
	}

	@Override
	public GeneratedFileOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(GeneratedFileOrigin origins) {
		this.origin = origins;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(origin);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		origin = (GeneratedFileOrigin) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
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
		GeneratedSourceFileData other = (GeneratedSourceFileData) obj;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		return true;
	}

}
