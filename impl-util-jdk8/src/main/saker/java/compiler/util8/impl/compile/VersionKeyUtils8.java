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
package saker.java.compiler.util8.impl.compile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.NavigableSet;
import java.util.TreeSet;

import saker.build.thirdparty.saker.util.ArrayUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.java.compiler.impl.compile.VersionKeyUtils;
import saker.java.compiler.impl.compile.VersionKeyUtils.ClassFileHasher;
import saker.java.compiler.impl.compile.VersionKeyUtils.NotAbiClassException;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.AnnotationVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ClassReader;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.FieldVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.MethodVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.Opcodes;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.Type;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.TypePath;
import testing.saker.java.compiler.TestFlag;

public class VersionKeyUtils8 {
	public static final int ASM_API_VERSION = Opcodes.ASM7;

	public static class AbiHasherClassVisitor8 extends ClassVisitor implements ClassFileHasher {
		private String className;
		private MessageDigest digest;
		private NavigableSet<String> fields = new TreeSet<>();
		private NavigableSet<String> methods = new TreeSet<>();
		private String classStr;
		private TreeSet<String> classAnnotations = new TreeSet<>();
		private String moduleStr;
		private StringBuilder sb = new StringBuilder();

		public AbiHasherClassVisitor8(int api) {
			super(api);
		}

		@Override
		public boolean update(ByteArrayRegion classbytes, MessageDigest digest) {
			this.digest = digest;
			try {
				ClassReader cr;
				try {
					cr = new ClassReader(classbytes.getArray(), classbytes.getOffset(), classbytes.getLength());
					cr.accept(this, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				} catch (RuntimeException e) {
					// it could be that ASM doesn't support the class file version yet.
					// in this case it throws IllegalArgumentException
					// however, it may be modified in the future to a different kind of exceptions
					// and if the class reader fails for any reason, we should fall back
					// in this case we use all bytes of the class to update the ABI hash
					if (TestFlag.ENABLED) {
						//if we're testing, then throw the exception to force a bugfix
						throw e;
					}

					//fall back to full hash
					digest.update(classbytes.getArray(), classbytes.getOffset(), classbytes.getLength());
					return true;
				}
			} catch (NotAbiClassException e) {
				return false;
			}
			return true;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			this.className = name;

			sb.setLength(0);
			sb.append("c:");
			sb.append(Integer.toHexString(version));
			sb.append('\r');
			sb.append(Integer.toHexString(access));
			sb.append('\r');
			sb.append(name);
			sb.append('\r');
			sb.append(signature);
			sb.append('\r');
			sb.append(superName);
			sb.append('\n');

			if (!ObjectUtils.isNullOrEmpty(interfaces)) {
				sb.append("itf:");
				for (String itf : VersionKeyUtils.clonedSorted(interfaces)) {
					sb.append('\r');
					sb.append(itf);
				}
				sb.append('\n');
			}
			classStr = sb.toString();

			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			return new AbiHasherAnnotationVisitor8(api, super.visitAnnotation(descriptor, visible), classAnnotations,
					"a:" + descriptor + "\r");
		}

		@Override
		public ModuleVisitor visitModule(String name, int access, String version) {
			return new AbiHasherModuleVisitor8(api, super.visitModule(name, access, version), this);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return new AbiHasherAnnotationVisitor8(api,
					super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), classAnnotations,
					"ta:" + Integer.toHexString(typeRef) + "\r" + typePath + "\r" + descriptor + "\r");
		}

