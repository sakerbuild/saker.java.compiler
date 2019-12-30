package test;

import java.lang.annotation.Native;
import java.util.function.Supplier;

public class Main {
	public static void main(String[] args) {
		System.out.println("Main.main()");
	}

	public native void nativef();

	public native <T> T nativef2();

	public native <T extends Thread> T nativef3();

	public native <T extends Thread & Supplier<Integer>> T nativef4();

	public native <T extends K, K extends Supplier<T>> T nativef5(T param, K p2);
}