package test;

public class J8 {
	public static void main(String[] args) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("J8.main(...).new Runnable() {...}.run()");
			}
		};
	}
}