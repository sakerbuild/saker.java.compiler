package saker.java.compiler.impl.compile.handler.invoker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;

public class SakerPathBytes implements Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;
	private ByteArrayRegion bytes;

	public SakerPathBytes() {
	}

	public SakerPath getPath() {
		return path;
	}

	public ByteArrayRegion getBytes() {
		return bytes;
	}

	public SakerPathBytes(SakerPath path, ByteArrayRegion bytes) {
		this.path = path;
		this.bytes = bytes;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(bytes);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		bytes = (ByteArrayRegion) in.readObject();
	}

	@Override
	public String toString() {
		return SakerPathFiles.toRelativeString(path);
	}
}