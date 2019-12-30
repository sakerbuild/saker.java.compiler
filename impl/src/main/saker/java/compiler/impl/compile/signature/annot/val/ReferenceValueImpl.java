package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.lang.model.element.Element;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.compile.signature.value.EnumOrConstantValueResolver;
import saker.java.compiler.impl.signature.element.AnnotationSignature.LiteralValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.AnnotationSignature.VariableValue;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class ReferenceValueImpl implements VariableValue, LiteralValue, Externalizable {
	private static final long serialVersionUID = 1L;

	private EnumOrConstantValueResolver resolver;

	public ReferenceValueImpl() {
	}

	public ReferenceValueImpl(EnumOrConstantValueResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public ConstantValueResolver getValue() {
		return resolver;
	}

	@Override
	public String getName() {
		return resolver.getIdentifier();
	}

	@Override
	public TypeSignature getEnclosingType(SakerElementsTypes elemTypes, Element resolutionelement) {
		return resolver.getType((IncrementalElementsTypes) elemTypes, resolutionelement);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(resolver);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		resolver = (EnumOrConstantValueResolver) in.readObject();
	}

	@Override
	public String toString() {
		return resolver.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resolver == null) ? 0 : resolver.hashCode());
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
		ReferenceValueImpl other = (ReferenceValueImpl) obj;
		if (resolver == null) {
			if (other.resolver != null)
				return false;
		} else if (!resolver.equals(other.resolver))
			return false;
		return true;
	}

	@Override
	public boolean signatureEquals(Value other) {
		if (!(other instanceof ReferenceValueImpl)) {
			return false;
		}
		return signatureEquals((VariableValue) other);
	}

	@Override
	public boolean signatureEquals(VariableValue other) {
		if (!(other instanceof ReferenceValueImpl)) {
			return false;
		}
		ReferenceValueImpl rvi = (ReferenceValueImpl) other;
		if (!resolver.signatureEquals(rvi.resolver)) {
			return false;
		}
		return true;
	}

}
