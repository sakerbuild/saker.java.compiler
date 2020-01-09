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
package saker.java.compiler.impl.processor;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

import javax.annotation.processing.Processor;

import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderUtil;
import saker.build.thirdparty.saker.util.classloader.JarClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.MultiClassLoader;
import saker.build.thirdparty.saker.util.classloader.MultiDataClassLoader;
import saker.build.thirdparty.saker.util.classloader.PathClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.io.JarFileUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.util.cache.CacheKey;
import saker.java.compiler.api.processing.SakerProcessingEnvironment;
import saker.java.compiler.api.processor.ProcessorCreationContext;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.std.api.file.location.LocalFileLocation;

public final class ClassLoaderProcessorCreator implements ProcessorCreator, Externalizable {
	private static final long serialVersionUID = 1L;

	private String className;
	private Set<LocalFileLocation> classPathFileLocations;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassLoaderProcessorCreator() {
	}

	public ClassLoaderProcessorCreator(String className, Set<LocalFileLocation> classPathFileLocations) {
		Objects.requireNonNull(className, "className");
		Objects.requireNonNull(classPathFileLocations, "classPathFileLocations");

		this.className = className;
		this.classPathFileLocations = classPathFileLocations;
	}

	@Override
	public String getName() {
		return className;
	}

	@Override
	public Processor create(ProcessorCreationContext creationcontext) throws Exception {
		SakerEnvironment environment = creationcontext.getEnvironment();
		Constructor<? extends Processor> constructor = environment
				.getCachedData(new ProcessorConstructorCacheKey(environment, className, classPathFileLocations));
		return constructor.newInstance();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(className);
		SerialUtils.writeExternalCollection(out, classPathFileLocations);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = in.readUTF();
		classPathFileLocations = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((classPathFileLocations == null) ? 0 : classPathFileLocations.hashCode());
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
		ClassLoaderProcessorCreator other = (ClassLoaderProcessorCreator) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (classPathFileLocations == null) {
			if (other.classPathFileLocations != null)
				return false;
		} else if (!classPathFileLocations.equals(other.classPathFileLocations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[className=" + className + ", classPathJarFileLocations="
				+ classPathFileLocations + "]";
	}

	private interface ClassLoaderDataFinderCreator extends Closeable {
		public ClassLoaderDataFinder create();
	}

	private static class JarClassLoaderCacheKey
			implements CacheKey<ClassLoaderDataFinder, ClassLoaderDataFinderCreator> {
		private LocalFileLocation fileLocation;

		public JarClassLoaderCacheKey(LocalFileLocation fileLocation) {
			this.fileLocation = fileLocation;
		}

		@Override
		public ClassLoaderDataFinderCreator allocate() throws Exception {
			Path realpath = LocalFileProvider.toRealPath(fileLocation.getLocalPath());
			if (Files.isDirectory(realpath)) {
				return new ClassLoaderDataFinderCreator() {
					@Override
					public void close() throws IOException {
					}

					@Override
					public ClassLoaderDataFinder create() {
						return new PathClassLoaderDataFinder(realpath);
					}
				};
			}
			return new ClassLoaderDataFinderCreator() {
				private final JarFile jarfile = JarFileUtils.createMultiReleaseJarFile(realpath);

				@Override
				public void close() throws IOException {
					jarfile.close();
				}

				@Override
				public ClassLoaderDataFinder create() {
					return new JarClassLoaderDataFinder(jarfile);
				}
			};
		}

		@Override
		public void close(ClassLoaderDataFinder data, ClassLoaderDataFinderCreator resource) throws Exception {
			resource.close();
		}

		@Override
		public ClassLoaderDataFinder generate(ClassLoaderDataFinderCreator resource) throws Exception {
			return resource.create();
		}

		@Override
		public long getExpiry() {
			// 5 min
			return 5 * 60 * 1000;
		}

		@Override
		public boolean validate(ClassLoaderDataFinder data, ClassLoaderDataFinderCreator resource) {
			return true;
		}
	}

	private static class ProcessorConstructorCacheKey implements CacheKey<Constructor<? extends Processor>, Object> {
		private static final ClassLoader PROCESSOR_PARENT_CLASSLOADER = MultiClassLoader.create(Arrays.asList(
				SakerProcessingEnvironment.class.getClassLoader(), ClassLoaderUtil.getPlatformClassLoaderParent()));

		private SakerEnvironment environment;
		private String className;
		private Set<JarClassLoaderCacheKey> cacheKeys;

		public ProcessorConstructorCacheKey(SakerEnvironment environment, String className,
				Set<LocalFileLocation> files) {
			this.environment = environment;
			this.className = className;
			this.cacheKeys = new LinkedHashSet<>();
			for (LocalFileLocation f : files) {
				cacheKeys.add(new JarClassLoaderCacheKey(f));
			}
		}

		@Override
		public Object allocate() throws Exception {
			return new Object();
		}

		@Override
		public void close(Constructor<? extends Processor> data, Object resource) throws Exception {
		}

		@Override
		public Constructor<? extends Processor> generate(Object resource) throws Exception {
			Set<ClassLoaderDataFinder> datafinders = new LinkedHashSet<>();
			for (JarClassLoaderCacheKey ck : cacheKeys) {
				ClassLoaderDataFinder jarcldf = environment.getCachedData(ck);
				datafinders.add(jarcldf);
			}
			MultiDataClassLoader cl = new MultiDataClassLoader(PROCESSOR_PARENT_CLASSLOADER, datafinders);
			return Class.forName(className, false, cl).asSubclass(Processor.class).getConstructor();
		}

		@Override
		public long getExpiry() {
			// 5 min
			return 5 * 60 * 1000;
		}

		@Override
		public boolean validate(Constructor<? extends Processor> data, Object resource) {
			return true;
		}

	}
}