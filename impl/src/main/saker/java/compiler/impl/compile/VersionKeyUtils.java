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
package saker.java.compiler.impl.compile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeSet;

import saker.build.thirdparty.org.objectweb.asm.AnnotationVisitor;
import saker.build.thirdparty.org.objectweb.asm.ClassReader;
import saker.build.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.build.thirdparty.org.objectweb.asm.FieldVisitor;
import saker.build.thirdparty.org.objectweb.asm.MethodVisitor;
import saker.build.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;
import saker.build.thirdparty.org.objectweb.asm.Type;
import saker.build.thirdparty.org.objectweb.asm.TypePath;
import saker.build.thirdparty.saker.util.ArrayUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;

public class VersionKeyUtils {
	public static byte[] createVersionKeyFromClassHashes(NavigableMap<String, byte[]> classfilehashes) {
		MessageDigest hasher = getMD5();
		for (byte[] h : classfilehashes.values()) {
			hasher.update(h);
		}
		return hasher.digest();
	}

	public static byte[] createAbiHashOfClassBytes(ByteArrayRegion classbytes) {
		MessageDigest hasher = getMD5();

		if (!updateAbiHashOfClassBytes(classbytes, hasher)) {
			return null;
		}
		return hasher.digest();
	}

	public static boolean updateAbiHashOfClassBytes(ByteArrayRegion classbytes, MessageDigest hasher) {
		ClassReader cr = new ClassReader(classbytes.getArray(), classbytes.getOffset(), classbytes.getLength());

		try {
			AbiHasherClassVisitor abivisitor = new AbiHasherClassVisitor(hasher);
			cr.accept(abivisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		} catch (NotAbiClassException e) {
			return false;
		}
		return true;
	}

	private VersionKeyUtils() {
		throw new UnsupportedOperationException();
	}

	public static MessageDigest getMD5() throws AssertionError {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

	private static String[] clonedSorted(String[] array) {
		if (array.length <= 1) {
			return array;
		}
		if (array.length == 2) {
			if (array[0].compareTo(array[1]) <= 0) {
				//already sorted
				return array;
			}
			//swapped the items
			return new String[] { array[1], array[0] };
		}
		String[] c = array.clone();
		Arrays.sort(c);
		return c;
	}

	private static class NotAbiClassException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public static final NotAbiClassException INSTANCE = new NotAbiClassException();

		public NotAbiClassException() {
			super(null, null, false, false);
		}
	}

	private static class AbiHasherAnnotationVisitor extends AnnotationVisitor {
		private TreeSet<String> values = new TreeSet<>();
		private StringBuilder sb = new StringBuilder();
		private TreeSet<String> resultSet;
		private String prefix;

		public AbiHasherAnnotationVisitor(int api, AnnotationVisitor annotationVisitor, TreeSet<String> result,
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
			return new AbiHasherAnnotationVisitor(api, super.visitAnnotation(name, descriptor), values,
					"an:" + name + "\r" + descriptor + "\r");
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return new AbiHasherAnnotationVisitor(api, super.visitArray(name), values, "tar" + name + "\r");
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

	private static class AbiHasherClassVisitor extends ClassVisitor {
		private String className;
		private MessageDigest digest;
		private NavigableSet<String> fields = new TreeSet<>();
		private NavigableSet<String> methods = new TreeSet<>();
		private String classStr;
		private TreeSet<String> classAnnotations = new TreeSet<>();
		private String moduleStr;
		private StringBuilder sb = new StringBuilder();

		public AbiHasherClassVisitor(MessageDigest digest) {
			super(Opcodes.ASM7);
			this.digest = digest;
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
				for (String itf : clonedSorted(interfaces)) {
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
			return new AbiHasherAnnotationVisitor(api, super.visitAnnotation(descriptor, visible), classAnnotations,
					"a:" + descriptor + "\r");
		}

		@Override
		public ModuleVisitor visitModule(String name, int access, String version) {
			return new AbiHasherModuleVisitor(api, super.visitModule(name, access, version));
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return new AbiHasherAnnotationVisitor(api,
					super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), classAnnotations,
					"ta:" + Integer.toHexString(typeRef) + "\r" + typePath + "\r" + descriptor + "\r");
		}

		@Override
		public void visitOuterClass(String owner, String name, String descriptor) {
			//if the class is a local or anonymous class
			throw NotAbiClassException.INSTANCE;
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			//don't include the class in the ABI if it is an inner private class
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
					return new AbiHasherAnnotationVisitor(api, super.visitAnnotation(descriptor, visible), phrases,
							"a:" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					return new AbiHasherAnnotationVisitor(api,
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
				for (String exc : clonedSorted(exceptions)) {
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
					return new AbiHasherAnnotationVisitor(api, super.visitAnnotationDefault(), phrases, "adef:");
				}

				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor(api, super.visitAnnotation(descriptor, visible), phrases,
							"a:" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					return new AbiHasherAnnotationVisitor(api,
							super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), phrases,
							"ta:" + Integer.toHexString(typeRef) + "\r" + typePath + "\r" + descriptor + "\r");
				}

				@Override
				public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
					return new AbiHasherAnnotationVisitor(api,
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

		private class AbiHasherModuleVisitor extends ModuleVisitor {
			private TreeSet<String> phrases = new TreeSet<>();
			private StringBuilder sb = new StringBuilder();

			public AbiHasherModuleVisitor(int api, ModuleVisitor moduleVisitor) {
				super(api, moduleVisitor);
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
					for (String m : clonedSorted(modules)) {
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
					for (String m : clonedSorted(modules)) {
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
					for (String p : clonedSorted(providers)) {
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
				moduleStr = sb.toString();
			}

		}

	}
}
