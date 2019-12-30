package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.Name;

public class IncrementalName implements Name, IncrementallyModelled, Comparable<IncrementalName> {
	public static final IncrementalName EMPTY_NAME = new IncrementalName("");

	private String name;

	public IncrementalName(String name) {
		this.name = name;
	}

	@Override
	public int length() {
		return name.length();
	}

	@Override
	public char charAt(int index) {
		return name.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return name.subSequence(start, end);
	}

	@Override
	public boolean contentEquals(CharSequence cs) {
		return name.contentEquals(cs);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncrementalName other = (IncrementalName) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public int compareTo(IncrementalName o) {
		return name.compareTo(o.name);
	}

}
