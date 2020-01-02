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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.LineIndexMapBuilder;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class CompilationResultSakerJavaFileObject extends CompilationResultSakerFileObject
		implements JavaCompilerOutputJavaFileObject {

	private Kind kind;
	private String className;

	private transient int[] lineIndexMap;

	public CompilationResultSakerJavaFileObject(String name, Kind kind, String classname) {
		super(name);
		this.kind = kind;
		this.className = classname;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return new FilterOutputStream(super.openOutputStream()) {
			@Override
			public void close() throws IOException {
				super.close();
				CompilationResultSakerJavaFileObject.this.lineIndexMap = StringUtils.getLineIndexMap(bytes.toString());
			}
		};
	}

	@Override
	public Writer openWriter() throws IOException {
		Writer w = super.openWriter();
		return new Writer() {
			private LineIndexMapBuilder indexBuilder = new LineIndexMapBuilder();

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				w.write(cbuf, off, len);
				indexBuilder.append(cbuf, off, off + len);
			}

			@Override
			public void flush() throws IOException {
				w.flush();
			}

			@Override
			public void close() throws IOException {
				w.close();
				CompilationResultSakerJavaFileObject.this.lineIndexMap = indexBuilder.getIndexMap();
			}
		};
	}

	@Override
	public int[] getLineIndexMap() {
		return lineIndexMap;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		if (this.kind != kind) {
			return false;
		}
		int dot = className.lastIndexOf('.');
		return className.substring(dot + 1).equals(simpleName);
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
	public String getClassName() {
		return className;
	}

	@Override
	public String getInferredBinaryName() {
		return className;
	}

	@Override
	public ByteArrayRegion getOutputBytes() {
		return bytes.toByteArrayRegion();
	}
}
