package saker.java.compiler.jdk.impl.incremental.model;

import javax.lang.model.util.Elements;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalElementsTypes extends IncrementalElementsTypes9 {
	public IncrementalElementsTypes(Elements realelements, Object javacsync, ParserCache cache) {
		super(realelements, javacsync, cache);
	}
}
