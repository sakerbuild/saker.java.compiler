package saker.java.compiler.jdk.impl.parser.signature;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util9.impl.parser.signature.CompilationUnitSignatureParser9;

public class CompilationUnitSignatureParser extends CompilationUnitSignatureParser9 {

	public CompilationUnitSignatureParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
