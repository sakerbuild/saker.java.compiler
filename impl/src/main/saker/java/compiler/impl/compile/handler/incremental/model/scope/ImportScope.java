package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.util.NavigableSet;

public interface ImportScope {
	public String getPackageName();

	public NavigableSet<? extends ImportDeclaration> getImportDeclarations();
}
