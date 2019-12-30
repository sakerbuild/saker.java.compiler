package saker.java.compiler.main.compile.option;

import saker.build.file.path.WildcardPath;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

public interface ClassPathTaskOptionVisitor {
	public void visitCompileClassPath(JavaCompilationWorkerTaskIdentifier compiletaskid);

	public void visitFileLocation(FileLocation fileLocation);

	public void visitClassPathReference(ClassPathReference classpath);

	public void visitSDKPath(SDKPathReference sdkpath);

	public void visitWildcard(WildcardPath wildcard);
}
