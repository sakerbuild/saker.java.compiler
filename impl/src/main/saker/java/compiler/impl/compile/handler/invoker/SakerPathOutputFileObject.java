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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.java.compiler.impl.compile.file.JavaCompilerFileObject;
import saker.java.compiler.impl.compile.file.OutputFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerPathOutputFileObject implements JavaCompilerFileObject, OutputFileObject {
	private SakerPath path;
	protected final UnsyncByteArrayOutputStream bytes = new UnsyncByteArrayOutputStream();

	private transient URI uri;

	public SakerPathOutputFileObject() {
	}

	public SakerPathOutputFileObject(SakerPath path) {
		this.path = path;
	}

	public final void setPath(SakerPath path) {
		this.path = path;
	}

	@Override
	public URI toUri() {
		if (uri == null) {
			uri = URI.create(CompilationHandler.URI_SCHEME_GENERATED + ":" + path.toString().replace(" ", "%20"));
		}
		return uri;
	}

	@Override
	public String getName() {
		return SakerPathFiles.toRelativeString(path);
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new UnsyncByteArrayInputStream(bytes.getBuffer(), 0, bytes.size());
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		bytes.reset();
		return bytes;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream());
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return bytes.toString();
	}

	@Override
	public Writer openWriter() throws IOException {
		return new OutputStreamWriter(openOutputStream(), StandardCharsets.UTF_8);
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
		return path;
	}

	@Override
	public ByteArrayRegion getOutputBytes() {
		return bytes.toByteArrayRegion();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}

}