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

import java.util.Set;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class ClassModifierABIChange implements AbiChange {
	///
	/// Allowed modifiers on a class declaration are: public, protected, private, final, abstract, static, strictfp
	///

	private static final Set<Modifier> VISIBILITY_MODIFIERS = ImmutableModifierSet.of(Modifier.PRIVATE,
			Modifier.PROTECTED, Modifier.PUBLIC);

	private String classCanonicalName;
	private transient Set<Modifier> classModifiers;
	private transient Set<Modifier> prevModifiers;

	public ClassModifierABIChange(ClassSignature clazz, Set<Modifier> prevmodifiers) {
		this.classModifiers = clazz.getModifiers();
		this.classCanonicalName = clazz.getCanonicalName();
		this.prevModifiers = prevmodifiers;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		//XXX make class modifier abi change detection more efficient
		return ClassChangedABIChange.affectsReferencedClass(classCanonicalName, usage, foundchanges);
	}

	//this method may be used later
//	private Set<Modifier> getChanges() {
//		Set<Modifier> prev = ObjectUtils.cloneEnumSet(Modifier.class, prevModifiers);
//		Set<Modifier> cur = ObjectUtils.cloneEnumSet(Modifier.class, classModifiers);
//		prev.removeAll(cur);
//		cur.removeAll(prevModifiers);
//		Set<Modifier> changed = prev;
//		changed.addAll(cur);
//		return changed;
//	}

	@Override
	public String toString() {
		return "Class modifiers changed: " + classCanonicalName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
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
		ClassModifierABIChange other = (ClassModifierABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		return true;
	}

}
