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
package saker.java.compiler.api.classpath;

import java.io.Externalizable;
import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredTaskResult;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;
import saker.std.api.file.location.FileLocation;

/**
 * Represents a classpath entry that is enclosed in a {@linkplain ClassPathReference classpath reference}.
 * <p>
 * The classpath entry is the extension point for defining custom classpaths. The entry provides access to the backing
 * file of the classpath, as well to other transitive classpaths, and related meta-data.
 * <p>
 * Clients should implement this interface. When doing so, make sure to adhere to the {@link #hashCode()} and
 * {@link #equals(Object)} contract. Implementations should also implement the {@link Externalizable} interface.
 * 
 * @see ClassPathReference
 * @see JavaClassPath
 * @see ClassPathVisitor
 * @see JavaClassPathBuilder#addClassPath(ClassPathReference)
 */
public interface ClassPathEntry {
	/**
	 * Gets the classpath file location.
	 * <p>
	 * The file may be a JAR, or class directory. The interface doesn't specify a restriction on its nature.
	 * 
	 * @return The location of the classpath file.
	 * @deprecated Use {@link #getInputFile()} instead. Implement {@link ClassPathEntryInputFileVisitor} to perform an
	 *                 appropriate action based on the classpath file type.
	 */
	@Deprecated
	public FileLocation getFileLocation();

	/**
	 * Gets the input file of this classpath entry.
	 * <p>
	 * The input file contains the necessary class files of this classpath entry. The entry may refer to class
	 * directories, or archives, in a configuration dependent way.
	 * <p>
	 * Implement a custom {@link ClassPathEntryInputFileVisitor} to examine the returned input file.
	 * 
	 * @return The input file of the classpath entry.
	 * @see ClassPathEntryInputFile#accept(ClassPathEntryInputFileVisitor)
	 * @since saker.java.compiler 0.8.4
	 */
	public default ClassPathEntryInputFile getInputFile() {
		FileLocation fl = getFileLocation();
		if (fl == null) {
			return null;
		}
		return ClassPathEntryInputFile.create(fl);
	}

	/**
	 * Gets the additional classpaths which are transitively included by this entry.
	 * <p>
	 * The returned collection specifies the transitive classpaths that this entry automatically includes.
	 * <p>
	 * This method has a default implementation that returns <code>null</code>, since saker.java.compiler version 0.8.5.
	 * 
	 * @return The additional classpaths or <code>null</code> if none.
	 */
	public default Collection<? extends ClassPathReference> getAdditionalClassPathReferences() {
		return null;
	}

	/**
	 * Gets the source directories that are associated with the classpath.
	 * <p>
	 * This is usually used to configure IDE projects appropriately. It can be useful to add a classpath as an source
	 * directory to the IDE project rather than a JAR or class directory as it can provide better documentation
	 * assistance to the developer.
	 * <p>
	 * This method has a default implementation that returns <code>null</code>, since saker.java.compiler version 0.8.5.
	 * 
	 * @return The source directories that are directly associated with the classpath or <code>null</code> if none.
	 */
	public default Collection<? extends JavaSourceDirectory> getSourceDirectories() {
		return null;
	}

	/**
	 * Gets a task result object that specifies the source attachment to the classpath.
	 * <p>
	 * The returned structured task result should be a {@link SakerPath} which points to an execution file location or a
	 * {@link FileLocation}.
	 * <p>
	 * This is used to configure IDE project with the sources of a classpath for better code assistance.
	 * 
	 * @return The source attachment task result or <code>null</code> if none.
	 */
	public default StructuredTaskResult getSourceAttachment() {
		return null;
	}

	/**
	 * Gets a task result object that specifies the documentation attachment to the classpath.
	 * <p>
	 * The returned structured task result should be a {@link SakerPath} which points to an execution file location or a
	 * {@link FileLocation}.
	 * <p>
	 * This is used to configure IDE project with the documentation of a classpath for better code assistance.
	 * 
	 * @return The documentation attachment task result or <code>null</code> if none.
	 */
	public default StructuredTaskResult getDocumentationAttachment() {
		return null;
	}

	/**
	 * Gets the display name for this classpath entry.
	 * <p>
	 * The display name is purely for informational purposes, and is generally used in error messages or in the IDE when
	 * IDE configurations are applied. They should be short, human readable, and identify the classpath entry in some
	 * way.
	 * 
	 * @return The display name or <code>null</code> if none.
	 * @since saker.java.compiler 0.8.6
	 */
	public default String getDisplayName() {
		return null;
	}

	/**
	 * Gets the ABI version key of the classpath.
	 * <p>
	 * An ABI version key is an arbitrary object that implements {@link Object#equals(Object)} and defines the current
	 * version of the ABI of the classpath.
	 * <p>
	 * In general, if the ABI version key of a classpath changes, the dependent code needs to be recompiled.
	 * <p>
	 * The ABI version key should include the signatures of the classes available in the (non-transitive) classpath, but
	 * shouldn't include the method implementations and other non-signature code.
	 * <p>
	 * This method has a default implementation that returns <code>null</code>, since saker.java.compiler version 0.8.5.
	 * 
	 * @return The ABI version key or <code>null</code> if none.
	 * @see JavaCompilerWorkerTaskOutput#getAbiVersionKey()
	 */
	public default Object getAbiVersionKey() {
		return null;
	}

	/**
	 * Gets the implementation version key of the classpath.
	 * <p>
	 * An implementation version key is an arbitrary object that implements {@link Object#equals(Object)} and defines
	 * the current version of the implementation of the classpath.
	 * <p>
	 * In general, if the implementation version key of a classpath changes, the dependent code needs to be re-executed
	 * or recompiled.
	 * <p>
	 * The implementation version key should include all aspects of the class files available in the (non-transitive)
	 * classpath.
	 * <p>
	 * This method has a default implementation that returns <code>null</code>, since saker.java.compiler version 0.8.5.
	 * 
	 * @return The implementation version key or <code>null</code> if none.
	 * @see JavaCompilerWorkerTaskOutput#getImplementationVersionKey()
	 */
	public default Object getImplementationVersionKey() {
		return null;
	}

	/**
	 * Checks if the {@linkplain #getFileLocation() file location} of this classpath may be considered static.
	 * <p>
	 * A static classpath is one that doesn't change during the lifetime of the enclosing build environment. The files
	 * of a static classpath will not be attempted to be modified by other agents on the same computer. If the classpath
	 * represents a directory, then the enclosed files in the directory mustn't change. If it is a JAR, or other
	 * archive, then the file itself mustn't change.
	 * <p>
	 * If a classpath is static, that means that the users of the classpath are allowed to load the files of the
	 * classpath directly from its location and doesn't need to copy it elsewhere. As the classpath is opened,
	 * modifications may be blocked to them by the operating system.
	 * <p>
	 * E.g. if a static JAR path is opened by the build environment, and the user attempts to delete, rename, modify, or
	 * otherwise manipulate the JAR, then it may fail, as the build environment loaded it.
	 * <p>
	 * In order to modify static classpaths, the user may need to reload the build environment. If that is distruptive
	 * to the normal workflow, then the classpath shouldn't be considered static.
	 * <p>
	 * An example for static classpaths are classpaths from SDKs, artifacts from repositories, and others. These are
	 * expected to not be modified after they've been published.
	 * <p>
	 * Using static classpaths can improve performance as various tasks may not need to cache them in an off-site
	 * location, but can use them in-place as that doesn't distrupt the workflow.
	 * 
	 * @return <code>true</code> if the classpath is static.
	 * @since saker.java.compiler 0.8.1
	 */
	public default boolean isStaticFile() {
		return false;
	}

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
