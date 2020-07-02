package saker.java.compiler.util14.impl.model;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.util.Elements;

import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingElementBase;
import saker.java.compiler.impl.compile.handler.invoker.CompilationContextInformation;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.util14.impl.model.elem.IncrementalRecordComponentElement;
import saker.java.compiler.util14.impl.model.forwarded.elem.ForwardingRecordComponentElement;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalElementsTypes14 extends IncrementalElementsTypes9 {

	public IncrementalElementsTypes14(Elements realelements, Object javacsync, ParserCache cache,
			CompilationContextInformation context) {
		super(realelements, javacsync, cache, context);
		setForwardingElementVisitor(new ForwardingElementVisitor14());
	}

	@Override
	public IncrementalElement<?> createRecordComponentElement(IncrementalTypeElement recordtype, FieldSignature m) {
		return new IncrementalRecordComponentElement(this, recordtype, m);
	}

	protected class ForwardingElementVisitor14 extends ForwardingElementVisitor {
		@Override
		public ForwardingElementBase<?> visitRecordComponent(RecordComponentElement t, Void p) {
			return new ForwardingRecordComponentElement(IncrementalElementsTypes14.this, t);
		}
	}
}
