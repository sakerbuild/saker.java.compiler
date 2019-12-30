package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnionTypeSignature;

public class IncrementalUnionType extends IncrementalTypeMirror<UnionTypeSignature> implements UnionType {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalUnionType, List> ARFU_alternatives = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalUnionType.class, List.class, "alternatives");

	private Element enclosingElement;

	private volatile transient List<TypeMirror> alternatives;

	public IncrementalUnionType(IncrementalElementsTypesBase elemTypes, UnionTypeSignature signature,
			Element enclosingElement) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingElement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.alternatives = null;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.UNION;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitUnion(this, p);
	}

	@Override
	public List<? extends TypeMirror> getAlternatives() {
		List<TypeMirror> thisalternatives = this.alternatives;
		if (thisalternatives != null) {
			return thisalternatives;
		}
		List<? extends TypeSignature> alternatives = signature.getAlternatives();
		thisalternatives = JavaTaskUtils.cloneImmutableList(alternatives,
				alt -> elemTypes.getTypeMirror(alt, enclosingElement));
		if (ARFU_alternatives.compareAndSet(this, null, thisalternatives)) {
			return thisalternatives;
		}
		return this.alternatives;
	}

}
