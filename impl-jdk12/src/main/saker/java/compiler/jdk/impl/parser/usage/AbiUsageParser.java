package saker.java.compiler.jdk.impl.parser.usage;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util12.impl.parser.usage.AbiUsageParser12;

public class AbiUsageParser extends AbiUsageParser12 {

	public AbiUsageParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
