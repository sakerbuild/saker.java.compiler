package saker.java.compiler.impl.compile.handler.invoker;

import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;

//see JavaCompilationInvoker
public interface CompilationContextInformation {
	@RMICacheResult
	public String getJavaVersionProperty();

	@RMICacheResult
	public int getCompilerJVMJavaMajorVersion();

	@RMICacheResult
	public String getSourceVersionName();

	public static CompilationContextInformation of(JavaCompilationInvoker invoker) {
		return new CompilationContextInformation() {
			@Override
			public String getSourceVersionName() {
				return invoker.getSourceVersionName();
			}

			@Override
			public String getJavaVersionProperty() {
				return invoker.getJavaVersionProperty();
			}

			@Override
			public int getCompilerJVMJavaMajorVersion() {
				return invoker.getCompilerJVMJavaMajorVersion();
			}
		};
	}
}
