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
package saker.java.compiler.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.dependencies.CommonTaskOutputChangeDetector;
import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.org.objectweb.asm.ClassReader;
import saker.build.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.build.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.compile.InternalJavaCompilerOutput;
import saker.java.compiler.impl.compile.util.LocalPathFileContentDescriptorExecutionProperty;

public class JavaTaskUtils {
	public static final String EXTENSION_CLASSFILE = "class";
	public static final String EXTENSION_SOURCEFILE = "java";

	public static final Set<String> ALL_UNNAMED_SINGLETON_SET = ImmutableUtils.singletonSet("ALL-UNNAMED");

	public static final TaskOutputChangeDetector IS_INSTANCE_OF_JAVA_COMPILER_OUTPUT = CommonTaskOutputChangeDetector
			.isInstanceOf(InternalJavaCompilerOutput.class);

	public static boolean isPackageInfoSource(String filename) {
		//17 == "package-info".length() + ".java".length()
		if (filename.length() != 17) {
			return false;
		}
		if (!FileUtils.hasExtensionIgnoreCase(filename, "java")) {
			return false;
		}
		if (!filename.startsWith("package-info")) {
			return false;
		}
		return true;
	}

	public static boolean isModuleInfoSource(String filename) {
		//16 == "module-info".length() + ".java".length()
		if (filename.length() != 16) {
			return false;
		}
		if (!FileUtils.hasExtensionIgnoreCase(filename, "java")) {
			return false;
		}
		if (!filename.startsWith("module-info")) {
			return false;
		}
		return true;
	}

	//TODO we might not need this at all, since dependency handles were introduced to TaskResultResolver
	public static TaskIdentifier createJavaCompilationConfigurationOutputTaskIdentifier(
			JavaCompilationWorkerTaskIdentifier workertaskid) {
		return new JavaCompilationOutputTaskIdentifier(workertaskid);
	}

	private static class JavaCompilationOutputTaskIdentifier implements TaskIdentifier, Externalizable {
		private static final long serialVersionUID = 1L;

		private JavaCompilationWorkerTaskIdentifier workerTaskId;

		/**
		 * For {@link Externalizable}.
		 */
		public JavaCompilationOutputTaskIdentifier() {
		}

