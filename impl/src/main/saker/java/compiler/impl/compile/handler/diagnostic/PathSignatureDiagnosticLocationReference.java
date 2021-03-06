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
package saker.java.compiler.impl.compile.handler.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;

public class PathSignatureDiagnosticLocationReference implements DiagnosticLocationReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;
	private SignaturePath signature;

	/**
	 * For {@link Externalizable}.
	 */
	public PathSignatureDiagnosticLocationReference() {
	}

	public PathSignatureDiagnosticLocationReference(SakerPath path, SignaturePath signature) {
		this.path = path;
		this.signature = signature;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public DiagnosticLocation getLocation(DiagnosticPositionTable table) {
		return table.getForPathSignature(path, signature);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(signature);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		signature = (SignaturePath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
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
		PathSignatureDiagnosticLocationReference other = (PathSignatureDiagnosticLocationReference) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (path != null ? "path=" + path + ", " : "")
				+ (signature != null ? "signature=" + signature : "") + "]";
	}

}
