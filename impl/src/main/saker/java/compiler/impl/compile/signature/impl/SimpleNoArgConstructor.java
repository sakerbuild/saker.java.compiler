package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public final class SimpleNoArgConstructor implements MethodSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected short modifierFlags;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleNoArgConstructor() {
	}

	public SimpleNoArgConstructor(Set<Modifier> modifiers) {
		this.modifierFlags = ImmutableModifierSet.getFlag(modifiers);
	}

	@Override
	public String getSimpleName() {
		return IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.forFlags(modifierFlags);
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.CONSTRUCTOR;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public String getDocComment() {
		return null;
	}

	@Override
	public List<? extends TypeParameterTypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getReturnType() {
		return NoTypeSignatureImpl.getVoid();
	}

	@Override
	public List<? extends MethodParameterSignature> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends TypeSignature> getThrowingTypes() {
		return Collections.emptyList();
	}

	@Override
	public Value getDefaultValue() {
		return null;
	}

	@Override
	public TypeSignature getReceiverParameter() {
		return null;
	}

	@Override
	public boolean isVarArg() {
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
	}

	@Override
	public int hashCode() {
		return modifierFlags;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleNoArgConstructor other = (SimpleNoArgConstructor) obj;
		if (modifierFlags != other.modifierFlags)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.modifiersToStringWithSpace(getModifiers()) + IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME
				+ "()";
	}

}
