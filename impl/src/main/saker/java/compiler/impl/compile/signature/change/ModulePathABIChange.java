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
package saker.java.compiler.impl.compile.signature.change;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;

public class ModulePathABIChange implements AbiChange {

	private transient SortedSet<String> previous;
	private transient SortedSet<String> current;

	public ModulePathABIChange(SortedSet<String> previous, SortedSet<String> current) {
		this.previous = previous;
		this.current = current;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		return true;
	}

	private SortedSet<String> getAddedModules() {
		SortedSet<String> result = new TreeSet<>(current);
		result.removeAll(previous);
		return result;
	}

	private SortedSet<String> getRemovedModules() {
		SortedSet<String> result = new TreeSet<>(previous);
		result.removeAll(current);
		return result;
	}

	@Override
	public String toString() {
		if (previous == null) {
			return "Compilation module path added: " + StringUtils.toStringJoin(", ", current);
		}
		if (current == null) {
			return "Compilation module path removed: " + StringUtils.toStringJoin(", ", previous);
		}
		SortedSet<String> added = getAddedModules();
		SortedSet<String> removed = getRemovedModules();
		return "Compilation module path changed:"
				+ (added.isEmpty() ? "" : " Added: " + StringUtils.toStringJoin(", ", added))
				+ (removed.isEmpty() ? "" : " Removed: " + StringUtils.toStringJoin(", ", removed));
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return ObjectUtils.isSameClass(this, o);
	}
}
