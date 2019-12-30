package test;

import java.util.List;

public class Caller<T extends Caller<T>> {
	public static void main(String[] args) {
		Main.function(0);
	}

	Caller<T> somefield;
	Caller<? extends T> wcefield;
	Caller<? super T> wcsfield;
}