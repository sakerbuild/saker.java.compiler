/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
