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
package saker.java.compiler.jdk.impl.compile;

import saker.java.compiler.impl.compile.VersionKeyUtils.ClassFileHasher;
import saker.java.compiler.util8.impl.compile.VersionKeyUtils8;

public class VersionKeyLangUtils {
	public static ClassFileHasher createAbiHashingClassVisitor() {
		return new VersionKeyUtils8.AbiHasherClassVisitor8(VersionKeyUtils8.ASM_API_VERSION);
	}
}
