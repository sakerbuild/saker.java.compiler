package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.ClassSignature.PermittedSubclassesList;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class ExplicitPermittedSubclassesList implements PermittedSubclassesList, Externalizable {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> types;

	/**
	 * For {@link Externalizable}.
	 */
	public ExplicitPermittedSubclassesList() {
	}

	public ExplicitPermittedSubclassesList(List<? extends TypeSignature> types) {
		this.types = types;
	}

	public List<? extends TypeSignature> getTypes() {
		return types;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitExplicit(types);
	}

	@Override
	public boolean signatureEquals(PermittedSubclassesList other) {
		if (other == null) {
			return false;
		}
		SignatureEqualsVisitor visitor = new SignatureEqualsVisitor();
		other.accept(visitor);
		return visitor.equals;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, types);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		types = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		ExplicitPermittedSubclassesList other = (ExplicitPermittedSubclassesList) obj;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExplicitPermittedSubclasses[types=" + types + "]";
	}

	private final class SignatureEqualsVisitor implements Visitor {
		private boolean equals = false;

		@Override
		public void visitUnspecified() {
		}

		@Override
		public void visitExplicit(List<? extends TypeSignature> types) {
			equals = ObjectUtils.collectionOrderedEquals(ExplicitPermittedSubclassesList.this.getTypes(), types,
					TypeSignature::signatureEquals);
		}
	}

}
