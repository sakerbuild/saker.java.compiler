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
package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.api.processor.ProcessorCreator;

public class SimpleProcessorConfiguration implements ProcessorConfiguration, Externalizable {
	private static final long serialVersionUID = 1L;

	private ProcessorCreator creator;

	private boolean aggregating = true;
	private boolean consistent = true;
	private boolean alwaysRun = false;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleProcessorConfiguration() {
	}

	public SimpleProcessorConfiguration(ProcessorCreator creator) {
		Objects.requireNonNull(creator, "creator");
		this.creator = creator;
	}

	public SimpleProcessorConfiguration(ProcessorCreator creator, boolean aggregating, boolean consistent,
			boolean alwaysRun) {
		Objects.requireNonNull(creator, "creator");
		this.creator = creator;
		this.aggregating = aggregating;
		this.consistent = consistent;
		this.alwaysRun = alwaysRun;
	}

	public SimpleProcessorConfiguration(ProcessorConfiguration copy) {
		this(Objects.requireNonNull(copy, "copy").getCreator());
		this.aggregating = copy.getAggregating();
		this.consistent = copy.getConsistent();
		this.alwaysRun = copy.getAlwaysRun();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(creator);
		out.writeBoolean(aggregating);
		out.writeBoolean(consistent);
		out.writeBoolean(alwaysRun);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		creator = (ProcessorCreator) in.readObject();
		aggregating = in.readBoolean();
		consistent = in.readBoolean();
		alwaysRun = in.readBoolean();
	}

	@Override
	public ProcessorCreator getCreator() {
		return creator;
	}

	@Override
	public boolean getAlwaysRun() {
		return alwaysRun;
	}

	@Override
	public boolean getConsistent() {
		return consistent;
	}

	@Override
	public boolean getAggregating() {
		return aggregating;
	}

	public void setAggregating(boolean aggregating) {
		this.aggregating = aggregating;
	}

	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}

	public void setAlwaysRun(boolean alwaysRun) {
		this.alwaysRun = alwaysRun;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (aggregating ? 1231 : 1237);
		result = prime * result + (alwaysRun ? 1231 : 1237);
		result = prime * result + (consistent ? 1231 : 1237);
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
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
		SimpleProcessorConfiguration other = (SimpleProcessorConfiguration) obj;
		if (aggregating != other.aggregating)
			return false;
		if (alwaysRun != other.alwaysRun)
			return false;
		if (consistent != other.consistent)
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (creator != null ? "creator=" + creator + ", " : "") + "aggregating="
				+ aggregating + ", consistent=" + consistent + ", alwaysRun=" + alwaysRun + "]";
	}

}
