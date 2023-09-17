package test;

public class Other {
	public void function() {
		System.out.println("Other.function() logging");
	}

	public void receiverFunction(Other this) {
		System.out.println("Other.receiverFunction() logging");
	}
}