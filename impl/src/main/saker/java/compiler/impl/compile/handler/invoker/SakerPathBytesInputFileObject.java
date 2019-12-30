package saker.java.compiler.impl.compile.handler.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.java.compiler.impl.compile.file.JavaCompilerFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerPathBytesInputFileObject implements JavaCompilerFileObject {
	protected final SakerPathBytes pathBytes;

	private transient URI uri;

	public SakerPathBytesInputFileObject(SakerPathBytes pathBytes) {
		this.pathBytes = pathBytes;
	}

	@Override
	public URI toUri() {
		if (uri == null) {
			uri = URI.create(
					CompilationHandler.URI_SCHEME_INPUT + ":" + pathBytes.getPath().toString().replace(" ", "%20"));
		}
		return uri;
	}

	@Override
	public String getName() {
		return SakerPathFiles.toRelativeString(pathBytes.getPath());
	}

	@Override
	public InputStream openInputStream() throws IOException {
		ByteArrayRegion bar = pathBytes.getBytes();
		return new UnsyncByteArrayInputStream(bar);
	}

	@Override
	public OutputStream openOutputStream() throws IOException, IllegalStateException {
		throw new IllegalStateException();
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream());
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return pathBytes.getBytes().toString();
	}

	@Override
	public Writer openWriter() throws IOException, IllegalStateException {
		throw new IllegalStateException();
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public boolean delete() {
		return false;
	}

	@Override
	public SakerPath getFileObjectSakerPath() {
		return pathBytes.getPath();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + pathBytes.toString() + "]";
	}
}