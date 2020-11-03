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

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.TypeElement;
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

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.CapturedTypeVariable;
import saker.java.compiler.jdk.impl.compat.type.DefaultedTypeVisitor;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IsSubTypeVisitor implements DefaultedTypeVisitor<Boolean, TypeMirror> {
	protected final IncrementalElementsTypesBase elemTypes;

	public IsSubTypeVisitor(IncrementalElementsTypesBase incrementalElementsTypes) {
		elemTypes = incrementalElementsTypes;
	}

	@Override
	public Boolean visitPrimitive(PrimitiveType t, TypeMirror p) {
		TypeKind pkind = p.getKind();
		TypeKind tkind = t.getKind();
		return pkind == tkind || IncrementalElementsTypes.PRIMITIVE_SUBTYPE_MAP
				.getOrDefault(tkind, Collections.emptySet()).contains(pkind);
	}

	@Override
	public Boolean visitNull(NullType t, TypeMirror p) {
		// §4.10.2: The direct supertypes of the null type are all reference types other than the null type itself.
		return IncrementalElementsTypes.REFERENCE_TYPEKINDS.contains(p.getKind());
	}

	@Override
	public Boolean visitArray(ArrayType t, TypeMirror p) {
		switch (p.getKind()) {
			case ARRAY: {
				ArrayType ap = (ArrayType) p;
				TypeMirror tct = t.getComponentType();
				TypeMirror atct = ap.getComponentType();
				if (tct.getKind().isPrimitive() || atct.getKind().isPrimitive()) {
					return tct == atct;
				}
				return callWithArguments(tct, atct);
			}
			case TYPEVAR: {
				TypeVariable tvp = (TypeVariable) p;
				TypeMirror lower = tvp.getLowerBound();
				if (lower.getKind() != TypeKind.NULL) {
					return elemTypes.isSubtype(t, lower);
				}
				return false;
			}
			case WILDCARD: {
				WildcardType wcp = (WildcardType) p;
				TypeMirror sup = wcp.getSuperBound();
				if (sup != null) {
					return elemTypes.isSubtype(t, sup);
				}
				return false;
			}
			case DECLARED: {
				return elemTypes.isSameType(elemTypes.getJavaLangObjectTypeMirror(), p)
						|| elemTypes.isSameType(elemTypes.getJavaLangCloneableTypeMirror(), p)
						|| elemTypes.isSameType(elemTypes.getJavaIoSerializableTypeMirror(), p);
			}
			default: {
				return false;
			}
		}
	}

	@Override
	public Boolean visitDeclared(DeclaredType t, TypeMirror p) {
		TypeKind pkind = p.getKind();
		switch (pkind) {
			case DECLARED: {
				DeclaredType dtp = (DeclaredType) p;
				TypeElement telem = (TypeElement) t.asElement();
				TypeElement pelem = (TypeElement) dtp.asElement();
				if (telem != pelem && !elemTypes.hasSuperType(telem, pelem)) {
					return false;
				}
				//t has a super type of p
				DeclaredType corrected = elemTypes.getSuperCorrectParameterizedTypeMirror(t, pelem);

				if (!isCompatibleDeclaredTypes(t, corrected, dtp, telem, pelem)) {
					return false;
				}

				return true;
			}
			case WILDCARD: {
				WildcardType wct = (WildcardType) p;
				TypeMirror sup = wct.getSuperBound();
				if (sup != null) {
					return elemTypes.isSubtype(t, sup);
				}
				return false;
			}
			case INTERSECTION: {
				IntersectionType it = (IntersectionType) p;
				//t must be subtype of all of the bounds of p 
				for (TypeMirror b : it.getBounds()) {
					if (!elemTypes.isSubtype(t, b)) {
						return false;
					}
				}
				return true;
			}
			case TYPEVAR: {
				//works only if the type variable is a captured wildcard variable
				TypeVariable tv = (TypeVariable) p;
				TypeParameterElement tpe = (TypeParameterElement) tv.asElement();
				if (tpe == null || tpe.getSimpleName() != IncrementalElementsTypes.getCapturedWildcardName()) {
					return false;
				}
				TypeMirror lower = tv.getLowerBound();
				if (lower.getKind() == TypeKind.NULL) {
					return false;
				}
				return elemTypes.isSubtype(t, lower);
			}
			case UNION: {
				//TODO visitDeclared test and implement UNION type
				return false;
			}

			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				//declared types cannot be assigned to primitives

			case ARRAY:
			case EXECUTABLE:
			case PACKAGE:
			case VOID:
			case NONE:
			case NULL:
			case OTHER:
			case ERROR:
			default: {
				return false;
			}
		}
	}

	@Override
	public Boolean visitError(ErrorType t, TypeMirror p) {
		return false;
	}

	private boolean visitTypeVariableWildcard(TypeVariable t, TypeParameterElement telem, TypeMirror sup) {
		if (sup != null) {
			for (TypeMirror b : telem.getBounds()) {
				if (elemTypes.isSubtype(b, sup)) {
					return true;
				}
			}
			return elemTypes.isSubtype(t, sup);
		}
		return false;
	}

	@Override
	public Boolean visitTypeVariable(TypeVariable t, TypeMirror p) {
		TypeParameterElement telem = (TypeParameterElement) t.asElement();
		if (telem == null) {
			return false;
		}
		switch (p.getKind()) {
			case ARRAY: {
				//XXX how can an array type be assigable to a type variable? or vice versa?
				for (TypeMirror b : telem.getBounds()) {
					if (callWithArguments(b, p)) {
						return true;
					}
				}
				return false;
			}
			case TYPEVAR: {
				TypeVariable ptv = (TypeVariable) p;
				TypeParameterElement pelem = (TypeParameterElement) ptv.asElement();
				if (pelem == null) {
					return false;
				}
				if (telem == pelem) {
					//same
					return true;
				}
				for (TypeMirror b : telem.getBounds()) {
					if (elemTypes.isSubtype(b, p)) {
						return true;
					}
				}
				if (pelem.getSimpleName() == IncrementalElementsTypes.getCapturedWildcardName()) {
					TypeMirror lower = ptv.getLowerBound();
					if (lower.getKind() != TypeKind.NULL) {
						return visitTypeVariableWildcard(t, telem, lower);
					}
				}
				return false;
			}
			case WILDCARD: {
				WildcardType wct = (WildcardType) p;
				TypeMirror sup = wct.getSuperBound();
				return visitTypeVariableWildcard(t, telem, sup);
			}
			case INTERSECTION: {
				IntersectionType it = (IntersectionType) p;
				for (TypeMirror b : it.getBounds()) {
					if (!elemTypes.isSubtype(t, b)) {
						return false;
					}
				}
				return true;
			}
			case DECLARED: {
				for (TypeMirror b : telem.getBounds()) {
					if (callWithArguments(b, p)) {
						return true;
					}
				}
				return false;
			}
			default: {
				return false;
			}
		}
	}

	@Override
	public Boolean visitWildcard(WildcardType t, TypeMirror p) {
		//a wildcard is not a subtype of anything
		//only if t == p (but thats handled before this visitor call)
		return false;
	}

	@Override
	public Boolean visitExecutable(ExecutableType t, TypeMirror p) {
		return false;
	}

	@Override
	public Boolean visitNoType(NoType t, TypeMirror p) {
		if (t.getKind() == TypeKind.PACKAGE) {
			return false;
		}
		return t.getKind() == p.getKind();
	}

	protected boolean callWithArguments(TypeMirror t, TypeMirror p) {
		return elemTypes.isSubtype(t, p);
	}

	@Override
	public Boolean visitIntersection(IntersectionType t, TypeMirror p) {
		switch (p.getKind()) {
			case WILDCARD: {
				//XXX visitIntersection test WILDCARD 
				return false;
			}
			case INTERSECTION: {
				//check if all of the types in p are in t
				IntersectionType pit = (IntersectionType) p;
				for (TypeMirror pbound : pit.getBounds()) {
					if (!elemTypes.isSubtype(t, pbound)) {
						return false;
					}
				}
				return true;
			}
			case TYPEVAR: {
				//filter out captured type variables
				TypeVariable tv = (TypeVariable) p;
				TypeParameterElement tpe = (TypeParameterElement) tv.asElement();
				if (tpe == null || tpe.getSimpleName() == IncrementalElementsTypes.getCapturedWildcardName()) {
					return false;
				}
				for (TypeMirror bound : t.getBounds()) {
					if (callWithArguments(bound, tv)) {
						return true;
					}
				}
				return false;
			}
			default: {
				for (TypeMirror bound : t.getBounds()) {
					if (callWithArguments(bound, p)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	@Override
	public Boolean visitUnion(UnionType t, TypeMirror p) {
		// TODO visitUnion test and implement UNION type
		return false;
	}

	private boolean isWildcardParametersCompatible(TypeMirror t, WildcardType p) {
		//assigning something to wildcard in parameter list
		//for example List<TypeMirror p> to List<? WildcardType p>
		switch (t.getKind()) {
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) t;
				TypeParameterElement tpe = (TypeParameterElement) tv.asElement();
				if (tpe == null || tpe.getSimpleName() != IncrementalElementsTypes.getCapturedWildcardName()) {
					break;
				}
				//if left is a captured wildcard 
				CapturedTypeVariable ctv = (CapturedTypeVariable) tv;
				if (ctv.getWildcardType() == p) {
					return true;
				}
				break;
			}
			default: {
				break;
			}
		}
		TypeMirror sup = p.getSuperBound();
		TypeMirror ext = p.getExtendsBound();

		if (sup != null) {
			//p is ? super
			return elemTypes.isSubtype(sup, t);
		}
		if (ext != null) {
			//p is ? extends
			return elemTypes.isSubtype(t, ext);
		}
		//assigning to ?, works every time
		return true;
	}

	private boolean isWildcardParametersCompatible(WildcardType t, TypeMirror p) {
		//assigning wildcard to something else in parameter list
		//for example List<? WildcardType t> to List<TypeMirror p>
		TypeMirror sup = t.getSuperBound();
		if (sup != null) {
			TypeMirror objectmirror = elemTypes.getJavaLangObjectTypeMirror();
			if (elemTypes.isSameType(objectmirror, sup)) {
				//t is ? super Object
				//this is an extreme case as only one type can satisfy this wildcard
				if (elemTypes.isSubtype(objectmirror, p)) {
					return true;
				}
			}
		}
		//this is a scenario where we are trying to assign for example
		//List<t> to List<p>
		//we test if t is compatible with p
		if (p.getKind() != TypeKind.WILDCARD) {
			if (elemTypes.isWildcardConstrainedToSingleType((CommonWildcardType) t)) {
				return elemTypes.isSameType(t.getSuperBound(), p);
			}
			return false;
		}
		TypeMirror ext = t.getExtendsBound();

		WildcardType wp = (WildcardType) p;
		TypeMirror wpext = wp.getExtendsBound();
		TypeMirror wpsup = wp.getSuperBound();

		if (sup != null) {
			//t is ? super
			if (wpext != null) {
				//can be for example
				//? super Integer can be assigned to ? extends Object
				return elemTypes.isSameType(elemTypes.getJavaLangObjectTypeMirror(), wpext);
			}
			if (wpsup != null) {
				//? super to ? super
				return elemTypes.isSubtype(wpsup, sup);
			}
			//? super to ?
			return true;
		}
		if (ext != null) {
			//t is ? extends
			if (wpext != null) {
				//? extends to ? extends
				return elemTypes.isSubtype(ext, wpext);
			}
			if (wpsup != null) {
				//? extends cannot be assigned to ? super
				return false;
			}
			return true;
		}
		//t is ?
		//? can be assigned to only ?
		if (wpext != null) {
			//? is subtype compatible of (? extends Object)
			return elemTypes.isSameType(elemTypes.getJavaLangObjectTypeMirror(), wpext);
		}
		return wpsup == null;
	}

	protected boolean isRawDeclaredTypesCompatible(boolean leftraw, boolean rightraw) {
		return rightraw;
	}

	protected boolean isDeclaredEnclosingTypesCompatible(TypeMirror leftenclosing, TypeMirror rightenclosing) {
		return elemTypes.isSubtype(leftenclosing, rightenclosing);
	}

	private boolean isCompatibleDeclaredTypes(DeclaredType realt, DeclaredType t, DeclaredType p, TypeElement realtelem,
			TypeElement pelem) {
		TypeMirror tenc = t.getEnclosingType();
		TypeMirror penc = p.getEnclosingType();
		if (tenc.getKind() != penc.getKind()) {
			return false;
		}
		if (!isDeclaredEnclosingTypesCompatible(tenc, penc)) {
			return false;
		}
		// if the real t type is raw, the the assignment can still go through. consider the following:
		// class Super<SuperT extends AbstractMap> {}
		// class Sub<T> extends Super<HashMap> {}
		// 
		// the following is a valid code inside a function of Super:
		// Sub sub = ...;
		// Super<SuperT> supert = ...;
		// supert = sub; //requires casting, but compiles
		boolean traw = realt.getTypeArguments().isEmpty() && !realtelem.getTypeParameters().isEmpty();
		List<? extends TypeMirror> targs = t.getTypeArguments();
		List<? extends TypeMirror> pargs = p.getTypeArguments();
		if (traw || targs.size() != pargs.size()) {
			//different parameter counts
			//can only be subtype, is p is raw, but t isnt
			return isRawDeclaredTypesCompatible(traw || targs.isEmpty(), pargs.isEmpty());
		}
		return ObjectUtils.collectionOrderedEquals(targs, pargs, (l, r) -> {
			if (l.getKind() == TypeKind.WILDCARD) {
				return isWildcardParametersCompatible((WildcardType) l, r);
			}
			if (r.getKind() == TypeKind.WILDCARD) {
				return isWildcardParametersCompatible(l, (WildcardType) r);
			}
			return elemTypes.isSameType(l, r);
		});
	}

}