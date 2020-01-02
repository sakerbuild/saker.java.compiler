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
package saker.java.compiler.impl.compile.handler;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

public class ExternalizableLocation implements Location, Externalizable, Comparable<ExternalizableLocation> {
	public static final ExternalizableLocation LOCATION_CLASS_OUTPUT = new ExternalizableLocation(
			StandardLocation.CLASS_OUTPUT);
	public static final ExternalizableLocation LOCATION_SOURCE_OUTPUT = new ExternalizableLocation(
			StandardLocation.SOURCE_OUTPUT);
	public static final ExternalizableLocation LOCATION_NATIVE_HEADER_OUTPUT = new ExternalizableLocation(
			StandardLocation.NATIVE_HEADER_OUTPUT);
	public static final ExternalizableLocation LOCATION_CLASS_PATH = new ExternalizableLocation(
			StandardLocation.CLASS_PATH);
	public static final ExternalizableLocation LOCATION_PLATFORM_CLASS_PATH = new ExternalizableLocation(
			StandardLocation.PLATFORM_CLASS_PATH);

	private static final long serialVersionUID = 1L;

	private String name;
	private boolean output;

	/**
	 * For {@link Externalizable}.
	 */
	public ExternalizableLocation() {
	}

	public ExternalizableLocation(String name, boolean output) {
		Objects.requireNonNull(name, "name");
		this.name = name;
		this.output = output;
	}

	public ExternalizableLocation(Location location) {
		this(location.getName(), location.isOutputLocation());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeBoolean(output);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
		output = in.readBoolean();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isOutputLocation() {
		return output;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (output ? 1231 : 1237);
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
		ExternalizableLocation other = (ExternalizableLocation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (output != other.output)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + name + ", output=" + output + "]";
	}

	@Override
	public int compareTo(ExternalizableLocation o) {
		int cmp;
		cmp = this.name.compareTo(o.name);
		if (cmp != 0) {
			return cmp;
		}
		return Boolean.compare(this.output, o.output);
	}

}
