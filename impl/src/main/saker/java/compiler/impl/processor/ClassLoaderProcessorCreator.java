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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
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
	private Set<LocalFileLocation> classPathJarFileLocations;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassLoaderProcessorCreator() {
	}

	public ClassLoaderProcessorCreator(String className, Set<LocalFileLocation> classPathJarFileLocations) {
		Objects.requireNonNull(className, "className");
		Objects.requireNonNull(classPathJarFileLocations, "classPathJarFileLocations");

		this.className = className;
		this.classPathJarFileLocations = classPathJarFileLocations;
	}

	@Override
	public String getName() {
		return className;
	}

	@Override
	public Processor create(ProcessorCreationContext creationcontext) throws Exception {
		SakerEnvironment environment = creationcontext.getEnvironment();
		Constructor<? extends Processor> constructor = environment
				.getCachedData(new ProcessorConstructorCacheKey(environment, className,
						classPathJarFileLocations));
		return constructor.newInstance();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(className);
		SerialUtils.writeExternalCollection(out, classPathJarFileLocations);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = in.readUTF();
		classPathJarFileLocations = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((classPathJarFileLocations == null) ? 0 : classPathJarFileLocations.hashCode());
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
		if (classPathJarFileLocations == null) {
			if (other.classPathJarFileLocations != null)
				return false;
		} else if (!classPathJarFileLocations.equals(other.classPathJarFileLocations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[className=" + className + ", classPathJarFileLocations="
				+ classPathJarFileLocations + "]";
	}

	private static class JarClassLoaderCacheKey implements CacheKey<JarClassLoaderDataFinder, JarFile> {
		private LocalFileLocation fileLocation;

		public JarClassLoaderCacheKey(LocalFileLocation fileLocation) {
			this.fileLocation = fileLocation;
		}

		@Override
		public JarFile allocate() throws Exception {
			return JarFileUtils.createMultiReleaseJarFile(LocalFileProvider.toRealPath(fileLocation.getLocalPath()));
		}

		@Override
		public void close(JarClassLoaderDataFinder data, JarFile resource) throws Exception {
			resource.close();
		}

		@Override
		public JarClassLoaderDataFinder generate(JarFile resource) throws Exception {
			return new JarClassLoaderDataFinder(resource);
		}

		@Override
		public long getExpiry() {
			// 5 min
			return 5 * 60 * 1000;
		}

		@Override
		public boolean validate(JarClassLoaderDataFinder data, JarFile resource) {
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
				JarClassLoaderDataFinder jarcldf = environment.getCachedData(ck);
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