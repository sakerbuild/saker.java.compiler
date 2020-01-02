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