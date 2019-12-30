package saker.java.compiler.api.classpath;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.Set;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.options.ClassPathReferenceOption;

final class JavaClassPathImpl implements JavaClassPath, Externalizable {
	private static final long serialVersionUID = 1L;

	protected Set<ClassPathReferenceOption> classPathReferences;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaClassPathImpl() {
	}

	protected JavaClassPathImpl(Set<? extends ClassPathReferenceOption> classPathReferences) {
		Objects.requireNonNull(classPathReferences, "class path");
		this.classPathReferences = ImmutableUtils.makeImmutableLinkedHashSet(classPathReferences);
	}

	@Override
	public boolean isEmpty() {
		return ObjectUtils.isNullOrEmpty(classPathReferences);
	}

	@Override
	public void accept(ClassPathVisitor visitor) {
		for (ClassPathReferenceOption cpref : classPathReferences) {
			cpref.accept(visitor);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, classPathReferences);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		classPathReferences = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classPathReferences == null) ? 0 : classPathReferences.hashCode());
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
		JavaClassPathImpl other = (JavaClassPathImpl) obj;
		if (classPathReferences == null) {
			if (other.classPathReferences != null)
				return false;
		} else if (!classPathReferences.equals(other.classPathReferences))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + classPathReferences + "]";
	}

}
