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
package saker.java.compiler.impl.compile.handler.incremental;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;

public class ProcessorReadResourceDependencyTag implements Externalizable {
	private static final long serialVersionUID = 1L;

	private ProcessorDetails processorDetails;

	/**
	 * For {@link Externalizable}.
	 */
	public ProcessorReadResourceDependencyTag() {
	}

	public ProcessorReadResourceDependencyTag(ProcessorDetails processorDetails) {
		this.processorDetails = processorDetails;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(processorDetails);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		processorDetails = (ProcessorDetails) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processorDetails == null) ? 0 : processorDetails.hashCode());
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
		ProcessorReadResourceDependencyTag other = (ProcessorReadResourceDependencyTag) obj;
		if (processorDetails == null) {
			if (other.processorDetails != null)
				return false;
		} else if (!processorDetails.equals(other.processorDetails))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (processorDetails != null ? "processorDetails=" + processorDetails : "") + "]";
	}

}
