package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.CapturedWildcardTypeParameterElement;

public class CapturedTypeVariable extends SimpleTypeMirror implements TypeVariable {
	private static final AtomicReferenceFieldUpdater<CapturedTypeVariable, CapturedWildcardTypeParameterElement> ARFU_typeParameter = AtomicReferenceFieldUpdater
			.newUpdater(CapturedTypeVariable.class, CapturedWildcardTypeParameterElement.class, "typeParameter");

	private CommonWildcardType wildcardType;
	private TypeMirror upperBound;
	private TypeMirror lowerBound;

	private volatile transient CapturedWildcardTypeParameterElement typeParameter;

	public CapturedTypeVariable(IncrementalElementsTypesBase elemTypes, CommonWildcardType wildcard, TypeMirror upperBound,
			TypeMirror lowerBound) {
		super(elemTypes);
		this.wildcardType = wildcard;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.TYPEVAR;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}

	@Override
	public Element asElement() {
		CapturedWildcardTypeParameterElement thistypeparameter = this.typeParameter;
		if (thistypeparameter == null) {
			thistypeparameter = new CapturedWildcardTypeParameterElement(elemTypes, this);
			if (ARFU_typeParameter.compareAndSet(this, null, thistypeparameter)) {
				return thistypeparameter;
			}
		}
		return this.typeParameter;
	}

	@Override
	public TypeMirror getUpperBound() {
		return upperBound;
	}

	@Override
	public TypeMirror getLowerBound() {
		return lowerBound;
	}

	public CommonWildcardType getWildcardType() {
		return wildcardType;
	}

	@Override
	public String toString() {
		return asElement().getSimpleName() + (lowerBound.getKind() == TypeKind.NULL ? "" : " super " + lowerBound)
				+ (" extends " + upperBound);
	}

}
