package saker.java.compiler.impl.compile.handler.info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public class GeneratedResourceFileData extends BasicFileData implements ProcessorGeneratedFileData {
	private static final long serialVersionUID = 1L;

	protected GeneratedFileOrigin origins;

	public GeneratedResourceFileData() {
		super();
	}

	public GeneratedResourceFileData(GeneratedResourceFileData data, GeneratedFileOrigin origins) {
		super(data);
		this.origins = origins;
	}

	public GeneratedResourceFileData(SakerPath path, ContentDescriptor contentdescriptor) {
		super(path, contentdescriptor);
	}

	public GeneratedResourceFileData(SakerPath path, ContentDescriptor contentdescriptor, GeneratedFileOrigin origins) {
		super(path, contentdescriptor);
		this.origins = origins;
	}

	@Override
	public FileDataKind getKind() {
		return FileDataKind.RESOURCE;
	}

	@Override
	public GeneratedFileOrigin getOrigin() {
		return origins;
	}

	public void setOrigin(GeneratedFileOrigin origins) {
		this.origins = origins;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(origins);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		origins = (GeneratedFileOrigin) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((origins == null) ? 0 : origins.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneratedResourceFileData other = (GeneratedResourceFileData) obj;
		if (origins == null) {
			if (other.origins != null)
				return false;
		} else if (!origins.equals(other.origins))
			return false;
		return true;
	}

}
