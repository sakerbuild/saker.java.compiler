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
