package test;

public class Main {
	private static firstpkg.FirstClass fc;

	public static void main(String[] args) {
		System.out.println("Main.main()");
		fc = new firstpkg.FirstClass();
		int j = 0;
		System.out.println("Main.main() " + j);
	}
}