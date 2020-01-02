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
package saker.java.compiler.impl.signature.type;

public interface TypeSignatureVisitor<R, P> {
	public R visitArray(ArrayTypeSignature type, P p);

	public R visitWildcard(WildcardTypeSignature type, P p);

	public R visitTypeVariable(TypeVariableTypeSignature type, P p);

	public R visitIntersection(IntersectionTypeSignature type, P p);

	public R visitNoType(NoTypeSignature type, P p);

	public R visitPrimitive(PrimitiveTypeSignature type, P p);

	public R visitUnion(UnionTypeSignature type, P p);

	public R visitUnknown(UnknownTypeSignature type, P p);

	public R visitUnresolved(UnresolvedTypeSignature type, P p);

	public R visitParameterized(ParameterizedTypeSignature type, P p);

	public R visitEncloser(ParameterizedTypeSignature type, P p);

	public R visitNull(NullTypeSignature type, P p);
}
