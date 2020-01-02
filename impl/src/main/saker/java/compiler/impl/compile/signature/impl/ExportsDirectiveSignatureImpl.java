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
package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.ModuleSignature.ExportsDirectiveSignature;
import saker.java.compiler.impl.signature.type.NameSignature;

public class ExportsDirectiveSignatureImpl implements ExportsDirectiveSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private NameSignature exportsPackage;
	private List<NameSignature> targetModules;

	/**
	 * For {@link Externalizable}.
	 */
	public ExportsDirectiveSignatureImpl() {
	}

	public ExportsDirectiveSignatureImpl(NameSignature exportsPackage, List<NameSignature> targetModules) {
		this.exportsPackage = exportsPackage;
		this.targetModules = targetModules;
	}

	@Override
	public NameSignature getExportsPackage() {
		return exportsPackage;
	}

	@Override
	public List<? extends NameSignature> getTargetModules() {
		return targetModules;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(exportsPackage);
		SerialUtils.writeExternalCollection(out, targetModules);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		exportsPackage = (NameSignature) in.readObject();
		targetModules = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exportsPackage == null) ? 0 : exportsPackage.hashCode());
		result = prime * result + ((targetModules == null) ? 0 : targetModules.hashCode());
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
		ExportsDirectiveSignatureImpl other = (ExportsDirectiveSignatureImpl) obj;
		if (exportsPackage == null) {
			if (other.exportsPackage != null)
				return false;
		} else if (!exportsPackage.equals(other.exportsPackage))
			return false;
		if (targetModules == null) {
			if (other.targetModules != null)
				return false;
		} else if (!targetModules.equals(other.targetModules))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "exports " + exportsPackage
				+ (targetModules == null ? "" : " to " + StringUtils.toStringJoin(", ", targetModules));
	}
}
