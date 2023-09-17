package test;

public class Main {
	public static void main(String[] args) {
		for (String s : Field.list) {
			System.out.println("Main.main() " + s);
		}
		
		new Field().function();
		new Field().receiverFunction();
		
		new Other().function();
		new Other().receiverFunction();
	}
}