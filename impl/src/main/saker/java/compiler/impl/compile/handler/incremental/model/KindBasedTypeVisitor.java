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
package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.EnumMap;
import java.util.Map;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import saker.build.thirdparty.saker.util.function.TriFunction;
import saker.java.compiler.jdk.impl.incremental.model.JavaModelUtils;

public class KindBasedTypeVisitor {
	@SuppressWarnings("rawtypes")
	private static final Map<TypeKind, TriFunction<TypeVisitor, ? extends TypeMirror, Object, Object>> KIND_CALL_MAPS = getKindBasedTypeVisitorMap();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<TypeKind, TriFunction<TypeVisitor, ? extends TypeMirror, Object, Object>> getKindBasedTypeVisitorMap() {
		EnumMap<TypeKind, TriFunction<TypeVisitor, ? extends TypeMirror, Object, Object>> result = new EnumMap<>(
				TypeKind.class);
		result.put(TypeKind.PACKAGE, (TriFunction<TypeVisitor, NoType, Object, Object>) TypeVisitor::visitNoType);
		result.put(TypeKind.VOID, (TriFunction<TypeVisitor, NoType, Object, Object>) TypeVisitor::visitNoType);
		result.put(TypeKind.NONE, (TriFunction<TypeVisitor, NoType, Object, Object>) TypeVisitor::visitNoType);

		result.put(TypeKind.BOOLEAN,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.CHAR,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.BYTE,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.SHORT,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.INT, (TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.LONG,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.FLOAT,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);
		result.put(TypeKind.DOUBLE,
				(TriFunction<TypeVisitor, PrimitiveType, Object, Object>) TypeVisitor::visitPrimitive);

		result.put(TypeKind.NULL, (TriFunction<TypeVisitor, NullType, Object, Object>) TypeVisitor::visitNull);

		result.put(TypeKind.ARRAY, (TriFunction<TypeVisitor, ArrayType, Object, Object>) TypeVisitor::visitArray);

		result.put(TypeKind.DECLARED,
				(TriFunction<TypeVisitor, DeclaredType, Object, Object>) TypeVisitor::visitDeclared);

		result.put(TypeKind.ERROR, (TriFunction<TypeVisitor, ErrorType, Object, Object>) TypeVisitor::visitError);

		result.put(TypeKind.TYPEVAR,
				(TriFunction<TypeVisitor, TypeVariable, Object, Object>) TypeVisitor::visitTypeVariable);

		result.put(TypeKind.WILDCARD,
				(TriFunction<TypeVisitor, WildcardType, Object, Object>) TypeVisitor::visitWildcard);

		result.put(TypeKind.EXECUTABLE,
				(TriFunction<TypeVisitor, ExecutableType, Object, Object>) TypeVisitor::visitExecutable);

		result.put(TypeKind.UNION, (TriFunction<TypeVisitor, UnionType, Object, Object>) TypeVisitor::visitUnion);

		result.put(TypeKind.INTERSECTION,
				(TriFunction<TypeVisitor, IntersectionType, Object, Object>) TypeVisitor::visitIntersection);

		result.put(TypeKind.OTHER, (TriFunction<TypeVisitor, TypeMirror, Object, Object>) TypeVisitor::visitUnknown);

		JavaModelUtils.addJava9KindBasedTypeVisitorFunctions(result);

		return result;
	}

	private KindBasedTypeVisitor() {
		throw new UnsupportedOperationException();
	}

	public static <R, P> R visit(TypeMirror type, TypeVisitor<R, P> visitor, P p) {
		return visit(type.getKind(), type, visitor, p);
	}

	@SuppressWarnings("unchecked")
	public static <R, P> R visit(TypeKind kind, TypeMirror type, TypeVisitor<R, P> visitor, P p) {
		@SuppressWarnings("rawtypes")
		TriFunction got = KIND_CALL_MAPS.get(kind);
		if (got == null) {
			return visitor.visitUnknown(type, p);
		}
		return (R) got.apply(visitor, type, p);
	}

}
