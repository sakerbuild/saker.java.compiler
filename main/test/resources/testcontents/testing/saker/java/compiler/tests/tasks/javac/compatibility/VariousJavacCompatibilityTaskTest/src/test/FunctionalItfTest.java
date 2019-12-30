package test;

import java.util.function.Supplier;

interface NonFunctional {
}

@FunctionalInterface
interface SimpleFunctional {
	public void f();
}

@FunctionalInterface
interface ExtRunnable extends Runnable {
}

interface ExtRunSupplier extends Runnable, Supplier<Object> {
}

@FunctionalInterface
interface ObjDef {
	public void f();

	@Override
	int hashCode();

	@Override
	boolean equals(Object obj);
}

@FunctionalInterface
interface ObjDefRunExt extends Runnable {

	@Override
	int hashCode();

	@Override
	boolean equals(Object obj);

	@Override
	String toString();
}

@FunctionalInterface
interface DefaultFunctional {
	void f();

	default void g() {
	}
}

@FunctionalInterface
interface SubDefaultFunctional extends DefaultFunctional {

}

@FunctionalInterface
interface SubDefaultFunctionalAbs extends DefaultFunctional {
	abstract void f();
}

@FunctionalInterface
interface SubDefaultFunctionalSwap extends DefaultFunctional {
	@Override
	abstract void g();

	@Override
	default void f() {
	}
}

@FunctionalInterface
interface SubDefaultFunctionalRun extends DefaultFunctional, Runnable {
	@Override
	default void f() {
	}
}

//https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html

//examples from https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html 9.8-1
interface Ex9_8_1 {
	interface NonFunc {
		boolean equals(Object obj);
	}

	@FunctionalInterface
	interface Func extends NonFunc {
		int compare(String o1, String o2);
	}

	@FunctionalInterface
	interface Comparator<T> {
		boolean equals(Object obj);

		int compare(T o1, T o2);
	}

	interface Foo {
		int m();

		Object clone();
	}
}

//examples from https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html 9.8-2
interface Ex9_8_2 {
	@FunctionalInterface
	interface X1 {
		int m(Iterable<String> arg);
	}

	@FunctionalInterface
	interface Y1 {
		int m(Iterable<String> arg);
	}

	@FunctionalInterface
	interface Z1 extends X1, Y1 {
	}

	@FunctionalInterface
	interface X2 {
		Iterable m(Iterable<String> arg);
	}

	@FunctionalInterface
	interface Y2 {
		Iterable<String> m(Iterable arg);
	}

	@FunctionalInterface
	interface Z2 extends X2, Y2 {
	}

	interface Foo3<T, N extends Number> {
		void m(T arg);

		void m(N arg);
	}

	interface Bar3 extends Foo3<String, Integer> {
	}

	@FunctionalInterface
	interface Baz3 extends Foo3<Integer, Integer> {
	}

	@FunctionalInterface
	interface Exec4 {
		<T> T execute(Supplier<T> a);
	}

	@FunctionalInterface
	interface X5 {
		<T> T execute(Supplier<T> a);
	}

	@FunctionalInterface
	interface Y5 {
		<S> S execute(Supplier<S> a);
	}

	@FunctionalInterface
	interface Exec5 extends X5, Y5 {
	}
}

//examples from https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html 9.8-3
interface Ex9_8_3 {
	@FunctionalInterface
	interface I {
		Object m(Class c);
	}

	@FunctionalInterface
	interface J<S> {
		S m(Class<?> c);
	}

	@FunctionalInterface
	interface K<T> {
		T m(Class<?> c);
	}

	@FunctionalInterface
	interface Functional<S, T> extends I, J<S>, K<T> {
	}
}