package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class SimpleEnumConstantFieldSignature implements FieldSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature type;
	protected String name;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleEnumConstantFieldSignature() {
	}

	public SimpleEnumConstantFieldSignature(TypeSignature type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public final String getSimpleName() {
		return name;
	}

	@Override
	public final Set<Modifier> getModifiers() {
		return IncrementalElementsTypes.MODIFIERS_PUBLIC_STATIC_FINAL;
	}

	@Override
	public final ElementKind getKind() {
		return ElementKind.ENUM_CONSTANT;
	}

	@Override
	public final Collection<? extends AnnotationSignature> getAnnotations() {
		return type.getAnnotations();
	}

	@Override
	public String getDocComment() {
		return null;
	}

	@Override
	public final TypeSignature getTypeSignature() {
		return type;
	}

	@Override
	public ConstantValueResolver getConstantValue() {
		return null;
	}

	@Override
	public boolean isEnumConstant() {
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeObject(type);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
		type = (TypeSignature) in.readObject();
	}

	@Override
	public final int hashCode() {
		return getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleEnumConstantFieldSignature other = (SimpleEnumConstantFieldSignature) obj;
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

}
