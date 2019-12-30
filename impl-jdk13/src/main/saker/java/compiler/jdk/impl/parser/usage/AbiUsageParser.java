package saker.java.compiler.jdk.impl.parser.usage;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util13.impl.parser.usage.AbiUsageParser13;

public class AbiUsageParser extends AbiUsageParser13 {

	public AbiUsageParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
