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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.SimpleAnnotationSignature;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public class AnnotatedUnresolvedTypeSignature extends AnnotatedSignatureImpl
		implements UnresolvedTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final AnnotatedUnresolvedTypeSignature INSTANCE_OVERRIDE_STRING;

	private static final Map<String, AnnotatedUnresolvedTypeSignature> CACHE_OVERRIDE;
	static {
		TreeMap<String, AnnotatedUnresolvedTypeSignature> overridecache = new TreeMap<>();
		List<SimpleAnnotationSignature> overrideannotlist = ImmutableUtils
				.singletonList(SimpleAnnotationSignature.INSTANCE_OVERRIDE);
		INSTANCE_OVERRIDE_STRING = initCache(overridecache, "String", overrideannotlist);
		initCache(overridecache, "Object", overrideannotlist);
		initCache(overridecache, "Byte", overrideannotlist);
		initCache(overridecache, "Short", overrideannotlist);
		initCache(overridecache, "Integer", overrideannotlist);
		initCache(overridecache, "Long", overrideannotlist);
		initCache(overridecache, "Float", overrideannotlist);
		initCache(overridecache, "Double", overrideannotlist);
		initCache(overridecache, "Character", overrideannotlist);
		initCache(overridecache, "Boolean", overrideannotlist);
		initCache(overridecache, "Void", overrideannotlist);
		CACHE_OVERRIDE = overridecache;
	}

	private static AnnotatedUnresolvedTypeSignature initCache(Map<String, AnnotatedUnresolvedTypeSignature> map,
			String canonicalName, List<? extends AnnotationSignature> annots) {
		AnnotatedUnresolvedTypeSignature signature = new AnnotatedUnresolvedTypeSignature(annots, canonicalName);
		map.put(canonicalName, signature);
		return signature;
	}

	protected String qualifiedName;
	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public AnnotatedUnresolvedTypeSignature() {
	}

	protected AnnotatedUnresolvedTypeSignature(List<? extends AnnotationSignature> annotations, String qualifiedName) {
		super(annotations);
		this.qualifiedName = qualifiedName;
	}

	public static UnresolvedTypeSignature create(List<? extends AnnotationSignature> annotations,
			String qualifiedName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return SimpleUnresolvedTypeSignature.create(qualifiedName);
		}
		return createOrCached(annotations, qualifiedName);
	}

	public static UnresolvedTypeSignature create(ParserCache cache, List<? extends AnnotationSignature> annotations,
			String qualifiedName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return cache.unresolved(qualifiedName);
		}
		return createOrCached(annotations, qualifiedName);
	}

	private static AnnotatedUnresolvedTypeSignature createOrCached(List<? extends AnnotationSignature> annotations,
			String qualifiedName) {
		if (annotations.size() == 1) {
			if (SimpleAnnotationSignature.INSTANCE_OVERRIDE.equals(annotations.get(0))) {
				AnnotatedUnresolvedTypeSignature cached = CACHE_OVERRIDE.get(qualifiedName);
				if (cached != null) {
					return cached;
				}
			}
		}
		return new AnnotatedUnresolvedTypeSignature(annotations, qualifiedName);
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public final String getUnresolvedName() {
		return qualifiedName;
	}

	@Override
	public final String getSimpleName() {
		return qualifiedName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(qualifiedName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.qualifiedName = (String) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
	}

	private Object readResolve() {
		if (annotations.size() == 1) {
			if (SimpleAnnotationSignature.INSTANCE_OVERRIDE.equals(annotations.get(0))) {
				return CACHE_OVERRIDE.getOrDefault(qualifiedName, this);
			}
		}
		return this;
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotatedUnresolvedTypeSignature other = (AnnotatedUnresolvedTypeSignature) obj;
		if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ParameterizedTypeSignature enclosing = getEnclosingSignature();
		return super.toString()
				+ (enclosing == null ? getCanonicalName() : enclosing.toString() + "." + getSimpleName());
	}
}
