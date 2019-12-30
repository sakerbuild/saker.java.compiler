package saker.java.compiler.impl.compile.handler.info;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public interface FileData {
	//TODO check why the subclasses implement hashcode and equals? should they?

	public FileDataKind getKind();

	public SakerPath getPath();

	public ContentDescriptor getFileContent();
}
