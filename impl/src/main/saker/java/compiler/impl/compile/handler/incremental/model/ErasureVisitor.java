package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.List;

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

import saker.java.compiler.impl.compile.handler.incremental.model.mirror.ErasedDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.ErasedExecutableType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleArrayType;
import saker.java.compiler.jdk.impl.compat.type.DefaultedTypeVisitor;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

/**
 * https://github.com/fschopp/java-types Visitor of a type mirror. Returns the erasure of the visited type mirror.
 */
public final class ErasureVisitor implements DefaultedTypeVisitor<TypeMirror, Void> {
	private final IncrementalElementsTypesBase elemTypes;

	public ErasureVisitor(IncrementalElementsTypesBase incrementalElementsTypes) {
		elemTypes = incrementalElementsTypes;
	}

	public TypeMirror erasure(TypeMirror t) {
		return t.accept(this, null);
	}

	@Override
	public DeclaredType visitDeclared(DeclaredType t, Void ignored) {
		if (IncrementalElementsTypes.hasAnyTypeArguments(t)) {
			return new ErasedDeclaredType(elemTypes, t);
		}
		//needs no erasure
		return t;
	}

	/**
	 * Returns the array type corresponding to the erasure of the component type.
	 */
	@Override
	public TypeMirror visitArray(ArrayType arrayType, Void ignored) {
		TypeMirror component = arrayType.getComponentType();
		TypeMirror erased = elemTypes.erasure(component);
		if (erased == component) {
			//no erasure needed
			return arrayType;
		}
		return SimpleArrayType.erasured(elemTypes, erased);
	}

	/**
	 * Returns the erasure of the leftmost bound of the given type variable.
	 * <p>
	 * The erasure of a type variable is the erasure of its leftmost bound (JLS §4.6). If multiple bounds are present,
	 * the upper bound is modelled as an intersection type. The erasure of an intersection type is guaranteed to have
	 * see right form (see {@link #visitIntersection(IntersectionType, Void)}).
	 */
	@Override
	public TypeMirror visitTypeVariable(TypeVariable typeVariable, Void ignored) {
		return elemTypes.erasure(typeVariable.getUpperBound());
	}

	/**
	 * Returns the erasure of the leftmost member of the given intersection type.
	 * <p>
	 * While JLS §4.6 does not mention intersection types (and thus, strictly speaking, the erasure of an intersection
	 * type should be the unmodified intersection type itself), this implementation computes the erasure of an
	 * intersection type as the erasure of its left-most type.
	 */
	@Override
	public TypeMirror visitIntersection(IntersectionType intersectionType, Void ignored) {
		//XXX if the leftmost bound is a type variable, javac returns java.lang.Object dont know why
		TypeMirror leftmost = intersectionType.getBounds().get(0);
		if (leftmost.getKind() == TypeKind.TYPEVAR) {
			return elemTypes.getJavaLangObjectTypeMirror();
		}
		return elemTypes.erasure(leftmost);
	}

	@Override
	public TypeMirror visitExecutable(ExecutableType t, Void p) {
		if (IncrementalElementsTypes.needsErasure(t)) {
			return new ErasedExecutableType(elemTypes, t);
		}
		return t;
	}

	@Override
	public TypeMirror visitWildcard(WildcardType t, Void p) {
		TypeMirror extendsbound = t.getExtendsBound();
		if (extendsbound != null) {
			return elemTypes.erasure(extendsbound);
		}

		TypeParameterElement corresponding = ((CommonWildcardType) t).getCorrespondingTypeParameter();
		if (corresponding != null) {
			List<? extends TypeMirror> bounds = corresponding.getBounds();
			if (!bounds.isEmpty()) {
				//the corresponding element has an upper bound
				//return the leftmost upper bound
				return elemTypes.erasure(bounds.get(0));
			}
		}
		return elemTypes.getJavaLangObjectTypeMirror();
	}

	@Override
	public TypeMirror visitPrimitive(PrimitiveType t, Void p) {
		return t;
	}

	@Override
	public TypeMirror visitNull(NullType t, Void p) {
		return t;
	}

	@Override
	public TypeMirror visitError(ErrorType t, Void p) {
		return t;
	}

	@Override
	public TypeMirror visitNoType(NoType t, Void p) {
		return t;
	}

	@Override
	public TypeMirror visitUnion(UnionType t, Void p) {
		return t;
	}

	@Override
	public TypeMirror visitUnknown(TypeMirror t, Void p) {
		return t;
	}
}