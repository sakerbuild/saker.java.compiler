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
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnionTypeSignature;

public class SimpleUnionTypeSignature implements UnionTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> alternatives;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleUnionTypeSignature() {
	}

	public SimpleUnionTypeSignature(List<? extends TypeSignature> alternatives) {
		this.alternatives = alternatives;
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
		SerialUtils.writeExternalCollection(out, alternatives);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		alternatives = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public List<? extends TypeSignature> getAlternatives() {
		return alternatives;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin(" | ", alternatives);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternatives == null) ? 0 : alternatives.hashCode());
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
		SimpleUnionTypeSignature other = (SimpleUnionTypeSignature) obj;
		if (alternatives == null) {
			if (other.alternatives != null)
				return false;
		} else if (!alternatives.equals(other.alternatives))
			return false;
		return true;
	}

}
