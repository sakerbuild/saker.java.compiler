package saker.java.compiler.impl.compile.file;

import javax.tools.FileObject;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;

public interface JavaCompilerFileObject extends FileObject {
	@RMICacheResult
	public SakerPath getFileObjectSakerPath();
}
