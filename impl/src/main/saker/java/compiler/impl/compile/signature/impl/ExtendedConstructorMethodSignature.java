package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class ExtendedConstructorMethodSignature extends SimpleConstructorMethodSignature {
	private static final long serialVersionUID = 1L;

	protected List<? extends TypeParameterTypeSignature> typeParameters;
	protected List<? extends TypeSignature> throwsTypes;

	/**
	 * For {@link Externalizable}.
	 */
	public ExtendedConstructorMethodSignature() {
	}

	public ExtendedConstructorMethodSignature(Set<Modifier> modifiers,
			List<? extends MethodParameterSignature> parameters,
			List<? extends TypeParameterTypeSignature> typeParameters, List<? extends TypeSignature> throwsTypes) {
		super(modifiers, parameters);
		this.typeParameters = typeParameters;
		this.throwsTypes = throwsTypes;
	}

	@Override
	public List<? extends TypeSignature> getThrowingTypes() {
		return throwsTypes;
	}

	@Override
	public List<? extends TypeParameterTypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		SerialUtils.writeExternalCollection(out, typeParameters);
		SerialUtils.writeExternalCollection(out, throwsTypes);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		typeParameters = SerialUtils.readExternalImmutableList(in);
		throwsTypes = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedConstructorMethodSignature other = (ExtendedConstructorMethodSignature) obj;
		if (throwsTypes == null) {
			if (other.throwsTypes != null)
				return false;
		} else if (!throwsTypes.equals(other.throwsTypes))
			return false;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}
}
