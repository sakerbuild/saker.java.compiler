package saker.java.compiler.api.modulepath;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.impl.options.CompilePassClassAndModulePathReferenceOption;
import saker.java.compiler.impl.options.FileLocationClassAndModulePathReferenceOption;
import saker.java.compiler.impl.options.ModulePathReferenceOption;
import saker.java.compiler.impl.options.SDKClassAndModulePathReferenceOption;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

final class JavaModulePathBuilderImpl implements JavaModulePathBuilder {
	protected Set<ModulePathReferenceOption> modulePathReferences = new LinkedHashSet<>();

	public JavaModulePathBuilderImpl() {
	}

	@Override
	public void addCompileModulePath(JavaCompilationWorkerTaskIdentifier compilationworkertaskid) {
		Objects.requireNonNull(compilationworkertaskid, "compilation task id");
		this.modulePathReferences.add(new CompilePassClassAndModulePathReferenceOption(compilationworkertaskid));
	}

	@Override
	public void addFileModulePath(FileLocation filelocation) {
		Objects.requireNonNull(filelocation, "file location");
		this.modulePathReferences.add(new FileLocationClassAndModulePathReferenceOption(filelocation));
	}

	@Override
	public void addSDKModulePath(SDKPathReference sdkpathreference) {
		Objects.requireNonNull(sdkpathreference, "sdk path reference");
		this.modulePathReferences.add(new SDKClassAndModulePathReferenceOption(sdkpathreference));
	}

	@Override
	public JavaModulePath build() {
		return new JavaModulePathImpl(modulePathReferences);
	}
}
