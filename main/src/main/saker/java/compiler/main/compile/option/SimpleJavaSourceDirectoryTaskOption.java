package saker.java.compiler.main.compile.option;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.thirdparty.saker.util.ObjectUtils;

final class SimpleJavaSourceDirectoryTaskOption implements JavaSourceDirectoryTaskOption {
	private final SakerPath path;
	private final Collection<WildcardPath> files;

	public SimpleJavaSourceDirectoryTaskOption(SakerPath path, Collection<WildcardPath> files) {
		this.path = path;
		this.files = files;
	}

	public SimpleJavaSourceDirectoryTaskOption(JavaSourceDirectoryTaskOption copy) {
		this.path = copy.getDirectory();
		this.files = ObjectUtils.cloneLinkedHashSet(copy.getFiles());
	}

	@Override
	public JavaSourceDirectoryTaskOption clone() {
		return this;
	}

	@Override
	public Collection<WildcardPath> getFiles() {
		return files;
	}

	@Override
	public SakerPath getDirectory() {
		return path;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}

}