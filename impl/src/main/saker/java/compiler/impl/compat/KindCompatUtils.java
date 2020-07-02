package saker.java.compiler.impl.compat;

import java.util.Arrays;

import javax.lang.model.element.ElementKind;

import saker.java.compiler.api.processing.exc.ElementKindNotFoundException;

public class KindCompatUtils {
	private KindCompatUtils() {
		throw new UnsupportedOperationException();
	}

	// ONLY ADDITIONAL MODIFICATIONS TO THESE ARRAYS
	//  for backward compatibility

	private static final String[] ELEMENTKIND_NAMES = { "PACKAGE", "ENUM", "CLASS", "ANNOTATION_TYPE", "INTERFACE",
			"ENUM_CONSTANT", "FIELD", "PARAMETER", "LOCAL_VARIABLE", "EXCEPTION_PARAMETER", "METHOD", "CONSTRUCTOR",
			"STATIC_INIT", "INSTANCE_INIT", "TYPE_PARAMETER", "OTHER", "RESOURCE_VARIABLE", "MODULE", "RECORD",
			"RECORD_COMPONENT" };

	private static final ElementKind[] ELEMENTKINDS = new ElementKind[ELEMENTKIND_NAMES.length];

	private static final IllegalArgumentException[] ELEMENTKIND_NOTFOUND_EXCEPTIONS = new IllegalArgumentException[ELEMENTKIND_NAMES.length];

	public static final byte ELEMENTKIND_INDEX_PACKAGE = 0;
	public static final byte ELEMENTKIND_INDEX_ENUM = 1;
	public static final byte ELEMENTKIND_INDEX_CLASS = 2;
	public static final byte ELEMENTKIND_INDEX_ANNOTATION_TYPE = 3;
	public static final byte ELEMENTKIND_INDEX_INTERFACE = 4;
	public static final byte ELEMENTKIND_INDEX_ENUM_CONSTANT = 5;
	public static final byte ELEMENTKIND_INDEX_FIELD = 6;
	public static final byte ELEMENTKIND_INDEX_PARAMETER = 7;
	public static final byte ELEMENTKIND_INDEX_LOCAL_VARIABLE = 8;
	public static final byte ELEMENTKIND_INDEX_EXCEPTION_PARAMETER = 9;
	public static final byte ELEMENTKIND_INDEX_METHOD = 10;
	public static final byte ELEMENTKIND_INDEX_CONSTRUCTOR = 11;
	public static final byte ELEMENTKIND_INDEX_STATIC_INIT = 12;
	public static final byte ELEMENTKIND_INDEX_INSTANCE_INIT = 13;
	public static final byte ELEMENTKIND_INDEX_TYPE_PARAMETER = 14;
	public static final byte ELEMENTKIND_INDEX_OTHER = 15;
	public static final byte ELEMENTKIND_INDEX_RESOURCE_VARIABLE = 16;
	public static final byte ELEMENTKIND_INDEX_MODULE = 17;
	public static final byte ELEMENTKIND_INDEX_RECORD = 18;
	public static final byte ELEMENTKIND_INDEX_RECORD_COMPONENT = 19;

	private static final byte[] ELEMENTKIND_ORDINAL_INDEX_LOOKUP;
	static {
		for (int i = 0; i < ELEMENTKIND_NAMES.length; i++) {
			try {
				ELEMENTKINDS[i] = ElementKind.valueOf(ELEMENTKIND_NAMES[i]);
			} catch (IllegalArgumentException e) {
				//an enum was not found in the current JVM
				//running on older version, not supported				
				ELEMENTKIND_NOTFOUND_EXCEPTIONS[i] = e;
			}
		}
		ELEMENTKIND_ORDINAL_INDEX_LOOKUP = new byte[Math.max(ELEMENTKIND_NAMES.length, ElementKind.values().length)];
		Arrays.fill(ELEMENTKIND_ORDINAL_INDEX_LOOKUP, (byte) -1);
		for (int i = 0; i < ELEMENTKINDS.length; i++) {
			ElementKind ek = ELEMENTKINDS[i];
			if (ek != null) {
				//can be null if the element kind is added to the enumeration
				ELEMENTKIND_ORDINAL_INDEX_LOOKUP[ek.ordinal()] = (byte) i;
			}
		}
	}

	public static byte getElementKindIndex(ElementKind kind) {
		if (kind == null) {
			return -1;
		}
		byte idx = ELEMENTKIND_ORDINAL_INDEX_LOOKUP[kind.ordinal()];
		if (idx < 0) {
			throw new AssertionError("Failed to determine ElementKind index for: " + kind);
		}
		return idx;
	}

	public static String getElementKindName(byte index) {
		if (index < 0) {
			return null;
		}
		if (index < ELEMENTKINDS.length) {
			return ELEMENTKIND_NAMES[index];
		}
		//this could only happen if someones uses an older version of the saker.java.compiler package
		throw new IllegalArgumentException("Element kind not found for index: " + index + " (Using an older version?)");
	}

	public static ElementKind getElementKind(byte index) {
		if (index < 0) {
			return null;
		}
		if (index < ELEMENTKINDS.length) {
			ElementKind result = ELEMENTKINDS[index];
			if (result != null) {
				return result;
			}
			throw new ElementKindNotFoundException(ELEMENTKIND_NAMES[index], ELEMENTKIND_NOTFOUND_EXCEPTIONS[index]);
		}
		//this could only happen if someones uses an older version of the saker.java.compiler package
		throw new UnsupportedOperationException(
				"Unrecognized index for ElementKind: " + index + " (Using older saker.java.compiler version?)");
	}
}
