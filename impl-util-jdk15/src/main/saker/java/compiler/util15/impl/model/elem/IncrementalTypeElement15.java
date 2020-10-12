package saker.java.compiler.util15.impl.model.elem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ClassSignature.PermittedSubclassesList;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.util14.impl.model.elem.IncrementalTypeElement14;

public class IncrementalTypeElement15 extends IncrementalTypeElement14 {

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement15, List> ARFU_permittedSubclasses = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement15.class, List.class, "permittedSubclasses");
	private volatile transient List<TypeMirror> permittedSubclasses;

	public IncrementalTypeElement15(ClassSignature signature, IncrementalElementsTypesBase elemTypes) {
		super(signature, elemTypes);
	}

	@Override
	public void invalidate() {
		super.invalidate();

		this.permittedSubclasses = null;
	}

	@Override
	public List<? extends TypeMirror> getPermittedSubclasses() {
		List<TypeMirror> thispermittedsubclasses = this.permittedSubclasses;
		if (thispermittedsubclasses != null) {
			return thispermittedsubclasses;
		}
		PermittedSubclassesList psc = signature.getPermittedSubclasses();
		if (psc == null) {
			thispermittedsubclasses = Collections.emptyList();
		} else {
			PermittedSubclassResolver resolver = new PermittedSubclassResolver();
			psc.accept(resolver);
			thispermittedsubclasses = resolver.subclasses;
		}
		if (ARFU_permittedSubclasses.compareAndSet(this, null, thispermittedsubclasses)) {
			return thispermittedsubclasses;
		}
		return this.permittedSubclasses;
	}

	private final class PermittedSubclassResolver implements PermittedSubclassesList.Visitor {
		protected List<TypeMirror> subclasses;

		@Override
		public void visitUnspecified() {
			subclasses = elemTypes.resolveUnspecifiedPermitSubclasses(IncrementalTypeElement15.this);
		}

		@Override
		public void visitExplicit(List<? extends TypeSignature> types) {
			subclasses = JavaTaskUtils.cloneImmutableList(types, ts -> {
				//use asType as the <T> parameterization needs to be kept.
				return elemTypes.getTypeElement(ts, IncrementalTypeElement15.this).asType();
			});
		}
	}
}
