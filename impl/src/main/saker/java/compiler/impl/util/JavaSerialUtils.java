package saker.java.compiler.impl.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.List;

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
}
