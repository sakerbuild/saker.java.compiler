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
package saker.java.compiler.impl.compile.handler.invoker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.api.processor.ProcessorCreator;

public class ProcessorDetails implements Externalizable {
	private static final long serialVersionUID = 1L;

	private Map<String, String> options;
	private ProcessorCreator creator;

	private boolean consistent;
	private boolean aggregating;

	private transient String name;

	/**
	 * For {@link Externalizable}.
	 */
	public ProcessorDetails() {
	}

	public ProcessorDetails(JavaAnnotationProcessor procref, LinkedHashMap<String, String> options) {
		Objects.requireNonNull(procref, "processor reference");

		ProcessorConfiguration procconfig = procref.getProcessor();
		Objects.requireNonNull(procconfig, "processor configuration");

		this.creator = procconfig.getCreator();
		Objects.requireNonNull(this.creator, "processor creator");

		String processorname = this.creator.getName();
		Objects.requireNonNull(processorname, "processor name");

		this.name = processorname;
		this.options = options;
		this.consistent = procref.getConsistent();
		this.aggregating = procref.getAggregating();
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public String getProcessorName() {
		return name;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		SerialUtils.writeExternalMap(out, options);
		out.writeObject(creator);
		out.writeBoolean(aggregating);
		out.writeBoolean(consistent);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
		options = SerialUtils.readExternalImmutableLinkedHashMap(in);
		creator = (ProcessorCreator) in.readObject();
		aggregating = in.readBoolean();
		consistent = in.readBoolean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (aggregating ? 1231 : 1237);
		result = prime * result + (consistent ? 1231 : 1237);
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
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
		ProcessorDetails other = (ProcessorDetails) obj;
		if (aggregating != other.aggregating)
			return false;
		if (consistent != other.consistent)
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (options != null ? "options=" + options + ", " : "")
				+ (creator != null ? "creator=" + creator + ", " : "") + "consistent=" + consistent + ", aggregating="
				+ aggregating + "]";
	}

}