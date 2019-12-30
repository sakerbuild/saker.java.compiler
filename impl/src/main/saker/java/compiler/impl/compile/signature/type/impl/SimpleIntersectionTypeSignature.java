package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.IntersectionTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleIntersectionTypeSignature implements IntersectionTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected List<? extends TypeSignature> bounds;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleIntersectionTypeSignature() {
	}

	public SimpleIntersectionTypeSignature(List<? extends TypeSignature> bounds) {
		this.bounds = bounds;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, bounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bounds = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public List<? extends TypeSignature> getBounds() {
		return bounds;
	}

	@Override
	public String toString() {
		return StringUtils.toStringJoin(" & ", bounds);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
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
		SimpleIntersectionTypeSignature other = (SimpleIntersectionTypeSignature) obj;
		if (bounds == null) {
			if (other.bounds != null)
				return false;
		} else if (!bounds.equals(other.bounds))
			return false;
		return true;
	}
}
