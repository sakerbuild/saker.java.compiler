package test;

public class J16 {
	public static void main(String[] args) {
		Object as = args.toString();
		if (as instanceof String s && s.length() > 10) {
			System.out.println("J16.main() " + s.substring(3));
		}
	}
}