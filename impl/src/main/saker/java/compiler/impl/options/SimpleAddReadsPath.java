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
import java.util.Collection;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.option.JavaAddReads;

public class SimpleAddReadsPath implements JavaAddReads, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final Pattern PATTERN_SPLIT_COMMA = Pattern.compile("[,]+");

	private String module;
	private Set<String> requires;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleAddReadsPath() {
	}

	public SimpleAddReadsPath(String module, Collection<String> requires) {
		Objects.requireNonNull(module, "add reads module");
		Objects.requireNonNull(requires, "add reads required modules");
		if (requires.isEmpty()) {
			throw new IllegalArgumentException("No required modules specified for add-reads.");
		}
		this.module = module;
		this.requires = ImmutableUtils.makeImmutableLinkedHashSet(requires);
	}

	public SimpleAddReadsPath(JavaAddReads copy) {
		this(copy.getModule(), copy.getRequires());
	}

	public static JavaAddReads valueOf(String cmdlineoption) {
		int eqidx = cmdlineoption.indexOf('=');
		if (eqidx < 0) {
			throw new IllegalArgumentException(
					"Invalid format: " + cmdlineoption + " expected: 'module=other-module(,other-module)*'.");
		}

		String module = cmdlineoption.substring(0, eqidx);

		String[] target = PATTERN_SPLIT_COMMA.split(cmdlineoption.substring(eqidx + 1));
		NavigableSet<String> reqmodules = ImmutableUtils.makeImmutableNavigableSet(target);

		//no need to copy twice
		SimpleAddReadsPath result = new SimpleAddReadsPath();
		result.module = module;
		result.requires = reqmodules;
		return result;
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public Set<String> getRequires() {
		return requires;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(module);
		SerialUtils.writeExternalCollection(out, requires);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		module = (String) in.readObject();
		requires = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((requires == null) ? 0 : requires.hashCode());
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
		SimpleAddReadsPath other = (SimpleAddReadsPath) obj;
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module))
			return false;
		if (requires == null) {
			if (other.requires != null)
				return false;
		} else if (!requires.equals(other.requires))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("[module=");
		sb.append(module);
		sb.append(", requires=");
		sb.append(requires);
		sb.append("]");
		return sb.toString();
	}
}