		@Override
		public void visitOuterClass(String owner, String name, String descriptor) {
			// if the class is a local or anonymous class
			throw NotAbiClassException.INSTANCE;
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			// don't include the class in the ABI if it is an inner private class
			if (name.equals(this.className)) {
				if (innerName == null) {
					throw NotAbiClassException.INSTANCE;
				}
				if (((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)) {
					throw NotAbiClassException.INSTANCE;
				}
			}
			super.visitInnerClass(name, outerName, innerName, access);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			if (((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)) {
				return null;
			}
			sb.setLength(0);
			sb.append("f:");
			sb.append(Integer.toHexString(access));
			sb.append('\r');
			sb.append(name);
			sb.append('\r');
			sb.append(descriptor);
			sb.append('\r');
			sb.append(signature);
			sb.append('\n');
			if (value != null) {
				sb.append("val:");
				sb.append(value.getClass().getName());
				sb.append('\r');
				sb.append(value);
				sb.append('\n');

			}
			return new FieldVisitor(api, super.visitField(access, name, descriptor, signature, value)) {
				private TreeSet<String> phrases = new TreeSet<>();

				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor8(api, super.visitAnnotation(descriptor, visible), phrases,
							"a:" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					return new AbiHasherAnnotationVisitor8(api,
							super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), phrases,
							"ta:" + Integer.toHexString(typeRef) + "\r" + typePath + "\r" + descriptor + "\r");
				}

				@Override
				public void visitEnd() {
					super.visitEnd();
					for (String p : phrases) {
						sb.append(p);
					}
					fields.add(sb.toString());
				}
			};
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			if (((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)) {
				return null;
			}
			if ("<clinit>".equals(name)) {
				return null;
			}
			sb.setLength(0);
			sb.append("m:");
			sb.append(Integer.toHexString(access));
			sb.append('\r');
			sb.append(name);
			sb.append('\r');
			sb.append(descriptor);
			sb.append('\r');
			sb.append(signature);
			sb.append('\n');

			if (!ObjectUtils.isNullOrEmpty(exceptions)) {
				sb.append("exc:");
				for (String exc : VersionKeyUtils.clonedSorted(exceptions)) {
					sb.append('\r');
					sb.append(exc);
				}
				sb.append('\n');

			}
			return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
				private TreeSet<String> phrases = new TreeSet<>();

				@Override
				public AnnotationVisitor visitAnnotationDefault() {
					sb.append("adef:");
					return new AbiHasherAnnotationVisitor8(api, super.visitAnnotationDefault(), phrases, "adef:");
				}

				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor8(api, super.visitAnnotation(descriptor, visible), phrases,
							"a:" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					return new AbiHasherAnnotationVisitor8(api,
							super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), phrases,
							"ta:" + Integer.toHexString(typeRef) + "\r" + typePath + "\r" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor8(api,
							super.visitParameterAnnotation(parameter, descriptor, visible), phrases,
							"pa:" + Integer.toHexString(parameter) + "\r" + descriptor + "\r");
				}

				@Override
				public void visitParameter(String name, int access) {
					phrases.add("p:" + name + "\r" + Integer.toHexString(access) + descriptor);
					super.visitParameter(name, access);
				}

				@Override
				public void visitEnd() {
					super.visitEnd();
					for (String p : phrases) {
						sb.append(p);
					}
					methods.add(sb.toString());
				}
			};
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
			if (moduleStr != null) {
				digest.update(moduleStr.getBytes(StandardCharsets.UTF_8));
			}
			for (String ca : classAnnotations) {
				digest.update(ca.getBytes(StandardCharsets.UTF_8));
			}
			digest.update(classStr.getBytes(StandardCharsets.UTF_8));
			for (String f : fields) {
				byte[] b = f.getBytes(StandardCharsets.UTF_8);
				digest.update(b);
			}
			for (String m : methods) {
				byte[] b = m.getBytes(StandardCharsets.UTF_8);
				digest.update(b);
			}
		}

		protected void acceptModuleString(String moduleStr) {
			this.moduleStr = moduleStr;
		}

	}

	public static class AbiHasherAnnotationVisitor8 extends AnnotationVisitor {
		private TreeSet<String> values = new TreeSet<>();
		private StringBuilder sb = new StringBuilder();
		private TreeSet<String> resultSet;
		private String prefix;

		public AbiHasherAnnotationVisitor8(int api, AnnotationVisitor annotationVisitor, TreeSet<String> result,
				String prefix) {
			super(api, annotationVisitor);
			this.resultSet = result;
			this.prefix = prefix;
		}

