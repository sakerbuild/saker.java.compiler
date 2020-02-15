package saker.java.compiler.impl.compat;

import javax.lang.model.element.ElementKind;

import saker.java.compiler.jdk.impl.JavaCompilationUtils;

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

	private static final ElementKind[] ELEMENTKINDS = { ElementKind.PACKAGE, ElementKind.ENUM, ElementKind.CLASS,
			ElementKind.ANNOTATION_TYPE, ElementKind.INTERFACE, ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
			ElementKind.PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.METHOD,
			ElementKind.CONSTRUCTOR, ElementKind.STATIC_INIT, ElementKind.INSTANCE_INIT, ElementKind.TYPE_PARAMETER,
			ElementKind.OTHER, ElementKind.RESOURCE_VARIABLE, JavaCompilationUtils.getModuleElementKind(),
			JavaCompilationUtils.getRecordElementKind(), JavaCompilationUtils.getRecordComponentElementKind(), };

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
		ElementKind[] vals = ElementKind.values();
		ELEMENTKIND_ORDINAL_INDEX_LOOKUP = new byte[vals.length];
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
		return ELEMENTKIND_ORDINAL_INDEX_LOOKUP[kind.ordinal()];
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
			throw new IllegalArgumentException(ELEMENTKIND_NAMES[index]);
		}
		//this could only happen if someones uses an older version of the saker.java.compiler package
		throw new IllegalArgumentException("Element kind not found for index: " + index + " (Using an older version?)");
	}
}
