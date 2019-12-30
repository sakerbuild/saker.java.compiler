package saker.java.compiler.impl.compile.handler.diagnostic;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import saker.build.file.path.SakerPath;
import saker.java.compiler.api.compile.JavaCompilerWarningType;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

public final class CompilerDiagnosticListener implements DiagnosticListener<JavaFileObject> {
	private volatile boolean hadError = false;
	private final Set<DiagnosticEntry> diagnostics = ConcurrentHashMap.newKeySet();

	public CompilerDiagnosticListener() {
	}

	public Set<DiagnosticEntry> getDiagnostics() {
		return diagnostics;
	}

	public void report(DiagnosticEntry entry) {
		if (entry.getKind() == Kind.ERROR) {
			hadError = true;
		}
		diagnostics.add(entry);
	}

	public boolean hadError() {
		return hadError;
	}

	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
		if (diagnostic.getKind() == Kind.ERROR) {
			hadError = true;
		}
		DiagnosticEntry entry = convertToDiagnosticEntry(diagnostic);
		diagnostics.add(entry);
	}

	public static DiagnosticEntry convertToDiagnosticEntry(Diagnostic<? extends JavaFileObject> diagnostic) {
		JavaFileObject source = diagnostic.getSource();
		SakerPath path = CompilationHandler.getFileObjectPath(source);
		return new DiagnosticEntry(diagnostic, path, JavaCompilerWarningType.JavacCompilationWarning);
	}

}