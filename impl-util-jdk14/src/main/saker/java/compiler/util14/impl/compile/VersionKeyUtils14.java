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
package saker.java.compiler.util14.impl.compile;

import saker.java.compiler.impl.thirdparty.org.objectweb.asm.Opcodes;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.RecordComponentVisitor;
import saker.java.compiler.util8.impl.compile.VersionKeyUtils8.AbiHasherClassVisitor8;

//suppress deprecation as we're using experimental features
@SuppressWarnings("deprecation")
public class VersionKeyUtils14 {
	public static final int ASM_API_VERSION = Opcodes.ASM8_EXPERIMENTAL;

	public static class AbiHasherClassVisitor14 extends AbiHasherClassVisitor8 {

		public AbiHasherClassVisitor14(int api) {
			super(api);
		}

		@Override
		public RecordComponentVisitor visitRecordComponentExperimental(int access, String name, String descriptor,
				String signature) {
			return new RecordComponentVisitor(api,
					super.visitRecordComponentExperimental(access, name, descriptor, signature)) {
				// TODO record API hash
			};
		}
	}
}
