package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class RecordComponentSignatureImpl extends FieldSignatureBase implements Externalizable {
	private static final long serialVersionUID = 1L;

	protected short modifierFlags;
	protected TypeSignature type;
	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public RecordComponentSignatureImpl() {
	}

	public RecordComponentSignatureImpl(Set<Modifier> modifiers, TypeSignature type, String name, String docComment) {
		super(name);
		this.modifierFlags = ImmutableModifierSet.getFlag(modifiers);
		this.type = type;
		this.docComment = docComment;
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
	public final List<? extends AnnotationSignature> getAnnotations() {
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
	public boolean isEnumConstant() {
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
		out.writeObject(type);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
		type = (TypeSignature) in.readObject();
		docComment = (String) in.readObject();
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
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
