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

public class SimpleDiagnosticLocationReference implements DiagnosticLocationReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private DiagnosticLocation location;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleDiagnosticLocationReference() {
	}

	public SimpleDiagnosticLocationReference(DiagnosticLocation location) {
		this.location = location;
	}

	@Override
	public SakerPath getPath() {
		return location.getPath();
	}

	@Override
	public DiagnosticLocation getLocation(DiagnosticPositionTable table) {
		return location;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(location);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		location = (DiagnosticLocation) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
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
		SimpleDiagnosticLocationReference other = (SimpleDiagnosticLocationReference) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + location + "]";
	}
}
