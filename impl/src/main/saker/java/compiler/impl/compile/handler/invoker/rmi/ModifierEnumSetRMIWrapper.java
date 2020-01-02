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
package saker.java.compiler.impl.compile.handler.invoker.rmi;

import java.io.IOException;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class ModifierEnumSetRMIWrapper implements RMIWrapper {
	private Set<Modifier> modifiers;

	public ModifierEnumSetRMIWrapper() {
	}

	public ModifierEnumSetRMIWrapper(Set<Modifier> modifiers) {
		this.modifiers = modifiers;
	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		ImmutableModifierSet.writeExternalFlag(out, ImmutableModifierSet.getFlag(modifiers));
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		modifiers = ImmutableModifierSet.readExternalFlagSet(in);
	}

	@Override
	public Object resolveWrapped() {
		return modifiers;
	}

	@Override
	public Object getWrappedObject() {
		throw new UnsupportedOperationException();
	}
}
