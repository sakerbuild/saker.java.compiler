package saker.java.compiler.util14.impl.model;

import javax.lang.model.util.Elements;

import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.util14.impl.model.elem.IncrementalRecordComponentElement;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalElementsTypes14 extends IncrementalElementsTypes9 {

	public IncrementalElementsTypes14(Elements realelements, Object javacsync, ParserCache cache) {
		super(realelements, javacsync, cache);
	}
	
	@Override
	public IncrementalElement<?> createRecordComponentElement(FieldSignature m) {
		return new IncrementalRecordComponentElement(this, m);
	}

}
