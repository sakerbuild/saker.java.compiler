package test;

import javax.lang.model.element.ElementKind;

public class Main {
	public static void main(String[] args) {
		Object o = null;
		if(o instanceof MyClass s) {
			System.out.println("Main.main() " + s);
		}
	}
}