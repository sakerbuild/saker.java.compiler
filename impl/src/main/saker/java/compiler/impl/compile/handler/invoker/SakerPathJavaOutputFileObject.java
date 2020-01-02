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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.LineIndexMapBuilder;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.java.compiler.impl.compile.file.JavaCompilerOutputJavaFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class SakerPathJavaOutputFileObject extends SakerPathOutputFileObject
		implements JavaCompilerOutputJavaFileObject {
	private Kind kind;
	private String className;

	private transient int[] lineIndexMap;

	public SakerPathJavaOutputFileObject(Kind kind, String className) {
		this.kind = kind;
		this.className = className;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return CompilationHandler.isNameCompatible(simpleName, kind, this.kind, getFileObjectSakerPath().getFileName());
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
	public OutputStream openOutputStream() throws IOException {
		return new FilterOutputStream(super.openOutputStream()) {
			@Override
			public void close() throws IOException {
				super.close();
				SakerPathJavaOutputFileObject.this.lineIndexMap = StringUtils.getLineIndexMap(bytes.toString());
			}
		};
	}

	@Override
	public Writer openWriter() throws IOException {
		return new OutputStreamWriter(super.openOutputStream(), StandardCharsets.UTF_8) {
			private LineIndexMapBuilder indexBuilder = new LineIndexMapBuilder();

			@Override
			public void write(int c) throws IOException {
				super.write(c);
				indexBuilder.append((char) c);
			}

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				super.write(cbuf, off, len);
				indexBuilder.append(cbuf, off, off + len);
			}

			@Override
			public void write(String str, int off, int len) throws IOException {
				super.write(str, off, len);
				indexBuilder.append(str, off, off + len);
			}

			@Override
			public void close() throws IOException {
				super.close();
				SakerPathJavaOutputFileObject.this.lineIndexMap = indexBuilder.getIndexMap();
			}
		};
	}

	@Override
	public int[] getLineIndexMap() {
		return lineIndexMap;
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
	public String getInferredBinaryName() {
		return className;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public ByteArrayRegion getOutputBytes() {
		return bytes.toByteArrayRegion();
	}

}