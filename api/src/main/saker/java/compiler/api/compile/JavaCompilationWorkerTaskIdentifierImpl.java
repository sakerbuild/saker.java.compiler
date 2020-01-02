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
package saker.java.compiler.api.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

final class JavaCompilationWorkerTaskIdentifierImpl implements Externalizable, JavaCompilationWorkerTaskIdentifier {
	private static final long serialVersionUID = 1L;

	protected String passIdentifier;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaCompilationWorkerTaskIdentifierImpl() {
	}

	JavaCompilationWorkerTaskIdentifierImpl(String passIdentifier) {
		this.passIdentifier = passIdentifier;
	}

	@Override
	public String getPassIdentifier() {
		return passIdentifier;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(passIdentifier);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		passIdentifier = in.readUTF();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + passIdentifier.hashCode();
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
		JavaCompilationWorkerTaskIdentifierImpl other = (JavaCompilationWorkerTaskIdentifierImpl) obj;
		if (passIdentifier == null) {
			if (other.passIdentifier != null)
				return false;
		} else if (!passIdentifier.equals(other.passIdentifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + passIdentifier + "]";
	}

}