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

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public class GeneratedResourceFileData extends BasicFileData implements ProcessorGeneratedFileData {
	private static final long serialVersionUID = 1L;

	protected GeneratedFileOrigin origins;

	public GeneratedResourceFileData() {
		super();
	}

	public GeneratedResourceFileData(GeneratedResourceFileData data, GeneratedFileOrigin origins) {
		super(data);
		this.origins = origins;
	}

	public GeneratedResourceFileData(SakerPath path, ContentDescriptor contentdescriptor) {
		super(path, contentdescriptor);
	}

	public GeneratedResourceFileData(SakerPath path, ContentDescriptor contentdescriptor, GeneratedFileOrigin origins) {
		super(path, contentdescriptor);
		this.origins = origins;
	}

	@Override
	public FileDataKind getKind() {
		return FileDataKind.RESOURCE;
	}

	@Override
	public GeneratedFileOrigin getOrigin() {
		return origins;
	}

	public void setOrigin(GeneratedFileOrigin origins) {
		this.origins = origins;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(origins);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		origins = (GeneratedFileOrigin) in.readObject();
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
		GeneratedResourceFileData other = (GeneratedResourceFileData) obj;
		if (origins == null) {
			if (other.origins != null)
				return false;
		} else if (!origins.equals(other.origins))
			return false;
		return true;
	}

}
