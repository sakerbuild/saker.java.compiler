package saker.java.compiler.impl.compile.handler.info;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public class ClassFileData extends CompiledFileData {
	private static final long serialVersionUID = 1L;

	/**
	 * For Externalizable implementation only.
	 */
	public ClassFileData() {
	}

	public ClassFileData(SakerPath path, ContentDescriptor contentdescriptor, SourceFileData sourceFile,
			String classBinaryName, byte[] abiHash, byte[] implementationHash) {
		super(path, contentdescriptor, sourceFile, classBinaryName, abiHash, implementationHash);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[lastModified=" + fileContent + ", "
				+ (sourceFile != null ? "sourceFile=" + sourceFile : "") + "]";
	}

}