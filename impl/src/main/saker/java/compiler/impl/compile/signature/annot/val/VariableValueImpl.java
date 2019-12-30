package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.lang.model.element.Element;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.element.AnnotationSignature.VariableValue;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class VariableValueImpl implements VariableValue, Externalizable {
	private static final long serialVersionUID = 1L;

	private String name;
	private TypeSignature enclosingType;

	public VariableValueImpl() {
	}

	public VariableValueImpl(String name, TypeSignature enclosingType) {
		this.name = name;
		this.enclosingType = enclosingType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeSignature getEnclosingType(SakerElementsTypes elemTypes, Element resolutionelement) {
		return enclosingType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEnclosingType(TypeSignature enclosingType) {
		this.enclosingType = enclosingType;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(name);
		out.writeObject(enclosingType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = (String) in.readObject();
		enclosingType = (TypeSignature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((enclosingType == null) ? 0 : enclosingType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		VariableValueImpl other = (VariableValueImpl) obj;
		if (enclosingType == null) {
			if (other.enclosingType != null)
				return false;
		} else if (!enclosingType.equals(other.enclosingType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return enclosingType + "." + name;
	}

	@Override
	public boolean signatureEquals(VariableValue other) {
		if (!(other instanceof VariableValueImpl)) {
			return false;
		}
		VariableValueImpl vvi = (VariableValueImpl) other;
		if (!name.equals(vvi.name)) {
			return false;
		}
		if (!enclosingType.signatureEquals(vvi.enclosingType)) {
			return false;
		}
		return true;
	}

}
