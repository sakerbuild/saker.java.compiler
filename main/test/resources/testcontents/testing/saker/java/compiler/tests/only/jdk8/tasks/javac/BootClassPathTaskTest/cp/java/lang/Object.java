package java.lang;

public class Object {
	//need to override hashcode and equals, so javac doesn't crash
	public boolean equals(Object obj) {
		return (this == obj);
	}

	public native int hashCode();
}