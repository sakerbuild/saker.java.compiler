/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.CanonicalTypeSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleCanonicalTypeSignature implements CanonicalTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_OBJECT = new SimpleCanonicalTypeSignature(
			"java.lang.Object");
	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_STRING = new SimpleCanonicalTypeSignature(
			"java.lang.String");
	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_ANNOTATION_ANNOTATION = new SimpleCanonicalTypeSignature(
			"java.lang.annotation.Annotation");

	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_DEPRECATED = new SimpleCanonicalTypeSignature(
			"java.lang.Deprecated");
	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_OVERRIDE = new SimpleCanonicalTypeSignature(
			"java.lang.Override");
	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_FUNCTIONALINTERFACE = new SimpleCanonicalTypeSignature(
			"java.lang.FunctionalInterface");
	public static final SimpleCanonicalTypeSignature INSTANCE_JAVA_LANG_SUPPRESSWARNINGS = new SimpleCanonicalTypeSignature(
			"java.lang.SuppressWarnings");

	private static final Map<String, SimpleCanonicalTypeSignature> SIMPLE_CACHE = new TreeMap<>();
	static {
		initSimpleCache(INSTANCE_JAVA_LANG_OBJECT);
		initSimpleCache(INSTANCE_JAVA_LANG_STRING);
		initSimpleCache(INSTANCE_JAVA_LANG_ANNOTATION_ANNOTATION);

		initSimpleCache(INSTANCE_JAVA_LANG_DEPRECATED);
		initSimpleCache(INSTANCE_JAVA_LANG_OVERRIDE);
		initSimpleCache(INSTANCE_JAVA_LANG_FUNCTIONALINTERFACE);
		initSimpleCache(INSTANCE_JAVA_LANG_SUPPRESSWARNINGS);
	}

	private static void initSimpleCache(SimpleCanonicalTypeSignature sig) {
		SIMPLE_CACHE.put(sig.getCanonicalName(), sig);
	}

	protected String canonicalName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleCanonicalTypeSignature() {
	}

	protected SimpleCanonicalTypeSignature(String canonicalName) {
		this.canonicalName = canonicalName;
	}

	public static SimpleCanonicalTypeSignature create(String name) {
		SimpleCanonicalTypeSignature cached = SIMPLE_CACHE.get(name);
		if (cached != null) {
			return cached;
		}
		return new SimpleCanonicalTypeSignature(name);
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public String getSimpleName() {
		return canonicalName;
	}

	@Override
	public String getCanonicalName() {
		return canonicalName;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(canonicalName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		canonicalName = in.readUTF();
	}

	private Object readResolve() {
		return SIMPLE_CACHE.getOrDefault(canonicalName, this);
	}

	@Override
	public String toString() {
		return canonicalName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((canonicalName == null) ? 0 : canonicalName.hashCode());
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
		SimpleCanonicalTypeSignature other = (SimpleCanonicalTypeSignature) obj;
		if (canonicalName == null) {
			if (other.canonicalName != null)
				return false;
		} else if (!canonicalName.equals(other.canonicalName))
			return false;
		return true;
	}

}
