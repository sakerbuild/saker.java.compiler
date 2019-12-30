package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.AnnotValue;

public class AnnotValueImpl implements AnnotValue, Externalizable {
	private static final long serialVersionUID = 1L;

	private AnnotationSignature annotation;

	public AnnotValueImpl() {
	}

	public AnnotValueImpl(AnnotationSignature annotation) {
		this.annotation = annotation;
	}

	@Override
	public AnnotationSignature getAnnotation() {
		return annotation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
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
		AnnotValueImpl other = (AnnotValueImpl) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return annotation.toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(annotation);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		annotation = (AnnotationSignature) in.readObject();
	}
}
