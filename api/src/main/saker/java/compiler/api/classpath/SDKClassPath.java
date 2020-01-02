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

import java.util.Map;

import saker.java.compiler.api.compile.JavaCompilationTaskBuilder;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;

/**
 * Represents a classpath entry that is backed by a path from and SDK.
 * <p>
 * The SDK path will be resolved against the SDK configuration passed to the Java compiler task.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaClassPath
 * @see ClassPathVisitor
 * @see JavaClassPathBuilder#addSDKClassPath(SDKPathReference)
 * @see SDKDescription
 * @see JavaCompilationTaskBuilder#setSDKs(Map)
 */
public interface SDKClassPath {
	/**
	 * Gets the SDK path reference of this classpath entry.
	 * 
	 * @return The SDK path reference.
	 */
	public SDKPathReference getSDKPathReference();
}
