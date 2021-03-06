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

public class ImplementationVersionKeyTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
	private static final long serialVersionUID = 1L;

	protected Object implementationVersionKey;

	/**
	 * For {@link Externalizable}.
	 */
	public ImplementationVersionKeyTaskOutputChangeDetector() {
	}

	public ImplementationVersionKeyTaskOutputChangeDetector(Object implementationVersionKey) {
		Objects.requireNonNull(implementationVersionKey, "implementationbersionkey");
		this.implementationVersionKey = implementationVersionKey;
	}

	@Override
	public boolean isChanged(Object taskoutput) {
		JavaCompilerWorkerTaskOutput compileroutput = (JavaCompilerWorkerTaskOutput) taskoutput;
		Object currentversionkey = compileroutput.getImplementationVersionKey();
		return currentversionkey == null || !currentversionkey.equals(implementationVersionKey);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(implementationVersionKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		implementationVersionKey = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((implementationVersionKey == null) ? 0 : implementationVersionKey.hashCode());
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
		ImplementationVersionKeyTaskOutputChangeDetector other = (ImplementationVersionKeyTaskOutputChangeDetector) obj;
		if (implementationVersionKey == null) {
			if (other.implementationVersionKey != null)
				return false;
		} else if (!implementationVersionKey.equals(other.implementationVersionKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (implementationVersionKey != null ? "implementationVersionKey=" + implementationVersionKey : "")
				+ "]";
	}
}