		@Override
		public void visit(String name, Object value) {
			Class<? extends Object> vclass = value.getClass();
			sb.setLength(0);
			sb.append(prefix);
			if (vclass == Type.class) {
				Type t = (Type) value;
				sb.append("t:");
				sb.append(name);
				sb.append('\r');
				sb.append(t.getDescriptor());
			} else if (vclass.isArray()) {
				sb.append("ar:");
				sb.append(name);
				sb.append('\r');
				ArrayUtils.arrayToString(value, sb);
			} else if (vclass == String.class) {
				sb.append("s:");
				sb.append(name);
				sb.append('\r');
				sb.append('\"');
				sb.append(value);
				sb.append('\"');
			} else {
				sb.append("c:");
				sb.append(name);
				sb.append('\r');
				sb.append(vclass.getName());
				sb.append("\r");
				sb.append(value);
			}
			values.add(sb.toString());
			super.visit(name, value);
		}

		@Override
		public void visitEnum(String name, String descriptor, String value) {
			sb.setLength(0);
			sb.append(prefix);
			sb.append("e:");
			sb.append(name);
			sb.append('\r');
			sb.append(descriptor);
			sb.append('\r');
			sb.append(value);
			values.add(sb.toString());
			super.visitEnum(name, descriptor, value);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String descriptor) {
			return new AbiHasherAnnotationVisitor8(api, super.visitAnnotation(name, descriptor), values,
					"an:" + name + "\r" + descriptor + "\r");
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return new AbiHasherAnnotationVisitor8(api, super.visitArray(name), values, "tar" + name + "\r");
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
			sb.setLength(0);
			for (String v : values) {
				sb.append(v);
			}
			sb.append('\n');
			resultSet.add(sb.toString());
		}
	}

	public static class AbiHasherModuleVisitor8 extends ModuleVisitor {
		private final AbiHasherClassVisitor8 hasher;
		private TreeSet<String> phrases = new TreeSet<>();
		private StringBuilder sb = new StringBuilder();

		public AbiHasherModuleVisitor8(int api, ModuleVisitor moduleVisitor, AbiHasherClassVisitor8 hasher) {
			super(api, moduleVisitor);
			this.hasher = hasher;
		}

		@Override
		public void visitMainClass(String mainClass) {
			sb.setLength(0);
			sb.append("m:");
			sb.append(mainClass);
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitMainClass(mainClass);
		}

		@Override
		public void visitPackage(String packaze) {
			sb.setLength(0);
			sb.append("p:");
			sb.append(packaze);
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitPackage(packaze);
		}

		@Override
		public void visitRequire(String module, int access, String version) {
			sb.setLength(0);
			sb.append("r:");
			sb.append(module);
			sb.append('\r');
			sb.append(Integer.toHexString(access));
			sb.append('\r');
			sb.append(version);
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitRequire(module, access, version);
		}

		@Override
		public void visitExport(String packaze, int access, String... modules) {
			sb.setLength(0);
			sb.append("e:");
			sb.append(packaze);
			sb.append('\r');
			sb.append(Integer.toHexString(access));
			if (!ObjectUtils.isNullOrEmpty(modules)) {
				for (String m : VersionKeyUtils.clonedSorted(modules)) {
					sb.append('\r');
					sb.append(m);
				}
			}
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitExport(packaze, access, modules);
		}

		@Override
		public void visitOpen(String packaze, int access, String... modules) {
			sb.setLength(0);
			sb.append("o:");
			sb.append(packaze);
			sb.append('\r');
			sb.append(Integer.toHexString(access));
			if (!ObjectUtils.isNullOrEmpty(modules)) {
				for (String m : VersionKeyUtils.clonedSorted(modules)) {
					sb.append('\r');
					sb.append(m);
				}
			}
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitOpen(packaze, access, modules);
		}

		@Override
		public void visitUse(String service) {
			sb.setLength(0);
			sb.append("u:");
			sb.append(service);
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitUse(service);
		}

		@Override
		public void visitProvide(String service, String... providers) {
			sb.setLength(0);
			sb.append("p:");
			sb.append(service);
			if (!ObjectUtils.isNullOrEmpty(providers)) {
				for (String p : VersionKeyUtils.clonedSorted(providers)) {
					sb.append('\r');
					sb.append(p);
				}
			}
			sb.append('\n');
			phrases.add(sb.toString());
			super.visitProvide(service, providers);
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
			sb.setLength(0);
			for (String p : phrases) {
				sb.append(p);
			}
			hasher.acceptModuleString(sb.toString());
		}
	}
}
