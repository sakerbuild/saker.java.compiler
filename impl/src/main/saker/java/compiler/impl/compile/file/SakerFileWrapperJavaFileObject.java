package saker.java.compiler.impl.compile.file;

import java.io.IOException;
import java.io.Reader;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.file.SakerFile;
import saker.build.thirdparty.saker.util.LineIndexMapBuilder;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerFileWrapperJavaFileObject extends SakerFileWrapperFileObject implements JavaCompilerJavaFileObject {

	private Kind kind;
	private String inferredBinaryName;

	private transient int[] lineIndexMap;

	public SakerFileWrapperJavaFileObject(SakerFile file, Kind kind, String inferredBinaryName) {
		super(file);
		this.kind = kind;
		this.inferredBinaryName = inferredBinaryName;
	}

	@Override
	public int[] getLineIndexMap() {
		return lineIndexMap;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		Reader r = super.openReader(ignoreEncodingErrors);
		return new Reader() {
			private LineIndexMapBuilder indexBuilder = new LineIndexMapBuilder();

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				int result = r.read(cbuf, off, len);
				if (result > 0) {
					indexBuilder.append(cbuf, off, off + result);
				}
				return result;
			}

			@Override
			public void close() throws IOException {
				r.close();
				SakerFileWrapperJavaFileObject.this.lineIndexMap = indexBuilder.getIndexMap();
			}
		};
	}
	//XXX maybe build index map in openInputStream?

	@Override
	public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
		String result = super.getCharContent(ignoreEncodingErrors);
		this.lineIndexMap = StringUtils.getLineIndexMap(result);
		return result;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return CompilationHandler.isNameCompatible(simpleName, kind, this.kind, file.getName());
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
		return inferredBinaryName == null ? toString() : inferredBinaryName;
	}

}
