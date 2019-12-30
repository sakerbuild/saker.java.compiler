package test;

public interface TestItf {
	private void method() {
	}

	private static void staticMethod() {

	}

	//compilation failure
//	private class PrivClass {}
//	private interface PrivItf {}
//	private enum PrivEnum {};
//	private @interface PrivAnnot {}
//	private static class PrivStaticClass {}
//	protected class ProtClass{}
//	private default void defmethod() {}
//	private abstract void absmethod();
//	private void absmethod2();
}