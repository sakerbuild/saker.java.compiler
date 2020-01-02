/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.file.JavaCompilerFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class IncrementalDirectoryFileInputFileObject implements JavaCompilerFileObject {
	protected final IncrementalDirectoryFile file;
	protected final Supplier<ByteArrayRegion> bytes;

	private transient URI uri;

	public IncrementalDirectoryFileInputFileObject(IncrementalDirectoryFile file) {
		this.file = file;
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
			uri = URI.create(CompilationHandler.URI_SCHEME_INPUT + ":"
					+ getFileObjectSakerPath().toString().replace(" ", "%20"));
		}
		return uri;
	}

	@Override
	public String getName() {
		return SakerPathFiles.toRelativeString(getFileObjectSakerPath());
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
		return file.getPath();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getFileObjectSakerPath() + "]";
	}

}