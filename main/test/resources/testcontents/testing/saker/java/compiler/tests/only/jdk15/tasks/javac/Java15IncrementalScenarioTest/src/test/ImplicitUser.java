package test;

public class ImplicitUser {
	public static void main(ImplicitPermit ip) {
		if (ip instanceof ImplicitSub2) {
			System.out.println("ImplicitUser.main()");
		}
	}
}