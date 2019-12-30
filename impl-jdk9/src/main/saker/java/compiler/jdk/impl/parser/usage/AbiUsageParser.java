package saker.java.compiler.jdk.impl.parser.usage;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util9.impl.parser.usage.AbiUsageParser9;

public class AbiUsageParser extends AbiUsageParser9 {

	public AbiUsageParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
