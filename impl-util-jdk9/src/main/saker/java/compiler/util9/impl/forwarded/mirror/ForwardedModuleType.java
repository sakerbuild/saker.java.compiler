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
package saker.java.compiler.util9.impl.forwarded.mirror;

import javax.lang.model.element.ModuleElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingTypeMirrorBase;
import saker.java.compiler.util9.impl.model.mirror.CommonModuleType;

public class ForwardedModuleType extends ForwardingTypeMirrorBase<CommonModuleType> implements CommonModuleType {

	public ForwardedModuleType(IncrementalElementsTypesBase elemTypes, CommonModuleType subject) {
		super(elemTypes, subject);
	}

	@Override
	public ModuleElement getModuleElement() {
		return subject.getModuleElement();
	}

}
