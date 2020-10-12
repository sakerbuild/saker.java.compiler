package saker.java.compiler.util15.impl.model;

import javax.lang.model.util.Elements;

import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.compile.handler.invoker.CompilationContextInformation;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.util14.impl.model.IncrementalElementsTypes14;
import saker.java.compiler.util15.impl.model.elem.IncrementalTypeElement15;

public class IncrementalElementsTypes15 extends IncrementalElementsTypes14 {

	public IncrementalElementsTypes15(Elements realelements, Object javacsync, ParserCache cache,
			CompilationContextInformation context) {
		super(realelements, javacsync, cache, context);
	}

	@Override
	public IncrementalTypeElement createIncrementalTypeElement(ClassSignature sig) {
		return new IncrementalTypeElement15(sig, this);
	}

}
