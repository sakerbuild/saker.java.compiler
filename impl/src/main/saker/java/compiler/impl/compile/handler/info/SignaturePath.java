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
import saker.java.compiler.impl.signature.element.ClassSignature;

public final class SignaturePath implements Externalizable, Cloneable {
	private static final long serialVersionUID = 1L;

	private static final class ClassSignaturePathSignature implements Signature, Externalizable {
		private static final long serialVersionUID = 1L;

		private String binaryName;

		/**
		 * For {@link Externalizable}.
		 */
		public ClassSignaturePathSignature() {
		}

		public ClassSignaturePathSignature(String binaryName) {
			this.binaryName = binaryName;
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

	protected SignaturePath parent;
	protected Signature signature;
	protected Object index;

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

	public final SignaturePath getParent() {
		return parent;
	}

	public final Signature getSignature() {
		return signature;
	}

	public Object getIndex() {
		return index;
	}

	@Override
	protected SignaturePath clone() {
		try {
			return (SignaturePath) super.clone();
		} catch (CloneNotSupportedException e) {
			//shouldnt happen
			throw new RuntimeException(e);
		}
	}

	public SignaturePath cloneWithPrefixed(SignaturePath parent) {
		SignaturePath res = this.clone();
		if (this.parent == null) {
			res.parent = parent;
		} else {
			res.parent = this.parent.cloneWithPrefixed(parent);
		}
		return res;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(parent);
		out.writeObject(signature);
		out.writeObject(index);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		parent = (SignaturePath) in.readObject();
		signature = (Signature) in.readObject();
		index = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SignaturePath))
			return false;
		SignaturePath other = (SignaturePath) obj;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
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
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (parent != null) {
			sb.append(parent);
			sb.append(" : ");
		}
		sb.append(signature);
		if (index != null) {
			sb.append(" (");
			sb.append(index);
			sb.append(")");
		}
		sb.append("]");
		return sb.toString();
	}

	public static Signature getClassSignature(ClassSignature sig) {
		return new ClassSignaturePathSignature(sig.getBinaryName());
	}

	public static SignaturePath createIndexed(Object index) {
		SignaturePath res = new SignaturePath();
		res.index = index;
		return res;
	}

	public static SignaturePath createIndexed(SignaturePath parent, Object index) {
		SignaturePath res = new SignaturePath(parent);
		res.index = index;
		return res;
	}

	public static SignaturePath createIndexed(SignaturePath parent, Signature signature, Object index) {
		SignaturePath res = new SignaturePath(parent, signature);
		res.index = index;
		return res;
//		return new IndexedSignaturePath(parent, signature, index);
	}

	public static SignaturePath createIndexed(Signature signature, Object index) {
		SignaturePath res = new SignaturePath(signature);
		res.index = index;
		return res;
//		return new IndexedSignaturePath(signature, index);
	}

}
