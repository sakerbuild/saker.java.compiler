package saker.java.compiler.api.classpath;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.impl.options.ClassPathReferenceOption;
import saker.java.compiler.impl.options.ClassPathReferenceOptionImpl;
import saker.java.compiler.impl.options.CompilePassClassAndModulePathReferenceOption;
import saker.java.compiler.impl.options.FileLocationClassAndModulePathReferenceOption;
import saker.java.compiler.impl.options.SDKClassAndModulePathReferenceOption;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

final class JavaClassPathBuilderImpl implements JavaClassPathBuilder {
	protected Set<ClassPathReferenceOption> classPathReferences = new LinkedHashSet<>();

	public JavaClassPathBuilderImpl() {
	}

	@Override
	public void addClassPath(ClassPathReference classpathref) {
		Objects.requireNonNull(classpathref, "classpath");
		this.classPathReferences.add(new ClassPathReferenceOptionImpl(classpathref));
	}

	@Override
	public void addCompileClassPath(JavaCompilationWorkerTaskIdentifier compilationworkertaskid) {
		Objects.requireNonNull(compilationworkertaskid, "compilation task id");
		this.classPathReferences.add(new CompilePassClassAndModulePathReferenceOption(compilationworkertaskid));
	}

	@Override
	public void addFileClassPath(FileLocation filelocation) {
		Objects.requireNonNull(filelocation, "file location");
		this.classPathReferences.add(new FileLocationClassAndModulePathReferenceOption(filelocation));
	}

	@Override
	public void addSDKClassPath(SDKPathReference sdkpathreference) {
		Objects.requireNonNull(sdkpathreference, "sdk path reference");
		this.classPathReferences.add(new SDKClassAndModulePathReferenceOption(sdkpathreference));
	}

	@Override
	public JavaClassPath build() {
		return new JavaClassPathImpl(classPathReferences);
	}
}
