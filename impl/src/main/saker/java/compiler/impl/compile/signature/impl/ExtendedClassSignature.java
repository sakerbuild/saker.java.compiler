package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class ExtendedClassSignature extends SimpleClassSignature {
	private static final long serialVersionUID = 1L;

	protected TypeSignature superClass;
	protected List<? extends TypeSignature> superInterfaces;

	/**
	 * For {@link Externalizable}.
	 */
	public ExtendedClassSignature() {
	}

	public ExtendedClassSignature(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members, ClassSignature enclosingClass, ElementKind kind,
			List<? extends TypeSignature> superInterfaces, TypeSignature superClass) {
		super(modifiers, packageName, name, members, enclosingClass, kind);
		this.superInterfaces = superInterfaces;
		this.superClass = superClass;
	}

	@Override
	public final TypeSignature getSuperClass() {
		return superClass;
	}

	@Override
	public final List<? extends TypeSignature> getSuperInterfaces() {
		return superInterfaces;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(superClass);
		SerialUtils.writeExternalCollection(out, superInterfaces);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		superClass = (TypeSignature) in.readObject();
		superInterfaces = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedClassSignature other = (ExtendedClassSignature) obj;
		if (superClass == null) {
			if (other.superClass != null)
				return false;
		} else if (!superClass.equals(other.superClass))
			return false;
		if (superInterfaces == null) {
			if (other.superInterfaces != null)
				return false;
		} else if (!superInterfaces.equals(other.superInterfaces))
			return false;
		return true;
	}

}
