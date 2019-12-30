package saker.java.compiler.impl.compile.file;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;

public interface OutputFileObject {
	public ByteArrayRegion getOutputBytes();

	@RMICacheResult
	public SakerPath getFileObjectSakerPath();
}