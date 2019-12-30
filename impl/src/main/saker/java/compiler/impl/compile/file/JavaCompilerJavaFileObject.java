package saker.java.compiler.impl.compile.file;

import javax.tools.JavaFileObject;

public interface JavaCompilerJavaFileObject extends JavaFileObject, JavaCompilerFileObject {
	public String getInferredBinaryName();

	public int[] getLineIndexMap();
}
