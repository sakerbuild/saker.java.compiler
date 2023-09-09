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
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.NullTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public final class NullTypeSignatureImpl extends AnnotatedSignatureImpl implements NullTypeSignature {
	private static final long serialVersionUID = 1L;

	private static final NullTypeSignature INSTANCE = new NullTypeSignatureImpl();

	/**
	 * For {@link Externalizable}.
	 */
	public NullTypeSignatureImpl() {
	}

	public static NullTypeSignature create() {
		return INSTANCE;
	}

	public static NullTypeSignature create(List<AnnotationSignature> annotations) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return INSTANCE;
		}
		return new NullTypeSignatureImpl(annotations);
	}

	private NullTypeSignatureImpl(List<AnnotationSignature> annotations) {
		super(annotations);
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

}
