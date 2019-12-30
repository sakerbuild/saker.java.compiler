package test;

public abstract class Main {
	public void testf() {
		try (MyResource res = null) {
		} catch (Throwable e) {
		}
	}
}