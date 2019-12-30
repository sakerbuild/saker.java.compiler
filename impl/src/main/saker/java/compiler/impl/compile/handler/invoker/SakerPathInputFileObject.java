package saker.java.compiler.impl.compile.handler.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URI;
import java.util.function.Supplier;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.file.JavaCompilerFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerPathInputFileObject implements JavaCompilerFileObject {
	protected final Supplier<SakerPath> path;
	protected final Supplier<ByteArrayRegion> bytes;

	private transient URI uri;

	@Deprecated
	public SakerPathInputFileObject(IncrementalDirectoryPaths directorypaths, SakerPath path) {
		this.path = Functionals.valSupplier(path);
		this.bytes = LazySupplier.of(() -> {
			try {
				return directorypaths.getFileBytes(path);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public SakerPathInputFileObject(IncrementalDirectoryFile file) {
		this.path = file::getPath;
		this.bytes = LazySupplier.of(() -> {
			try {
				return file.getBytes();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Override
	public URI toUri() {
		if (uri == null) {
			uri = URI.create(
					CompilationHandler.URI_SCHEME_INPUT + ":" + path.get().toString().replace(" ", "%20"));
		}
		return uri;
	}

	@Override
	public String getName() {
		return SakerPathFiles.toRelativeString(path.get());
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new UnsyncByteArrayInputStream(bytes.get());
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
		return bytes.get().toString();
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
		return path.get();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}

}