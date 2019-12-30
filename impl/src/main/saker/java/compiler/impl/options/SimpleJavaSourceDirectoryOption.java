package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.JavaSourceDirectory;

public final class SimpleJavaSourceDirectoryOption implements JavaSourceDirectory, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath directory;
	private Set<WildcardPath> files;

	public SimpleJavaSourceDirectoryOption() {
	}

	public SimpleJavaSourceDirectoryOption(SakerPath directory, Collection<? extends WildcardPath> files) {
		Objects.requireNonNull(directory, "source directory path");
		if (!directory.isAbsolute()) {
			throw new InvalidPathFormatException("Source directory path is not absolute: " + directory);
		}
		this.directory = directory;
		this.files = ImmutableUtils.makeImmutableNavigableSet(files);
	}

	public SimpleJavaSourceDirectoryOption(JavaSourceDirectory copy) {
		this(copy.getDirectory(), copy.getFiles());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(directory);
		SerialUtils.writeExternalCollection(out, files);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		directory = (SakerPath) in.readObject();
		files = SerialUtils.readExternalImmutableNavigableSet(in);
	}

	@Override
	public SakerPath getDirectory() {
		return directory;
	}

	@Override
	public Set<WildcardPath> getFiles() {
		return files;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((files == null) ? 0 : files.hashCode());
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
		SimpleJavaSourceDirectoryOption other = (SimpleJavaSourceDirectoryOption) obj;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleJavaSourceDirectory [" + (directory != null ? "directory=" + directory + ", " : "")
				+ (files != null ? "files=" + files : "") + "]";
	}

}
