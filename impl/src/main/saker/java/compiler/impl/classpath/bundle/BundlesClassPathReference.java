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
package saker.java.compiler.impl.classpath.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.ClassPathEntry;
import saker.java.compiler.api.classpath.ClassPathReference;

public class BundlesClassPathReference implements ClassPathReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private Collection<? extends BundleClassPathEntry> entries;

	/**
	 * For {@link Externalizable}.
	 */
	public BundlesClassPathReference() {
	}

	public BundlesClassPathReference(Collection<? extends BundleClassPathEntry> entries) {
		this.entries = entries;
	}

	@Override
	public Collection<? extends ClassPathEntry> getEntries() {
		return entries;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, entries);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		entries = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
		BundlesClassPathReference other = (BundlesClassPathReference) obj;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (entries != null ? "entries=" + entries : "") + "]";
	}

}
