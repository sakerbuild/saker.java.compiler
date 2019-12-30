package test;

import javax.lang.model.element.ElementKind;

public class Main {
	public static void main(String[] args) {
		String val = switch (getKind()) {
			case CLASS -> "C";
			case CONSTRUCTOR -> {
				break "x";
			}
			case INTERFACE, ANNOTATION_TYPE -> {
				break "y";
			}
			case ENUM, ENUM_CONSTANT -> "Z";
			default -> "none";
		};
	}

	private static ElementKind getKind() {
		return ElementKind.CLASS;
	}
}