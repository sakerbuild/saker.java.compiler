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
package saker.java.compiler.impl.find;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.org.objectweb.asm.ClassWriter;
import saker.build.thirdparty.org.objectweb.asm.MethodVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.thirdparty.saker.util.io.JarFileUtils;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.build.util.cache.CacheKey;
import saker.build.util.java.JavaTools;
import saker.nest.bundle.NestBundleClassLoader;

public abstract class EchoJavaEnvironmentProperty<T> implements EnvironmentProperty<T>, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final FileTime EPOCH_FILETIME = FileTime.fromMillis(0);

	private static final String ECHO_JAR_CLASSPATH_NAME = "echo.jar";

	protected static final String JAVA_VERSION_ECHO_CLASS_NAME = "JavaVersionEchoClass";
	protected static final String JAVA_MAJOR_ECHO_CLASS_NAME = "JavaMajorEchoClass";
	protected static final String JAVA_VERSION_AND_MAJOR_ECHO_CLASS_NAME = "JavaVersionAndMajorEchoClass";

	protected SakerPath jdkPath;

	/**
	 * For {@link Externalizable}.
	 */
	protected EchoJavaEnvironmentProperty() {
	}

	protected EchoJavaEnvironmentProperty(SakerPath jdkPath) {
		Objects.requireNonNull(jdkPath, "jdk path");
		this.jdkPath = jdkPath;
	}

	@Override
	public final T getCurrentValue(SakerEnvironment environment) throws Exception {
		Path classpath = environment.getCachedData(new EchoClassPathCacheKey());
		Path exepath = JavaTools.getJavaExeProcessPath(LocalFileProvider.toRealPath(jdkPath));
		String[] command = new String[] { exepath.normalize().toString(), "-classpath", classpath.toString(),
				getMainClassName() };
		ProcessBuilder pb = new ProcessBuilder(command);
		//XXX the classpath can be a directory that is longer than MAX_PATH. This will cause the process to fail to start on Windows
		//as the process doesn't modify anything, don't change the working directory, so we can start the process.
		pb.environment().clear();
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			String result = StreamUtils.readStreamStringFully(proc.getInputStream()).trim();
			int rescode;
			try {
				rescode = proc.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw e;
			}
			if (rescode == 0 && !result.isEmpty()) {
				return parseOutput(result);
			}
			throw new IOException("Failed to execute java process at: " + jdkPath + " (" + getClass().getName() + ") ("
					+ Arrays.toString(command) + ") (process exited with: " + rescode + ") StdOut & Err: \n" + result);
		} catch (IOException | InterruptedException e) {
			throw new IOException("Failed to execute java process at: " + jdkPath + " (" + getClass().getName() + ") ("
					+ Arrays.toString(command) + ") (" + e + ")", e);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(jdkPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		jdkPath = (SakerPath) in.readObject();
	}

	protected abstract T parseOutput(String result) throws Exception;

	protected abstract String getMainClassName();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jdkPath == null) ? 0 : jdkPath.hashCode());
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
		EchoJavaEnvironmentProperty<?> other = (EchoJavaEnvironmentProperty<?>) obj;
		if (jdkPath == null) {
			if (other.jdkPath != null)
				return false;
		} else if (!jdkPath.equals(other.jdkPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[jdkPath=" + jdkPath + "]";
	}

	static class EchoClassPath {
		private Path path;
		private FileEntry classFileAttrs;

		public EchoClassPath(Path path, FileEntry classFileAttrs) {
			this.path = path;
			this.classFileAttrs = classFileAttrs;
		}
	}

	static class EchoClassPathCacheKey implements CacheKey<Path, EchoClassPath> {
		@Override
		public EchoClassPath allocate() throws Exception {
			Path classpath;
			NestBundleClassLoader bcl = (NestBundleClassLoader) getClass().getClassLoader();
			Path storageDirectoryPath = bcl.getBundle().getBundleStoragePath();
			if (storageDirectoryPath != null) {
				classpath = storageDirectoryPath.resolve("java.version.echo");
			} else {
				String tmpdir = System.getProperty("java.io.tmpdir");
				if (tmpdir == null) {
					throw new FileNotFoundException("Property java.io.tmpdir not found.");
				}
				classpath = Paths.get(tmpdir).resolve(".saker/java.version.echo");
			}
			Path jarfile = classpath.resolve(ECHO_JAR_CLASSPATH_NAME);
			Files.createDirectories(classpath);
			byte[] echojarbytes = generateEchoJarBytes();
			FileUtils.writeStreamEqualityCheckTo(new UnsyncByteArrayInputStream(echojarbytes), jarfile);
			FileEntry cfattrs = LocalFileProvider.getInstance().getFileAttributes(jarfile);
			return new EchoClassPath(jarfile, cfattrs);
		}

		@Override
		public Path generate(EchoClassPath resource) throws Exception {
			return resource.path;
		}

		@Override
		public boolean validate(Path data, EchoClassPath resource) {
			return !LocalFileProvider.getInstance().isChanged(resource.path, resource.classFileAttrs.getSize(),
					resource.classFileAttrs.getLastModifiedMillis());
		}

		@Override
		public long getExpiry() {
			return Long.MAX_VALUE;
		}

		@Override
		public void close(Path data, EchoClassPath resource) throws Exception {
		}

		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return ObjectUtils.isSameClass(this, obj);
		}
	}

	private static byte[] generateJavaMajorEchoClassBefore9() {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, JAVA_MAJOR_ECHO_CLASS_NAME, null,
				"java/lang/Object", null);

		asmNoArgConstructor(cw);

		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		main.visitCode();
		asmPrintlnJavaMajorBefore9(main);
		asmSystemExitZero(main);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(0, 0);
		main.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static byte[] generateJavaMajorEchoClassAfter9() {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, JAVA_MAJOR_ECHO_CLASS_NAME, null,
				"java/lang/Object", null);

		asmNoArgConstructor(cw);

		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		main.visitCode();
		asmPrintlnJavaMajorAfter9(main);
		asmSystemExitZero(main);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(0, 0);
		main.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static byte[] generateJavaVersionEchoClass() {
		//XXX we could actually cache this byte array in compile time
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, JAVA_VERSION_ECHO_CLASS_NAME, null,
				"java/lang/Object", null);

		asmNoArgConstructor(cw);

		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		main.visitCode();
		asmPrintlnJavaVersionProperty(main);
		asmSystemExitZero(main);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(0, 0);
		main.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static byte[] generateJavaVersionAndMajorEchoClassBefore9() {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, JAVA_VERSION_AND_MAJOR_ECHO_CLASS_NAME, null,
				"java/lang/Object", null);

		asmNoArgConstructor(cw);

		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		main.visitCode();
		asmPrintlnJavaVersionProperty(main);
		asmPrintlnJavaMajorBefore9(main);
		asmSystemExitZero(main);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(0, 0);
		main.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static byte[] generateJavaVersionAndMajorEchoClassAfter9() {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, JAVA_VERSION_AND_MAJOR_ECHO_CLASS_NAME, null,
				"java/lang/Object", null);

		asmNoArgConstructor(cw);

		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		main.visitCode();
		asmPrintlnJavaVersionProperty(main);
		asmPrintlnJavaMajorAfter9(main);
		asmSystemExitZero(main);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(0, 0);
		main.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static byte[] generateEchoJarBytes() throws IOException {
		Manifest man = new Manifest();
		Attributes mainattrs = man.getMainAttributes();
		mainattrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainattrs.put(JarFileUtils.getMultiReleaseManifestAttributeName(), "true");
		try (UnsyncByteArrayOutputStream os = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(os, man)) {
				addJarClass(jaros, JAVA_VERSION_ECHO_CLASS_NAME, generateJavaVersionEchoClass());

				addJarClass(jaros, JAVA_MAJOR_ECHO_CLASS_NAME, generateJavaMajorEchoClassBefore9());
				addJarClass(jaros, generateJavaMajorEchoClassAfter9(),
						"META-INF/versions/9/" + JAVA_MAJOR_ECHO_CLASS_NAME.replace('.', '/') + ".class");

				addJarClass(jaros, JAVA_VERSION_AND_MAJOR_ECHO_CLASS_NAME,
						generateJavaVersionAndMajorEchoClassBefore9());
				addJarClass(jaros, generateJavaVersionAndMajorEchoClassAfter9(),
						"META-INF/versions/9/" + JAVA_VERSION_AND_MAJOR_ECHO_CLASS_NAME.replace('.', '/') + ".class");
			}
			return os.toByteArray();
		}
	}

	private static void asmSystemExitZero(MethodVisitor main) {
		main.visitLdcInsn(0);
		main.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
	}

	private static void asmNoArgConstructor(ClassWriter cw) {
		MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		constructor.visitCode();
		constructor.visitVarInsn(Opcodes.ALOAD, 0);
		constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		constructor.visitInsn(Opcodes.RETURN);
		constructor.visitMaxs(0, 0);
		constructor.visitEnd();
	}

	private static void asmPrintlnJavaVersionProperty(MethodVisitor main) {
		main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		main.visitLdcInsn("java.version");
		main.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty",
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
	}

	private static void asmPrintlnJavaMajorBefore9(MethodVisitor main) {
		main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		main.visitLdcInsn("8");
		main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
	}

	private static void asmPrintlnJavaMajorAfter9(MethodVisitor main) {
		main.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		main.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "version", "()Ljava/lang/Runtime$Version;",
				false);
		main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime$Version", "major", "()I", false);
		main.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
	}

	private static void addJarClass(JarOutputStream jaros, String classname, byte[] classbytes) throws IOException {
		String path = classname.replace('.', '/') + ".class";
		addJarClass(jaros, classbytes, path);
	}

	private static void addJarClass(JarOutputStream jaros, byte[] classbytes, String path) throws IOException {
		JarEntry entry = new JarEntry(path);
		entry.setLastModifiedTime(EPOCH_FILETIME);
		entry.setCreationTime(EPOCH_FILETIME);
		entry.setLastAccessTime(EPOCH_FILETIME);

		jaros.putNextEntry(entry);
		jaros.write(classbytes);
		jaros.closeEntry();
	}

}
