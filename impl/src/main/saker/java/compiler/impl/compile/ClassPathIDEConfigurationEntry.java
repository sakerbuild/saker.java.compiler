package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.std.api.file.location.FileLocation;

public class ClassPathIDEConfigurationEntry implements Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation fileLocation;
	private Collection<? extends JavaSourceDirectory> sourceDirectories;
	private StructuredTaskResult sourceAttachment;
	private StructuredTaskResult docAttachment;
	private SakerPath sourceGenDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassPathIDEConfigurationEntry() {
	}

	public ClassPathIDEConfigurationEntry(FileLocation fileLocation,
			Collection<? extends JavaSourceDirectory> sourceDirectories, StructuredTaskResult sourceAttachment,
			StructuredTaskResult docAttachment) {
		this.fileLocation = fileLocation;
		this.sourceDirectories = sourceDirectories;
		this.sourceAttachment = sourceAttachment;
		this.docAttachment = docAttachment;
	}

	public ClassPathIDEConfigurationEntry(FileLocation fileLocation) {
		this.fileLocation = fileLocation;
	}

	public FileLocation getFileLocation() {
		return fileLocation;
	}

	public Collection<? extends JavaSourceDirectory> getSourceDirectories() {
		return sourceDirectories;
	}

	public StructuredTaskResult getSourceAttachment() {
		return sourceAttachment;
	}

	public StructuredTaskResult getDocumentationAttachment() {
		return docAttachment;
	}

	public void setSourceGenDirectory(SakerPath sourceGenDirectory) {
		this.sourceGenDirectory = sourceGenDirectory;
	}

	public SakerPath getSourceGenDirectory() {
		return sourceGenDirectory;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileLocation);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
		out.writeObject(sourceAttachment);
		out.writeObject(docAttachment);
		out.writeObject(sourceGenDirectory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		fileLocation = (FileLocation) in.readObject();
		sourceDirectories = SerialUtils.readExternalImmutableLinkedHashSet(in);
		sourceAttachment = (StructuredTaskResult) in.readObject();
		docAttachment = (StructuredTaskResult) in.readObject();
		sourceGenDirectory = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docAttachment == null) ? 0 : docAttachment.hashCode());
		result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
		result = prime * result + ((sourceAttachment == null) ? 0 : sourceAttachment.hashCode());
		result = prime * result + ((sourceDirectories == null) ? 0 : sourceDirectories.hashCode());
		result = prime * result + ((sourceGenDirectory == null) ? 0 : sourceGenDirectory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassPathIDEConfigurationEntry other = (ClassPathIDEConfigurationEntry) obj;
		if (docAttachment == null) {
			if (other.docAttachment != null)
				return false;
		} else if (!docAttachment.equals(other.docAttachment))
			return false;
		if (fileLocation == null) {
			if (other.fileLocation != null)
				return false;
		} else if (!fileLocation.equals(other.fileLocation))
			return false;
		if (sourceAttachment == null) {
			if (other.sourceAttachment != null)
				return false;
		} else if (!sourceAttachment.equals(other.sourceAttachment))
			return false;
		if (sourceDirectories == null) {
			if (other.sourceDirectories != null)
				return false;
		} else if (!sourceDirectories.equals(other.sourceDirectories))
			return false;
		if (sourceGenDirectory == null) {
			if (other.sourceGenDirectory != null)
				return false;
		} else if (!sourceGenDirectory.equals(other.sourceGenDirectory))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (fileLocation != null ? "fileLocation=" + fileLocation + ", " : "")
				+ (sourceDirectories != null ? "sourceDirectories=" + sourceDirectories + ", " : "")
				+ (sourceAttachment != null ? "sourceAttachment=" + sourceAttachment + ", " : "")
				+ (docAttachment != null ? "docAttachment=" + docAttachment : "") + "]";
	}

}
