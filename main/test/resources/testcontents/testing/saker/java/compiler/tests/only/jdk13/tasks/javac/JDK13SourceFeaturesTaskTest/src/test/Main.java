package test;

import javax.lang.model.element.ElementKind;

public class Main {
	public static void main(String[] args) {
		String val = switch (getKind()) {
			case CLASS -> "C";
			case CONSTRUCTOR -> {
				yield "x";
			}
			case INTERFACE, ANNOTATION_TYPE -> {
				yield "y";
			}
			case ENUM, ENUM_CONSTANT -> "Z";
			default -> "none";
		};
		String html = """
	              <html>
	                  <body>
	                      <p>Hello, world</p>
	                  </body>
	              </html>
	              """;
	}

	private static ElementKind getKind() {
		return ElementKind.CLASS;
	}
}