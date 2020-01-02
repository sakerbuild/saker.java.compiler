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
package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import java.util.Collection;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalAnnotatedConstruct;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public abstract class IncrementalElement<Sig extends AnnotatedSignature> extends IncrementalAnnotatedConstruct
		implements CommonElement, SignaturedElement<Sig> {
	protected Sig signature;

	public IncrementalElement(IncrementalElementsTypesBase elemTypes, Sig signature) {
		super(elemTypes);
		this.signature = signature;
	}

	@Override
	protected Collection<? extends AnnotationSignature> getSignatureAnnotations() {
		return signature.getAnnotations();
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return this;
	}

	@Override
	public Sig getSignature() {
		return signature;
	}

	public void setSignature(Sig signature) {
		super.invalidate();
		this.signature = signature;
	}

	@Override
	public String toString() {
		return signature.toString();
	}

	@Override
	public boolean isDeprecated() {
		return hasDeprecatedAnnotation();
	}

//	
//	hashCode and equals inherited from Object
//	
}
