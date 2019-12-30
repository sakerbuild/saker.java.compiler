package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.util.Iterator;

public class QualifiedNameIterator implements Iterator<String> {
	private String qualifiedName;
	private int startIndex = 0;

	public QualifiedNameIterator(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	@Override
	public boolean hasNext() {
		return startIndex < qualifiedName.length();
	}

	public boolean hasMultiple() {
		return qualifiedName.indexOf('.', startIndex) >= 0;
	}

	@Override
	public String next() {
		int dotidx = qualifiedName.indexOf('.', startIndex);
		if (dotidx < 0) {
			dotidx = qualifiedName.length();
		}
		String result = qualifiedName.substring(startIndex, dotidx);
		startIndex = dotidx + 1;
		return result;
	}

	public void reset() {
		startIndex = 0;
	}

	@Override
	public String toString() {
		return qualifiedName.substring(startIndex);
	}

	public String getRemaining() {
		return qualifiedName.substring(startIndex);
	}
}
