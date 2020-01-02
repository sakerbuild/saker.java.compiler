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

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public class IncrementalAnnotationValue implements IncrementallyModelled, AnnotationValue {
	private static final String VALUE_ERROR = "<error>";

	private IncrementalElementsTypesBase elementTypes;
	private AnnotationSignature.Value value;
	private TypeMirror targetTypeMirror;

	private Element resolutionElement;

	public IncrementalAnnotationValue(IncrementalElementsTypesBase elementTypes, AnnotationSignature.Value value,
			TypeMirror targettypemirror, Element resolutionElement) {
		this.elementTypes = elementTypes;
		this.value = value;
		this.targetTypeMirror = targettypemirror;
		this.resolutionElement = resolutionElement;
	}

	public AnnotationSignature.Value getSignatureValue() {
		return value;
	}

	@Override
	public Object getValue() {
		//XXX cache this?
		return elementTypes.getAnnotationValue(value, targetTypeMirror, resolutionElement);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		//XXX do annotation value visiting better instead of lot of instanceofs
		Object val = getValue();
		if (val instanceof Byte) {
			return v.visitByte((byte) val, p);
		}
		if (val instanceof Short) {
			return v.visitShort((short) val, p);
		}
		if (val instanceof Integer) {
			return v.visitInt((int) val, p);
		}
		if (val instanceof Long) {
			return v.visitLong((long) val, p);
		}
		if (val instanceof Float) {
			return v.visitFloat((float) val, p);
		}
		if (val instanceof Double) {
			return v.visitDouble((double) val, p);
		}
		if (val instanceof Character) {
			return v.visitChar((char) val, p);
		}
		if (val instanceof Boolean) {
			return v.visitBoolean((boolean) val, p);
		}
		if (val instanceof String) {
			return v.visitString((String) val, p);
		}
		if (val instanceof AnnotationMirror) {
			return v.visitAnnotation((AnnotationMirror) val, p);
		}
		if (val instanceof List) {
			return v.visitArray((List<? extends AnnotationValue>) val, p);
		}
		if (val instanceof TypeMirror) {
			return v.visitType((TypeMirror) val, p);
		}
		if (val instanceof VariableElement) {
			return v.visitEnumConstant((VariableElement) val, p);
		}
		//if error, apply visitString with <error> parameter (same as javac)
		return v.visitString(VALUE_ERROR, p);
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Object val = getValue();
		result = prime * result + ((val == null) ? 0 : val.hashCode());
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
		IncrementalAnnotationValue other = (IncrementalAnnotationValue) obj;
		Object val = other.getValue();
		if (getValue() == null) {
			if (val != null)
				return false;
		} else if (!getValue().equals(val))
			return false;
		return true;
	}

}
