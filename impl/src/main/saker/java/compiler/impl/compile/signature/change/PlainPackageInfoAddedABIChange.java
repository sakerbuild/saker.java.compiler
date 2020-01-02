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

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;

public class PlainPackageInfoAddedABIChange implements AbiChange {

	private String packagePath;

	public PlainPackageInfoAddedABIChange(String packagePath) {
		this.packagePath = packagePath;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		//adding a package-info java file doesn't affect any other files
		//if the package has any annotations then an other kind of ABIChange is used
		return false;
	}

	@Override
	public String toString() {
		return "Package info added: " + packagePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packagePath == null) ? 0 : packagePath.hashCode());
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
		PlainPackageInfoAddedABIChange other = (PlainPackageInfoAddedABIChange) obj;
		if (packagePath == null) {
			if (other.packagePath != null)
				return false;
		} else if (!packagePath.equals(other.packagePath))
			return false;
		return true;
	}

}
