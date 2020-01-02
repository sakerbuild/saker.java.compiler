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
import java.util.EnumSet;
import java.util.Set;

import javax.tools.JavaFileObject;

import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;

public class JavaFileObjectKindEnumSetRMIWrapper implements RMIWrapper {
	private Set<JavaFileObject.Kind> values;

	public JavaFileObjectKindEnumSetRMIWrapper() {
	}

	public JavaFileObjectKindEnumSetRMIWrapper(Set<JavaFileObject.Kind> modifiers) {
		this.values = modifiers;
	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		//if the modifiers are null, handle it as empty, and instantiate a set on the receiving side anyway
		if (values != null) {
			for (JavaFileObject.Kind m : values) {
				out.writeEnumObject(m);
			}
		}
		out.writeObject(null);
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		values = EnumSet.noneOf(JavaFileObject.Kind.class);

		for (JavaFileObject.Kind m; (m = (JavaFileObject.Kind) in.readObject()) != null;) {
			values.add(m);
		}
	}

	@Override
	public Object resolveWrapped() {
		return values;
	}

	@Override
	public Object getWrappedObject() {
		throw new UnsupportedOperationException();
	}
}
