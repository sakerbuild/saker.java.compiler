package test;

import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.AnnoConstruct;

public class Main {
	public static void main(String[] args) {
		//these classes cannot be accessed unless we add the exports
		BasicJavacTask jt;
		AnnoConstruct ac;
	}
}