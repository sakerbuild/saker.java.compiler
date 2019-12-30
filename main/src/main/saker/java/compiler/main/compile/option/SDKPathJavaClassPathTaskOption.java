package saker.java.compiler.main.compile.option;

import saker.build.task.TaskContext;
import saker.sdk.support.api.SDKPathReference;

final class SDKPathJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final SDKPathReference pathreference;

	SDKPathJavaClassPathTaskOption(SDKPathReference pathreference) {
		this.pathreference = pathreference;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitSDKPath(pathreference);
	}
}