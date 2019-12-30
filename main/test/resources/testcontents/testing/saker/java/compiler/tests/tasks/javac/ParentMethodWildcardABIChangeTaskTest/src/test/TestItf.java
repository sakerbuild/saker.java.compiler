package test;

import java.util.List;

public interface TestItf {
	public void f(List<? extends Runnable> l);
}
