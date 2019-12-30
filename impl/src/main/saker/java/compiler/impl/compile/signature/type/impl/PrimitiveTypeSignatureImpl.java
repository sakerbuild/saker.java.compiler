package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.lang.model.type.TypeKind;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.PrimitiveTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class PrimitiveTypeSignatureImpl extends AnnotatedSignatureImpl implements PrimitiveTypeSignature {
	private static final long serialVersionUID = 1L;

	private static final PrimitiveTypeSignature INSTANCE_BOOLEAN = new PrimitiveTypeSignatureImpl(
			Collections.emptyList(), TypeKind.BOOLEAN);
	private static final PrimitiveTypeSignature INSTANCE_BYTE = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.BYTE);
	private static final PrimitiveTypeSignature INSTANCE_SHORT = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.SHORT);
	private static final PrimitiveTypeSignature INSTANCE_INT = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.INT);
	private static final PrimitiveTypeSignature INSTANCE_LONG = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.LONG);
	private static final PrimitiveTypeSignature INSTANCE_CHAR = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.CHAR);
	private static final PrimitiveTypeSignature INSTANCE_FLOAT = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.FLOAT);
	private static final PrimitiveTypeSignature INSTANCE_DOUBLE = new PrimitiveTypeSignatureImpl(
			Collections.emptyList(), TypeKind.DOUBLE);

	private static final EnumMap<TypeKind, PrimitiveTypeSignature> SIMPLE_PRIMTIVE_SIGNATURES = new EnumMap<>(
			TypeKind.class);
	static {
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.BOOLEAN, INSTANCE_BOOLEAN);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.BYTE, INSTANCE_BYTE);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.SHORT, INSTANCE_SHORT);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.INT, INSTANCE_INT);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.LONG, INSTANCE_LONG);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.CHAR, INSTANCE_CHAR);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.FLOAT, INSTANCE_FLOAT);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.DOUBLE, INSTANCE_DOUBLE);
	}

	private TypeKind typeKind;

	public PrimitiveTypeSignatureImpl() {
	}

	public static PrimitiveTypeSignature create(TypeKind kind) {
		return SIMPLE_PRIMTIVE_SIGNATURES.get(kind);
	}

	public static PrimitiveTypeSignature create(List<? extends AnnotationSignature> annotations, TypeKind typeKind) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(typeKind);
		}
		return new PrimitiveTypeSignatureImpl(annotations, typeKind);
	}

	private PrimitiveTypeSignatureImpl(List<? extends AnnotationSignature> annotations, TypeKind typeKind) {
		super(annotations);
		this.typeKind = typeKind;
	}

	@Override
	public TypeKind getTypeKind() {
		return typeKind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((typeKind == null) ? 0 : typeKind.hashCode());
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
		PrimitiveTypeSignatureImpl other = (PrimitiveTypeSignatureImpl) obj;
		if (typeKind != other.typeKind)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + Objects.toString(typeKind).toLowerCase(Locale.ENGLISH);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(typeKind);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		typeKind = (TypeKind) in.readObject();
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return Objects.toString(typeKind).toLowerCase(Locale.ENGLISH);
	}

}
