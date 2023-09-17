package test;

public class Field {
	public static final java.util.List<String> list = null;

	public void function() {
		System.out.println("Field.function() logging");
	}

	public void receiverFunction(Field this) {
		System.out.println("Field.receiverFunction() logging");
	}
}