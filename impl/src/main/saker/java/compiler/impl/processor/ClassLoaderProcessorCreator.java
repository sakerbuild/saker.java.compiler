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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.processing.Processor;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderUtil;
import saker.build.thirdparty.saker.util.classloader.JarClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.MultiClassLoader;
import saker.build.thirdparty.saker.util.classloader.MultiDataClassLoader;
import saker.build.thirdparty.saker.util.classloader.PathClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.io.ResourceCloser;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.processing.SakerProcessingEnvironment;
import saker.java.compiler.api.processor.ProcessorCreationContext;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.compile.handler.ProcessorCreationContextImpl;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;

public final class ClassLoaderProcessorCreator implements ProcessorCreator, Externalizable {
	private static final long serialVersionUID = 1L;

	private transient String className;
	private transient Set<FileLocation> fileLocations;

	/**
	 * An UUID that identifies this processor version instance. It is newly generated every time the processor creating
	 * task is run.
	 */
	private UUID changedUUID;

	private transient volatile Supplier<Processor> supplier;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassLoaderProcessorCreator() {
	}

	public ClassLoaderProcessorCreator(String className, Set<FileLocation> fileLocations) {
		Objects.requireNonNull(className, "className");
		Objects.requireNonNull(fileLocations, "fileLocations");

		this.className = className;
		this.fileLocations = fileLocations;
		this.changedUUID = UUID.randomUUID();
	}

	@Override
	public String getName() {
		return className;
	}

	@Override
	public Processor create(ProcessorCreationContext creationcontext) throws Exception {
		Supplier<Processor> supp = this.supplier;
		if (supp != null) {
			return supp.get();
		}
		//TODO REWORK this implementation to cache the classpath in the bundle storage and use cached data to properly close the JARs
		ProcessorCreationContextImpl contextimpl = (ProcessorCreationContextImpl) creationcontext;
		synchronized (this) {
			if (this.supplier == null) {
				Collection<ClassLoaderDataFinder> datafinders = new ArrayList<>(fileLocations.size());
				try (ResourceCloser closer = new ResourceCloser()) {
					for (FileLocation filelocation : fileLocations) {
						//XXX delay the jar opening until a class or file is accessed from it
						filelocation.accept(new FileLocationVisitor() {
							@Override
							public void visit(ExecutionFileLocation loc) {
								SakerFile file = SakerPathFiles.resolveAtPath(contextimpl.getExecutionContext(), null,
										loc.getPath());
								try {
									if (file == null) {
										throw new FileNotFoundException(
												"Processor class path file not found: " + loc.getPath());
									}
									Path mirrorpath = contextimpl.mirror(file);
									ClassLoaderDataFinder clfinder;
									if (file instanceof SakerDirectory) {
										clfinder = new PathClassLoaderDataFinder(mirrorpath);
									} else {
										clfinder = new JarClassLoaderDataFinder(mirrorpath);
									}
									datafinders.add(clfinder);
								} catch (IOException e) {
									throw ObjectUtils.sneakyThrow(e);
								}
							}

							@Override
							public void visit(LocalFileLocation loc) {
								SakerPath path = loc.getLocalPath();
								try {
									Path realpath = LocalFileProvider.toRealPath(path);
									FileEntry attrs = LocalFileProvider.getInstance().getFileAttributes(realpath);
									ClassLoaderDataFinder clfinder;
									if (attrs.isDirectory()) {
										clfinder = new PathClassLoaderDataFinder(realpath);
									} else {
										clfinder = new JarClassLoaderDataFinder(realpath);
									}
									datafinders.add(clfinder);
								} catch (IOException e) {
									throw ObjectUtils.sneakyThrow(e);
								}
							}
						});
					}
					//TODO use environment caching for the class loader and the files and jars
					// if someone wants to unpack to the file we use as classloader, then some invalidation should go through

					//the processor parent classloader is the API bundle classloader
					//  so they can access the API classes of the incremental compiler
					//the platform classloader is also added, so the javac classes are also available to the processor
					ClassLoader parentcl = SakerProcessingEnvironment.class.getClassLoader();
					try {
						ClassLoader cl = new MultiDataClassLoader(
								MultiClassLoader.create(
										Arrays.asList(parentcl, ClassLoaderUtil.getPlatformClassLoaderParent())),
								datafinders);
						Class<? extends Processor> procclass = Class.forName(className, false, cl)
								.asSubclass(Processor.class);
						//XXX reify exceptions
						supplier = () -> {
							try {
								return ReflectUtils.newInstance(procclass);
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
									| InvocationTargetException | NoSuchMethodException | SecurityException e) {
								throw new IllegalArgumentException("Failed to instantiate Processor: " + className, e);
							}
						};
					} catch (ClassNotFoundException e) {
						throw new ClassNotFoundException(
								"Processor class not found: " + className + " in " + datafinders, e);
					} catch (ClassCastException e) {
						throw new IllegalArgumentException(
								"Class doesn't implement " + Processor.class.getName() + ": " + className, e);
					}
					closer.clearWithoutClosing();
				}
			}
		}
		return this.supplier.get();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(className);
		SerialUtils.writeExternalCollection(out, fileLocations);
		out.writeObject(changedUUID);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = in.readUTF();
		fileLocations = SerialUtils.readExternalImmutableLinkedHashSet(in);
		changedUUID = (UUID) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changedUUID == null) ? 0 : changedUUID.hashCode());
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
		if (changedUUID == null) {
			if (other.changedUUID != null)
				return false;
		} else if (!changedUUID.equals(other.changedUUID))
			return false;
		return true;
	}

}