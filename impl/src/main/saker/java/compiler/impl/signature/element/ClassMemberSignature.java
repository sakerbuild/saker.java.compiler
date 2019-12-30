package saker.java.compiler.impl.signature.element;

import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

public interface ClassMemberSignature extends AnnotatedSignature, DocumentedSignature {
	public String getSimpleName();

	public Set<Modifier> getModifiers();

	public ElementKind getKind();

	public static boolean signatureEquals(ClassMemberSignature first, ClassMemberSignature other) {
		if (!AnnotatedSignature.annotationSignaturesEqual(first, other)) {
			return false;
		}
		if (first.getKind() != other.getKind()) {
			return false;
		}
		if (!Objects.equals(first.getSimpleName(), other.getSimpleName())) {
			return false;
		}
		if (!Objects.equals(first.getModifiers(), other.getModifiers())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode();
}
