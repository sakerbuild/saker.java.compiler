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
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import saker.java.compiler.impl.signature.Signature;
import testing.saker.java.compiler.TestFlag;

/**
 * Contains the source positions for the element signatures in a single source file.
 */
public class SignatureSourcePositions implements Externalizable {
	private static final long serialVersionUID = 1L;

	public static final class Position {
		protected int length;
		protected int lineIndex;
		protected int linePositionIndex;

		public Position(int length, int lineidx, int linepositionidx) {
			this.length = length;
			this.lineIndex = lineidx;
			this.linePositionIndex = linepositionidx;
		}

		public int getLength() {
			return length;
		}

		public int getLineIndex() {
			return lineIndex;
		}

		public int getLinePositionIndex() {
			return linePositionIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + length;
			result = prime * result + lineIndex;
			result = prime * result + linePositionIndex;
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
			Position other = (Position) obj;
			if (length != other.length)
				return false;
			if (lineIndex != other.lineIndex)
				return false;
			if (linePositionIndex != other.linePositionIndex)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + (lineIndex + 1) + ":" + (linePositionIndex + 1) + "-"
					+ (linePositionIndex + 1 + length) + "]";
		}
	}

	private Map<Object, SignatureSourcePositions> subPositions;

	/**
	 * The position of the signature associated with this instance. <code>null</code> for the top level instance or if
	 * no position is set.
	 */
	private Position position;

	public SignatureSourcePositions() {
	}

	public Position getPosition(SignaturePath signature) {
		SignatureSourcePositions pathpos = getPathPositions(signature, false);
		if (pathpos != null) {
			return pathpos.position;
		}
		return null;
	}

	public Position putPosition(SignaturePath sig, int startpos, int endpos, int lineidx, int linepositionidx) {
		Position result = new Position(Math.max(endpos - startpos, 0), lineidx, linepositionidx);
		if (TestFlag.ENABLED) {
			if (startpos < 0) {
				throw new IllegalArgumentException("Invalid position: " + result);
			}
		}

		SignatureSourcePositions sigpositions = getPathPositions(sig, true);
		if (sigpositions.position == null) {
			//can be duplicate in case syntax errors/compilation failures
			sigpositions.position = result;
		}
		return result;
	}

	private Map<Object, SignatureSourcePositions> getSubPositions() {
		return subPositions == null ? (subPositions = new HashMap<>()) : subPositions;
	}

	private SignatureSourcePositions getPathPositions(SignaturePath path, boolean create) {
		SignaturePath parent = path.getParent();
		Signature sig = path.getSignature();
		SignatureSourcePositions computeon;
		if (parent == null) {
			computeon = this;
		} else {
			computeon = getPathPositions(parent, create);
			if (computeon == null) {
				return null;
			}
		}
		Object index = path.getIndex();
		Object key = index == null ? sig : new IndexedKey(sig, index);
		if (create) {
			return computeon.getSubPositions().computeIfAbsent(key, (k) -> new SignatureSourcePositions());
		}
		return computeon.getSubPositions().get(key);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		int size = subPositions == null ? 0 : subPositions.size();

		if (position == null) {
			out.writeInt(-size - 1);
		} else {
			out.writeInt(position.length);
			out.writeInt(position.lineIndex);
			out.writeInt(position.linePositionIndex);

			out.writeInt(size);
		}

		if (size > 0) {
			Iterator<Entry<Object, SignatureSourcePositions>> it = subPositions.entrySet().iterator();
			while (size-- > 0) {
				Entry<Object, SignatureSourcePositions> entry = it.next();
				Object k = entry.getKey();
				if (k instanceof IndexedKey) {
					IndexedKey ik = (IndexedKey) k;
					out.writeObject(ik.index);
					out.writeObject(ik.sig);
				} else {
					out.writeObject(k);
				}

				SignatureSourcePositions sp = entry.getValue();
				sp.writeExternal(out);
			}
			if (it.hasNext()) {
				throw new ConcurrentModificationException("More entries encountered than expected: " + size);
			}
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int readsize = in.readInt();
		if (readsize >= 0) {
			//got position

			int length = readsize;
			int lineIndex = in.readInt();
			int linePositionIndex = in.readInt();
			this.position = new Position(length, lineIndex, linePositionIndex);

			readsize = in.readInt();
		} else {
			readsize = -(readsize + 1);
		}

		if (readsize > 0) {
			this.subPositions = new HashMap<>();
			while (readsize-- > 0) {
				Object ro = in.readObject();
				Object key;
				if (ro instanceof Signature) {
					key = ro;
				} else {
					Signature sig = (Signature) in.readObject();
					key = new IndexedKey(sig, ro);
				}

				SignatureSourcePositions sp = new SignatureSourcePositions();
				sp.readExternal(in);
				this.subPositions.put(key, sp);
			}
		}
	}

	private static final class IndexedKey {
		protected final Signature sig;
		protected final Object index;

		public IndexedKey(Signature sig, Object index) {
			if (TestFlag.ENABLED) {
				if (index instanceof Signature) {
					//shouldn't be Signature, as we rely on this for serialization
					//shouldn't happen, but check just in case during testing
					throw new AssertionError("invalid index object: " + index);
				}
			}

			this.sig = sig;
			this.index = index;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((index == null) ? 0 : index.hashCode());
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof IndexedKey))
				return false;
			IndexedKey other = (IndexedKey) obj;
			if (index == null) {
				if (other.index != null)
					return false;
			} else if (!index.equals(other.index))
				return false;
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "IndexedKey [sig=" + sig + ", index=" + index + "]";
		}

	}

}
