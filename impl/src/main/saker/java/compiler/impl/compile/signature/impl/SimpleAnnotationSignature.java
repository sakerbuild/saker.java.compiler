package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Map;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleAnnotationSignature implements AnnotationSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature annotationType;

	public SimpleAnnotationSignature() {
	}

	public SimpleAnnotationSignature(TypeSignature annotationType) {
		this.annotationType = annotationType;
	}

	@Override
	public TypeSignature getAnnotationType() {
		return annotationType;
	}

	@Override
	public Map<String, ? extends Value> getValues() {
		return Collections.emptyMap();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(annotationType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		annotationType = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return "@" + annotationType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationType == null) ? 0 : annotationType.hashCode());
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
		SimpleAnnotationSignature other = (SimpleAnnotationSignature) obj;
		if (annotationType == null) {
			if (other.annotationType != null)
				return false;
		} else if (!annotationType.equals(other.annotationType))
			return false;
		return true;
	}
}
