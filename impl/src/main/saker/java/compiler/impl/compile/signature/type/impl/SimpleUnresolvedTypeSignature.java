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
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;

public class SimpleUnresolvedTypeSignature implements UnresolvedTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	//some common instances
	public static final SimpleUnresolvedTypeSignature INSTANCE_OBJECT = new SimpleUnresolvedTypeSignature("Object");
	public static final SimpleUnresolvedTypeSignature INSTANCE_OVERRIDE = new SimpleUnresolvedTypeSignature("Override");
	public static final SimpleUnresolvedTypeSignature INSTANCE_DEPRECATED = new SimpleUnresolvedTypeSignature(
			"Deprecated");
	public static final SimpleUnresolvedTypeSignature INSTANCE_STRING = new SimpleUnresolvedTypeSignature("String");

	public static final SimpleUnresolvedTypeSignature INSTANCE_BOOLEAN = new SimpleUnresolvedTypeSignature("Boolean");
	public static final SimpleUnresolvedTypeSignature INSTANCE_BYTE = new SimpleUnresolvedTypeSignature("Byte");
	public static final SimpleUnresolvedTypeSignature INSTANCE_SHORT = new SimpleUnresolvedTypeSignature("Short");
	public static final SimpleUnresolvedTypeSignature INSTANCE_INTEGER = new SimpleUnresolvedTypeSignature("Integer");
	public static final SimpleUnresolvedTypeSignature INSTANCE_LONG = new SimpleUnresolvedTypeSignature("Long");
	public static final SimpleUnresolvedTypeSignature INSTANCE_FLOAT = new SimpleUnresolvedTypeSignature("Float");
	public static final SimpleUnresolvedTypeSignature INSTANCE_DOUBLE = new SimpleUnresolvedTypeSignature("Double");
	public static final SimpleUnresolvedTypeSignature INSTANCE_VOID = new SimpleUnresolvedTypeSignature("Void");
	public static final SimpleUnresolvedTypeSignature INSTANCE_CHARACTER = new SimpleUnresolvedTypeSignature(
			"Character");

	private static final Map<String, SimpleUnresolvedTypeSignature> SIMPLE_CACHE = new TreeMap<>();
	static {
		initSimpleCache(INSTANCE_OBJECT);
		initSimpleCache(INSTANCE_OVERRIDE);
		initSimpleCache(INSTANCE_DEPRECATED);
		initSimpleCache(INSTANCE_STRING);

		initSimpleCache(INSTANCE_BOOLEAN);
		initSimpleCache(INSTANCE_BYTE);
		initSimpleCache(INSTANCE_SHORT);
		initSimpleCache(INSTANCE_INTEGER);
		initSimpleCache(INSTANCE_LONG);
		initSimpleCache(INSTANCE_FLOAT);
		initSimpleCache(INSTANCE_DOUBLE);
		initSimpleCache(INSTANCE_VOID);
		initSimpleCache(INSTANCE_CHARACTER);
	}

	private static void initSimpleCache(SimpleUnresolvedTypeSignature sig) {
		SIMPLE_CACHE.put(sig.getUnresolvedName(), sig);
	}

	protected String qualifiedName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleUnresolvedTypeSignature() {
	}

	protected SimpleUnresolvedTypeSignature(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public static SimpleUnresolvedTypeSignature create(String qualifiedName) {
		SimpleUnresolvedTypeSignature cached = SIMPLE_CACHE.get(qualifiedName);
		if (cached != null) {
			return cached;
		}
		return new SimpleUnresolvedTypeSignature(qualifiedName);
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getUnresolvedName() {
		return qualifiedName;
	}

	@Override
	public String getSimpleName() {
		return qualifiedName;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(qualifiedName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		qualifiedName = in.readUTF();
	}

	private Object readResolve() {
		return SIMPLE_CACHE.getOrDefault(qualifiedName, this);
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public final int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleUnresolvedTypeSignature other = (SimpleUnresolvedTypeSignature) obj;
		if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return qualifiedName;
	}
}
