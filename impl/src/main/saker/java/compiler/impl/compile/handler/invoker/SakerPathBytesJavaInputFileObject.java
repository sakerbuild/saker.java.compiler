package saker.java.compiler.impl.compile.handler.invoker;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerPathBytesJavaInputFileObject extends SakerPathBytesInputFileObject
		implements JavaCompilerJavaFileObject {
	private Kind kind;
	private String inferredBinaryName;

	public SakerPathBytesJavaInputFileObject(SakerPathBytes pathBytes, Kind kind, String inferredBinaryName) {
		super(pathBytes);
		this.kind = kind;
		this.inferredBinaryName = inferredBinaryName;
	}

	@Override
	public int[] getLineIndexMap() {
		return StringUtils.getLineIndexMap(pathBytes.getBytes().toString());
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return CompilationHandler.isNameCompatible(simpleName, kind, this.kind, pathBytes.getPath().getFileName());
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
	public String getInferredBinaryName() {
		return inferredBinaryName;
	}

}