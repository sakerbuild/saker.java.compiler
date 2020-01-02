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
import java.util.HashMap;
import java.util.Map;

import saker.build.thirdparty.saker.util.io.SerialUtils;

/**
 * Contains the source positions for the element signatures in a single source file.
 */
public class SignatureSourcePositions implements Externalizable {
	private static final long serialVersionUID = 1L;

	public static class Position implements Externalizable {
		private static final long serialVersionUID = 1L;

		private int startPosition;
		private int endPosition;
		private int lineIndex;
		private int linePositionIndex;

		/**
		 * For {@link Externalizable}.
		 */
		public Position() {
		}

		public Position(int startpos, int endpos, int lineidx, int linepositionidx) {
			this.startPosition = startpos;
			this.endPosition = endpos;
			this.lineIndex = lineidx;
			this.linePositionIndex = linepositionidx;
		}

		/**
		 * Gets the start position compared to the beginning of the file.
		 * <p>
		 * The start position is inclusive.
		 * 
		 * @return The start position.
		 */
		public int getStartPosition() {
			return startPosition;
		}

		/**
		 * Gets the end position compared to the beginning of the file.
		 * <p>
		 * The end position is exclusive.
		 * 
		 * @return The end position.
		 */
		public int getEndPosition() {
			return endPosition;
		}

		public int getLineIndex() {
			return lineIndex;
		}

		public int getLinePositionIndex() {
			return linePositionIndex;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(startPosition);
			out.writeInt(endPosition);
			out.writeInt(lineIndex);
			out.writeInt(linePositionIndex);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			startPosition = in.readInt();
			endPosition = in.readInt();
			lineIndex = in.readInt();
			linePositionIndex = in.readInt();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + endPosition;
			result = prime * result + lineIndex;
			result = prime * result + linePositionIndex;
			result = prime * result + startPosition;
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
			if (endPosition != other.endPosition)
				return false;
			if (lineIndex != other.lineIndex)
				return false;
			if (linePositionIndex != other.linePositionIndex)
				return false;
			if (startPosition != other.startPosition)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + lineIndex + ":" + startPosition + "-" + endPosition
					+ ", linePosIdx=" + linePositionIndex + "]";
		}
	}

	private Map<SignaturePath, Position> positions = new HashMap<>();

	public SignatureSourcePositions() {
	}

	public Position getPosition(SignaturePath signature) {
		return positions.get(signature);
	}

	public Position putPosition(SignaturePath sig, int startpos, int endpos, int lineidx, int linepositionidx) {
		Position result = new Position(startpos, endpos, lineidx, linepositionidx);
		positions.put(sig, result);
		return result;
	}

	public Map<SignaturePath, Position> getPositions() {
		return positions;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, positions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		positions = SerialUtils.readExternalImmutableHashMap(in);
	}

}
