package saker.java.compiler.jdk.impl.parser.signature;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util8.impl.parser.signature.CompilationUnitSignatureParser8;

public class CompilationUnitSignatureParser extends CompilationUnitSignatureParser8 {

	public CompilationUnitSignatureParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
