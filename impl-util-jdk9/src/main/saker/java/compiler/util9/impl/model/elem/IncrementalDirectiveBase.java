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
package saker.java.compiler.util9.impl.model.elem;

import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignature;
import saker.java.compiler.util9.impl.Java9LanguageUtils;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public abstract class IncrementalDirectiveBase<Sig extends DirectiveSignature>
		implements Directive, IncrementallyModelled {
	protected IncrementalElementsTypes9 elemTypes;
	protected IncrementalModuleElement module;
	protected Sig signature;
	private DirectiveKind kind;

	public IncrementalDirectiveBase(IncrementalElementsTypes9 elemTypes, Sig signature,
			IncrementalModuleElement module) {
		this.elemTypes = elemTypes;
		this.signature = signature;
		this.module = module;
		this.kind = Java9LanguageUtils.toDirectiveKind(signature.getKind());
	}

	@Override
	public DirectiveKind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return signature.toString();
	}
}
