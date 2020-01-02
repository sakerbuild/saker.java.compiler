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
package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.Collection;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public class SignatureIncrementalAnnotatedConstruct extends IncrementalAnnotatedConstruct {
	private AnnotatedSignature signature;
	private Element enclosingElement;

	public SignatureIncrementalAnnotatedConstruct(IncrementalElementsTypesBase elemTypes, AnnotatedSignature signature,
			Element enclosingElement) {
		super(elemTypes);
		this.signature = signature;
		this.enclosingElement = enclosingElement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	protected Collection<? extends AnnotationSignature> getSignatureAnnotations() {
		return signature.getAnnotations();
	}

	@Override
	public String toString() {
		return signature.toString();
	}
}
