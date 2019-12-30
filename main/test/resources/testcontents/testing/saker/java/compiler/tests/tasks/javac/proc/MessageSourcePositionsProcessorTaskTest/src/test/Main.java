package test;

public class Main<@MyAnnot T> implements Runnable {
	@Override
	@MyAnnot
	public void run() {
	}

	@MyAnnot(123)
	public void f() {

	}
}