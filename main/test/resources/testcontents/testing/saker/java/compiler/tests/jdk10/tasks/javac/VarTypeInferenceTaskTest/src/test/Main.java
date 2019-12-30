package test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.lang.model.element.ElementKind;

public class Main {
	public static void main(String[] args) {
		var list = new ArrayList<String>();
		list.add("str");
		var r = new Runnable() {
			public int x;

			@Override
			public void run() {
			}
		};
		System.out.println("Main.main() " + r.x);
		Function<Integer, String> f1 = (i) -> Integer.toString(i);
		Function<Integer, String> f2 = (Integer i) -> Integer.toString(i);
		
		//not supported on jdk10:
//		Function<Integer, String> f3 = (var i) -> Integer.toString(i);
//		BiFunction<Integer, Long, String> bf1 = (var i, var l) -> Integer.toString(i) + Long.toString(l);
	}

}