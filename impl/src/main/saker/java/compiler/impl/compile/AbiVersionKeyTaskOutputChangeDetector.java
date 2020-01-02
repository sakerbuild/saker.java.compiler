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
package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;

public class AbiVersionKeyTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
	private static final long serialVersionUID = 1L;

	protected Object abiVersionKey;

	/**
	 * For {@link Externalizable}.
	 */
	public AbiVersionKeyTaskOutputChangeDetector() {
	}

	public AbiVersionKeyTaskOutputChangeDetector(Object abiVersionKey) {
		Objects.requireNonNull(abiVersionKey, "abiversionkey");
		this.abiVersionKey = abiVersionKey;
	}

	@Override
	public boolean isChanged(Object taskoutput) {
		JavaCompilerWorkerTaskOutput compileroutput = (JavaCompilerWorkerTaskOutput) taskoutput;
		Object currentabikey = compileroutput.getAbiVersionKey();
		return currentabikey == null || !currentabikey.equals(abiVersionKey);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(abiVersionKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		abiVersionKey = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abiVersionKey == null) ? 0 : abiVersionKey.hashCode());
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
		AbiVersionKeyTaskOutputChangeDetector other = (AbiVersionKeyTaskOutputChangeDetector) obj;
		if (abiVersionKey == null) {
			if (other.abiVersionKey != null)
				return false;
		} else if (!abiVersionKey.equals(other.abiVersionKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (abiVersionKey != null ? "abiVersionKey=" + abiVersionKey : "") + "]";
	}

}
