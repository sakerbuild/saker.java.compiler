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
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnionTypeSignature;

public class UnionTypeSignatureImpl extends AnnotatedSignatureImpl implements UnionTypeSignature {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> alternatives;

	public UnionTypeSignatureImpl() {
	}

	public static UnionTypeSignature create(List<? extends TypeSignature> alternatives) {
		return new SimpleUnionTypeSignature(alternatives);
	}

	public static UnionTypeSignature create(List<? extends AnnotationSignature> annotations,
			List<? extends TypeSignature> alternatives) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(alternatives);
		}
		return new UnionTypeSignatureImpl(annotations, alternatives);
	}

	private UnionTypeSignatureImpl(List<? extends AnnotationSignature> annotations,
			List<? extends TypeSignature> alternatives) {
		super(annotations);
		this.alternatives = alternatives;
	}

	@Override
	public List<? extends TypeSignature> getAlternatives() {
		return alternatives;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((alternatives == null) ? 0 : alternatives.hashCode());
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
		UnionTypeSignatureImpl other = (UnionTypeSignatureImpl) obj;
		if (alternatives == null) {
			if (other.alternatives != null)
				return false;
		} else if (!alternatives.equals(other.alternatives))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin(" | ", alternatives);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		SerialUtils.writeExternalCollection(out, alternatives);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		alternatives = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}
}
