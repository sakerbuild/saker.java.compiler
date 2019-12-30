package saker.java.compiler.jdk.impl.incremental.model;

import javax.lang.model.util.Elements;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util8.impl.model.IncrementalElementsTypes8;

public class IncrementalElementsTypes extends IncrementalElementsTypes8 {

	public IncrementalElementsTypes(Elements realelements, Object javacsync, ParserCache cache) {
		super(realelements, javacsync, cache);
	}
}