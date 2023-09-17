package saker.java.compiler.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compile.signature.impl.MethodParameterSignatureImpl;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class JavaSerialUtils {
	private JavaSerialUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Writes an open ended list.
	 * <p>
	 * The elements of the list is simply written to the output sequentially.
	 * <p>
	 * Note that for these serialization methods to work, there must be an additional object written to the output after
	 * this call. This additional object must not be an instance of the type that is used during reading for
	 * <code>instanceof</code> checks.
	 * 
	 * @param objects
	 *            The objects.
	 * @param out
	 *            The output
	 */
	public static void writeOpenEndedList(List<?> objects, ObjectOutput out) throws IOException {
		if (objects == null) {
			return;
		}
		for (Iterator<?> it = objects.iterator(); it.hasNext();) {
			Object o = it.next();
			out.writeObject(o);
		}
	}

	/**
	 * Starts reading an open ended list from the given input.
	 * 
	 * @param <T>
	 *            The object type.
	 * @param objtype
	 *            The type that the objects should be an instance of.
	 * @param list
	 *            The list to read into.
	 * @param in
	 *            The input to read from.
	 * @return The last object that was read from the input and is not an instance of the type.
	 */
	public static <T> Object readOpenEndedList(Class<T> objtype, List<? super T> list, ObjectInput in)
			throws ClassNotFoundException, IOException {
		return readOpenEndedList(in.readObject(), objtype, list, in);
	}

	/**
	 * Continue reading an open ended list from the given input.
	 * 
	 * @param <T>
	 *            The object type.
	 * @param o
	 *            The last read object from the input.
	 * @param objtype
	 *            The type that the objects should be an instance of.
	 * @param list
	 *            The list to read into.
	 * @param in
	 *            The input to read from.
	 * @return The last object that was read from the input and is not an instance of the type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Object readOpenEndedList(Object o, Class<T> objtype, List<? super T> list, ObjectInput in)
			throws ClassNotFoundException, IOException {
		if (!objtype.isInstance(o)) {
			return o;
		}
		list.add((T) o);
		while (true) {
			o = in.readObject();
			if (!objtype.isInstance(o)) {
				break;
			}
			list.add((T) o);
		}
		return o;
	}

	/**
	 * Writes an open ended list of method parameters.
	 * <p>
	 * This methods relies on the known implementations of the {@link MethodParameterSignature} interface.
	 * <p>
	 * The object types written after this should not be instance of any of the following:
	 * <ul>
	 * <li>{@link TypeSignature}</li>
	 * <li>{@link String}</li>
	 * </ul>
	 * 
	 * @param parameters
	 *            The parameter signatures.
	 * @param out
	 *            The output
	 */
	public static void writeOpenEndedMethodParameterList(List<? extends MethodParameterSignature> parameters,
			ObjectOutput out) throws IOException {
		if (parameters == null) {
			return;
		}
		for (MethodParameterSignature sig : parameters) {
			((Externalizable) sig).writeExternal(out);
		}
	}

	public static Object readOpenEndedMethodParameterList(List<? super MethodParameterSignature> list, ObjectInput in)
			throws ClassNotFoundException, IOException {
		return readOpenEndedMethodParameterList(in.readObject(), list, in);
	}

	public static Object readOpenEndedMethodParameterList(Object o, List<? super MethodParameterSignature> list,
			ObjectInput in) throws ClassNotFoundException, IOException {
		while (true) {
			String name;
			TypeSignature type;
			Set<Modifier> modifiers;
			if (o instanceof String) {
				//MethodParameterSignatureImpl
				name = (String) o;
				type = (TypeSignature) in.readObject();
				modifiers = ImmutableModifierSet.empty();
			} else if (o instanceof TypeSignature) {
				type = (TypeSignature) o;
				Object next = in.readObject();
				if (ImmutableModifierSet.isExternalObjectFlag(next)) {
					//FullMethodParameterSignatureImpl
					modifiers = ImmutableModifierSet.setFromExternalObjectFlag(next);
					name = (String) in.readObject();
				} else {
					//FinalMethodParameterSignatureImpl
					name = (String) next;
					modifiers = IncrementalElementsTypes.MODIFIERS_FINAL;
				}
			} else {
				return o;
			}
			list.add(MethodParameterSignatureImpl.create(modifiers, type, name));
			o = in.readObject();
		}
	}
}
