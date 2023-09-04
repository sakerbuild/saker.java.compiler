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

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public class IncrementalAnnotationMirror implements AnnotationMirror, IncrementallyModelled {
	private IncrementalElementsTypesBase elemTypes;
	private AnnotationSignature signature;
	private Element enclosingElement;

	private SignaturePath annotationSignaturePath;

	public IncrementalAnnotationMirror(IncrementalElementsTypesBase elementTypes, AnnotationSignature signature,
			Element enclosingElement, SignaturePath annotationSignaturePath) {
		this.elemTypes = elementTypes;
		this.signature = signature;
		this.enclosingElement = enclosingElement;
		this.annotationSignaturePath = annotationSignaturePath;
	}

	public Element getResolutionEnclosingElement() {
		return enclosingElement;
	}

	public AnnotationSignature getSignature() {
		return signature;
	}

	public SignaturePath getAnnotationSignaturePath() {
		return annotationSignaturePath;
	}

	@Override
	public DeclaredType getAnnotationType() {
		return elemTypes.getAnnotationDeclaredType(signature, enclosingElement);
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
		return elemTypes.getAnnotationValues(this, false, enclosingElement);
	}

	@Override
	public String toString() {
		return signature.toString();
	}
}
