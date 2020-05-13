package test;

public class J14 {
	public static void main(String[] args) {
		int i = switch(args[0]) {
			case "a" -> 0;
			default -> 2;
		};
		System.out.println("J14.main() " + i);
	}
}