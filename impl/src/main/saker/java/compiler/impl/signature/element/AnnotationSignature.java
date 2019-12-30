package saker.java.compiler.impl.signature.element;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public interface AnnotationSignature extends Signature {
	/**
	 * Represents a value of an annotation.
	 */
	public interface Value extends Signature {
		public boolean signatureEquals(Value other);
	}

	/**
	 * The value is another annotation.
	 */
	public interface AnnotValue extends Value {
		public AnnotationSignature getAnnotation();

		@Override
		public default boolean signatureEquals(Value other) {
			if (!(other instanceof AnnotValue)) {
				return false;
			}
			return signatureEquals((AnnotValue) other);
		}

		public default boolean signatureEquals(AnnotValue other) {
			return getAnnotation().signatureEquals(other.getAnnotation());
		}
	}

	/**
	 * The value is an array.
	 */
	public interface ArrayValue extends Value {
		public List<? extends Value> getValues();

		@Override
		public default boolean signatureEquals(Value other) {
			if (!(other instanceof ArrayValue)) {
				return false;
			}
			return signatureEquals((ArrayValue) other);
		}

		public default boolean signatureEquals(ArrayValue other) {
			return ObjectUtils.collectionOrderedEquals(getValues(), other.getValues(), Value::signatureEquals);
		}
	}

	/**
	 * The value is a type such as Type.class.
	 */
	public interface TypeValue extends Value {
		public TypeSignature getType();

		@Override
		public default boolean signatureEquals(Value other) {
			if (!(other instanceof TypeValue)) {
				return false;
			}
			return signatureEquals((TypeValue) other);
		}

		public default boolean signatureEquals(TypeValue other) {
			return getType().signatureEquals(other.getType());
		}
	}

	/**
	 * The value is a primitive literal or String.
	 */
	public interface LiteralValue extends Value {
		public ConstantValueResolver getValue();

		@Override
		public default boolean signatureEquals(Value other) {
			if (!(other instanceof LiteralValue)) {
				return false;
			}
			return signatureEquals((LiteralValue) other);
		}

		public default boolean signatureEquals(LiteralValue other) {
			return getValue().signatureEquals(other.getValue());
		}
	}

	/**
	 * The value is an enum constant.
	 */
	public interface VariableValue extends Value {
		public String getName();

		public TypeSignature getEnclosingType(SakerElementsTypes elemTypes, Element resolutionelement);

		@Override
		public default boolean signatureEquals(Value other) {
			if (!(other instanceof VariableValue)) {
				return false;
			}
			return signatureEquals((VariableValue) other);
		}

		public boolean signatureEquals(VariableValue other);
	}

	public interface UnknownValue extends Value {
		public Object getValue();

		@Override
		public default boolean signatureEquals(Value other) {
			return false;
		}
	}

	public TypeSignature getAnnotationType();

	public Map<String, ? extends Value> getValues();

	public default boolean signatureEquals(AnnotationSignature other) {
		if (!ObjectUtils.objectsEquals(getAnnotationType(), other.getAnnotationType(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.mapOrderedEquals(this.getValues(), other.getValues(), Value::signatureEquals)) {
			return false;
		}
		return true;
	}
}
