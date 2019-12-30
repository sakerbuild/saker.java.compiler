package saker.java.compiler.impl.compile.handler.info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public abstract class BasicFileData implements Externalizable, FileData {
	private static final long serialVersionUID = 1L;

	protected SakerPath path;
	protected ContentDescriptor fileContent;

	/**
	 * For Externalizable implementation only.
	 */
	public BasicFileData() {
	}

	public BasicFileData(BasicFileData data) {
		this.path = data.path;
		this.fileContent = data.fileContent;
	}

	public BasicFileData(SakerPath path, ContentDescriptor contentdescriptor) {
		this.path = path;
		this.fileContent = contentdescriptor;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public ContentDescriptor getFileContent() {
		return fileContent;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + "path=" + path + ", " + "fileContent=" + fileContent + "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(fileContent);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		fileContent = (ContentDescriptor) in.readObject();
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicFileData other = (BasicFileData) obj;
		if (fileContent == null) {
			if (other.fileContent != null)
				return false;
		} else if (!fileContent.equals(other.fileContent))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}