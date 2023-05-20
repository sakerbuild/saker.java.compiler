package test2;

import test1.Main1;

public class Main2 {
	//reference the jar classpath
	private firstpkg.FirstClass fc;

	public static void main(String[] args) {
		System.out.println("Main2.main()");
		Main1.main(args);
	}
}