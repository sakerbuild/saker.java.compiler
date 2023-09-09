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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnionTypeSignature;

public final class UnionTypeSignatureImpl extends AnnotatedSignatureImpl implements UnionTypeSignature {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> alternatives;

	public UnionTypeSignatureImpl() {
	}

	public static UnionTypeSignature create(List<? extends TypeSignature> alternatives) {
		return new SimpleUnionTypeSignature(alternatives);
	}

	public static UnionTypeSignature create(List<? extends AnnotationSignature> annotations,
			List<? extends TypeSignature> alternatives) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(alternatives);
		}
		return new UnionTypeSignatureImpl(annotations, alternatives);
	}

	private UnionTypeSignatureImpl(List<? extends AnnotationSignature> annotations,
			List<? extends TypeSignature> alternatives) {
		super(annotations);
		this.alternatives = alternatives;
	}

	@Override
	public List<? extends TypeSignature> getAlternatives() {
		return alternatives;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((alternatives == null) ? 0 : alternatives.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnionTypeSignatureImpl other = (UnionTypeSignatureImpl) obj;
		if (alternatives == null) {
			if (other.alternatives != null)
				return false;
		} else if (!alternatives.equals(other.alternatives))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin(" | ", alternatives);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		SerialUtils.writeExternalCollection(out, alternatives);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		alternatives = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}
}
