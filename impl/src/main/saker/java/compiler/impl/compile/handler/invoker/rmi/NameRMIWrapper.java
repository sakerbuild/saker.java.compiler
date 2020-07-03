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

import javax.lang.model.element.Name;

import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;

public class NameRMIWrapper implements RMIWrapper {
	private String name;

	public NameRMIWrapper() {
	}

	public NameRMIWrapper(Name name) {
		this.name = name.toString();
	}

	public NameRMIWrapper(CharSequence name) {
		this.name = name.toString();
	}

	public NameRMIWrapper(String name) {
		this.name = name.toString();
	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		out.writeObject(name);
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		this.name = (String) in.readObject();
	}

	@Override
	public Object resolveWrapped() {
		return new IncrementalName(name);
	}

	@Override
	public Object getWrappedObject() {
		throw new UnsupportedOperationException();
	}
}