		public JavaCompilationOutputTaskIdentifier(JavaCompilationWorkerTaskIdentifier workerTaskId) {
			this.workerTaskId = workerTaskId;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(workerTaskId);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			workerTaskId = (JavaCompilationWorkerTaskIdentifier) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((workerTaskId == null) ? 0 : workerTaskId.hashCode());
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
			JavaCompilationOutputTaskIdentifier other = (JavaCompilationOutputTaskIdentifier) obj;
			if (workerTaskId == null) {
				if (other.workerTaskId != null)
					return false;
			} else if (!workerTaskId.equals(other.workerTaskId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "JavaCompilationOutputTaskIdentifier[" + (workerTaskId != null ? "workerTaskId=" + workerTaskId : "")
					+ "]";
		}
	}

	public static NavigableSet<String> makeImmutableIgnoreCaseNullableStringCollection(Collection<String> strings) {
		if (strings == null) {
			return null;
		}
		return ImmutableUtils.makeImmutableNavigableSet(strings, getIgnoreCaseNullableComparator());
	}

	public static NavigableSet<String> emptyImmutableIgnoreCaseNullableStringCollection() {
		return ImmutableUtils.emptyNavigableSet(getIgnoreCaseNullableComparator());
	}

	public static Comparator<? super String> getIgnoreCaseNullableComparator() {
		return StringUtils::compareStringsNullFirstIgnoreCase;
	}

	public static NavigableSet<String> newIgnoreCaseNullableStringCollection(Collection<String> strings) {
		TreeSet<String> result = new TreeSet<>(getIgnoreCaseNullableComparator());
		if (strings != null) {
			result.addAll(strings);
		}
		return result;
	}

	public static Collection<String> toAddExportsCommandLineStrings(JavaAddExports addexports) {
		return SakerJavaCompilerUtils.toAddExportsCommandLineStrings(addexports);
	}

	public static class LocalDirectoryClassFilesExecutionProperty
			implements ExecutionProperty<LocalDirectoryClassFilesExecutionProperty.PropertyValue>, Externalizable {
		private static final long serialVersionUID = 1L;

		public static class PropertyValue implements Externalizable {
			private static final long serialVersionUID = 1L;

			//TODO use MultiPathContentDescriptor
			private NavigableMap<SakerPath, ContentDescriptor> contents;

			/**
			 * For {@link Externalizable}.
			 */
			public PropertyValue() {
			}

			public PropertyValue(NavigableMap<SakerPath, ContentDescriptor> contents) {
				this.contents = contents;
			}

			public NavigableMap<SakerPath, ? extends ContentDescriptor> getContents() {
				return contents;
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				SerialUtils.writeExternalMap(out, contents);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				contents = SerialUtils.readExternalImmutableNavigableMap(in);
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((contents == null) ? 0 : contents.hashCode());
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
				PropertyValue other = (PropertyValue) obj;
				if (!ObjectUtils.mapOrderedEquals(this.contents, other.contents)) {
					return false;
				}
				return true;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "[" + (contents != null ? "contents=" + contents : "") + "]";
			}

		}

		private TaskIdentifier associatedTaskId;
		private SakerPath path;

		/**
		 * For {@link Externalizable}.
		 */
		public LocalDirectoryClassFilesExecutionProperty() {
		}

		public LocalDirectoryClassFilesExecutionProperty(TaskIdentifier associatedTaskId, SakerPath path) {
			this.associatedTaskId = associatedTaskId;
			this.path = path;
		}

		@Override
		public PropertyValue getCurrentValue(ExecutionContext executioncontext) throws Exception {
			NavigableMap<SakerPath, ContentDescriptor> result = new TreeMap<>();
			for (Entry<SakerPath, ? extends FileEntry> entry : LocalFileProvider.getInstance()
					.getDirectoryEntriesRecursively(path).entrySet()) {
				if (!entry.getValue().isRegularFile()) {
					continue;
				}
				SakerPath keypath = entry.getKey();
				if (!StringUtils.endsWithIgnoreCase(keypath.getFileName(), "." + EXTENSION_CLASSFILE)) {
					//not a class file
					continue;
				}
				SakerPath cpabspath = path.resolve(keypath);
				ContentDescriptor classfilecd = executioncontext.getExecutionPropertyCurrentValue(
						new LocalPathFileContentDescriptorExecutionProperty(associatedTaskId, cpabspath));
				if (classfilecd == null) {
					continue;
				}
				result.put(cpabspath, classfilecd);
			}
			return new PropertyValue(ImmutableUtils.unmodifiableNavigableMap(result));
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(associatedTaskId);
			out.writeObject(path);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			associatedTaskId = (TaskIdentifier) in.readObject();
			path = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((associatedTaskId == null) ? 0 : associatedTaskId.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
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
			LocalDirectoryClassFilesExecutionProperty other = (LocalDirectoryClassFilesExecutionProperty) obj;
			if (associatedTaskId == null) {
				if (other.associatedTaskId != null)
					return false;
			} else if (!associatedTaskId.equals(other.associatedTaskId))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + path + "]";
		}
	}

	public static String getModuleInfoModuleName(ByteArrayRegion moduleinfocontents) {
		ClassReader cr = new ClassReader(moduleinfocontents.getArray(), moduleinfocontents.getOffset(),
				moduleinfocontents.getLength());
		String[] result = { null };
		cr.accept(new ClassVisitor(Opcodes.ASM7) {
			@Override
			public ModuleVisitor visitModule(String name, int access, String version) {
				result[0] = name;
				return null;
			}
		}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		return result[0];
	}

	public static <E, T> List<T> cloneImmutableList(Collection<? extends E> source,
			Function<? super E, ? extends T> cloner) {
		if (source == null) {
			return null;
		}
		int s = source.size();
		if (s <= 0) {
			return Collections.emptyList();
		}
		if (s == 1) {
			return ImmutableUtils.singletonList(cloner.apply(source.iterator().next()));
		}
		Object[] elems = new Object[s];
		int i = 0;
		for (E e : source) {
			elems[i++] = cloner.apply(e);
		}
		@SuppressWarnings("unchecked")
		List<T> result = ImmutableUtils.unmodifiableArrayList((T[]) elems);
		return result;
	}

	public static <E, T> Set<T> cloneImmutableHashSet(Collection<? extends E> source,
			Function<? super E, ? extends T> cloner) {
		if (source == null) {
			return null;
		}
		int s = source.size();
		if (s <= 0) {
			return Collections.emptySet();
		}
		if (s == 1) {
			return ImmutableUtils.singletonSet(cloner.apply(source.iterator().next()));
		}
		Set<T> result = new HashSet<>();
		for (E e : source) {
			result.add(cloner.apply(e));
		}
		return ImmutableUtils.unmodifiableSet(result);
	}

	public static <E, T> Set<T> cloneImmutableLinkedHashSet(Collection<? extends E> source,
			Function<? super E, ? extends T> cloner) {
		if (source == null) {
			return null;
		}
		int s = source.size();
		if (s <= 0) {
			return Collections.emptySet();
		}
		if (s == 1) {
			return ImmutableUtils.singletonSet(cloner.apply(source.iterator().next()));
		}
		Set<T> result = new LinkedHashSet<>();
		for (E e : source) {
			result.add(cloner.apply(e));
		}
		return ImmutableUtils.unmodifiableSet(result);
	}

	private JavaTaskUtils() {
		throw new UnsupportedOperationException();
	}
}
