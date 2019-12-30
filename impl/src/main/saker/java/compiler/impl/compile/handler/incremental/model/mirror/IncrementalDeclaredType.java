package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalDeclaredType extends IncrementalTypeMirror<ParameterizedTypeSignature>
		implements CommonDeclaredType {
	private static final AtomicReferenceFieldUpdater<IncrementalDeclaredType, TypeElement> ARFU_asElement = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalDeclaredType.class, TypeElement.class, "asElement");
	private static final AtomicReferenceFieldUpdater<IncrementalDeclaredType, TypeMirror> ARFU_enclosingType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalDeclaredType.class, TypeMirror.class, "enclosingType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalDeclaredType, List> ARFU_typeArguments = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalDeclaredType.class, List.class, "typeArguments");
	private static final AtomicReferenceFieldUpdater<IncrementalDeclaredType, DeclaredType> ARFU_capturedType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalDeclaredType.class, DeclaredType.class, "capturedType");

	private Element enclosingElement;

	private volatile transient TypeElement asElement;
	private volatile transient TypeMirror enclosingType;
	private volatile transient List<? extends TypeMirror> typeArguments;
	private volatile transient DeclaredType capturedType;

	public IncrementalDeclaredType(IncrementalElementsTypesBase elemTypes, ParameterizedTypeSignature signature,
			TypeElement element, Element enclosingElement) {
		super(elemTypes, signature);
		this.asElement = element;
		this.enclosingElement = enclosingElement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	public Element getEnclosingElement() {
		return enclosingElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.enclosingType = null;
		this.typeArguments = null;
		this.capturedType = null;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.DECLARED;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitDeclared(this, p);
	}

	@Override
	public TypeElement asElement() {
		TypeElement thisaselement = this.asElement;
		if (thisaselement != null) {
			return thisaselement;
		}
		thisaselement = elemTypes.getTypeElement(signature, enclosingElement);
		if (ARFU_asElement.compareAndSet(this, null, thisaselement)) {
			return thisaselement;
		}
		return this.asElement;
	}

	@Override
	public TypeMirror getEnclosingType() {
		TypeMirror thisenclosingtype = this.enclosingType;
		if (thisenclosingtype != null) {
			return thisenclosingtype;
		}
		ParameterizedTypeSignature enclosing = signature.getEnclosingSignature();
		if (enclosing == null) {
			thisenclosingtype = IncrementalElementsTypes.getNoneTypeKind();
		} else {
			thisenclosingtype = elemTypes.getTypeMirror(enclosing, enclosingElement);
		}
		if (ARFU_enclosingType.compareAndSet(this, null, thisenclosingtype)) {
			return thisenclosingtype;
		}
		return this.enclosingType;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		List<? extends TypeMirror> thistypearguments = this.typeArguments;
		if (thistypearguments != null) {
			return thistypearguments;
		}
		thistypearguments = elemTypes.getDeclaredTypeArguments(this);
		if (ARFU_typeArguments.compareAndSet(this, null, thistypearguments)) {
			return thistypearguments;
		}
		return this.typeArguments;
	}

	@Override
	public DeclaredType getCapturedType() {
		DeclaredType thiscapturedtype = capturedType;
		if (thiscapturedtype != null) {
			return thiscapturedtype;
		}
		thiscapturedtype = elemTypes.captureImpl(this);
		if (ARFU_capturedType.compareAndSet(this, null, thiscapturedtype)) {
			return thiscapturedtype;
		}
		return this.capturedType;
	}

}
