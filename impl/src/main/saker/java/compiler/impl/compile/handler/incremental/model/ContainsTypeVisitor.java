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

import javax.lang.model.element.TypeParameterElement;
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
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import saker.java.compiler.jdk.impl.compat.type.DefaultedTypeVisitor;

public class ContainsTypeVisitor implements DefaultedTypeVisitor<Boolean, WildcardType> {
	protected final IncrementalElementsTypesBase elemTypes;

	public ContainsTypeVisitor(IncrementalElementsTypesBase incrementalElementsTypes) {
		elemTypes = incrementalElementsTypes;
	}

	public boolean contains(WildcardType wct, TypeMirror t) {
		return t.accept(this, wct);
	}

	@Override
	public Boolean visitPrimitive(PrimitiveType t, WildcardType p) {
		return p.getExtendsBound() == null && p.getSuperBound() == null;
	}

	@Override
	public Boolean visitNull(NullType t, WildcardType p) {
		return p.getSuperBound() == null;
	}

	@Override
	public Boolean visitArray(ArrayType t, WildcardType p) {
		TypeMirror pext = p.getExtendsBound();
		TypeMirror psup = p.getSuperBound();
		if (pext != null) {
			//? extends X contains
			if (pext.getKind() != TypeKind.ARRAY) {
				if (pext.getKind() == TypeKind.DECLARED) {
					if (elemTypes.isSameType(pext, elemTypes.getJavaIoSerializableTypeMirror())) {
						return true;
					}
					if (elemTypes.isSameType(pext, elemTypes.getJavaLangCloneableTypeMirror())) {
						return true;
					}
					if (elemTypes.isSameType(pext, elemTypes.getJavaLangObjectTypeMirror())) {
						return true;
					}
				}
				return false;
			}
			return elemTypes.isSubtype(t, pext);
		}
		if (psup != null) {
			//? super X contains
			return elemTypes.isSubtype(psup, t);
		}
		//? contains everything
		return true;
	}

	@Override
	public Boolean visitDeclared(DeclaredType t, WildcardType p) {
		TypeMirror pext = p.getExtendsBound();
		TypeMirror psup = p.getSuperBound();
		if (pext != null) {
			//? extends X contains
			return elemTypes.isSubtype(t, pext);
		}
		if (psup != null) {
			//? super X contains
			return elemTypes.isSubtype(psup, t);
		}
		//? contains everything
		return true;
	}

	@Override
	public Boolean visitError(ErrorType t, WildcardType p) {
		return false;
	}

	@Override
	public Boolean visitTypeVariable(TypeVariable t, WildcardType p) {
		TypeMirror pext = p.getExtendsBound();
		TypeMirror psup = p.getSuperBound();
		if (pext != null) {
			//? extends X contains
			return elemTypes.isSubtype(t, pext);
		}
		if (psup != null) {
			//? super X contains
			return elemTypes.isSubtype(psup, t);
		}
		//? contains everything
		return true;
	}

	@Override
	public Boolean visitWildcard(WildcardType t, WildcardType p) {
		//p contains t ?
		TypeMirror pext = p.getExtendsBound();
		TypeMirror psup = p.getSuperBound();

		TypeMirror text = t.getExtendsBound();
		TypeMirror tsup = t.getSuperBound();

		if (pext != null) {
			//? extends X contains
			if (text != null) {
				//contains ? extends Y
				return elemTypes.contains(p, text);
			}
			CommonWildcardType cwt = (CommonWildcardType) t;
			TypeParameterElement tpe = cwt.getCorrespondingTypeParameter();
			if (tpe != null) {
				return elemTypes.contains(p, tpe.asType());
			}
			return false;
		}
		if (psup != null) {
			//? super X contains
			if (text != null) {
				//contains ? extends Y
				return false;
			}
			if (tsup != null) {
				//contains ? super Y
				return elemTypes.isSubtype(psup, tsup);
			}
			//contains ?
			return false;
		}
		//? contains
		//? contains everything
		return true;
	}

	@Override
	public Boolean visitExecutable(ExecutableType t, WildcardType p) {
		return false;
	}

	@Override
	public Boolean visitNoType(NoType t, WildcardType p) {
		return p.getExtendsBound() == null && p.getSuperBound() == null
				&& (t.getKind() == TypeKind.NONE || t.getKind() == TypeKind.VOID);
	}

	@Override
	public Boolean visitIntersection(IntersectionType t, WildcardType p) {
		TypeMirror pext = p.getExtendsBound();
		TypeMirror psup = p.getSuperBound();
		if (pext != null) {
			//? extends X contains
			return elemTypes.isSubtype(t, pext);
		}
		if (psup != null) {
			//? super X contains
			//check if all of the bounds of the intersection are supertype of psup
			for (TypeMirror b : t.getBounds()) {
				if (!elemTypes.isSubtype(psup, b)) {
					return false;
				}
			}
			return true;
		}
		//? contains everything
		return true;
	}

	@Override
	public Boolean visitUnion(UnionType t, WildcardType p) {
		TypeMirror pext = p.getExtendsBound();
		TypeMirror psup = p.getSuperBound();
		if (pext != null) {
			//? extends X contains
			//TODO test and implement UNION type
			return false;
		}
		if (psup != null) {
			//? super X contains
			//TODO test and implement UNION type
			return false;
		}
		//? contains everything
		return true;
	}
}