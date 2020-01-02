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
package saker.java.compiler.impl.compile.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerFileWrapperFileObject implements JavaCompilerFileObject {
	protected SakerFile file;

	private transient URI uri;

	public SakerFileWrapperFileObject(SakerFile file) {
		this.file = file;
	}

	protected String getURIProtocol() {
		return CompilationHandler.URI_SCHEME_INPUT;
	}

	@Override
	public URI toUri() {
		if (uri == null) {
			uri = URI.create(getURIProtocol() + ":" + file.getSakerPath().toString().replace(" ", "%20"));
		}
		return uri;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return file.openInputStream();
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream(), StandardCharsets.UTF_8);
	}

	@Override
	public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return file.getContent();
	}

	@Override
	public OutputStream openOutputStream() throws IOException, IllegalStateException {
		throw openedForReading();
	}

	private static IllegalStateException openedForReading() {
		throw new IllegalStateException("File was opened for reading.");
	}

	@Override
	public Writer openWriter() throws IOException {
		throw openedForReading();
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
	public String getName() {
		return getFileObjectSakerPath().toString();
	}

	@Override
	public String toString() {
		return SakerPathFiles.toRelativeString(getFileObjectSakerPath());
	}

	@Override
	public SakerPath getFileObjectSakerPath() {
		return file.getSakerPath();
	}

}
