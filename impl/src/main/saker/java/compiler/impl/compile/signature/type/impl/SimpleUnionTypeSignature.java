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
package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnionTypeSignature;

public final class SimpleUnionTypeSignature implements UnionTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> alternatives;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleUnionTypeSignature() {
	}

	public SimpleUnionTypeSignature(List<? extends TypeSignature> alternatives) {
		this.alternatives = alternatives;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, alternatives);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		alternatives = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public List<? extends TypeSignature> getAlternatives() {
		return alternatives;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin(" | ", alternatives);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(alternatives);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleUnionTypeSignature other = (SimpleUnionTypeSignature) obj;
		if (alternatives == null) {
			if (other.alternatives != null)
				return false;
		} else if (!alternatives.equals(other.alternatives))
			return false;
		return true;
	}

}
