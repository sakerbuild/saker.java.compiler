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
package saker.java.compiler.impl.compile.handler.info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class RealizedSignatureData implements RealizedSignatureHolder, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableMap<String, ? extends ClassSignature> realizedClasses;
	private PackageSignature realizedPackageSignature;
	private ModuleSignature realizedModuleSignature;

	/**
	 * For {@link Externalizable}.
	 */
	public RealizedSignatureData() {
	}

	public RealizedSignatureData(NavigableMap<String, ? extends ClassSignature> realizedClasses,
			PackageSignature realizedPackageSignature, ModuleSignature realizedModuleSignature) {
		this.realizedClasses = realizedClasses;
		this.realizedPackageSignature = realizedPackageSignature;
		this.realizedModuleSignature = realizedModuleSignature;
	}

	@Override
	public NavigableMap<String, ? extends ClassSignature> getRealizedClasses() {
		return realizedClasses;
	}

	@Override
	public PackageSignature getRealizedPackageSignature() {
		return realizedPackageSignature;
	}

	@Override
	public ModuleSignature getRealizedModuleSignature() {
		return realizedModuleSignature;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, realizedClasses);
		out.writeObject(realizedPackageSignature);
		out.writeObject(realizedModuleSignature);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		realizedClasses = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		realizedPackageSignature = (PackageSignature) in.readObject();
		realizedModuleSignature = (ModuleSignature) in.readObject();
	}

}
