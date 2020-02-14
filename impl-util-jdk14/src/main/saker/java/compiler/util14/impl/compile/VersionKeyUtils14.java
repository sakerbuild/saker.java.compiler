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

import java.nio.charset.StandardCharsets;
import java.util.NavigableSet;
import java.util.TreeSet;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.AnnotationVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.Attribute;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.Opcodes;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.RecordComponentVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.TypePath;
import saker.java.compiler.util8.impl.compile.VersionKeyUtils8.AbiHasherAnnotationVisitor8;
import saker.java.compiler.util8.impl.compile.VersionKeyUtils8.AbiHasherClassVisitor8;

//suppress deprecation as we're using experimental features
@SuppressWarnings("deprecation")
public class VersionKeyUtils14 {
	public static final int ASM_API_VERSION = Opcodes.ASM8_EXPERIMENTAL;

	public static class AbiHasherClassVisitor14 extends AbiHasherClassVisitor8 {
		private NavigableSet<String> records = new TreeSet<>();
		private StringBuilder sb = new StringBuilder();

		public AbiHasherClassVisitor14(int api) {
			super(api);
		}

		@Override
		public RecordComponentVisitor visitRecordComponentExperimental(int access, String name, String descriptor,
				String signature) {
			sb.setLength(0);
			sb.append("r:");
			sb.append(Integer.toHexString(access));
			sb.append('\r');
			sb.append(name);
			sb.append('\r');
			sb.append(descriptor);
			sb.append('\r');
			sb.append(ObjectUtils.nullDefault(signature, (String) null));
			return new RecordComponentVisitor(api,
					super.visitRecordComponentExperimental(access, name, descriptor, signature)) {

				private NavigableSet<String> componentAnnotations = new TreeSet<>();

				@Override
				public AnnotationVisitor visitAnnotationExperimental(String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor8(api, super.visitAnnotationExperimental(descriptor, visible),
							componentAnnotations, "a:" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitTypeAnnotationExperimental(int typeRef, TypePath typePath,
						String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor8(api,
							super.visitTypeAnnotationExperimental(typeRef, typePath, descriptor, visible),
							componentAnnotations,
							"ta:" + Integer.toHexString(typeRef) + "\r" + typePath + "\r" + descriptor + "\r");
				}

				@Override
				public void visitAttributeExperimental(Attribute attribute) {
					// non-standard. probably can be ignored 
					super.visitAttributeExperimental(attribute);
				}

				@Override
				public void visitEndExperimental() {
					super.visitEndExperimental();
					for (String ca : componentAnnotations) {
						sb.append(ca);
					}
					records.add(sb.toString());
				}
			};
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
			for (String r : records) {
				digest.update(r.getBytes(StandardCharsets.UTF_8));
			}
		}
	}
}
