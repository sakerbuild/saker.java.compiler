package saker.java.compiler.impl.compile.handler.diagnostic;

import saker.build.file.path.SakerPath;

public interface DiagnosticLocationReference {
	public SakerPath getPath();

	public DiagnosticLocation getLocation(DiagnosticPositionTable table);

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
