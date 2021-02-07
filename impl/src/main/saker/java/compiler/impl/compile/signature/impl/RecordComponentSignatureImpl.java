package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class RecordComponentSignatureImpl implements FieldSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected short modifierFlags;
	protected TypeSignature type;
	protected String name;
	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public RecordComponentSignatureImpl() {
	}

	public RecordComponentSignatureImpl(Set<Modifier> modifiers, TypeSignature type, String name, String docComment) {
		this.modifierFlags = ImmutableModifierSet.getFlag(modifiers);
		this.type = type;
		this.name = name;
		this.docComment = docComment;
	}

	@Override
	public String getSimpleName() {
		return name;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.forFlags(modifierFlags);
	}

	@Override
	public ElementKind getKind() {
		return ElementKindCompatUtils.getElementKind(getKindIndex());
	}

	@Override
	public byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_RECORD_COMPONENT;
	}

	@Override
	public final Collection<? extends AnnotationSignature> getAnnotations() {
		return type.getAnnotations();
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public TypeSignature getTypeSignature() {
		return type;
	}

	@Override
	public ConstantValueResolver getConstantValue() {
		return null;
	}

	@Override
	public boolean isEnumConstant() {
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
		out.writeObject(type);
		out.writeUTF(name);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
		type = (TypeSignature) in.readObject();
		name = in.readUTF();
		docComment = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docComment == null) ? 0 : docComment.hashCode());
		result = prime * result + modifierFlags;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordComponentSignatureImpl other = (RecordComponentSignatureImpl) obj;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		if (modifierFlags != other.modifierFlags)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.modifiersToStringWithSpace(getModifiers()) + type + " " + name;
	}
}
