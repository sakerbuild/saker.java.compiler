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
package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.List;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleIntersectionType extends SimpleTypeMirror implements IntersectionType {
	private List<? extends TypeMirror> bounds;

	public SimpleIntersectionType(IncrementalElementsTypesBase elemTypes, List<? extends TypeMirror> bounds) {
		super(elemTypes);
		this.bounds = bounds;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.INTERSECTION;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitIntersection(this, p);
	}

	@Override
	public List<? extends TypeMirror> getBounds() {
		return bounds;
	}

	@Override
	public String toString() {
		return StringUtils.toStringJoin(" & ", bounds);
	}
}
