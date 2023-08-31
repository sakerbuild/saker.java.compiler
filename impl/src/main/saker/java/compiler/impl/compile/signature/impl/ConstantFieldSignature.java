package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public class ConstantFieldSignature extends SimpleFieldSignature {
	private static final long serialVersionUID = 1L;

	protected ConstantValueResolver constantValue;

	/**
	 * For {@link Externalizable}.
	 */
	public ConstantFieldSignature() {
	}

	public ConstantFieldSignature(Set<Modifier> modifiers, TypeSignature type, String name,
			ConstantValueResolver constantValue) {
		super(modifiers, type, name);
		this.constantValue = constantValue;
	}

	@Override
	public ConstantValueResolver getConstantValue() {
		return constantValue;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(constantValue);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		constantValue = (ConstantValueResolver) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConstantFieldSignature other = (ConstantFieldSignature) obj;
		if (constantValue == null) {
			if (other.constantValue != null)
				return false;
		} else if (!constantValue.equals(other.constantValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		Collection<? extends AnnotationSignature> annots = getAnnotations();
		StringBuilder sb = new StringBuilder();
		if (!ObjectUtils.isNullOrEmpty(annots)) {
			sb.append(String.join(" ", StringUtils.asStringIterable(annots)));
			sb.append(" ");
		}
		sb.append(JavaUtil.modifiersToStringWithSpace(getModifiers()));
		sb.append(type);
		sb.append(" ");
		sb.append(name);
		ConstantValueResolver cv = getConstantValue();
		if (cv != null) {
			sb.append(" = ");
			sb.append(cv);
		}
		return sb.toString();
	}
}
