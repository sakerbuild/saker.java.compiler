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

import saker.java.compiler.impl.signature.Signature;

public final class SignaturePath implements Externalizable {
	private static final long serialVersionUID = 1L;

	public static final class ClassSignaturePathSignature implements Signature, Externalizable {
		private static final long serialVersionUID = 1L;

		private String binaryName;

		/**
		 * For {@link Externalizable}.
		 */
		public ClassSignaturePathSignature() {
		}

		public ClassSignaturePathSignature(String canonicalName) {
			this.binaryName = canonicalName;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(binaryName);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			binaryName = (String) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((binaryName == null) ? 0 : binaryName.hashCode());
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
			ClassSignaturePathSignature other = (ClassSignaturePathSignature) obj;
			if (binaryName == null) {
				if (other.binaryName != null)
					return false;
			} else if (!binaryName.equals(other.binaryName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return binaryName;
		}
	}

	private SignaturePath parent;
	private Signature signature;

	public SignaturePath() {
	}

	public SignaturePath(Signature signature) {
		this.signature = signature;
	}

	public SignaturePath(SignaturePath parent) {
		this.parent = parent;
	}

	public SignaturePath(SignaturePath parent, Signature signature) {
		this.parent = parent;
		this.signature = signature;
	}

	public void setParent(SignaturePath parent) {
		this.parent = parent;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}

	public SignaturePath getParent() {
		return parent;
	}

	public Signature getSignature() {
		return signature;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(parent);
		out.writeObject(signature);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		parent = (SignaturePath) in.readObject();
		signature = (Signature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		SignaturePath other = (SignaturePath) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
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
		return "[" + (parent == null ? "" : parent + " : ") + signature + "]";
	}

}
