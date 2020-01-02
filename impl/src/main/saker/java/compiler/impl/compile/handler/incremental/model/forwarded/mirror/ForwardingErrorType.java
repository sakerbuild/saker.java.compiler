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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingErrorType extends ForwardingTypeMirrorBase<ErrorType> implements ErrorType {

	public ForwardingErrorType(IncrementalElementsTypesBase elemTypes, ErrorType subject) {
		super(elemTypes, subject);
	}

	@Override
	public Element asElement() {
		return elemTypes.forwardElement(elemTypes.javac(subject::asElement));
	}

	@Override
	public TypeMirror getEnclosingType() {
		return elemTypes.forwardType(subject::getEnclosingType);
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		return elemTypes.forwardTypes(subject::getTypeArguments);
	}

}
