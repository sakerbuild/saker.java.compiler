package saker.java.compiler.util14.impl.model.elem;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.DocumentedIncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalExecutableElement;
import saker.java.compiler.impl.compile.signature.impl.FullMethodSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalRecordComponentElement extends IncrementalElement<FieldSignature>
		implements RecordComponentElement, DocumentedIncrementalElement<FieldSignature> {
	private static final AtomicReferenceFieldUpdater<IncrementalRecordComponentElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalRecordComponentElement.class, TypeMirror.class, "asType");

	private volatile TypeMirror asType;

	private IncrementalElement<?> enclosingElement;
	private ExecutableElement accessor;

	public IncrementalRecordComponentElement(IncrementalElementsTypesBase elemTypes, FieldSignature signature) {
		super(elemTypes, signature);
		accessor = new IncrementalExecutableElement(FullMethodSignature.create(signature.getSimpleName(),
				IncrementalElementsTypes.MODIFIERS_PUBLIC, null, null, signature.getTypeSignature(), null,
				ElementKind.METHOD, null, null, false, null), this, elemTypes);
		;
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype != null) {
			return thisastype;
		}
		thisastype = elemTypes.getTypeMirror(signature.getTypeSignature(), this);
		if (ARFU_asType.compareAndSet(this, null, thisastype)) {
			return thisastype;
		}
		return this.asType;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.RECORD_COMPONENT;
	}

	@Override
	public ExecutableElement getAccessor() {
		return accessor;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return IncrementalElementsTypes.MODIFIERS_PUBLIC;
	}

	@Override
	public Name getSimpleName() {
		return new IncrementalName(signature.getSimpleName());
	}

	@Override
	public Element getEnclosingElement() {
		return enclosingElement;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitRecordComponent(this, p);
	}

	@Override
	public String getDocComment() {
		//TODO the record component element may not be directly documented? check this
		return signature.getDocComment();
	}
}
