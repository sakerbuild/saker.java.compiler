package test;

import java.util.List;
import java.util.Map;

public class Main {
	public static <T extends E, E extends List<T>> void function() {}
	public static <T extends E, E extends List<T>> void function(List<E> list) {}
	public static <T extends E, E extends List<T>> void function(Map<T, ? extends E> map) {}
	public static <T extends E, E extends List<T>> void function(int i) {}
	//replace
	
	public static <T extends List<T>> void rec(List<T> l) {}
}