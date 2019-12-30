package saker.java.compiler.jdk.impl.parser.usage;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util8.impl.parser.usage.AbiUsageParser8;

public class AbiUsageParser extends AbiUsageParser8 {

	public AbiUsageParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
