package test;

import javax.lang.model.element.ElementKind;

public class Main {
	public static void main(String[] args) {
		Object o = null;
		if(o instanceof TestRecord s) {
			System.out.println("Main.main() " + s);
		}
	}

	protected record ProtRecord(int i) {
	}

	record DefRecord(int i) {
	}

	public record PublicRecord(int i) {
	}

	private record PrivateRecord(int i) {
	}
}