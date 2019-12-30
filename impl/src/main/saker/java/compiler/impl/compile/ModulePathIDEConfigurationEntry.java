package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.std.api.file.location.FileLocation;

public class ModulePathIDEConfigurationEntry implements Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation fileLocation;
	private Collection<? extends JavaSourceDirectory> sourceDirectories;
	private StructuredTaskResult sourceAttachment;

	/**
	 * For {@link Externalizable}.
	 */
	public ModulePathIDEConfigurationEntry() {
	}

	public ModulePathIDEConfigurationEntry(FileLocation fileLocation) {
		this.fileLocation = fileLocation;
	}

	public FileLocation getFileLocation() {
		return fileLocation;
	}

	public void setSourceAttachment(StructuredTaskResult sourceAttachment) {
		this.sourceAttachment = sourceAttachment;
	}

	public StructuredTaskResult getSourceAttachment() {
		return sourceAttachment;
	}

	public void setSourceDirectories(Collection<? extends JavaSourceDirectory> sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}

	public Collection<? extends JavaSourceDirectory> getSourceDirectories() {
		return sourceDirectories;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileLocation);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
		out.writeObject(sourceAttachment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		fileLocation = (FileLocation) in.readObject();
		sourceDirectories = SerialUtils.readExternalImmutableLinkedHashSet(in);
		sourceAttachment = (StructuredTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
		result = prime * result + ((sourceAttachment == null) ? 0 : sourceAttachment.hashCode());
		result = prime * result + ((sourceDirectories == null) ? 0 : sourceDirectories.hashCode());
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
		ModulePathIDEConfigurationEntry other = (ModulePathIDEConfigurationEntry) obj;
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
		return true;
	}

}
