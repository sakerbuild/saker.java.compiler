package saker.java.compiler.impl.compile.handler.invoker;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerPathJavaInputFileObject extends SakerPathInputFileObject implements JavaCompilerJavaFileObject {
	private Kind kind;
	private String inferredBinaryName;

	@Deprecated
	public SakerPathJavaInputFileObject(IncrementalDirectoryPaths directorypaths, SakerPath path, Kind kind,
			String inferredBinaryName) {
		super(directorypaths, path);
		this.kind = kind;
		this.inferredBinaryName = inferredBinaryName;
	}

	public SakerPathJavaInputFileObject(IncrementalDirectoryFile file, Kind kind, String inferredBinaryName) {
		super(file);
		this.kind = kind;
		this.inferredBinaryName = inferredBinaryName;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return CompilationHandler.isNameCompatible(simpleName, kind, this.kind, path.get().getFileName());
	}

	@Override
	public NestingKind getNestingKind() {
		return null;
	}

	@Override
	public Modifier getAccessLevel() {
		return null;
	}

	@Override
	public int[] getLineIndexMap() {
		return StringUtils.getLineIndexMap(bytes.get().toString());
	}

	@Override
	public String getInferredBinaryName() {
		return inferredBinaryName;
	}

}