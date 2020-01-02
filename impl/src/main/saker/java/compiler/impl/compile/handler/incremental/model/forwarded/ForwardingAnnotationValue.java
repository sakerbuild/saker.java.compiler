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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractAnnotationValueVisitor8;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingAnnotationValue implements ForwardingObject<AnnotationValue>, AnnotationValue {
	protected IncrementalElementsTypesBase elemTypes;
	protected AnnotationValue subject;

	public ForwardingAnnotationValue(IncrementalElementsTypesBase elemTypes, AnnotationValue subject) {
		this.elemTypes = elemTypes;
		this.subject = subject;
	}

	@Override
	public AnnotationValue getForwardedSubject() {
		return subject;
	}

	@Override
	public Object getValue() {
		//XXX cache this?
		return elemTypes.javac(() -> subject.accept(ValueVisitor.INSTANCE, null));
	}

	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		return elemTypes.javac(() -> subject.accept(new VisitorForwarder<>(v), p));
	}

	@Override
	public String toString() {
		return elemTypes.javac(subject::toString);
	}

	private static class ValueVisitor extends SimpleAnnotationValueVisitor8<Object, ForwardingAnnotationValue> {
		public static final ValueVisitor INSTANCE = new ValueVisitor();

		@Override
		protected Object defaultAction(Object o, ForwardingAnnotationValue p) {
			return o;
		}

		@Override
		public TypeMirror visitType(TypeMirror t, ForwardingAnnotationValue p) {
			return p.elemTypes.forwardType(t);
		}

		@Override
		public VariableElement visitEnumConstant(VariableElement c, ForwardingAnnotationValue p) {
			return p.elemTypes.forwardElement(c);
		}

		@Override
		public AnnotationMirror visitAnnotation(AnnotationMirror a, ForwardingAnnotationValue p) {
			return p.elemTypes.forward(a);
		}

		@Override
		public List<? extends AnnotationValue> visitArray(List<? extends AnnotationValue> vals,
				ForwardingAnnotationValue p) {
			return forward(vals, p.elemTypes);
		}

	}

	private static List<? extends AnnotationValue> forward(List<? extends AnnotationValue> vals,
			IncrementalElementsTypesBase elemTypes) {
		return JavaTaskUtils.cloneImmutableList(vals, elemTypes::forward);
	}

	private class VisitorForwarder<R, P> extends AbstractAnnotationValueVisitor8<R, P> {
		private AnnotationValueVisitor<R, P> v;

		public VisitorForwarder(AnnotationValueVisitor<R, P> v) {
			this.v = v;
		}

		@Override
		public R visitBoolean(boolean b, P p) {
			return v.visitBoolean(b, p);
		}

		@Override
		public R visitByte(byte b, P p) {
			return v.visitByte(b, p);
		}

		@Override
		public R visitChar(char c, P p) {
			return v.visitChar(c, p);
		}

		@Override
		public R visitDouble(double d, P p) {
			return v.visitDouble(d, p);
		}

		@Override
		public R visitFloat(float f, P p) {
			return v.visitFloat(f, p);
		}

		@Override
		public R visitInt(int i, P p) {
			return v.visitInt(i, p);
		}

		@Override
		public R visitLong(long i, P p) {
			return v.visitLong(i, p);
		}

		@Override
		public R visitShort(short s, P p) {
			return v.visitShort(s, p);
		}

		@Override
		public R visitString(String s, P p) {
			return v.visitString(s, p);
		}

		@Override
		public R visitType(TypeMirror t, P p) {
			return v.visitType(elemTypes.forwardType(t), p);
		}

		@Override
		public R visitEnumConstant(VariableElement c, P p) {
			return v.visitEnumConstant(elemTypes.forwardElement(c), p);
		}

		@Override
		public R visitAnnotation(AnnotationMirror a, P p) {
			return v.visitAnnotation(elemTypes.forward(a), p);
		}

		@Override
		public R visitArray(List<? extends AnnotationValue> vals, P p) {
			return v.visitArray(forward(vals, elemTypes), p);
		}

		@Override
		public R visitUnknown(AnnotationValue av, P p) {
			return v.visitUnknown(elemTypes.forward(av), p);
		}
	}

}
