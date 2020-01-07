package test;

import test.p1.ImportedClass;

public class ItfImpl implements Itf {

	@Override
	public void f(ImportedClass i) {
		i.p1();
	}

}
