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
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;

public class WildcardTypeSignatureImpl extends AnnotatedSignatureImpl implements WildcardTypeSignature {
	private static final long serialVersionUID = 1L;

	private static final WildcardTypeSignature BOUNDLESS_INSTANCE = new WildcardTypeSignatureImpl(
			Collections.emptyList(), null, null);

	private TypeSignature lowerBounds;
	private TypeSignature upperBounds;

	public WildcardTypeSignatureImpl() {
	}

	public static WildcardTypeSignature create(ParserCache cache, TypeSignature lowerBounds,
			TypeSignature upperBounds) {
		if (lowerBounds == null) {
			if (upperBounds == null) {
				return BOUNDLESS_INSTANCE;
			}
			return cache.extendsWildcard(upperBounds);
		}
		if (upperBounds == null) {
			return cache.superWildcard(lowerBounds);
		}
		return new WildcardTypeSignatureImpl(Collections.emptyList(), lowerBounds, upperBounds);
	}

	public static WildcardTypeSignature create(ParserCache cache, List<AnnotationSignature> annotations,
			TypeSignature lowerBounds, TypeSignature upperBounds) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(cache, lowerBounds, upperBounds);
		}
		return new WildcardTypeSignatureImpl(annotations, lowerBounds, upperBounds);
	}

	private WildcardTypeSignatureImpl(List<AnnotationSignature> annotations, TypeSignature lowerBounds,
			TypeSignature upperBounds) {
		super(annotations);
		this.lowerBounds = lowerBounds;
		this.upperBounds = upperBounds;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return "?";
	}

	@Override
	public TypeSignature getUpperBounds() {
		return upperBounds;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return lowerBounds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lowerBounds == null) ? 0 : lowerBounds.hashCode());
		result = prime * result + ((upperBounds == null) ? 0 : upperBounds.hashCode());
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
		WildcardTypeSignatureImpl other = (WildcardTypeSignatureImpl) obj;
		if (lowerBounds == null) {
			if (other.lowerBounds != null)
				return false;
		} else if (!lowerBounds.equals(other.lowerBounds))
			return false;
		if (upperBounds == null) {
			if (other.upperBounds != null)
				return false;
		} else if (!upperBounds.equals(other.upperBounds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("?");
		if (upperBounds != null) {
			sb.append(" extends ");
			sb.append(upperBounds);
		}
		if (lowerBounds != null) {
			sb.append(" super ");
			sb.append(lowerBounds);
		}
		return sb.toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(upperBounds);
		out.writeObject(lowerBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		upperBounds = (TypeSignature) in.readObject();
		lowerBounds = (TypeSignature) in.readObject();
	}
}
