package test;

public class Field {
	public static final java.util.List<String> list = null;

	public void function() {
		System.out.println("Field.function() logging");
		System.out.println("Field.function() " + "anonreplace");
		class FuncInner {

		}
	}
	//intplaceholder

	private static class PrivateInner {
	}

	public static class PublicInner {
	}
}