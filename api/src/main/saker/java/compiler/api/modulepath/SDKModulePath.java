package saker.java.compiler.api.modulepath;

import java.util.Map;

import saker.java.compiler.api.compile.JavaCompilationTaskBuilder;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;

/**
 * Represents a modulepath entry that is backed by a path from and SDK.
 * <p>
 * The SDK path will be resolved against the SDK configuration passed to the Java compiler task.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaModulePath
 * @see ModulePathVisitor
 * @see JavaModulePathBuilder#addSDKModulePath(SDKPathReference)
 * @see SDKDescription
 * @see JavaCompilationTaskBuilder#setSDKs(Map)
 */
public interface SDKModulePath {
	/**
	 * Gets the SDK path reference of this modulepath entry.
	 * 
	 * @return The SDK path reference.
	 */
	public SDKPathReference getSDKPathReference();
}
