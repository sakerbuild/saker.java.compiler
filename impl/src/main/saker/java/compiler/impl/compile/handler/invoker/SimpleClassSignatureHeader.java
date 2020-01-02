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

import javax.lang.model.element.NestingKind;

import saker.java.compiler.impl.signature.element.ClassSignatureHeader;

class SimpleClassSignatureHeader implements ClassSignatureHeader {
	private ClassSignatureHeader enclosingSignature;
	private String simpleName;
	private String binaryName;
	private NestingKind nestingKind;

	public SimpleClassSignatureHeader(ClassSignatureHeader enclosingSignature, String simpleName, String binaryName,
			NestingKind nestingKind) {
		this.enclosingSignature = enclosingSignature;
		this.simpleName = simpleName;
		this.binaryName = binaryName;
		this.nestingKind = nestingKind;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public String getBinaryName() {
		return binaryName;
	}

	@Override
	public NestingKind getNestingKind() {
		return nestingKind;
	}

	@Override
	public ClassSignatureHeader getEnclosingSignature() {
		return enclosingSignature;
	}

	@Override
	public String toString() {
		return binaryName;
	}

}