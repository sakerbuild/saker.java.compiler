package test;

public class Main<PT extends Runnable> {
	public static void main(String[] args) {
		System.out.println("Main.main()");
	}

	@Override
	public String toString() {
		return super.toString();
	}
}