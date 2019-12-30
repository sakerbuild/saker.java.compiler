package saker.java.compiler.impl.compile.handler.diagnostic;

import saker.build.file.path.SakerPath;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;

public interface DiagnosticPositionTable {
	public DiagnosticLocation getForPathSignature(SakerPath path, SignaturePath signature);
}
