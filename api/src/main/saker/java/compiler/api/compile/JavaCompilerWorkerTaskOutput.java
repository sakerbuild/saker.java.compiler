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
package saker.java.compiler.api.compile;

import java.util.Collection;

import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.modulepath.JavaModulePath;

/**
 * Interface representing the output of the Java compiler worker task.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface JavaCompilerWorkerTaskOutput extends JavaCompilationConfigurationOutput {
	/**
	 * Gets the source directories that were compiled during the operation.
	 * 
	 * @return The source directory configurations.
	 */
	public Collection<JavaSourceDirectory> getSourceDirectories();

	/**
	 * Gets the classpath that was specified as an input to the compilation.
	 * 
	 * @return The classpath. (May be empty or <code>null</code>.)
	 */
	public JavaClassPath getClassPath();

	/**
	 * Gets the modulepath that was specified as an input to the compilation.
	 * 
	 * @return The modulepath. (May be empty or <code>null</code>.)
	 */
	public JavaModulePath getModulePath();

	/**
	 * Gets the ABI version key of the compilation result.
	 * <p>
	 * The ABI version key is an arbitrary object that implements {@link Object#equals(Object)}. It can be used to
	 * compare against a previous ABI version key in order to detect changes in the signature of the compiled classes.
	 * <p>
	 * If a previous version key and the current one doesn't {@linkplain Object#equals(Object) equal}, then the
	 * signature of the classes have been changed. The consumer should invalidate any dependencies that depend on the
	 * signatures of the classes.
	 * <p>
	 * The ABI version key doesn't include the method code in it, and is only derived from the signatures of the
	 * classes. (Including annotations present on the elements.)
	 * 
	 * @return The ABI version key or <code>null</code> if none.
	 */
	public Object getAbiVersionKey();

	/**
	 * Gets the implementation version key of the compilation result.
	 * <p>
	 * The implementation version key is an arbitrary object that implements {@link Object#equals(Object)}. It can be
	 * used to compare against a previous implementation version key in order to detect changes in the signature of the
	 * compiled classes.
	 * <p>
	 * If a previous version key and the current one doesn't {@linkplain Object#equals(Object) equal}, then the
	 * implementation or signature of the classes have been changed. The consumer should invalidate any dependencies
	 * that depend on the implementation or signature of the classes.
	 * <p>
	 * The implementation version key includes all aspects of the compiled classes in it.
	 * 
	 * @return The implementation version key or <code>null</code> if none.
	 */
	public Object getImplementationVersionKey();
}