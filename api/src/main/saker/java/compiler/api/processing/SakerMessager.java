package saker.java.compiler.api.processing;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Describes the {@link Messager} functionality provided by the incremental Java compiler.
 * <p>
 * The {@link Messager} instance can be downcasted to {@link SakerMessager} during annotation processing.
 * <p>
 * The interface provides additional functions for printing messages that are based on some other elements. These
 * functions allow providing originating elements the same way as the {@link Filer} class.
 */
public interface SakerMessager extends Messager {
	/**
	 * Prints a message of the specified kind.
	 *
	 * @param kind
	 *            The kind of message.
	 * @param msg
	 *            The message, or an empty string if none.
	 * @param originatingElements
	 *            The elements causally associated with the origin of this message, may be elided or {@code null}.
	 */
	public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element... originatingElements);

	/**
	 * Prints a message of the specified kind at the location of the element.
	 *
	 * @param kind
	 *            The kind of message.
	 * @param msg
	 *            The message, or an empty string if none.
	 * @param e
	 *            The element to use as a position hint.
	 * @param originatingElements
	 *            The elements causally associated with the origin of this message, may be elided or {@code null}.
	 */
	public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, Element... originatingElements);

	/**
	 * Prints a message of the specified kind at the location of the annotation mirror of the annotated element.
	 *
	 * @param kind
	 *            The kind of message.
	 * @param msg
	 *            The message, or an empty string if none.
	 * @param e
	 *            The annotated element.
	 * @param a
	 *            The annotation to use as a position hint.
	 * @param originatingElements
	 *            The elements causally associated with the origin of this message, may be elided or {@code null}.
	 */
	public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
			Element... originatingElements);

	/**
	 * Prints a message of the specified kind at the location of the annotation value inside the annotation mirror of
	 * the annotated element.
	 *
	 * @param kind
	 *            The kind of message.
	 * @param msg
	 *            The message, or an empty string if none.
	 * @param e
	 *            The annotated element.
	 * @param a
	 *            The annotation containing the annotation value.
	 * @param v
	 *            The annotation value to use as a position hint.
	 * @param originatingElements
	 *            The elements causally associated with the origin of this message, may be elided or {@code null}.
	 */
	public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v,
			Element... originatingElements);
}
