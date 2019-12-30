package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import javax.lang.model.element.Element;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.PrimitiveTypeSignature;

public class IncrementalPrimitiveType extends IncrementalTypeMirror<PrimitiveTypeSignature> implements PrimitiveType {
	private Element enclosingElement;

	public IncrementalPrimitiveType(IncrementalElementsTypesBase elemTypes, PrimitiveTypeSignature signature,
			Element enclosingelement) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingelement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	public TypeKind getKind() {
		return signature.getTypeKind();
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitPrimitive(this, p);
	}
}
