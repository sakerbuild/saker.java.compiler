package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.IntersectionTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class IntersectionTypeSignatureImpl extends AnnotatedSignatureImpl implements IntersectionTypeSignature {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> bounds;

	public IntersectionTypeSignatureImpl() {
	}

	public static IntersectionTypeSignature create(List<? extends TypeSignature> bounds) {
		return new SimpleIntersectionTypeSignature(bounds);
	}

	public static IntersectionTypeSignature create(List<? extends AnnotationSignature> annotations,
			List<? extends TypeSignature> bounds) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(bounds);
		}
		return new IntersectionTypeSignatureImpl(annotations, bounds);
	}

	private IntersectionTypeSignatureImpl(List<? extends AnnotationSignature> annotations,
			List<? extends TypeSignature> bounds) {
		super(annotations);
		this.bounds = bounds;
	}

	@Override
	public List<? extends TypeSignature> getBounds() {
		return bounds;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin(" & ", bounds);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntersectionTypeSignatureImpl other = (IntersectionTypeSignatureImpl) obj;
		if (bounds == null) {
			if (other.bounds != null)
				return false;
		} else if (!bounds.equals(other.bounds))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		SerialUtils.writeExternalCollection(out, bounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		bounds = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return null;
	}
}
