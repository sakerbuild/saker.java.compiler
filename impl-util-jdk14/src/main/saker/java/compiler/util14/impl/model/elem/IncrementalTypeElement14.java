package saker.java.compiler.util14.impl.model.elem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.RecordComponentElement;

import saker.java.compiler.impl.compat.KindCompatUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;

public class IncrementalTypeElement14 extends IncrementalTypeElement {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement14, List> ARFU_recordComponents = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement14.class, List.class, "recordComponents");
	private volatile transient List<RecordComponentElement> recordComponents;

	public IncrementalTypeElement14(ClassSignature signature, IncrementalElementsTypesBase elemTypes) {
		super(signature, elemTypes);
	}

	@Override
	public void invalidate() {
		super.invalidate();

		recordComponents = null;
	}

	@Override
	public List<? extends RecordComponentElement> getRecordComponents() {
		List<RecordComponentElement> thisrecordcomponents = this.recordComponents;
		if (thisrecordcomponents != null) {
			return thisrecordcomponents;
		}
		if (signature.getKindIndex() != KindCompatUtils.ELEMENTKIND_INDEX_RECORD) {
			thisrecordcomponents = Collections.emptyList();
		} else {
			thisrecordcomponents = new ArrayList<>();
			for (ClassMemberSignature m : signature.getMembers()) {
				if (m.getKindIndex() != KindCompatUtils.ELEMENTKIND_INDEX_RECORD_COMPONENT) {
					continue;
				}
				thisrecordcomponents.add(new IncrementalRecordComponentElement(elemTypes, this, (FieldSignature) m));
			}
		}
		if (ARFU_recordComponents.compareAndSet(this, null, thisrecordcomponents)) {
			return thisrecordcomponents;
		}
		return this.recordComponents;
	}

}
