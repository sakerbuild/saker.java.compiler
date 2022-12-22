package test;

public class Main {

	public static void main(String[] args) {
		System.out.println(args);
	}

	public static enum MyEnum {
		FIELD1(123),
		FIELD2(456);

		public final int val;

		private MyEnum(int val) {
			this.val = val;
		}
	}

	public static class Constants {

		public static final int F1VAL = enumgetter().val;

		public static MyEnum enumgetter() {
			return MyEnum.FIELD1;
		}
	}

}