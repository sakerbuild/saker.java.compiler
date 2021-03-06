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
package saker.java.compiler.api.modulepath;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.Set;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.options.ModulePathReferenceOption;

final class JavaModulePathImpl implements JavaModulePath, Externalizable {
	private static final long serialVersionUID = 1L;

	protected Set<ModulePathReferenceOption> modulePathReferences;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaModulePathImpl() {
	}

	protected JavaModulePathImpl(Set<ModulePathReferenceOption> modulePathReferences) {
		Objects.requireNonNull(modulePathReferences, "module path");
		this.modulePathReferences = ImmutableUtils.makeImmutableLinkedHashSet(modulePathReferences);
	}

	@Override
	public boolean isEmpty() {
		return ObjectUtils.isNullOrEmpty(modulePathReferences);
	}

	@Override
	public void accept(ModulePathVisitor visitor) {
		for (ModulePathReferenceOption cpref : modulePathReferences) {
			cpref.accept(visitor);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, modulePathReferences);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		modulePathReferences = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modulePathReferences == null) ? 0 : modulePathReferences.hashCode());
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
		JavaModulePathImpl other = (JavaModulePathImpl) obj;
		if (modulePathReferences == null) {
			if (other.modulePathReferences != null)
				return false;
		} else if (!modulePathReferences.equals(other.modulePathReferences))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + modulePathReferences + "]";
	}

}
