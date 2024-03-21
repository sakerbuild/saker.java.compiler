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
import java.util.Objects;
import java.util.TreeMap;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.SimpleAnnotationSignature;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.CanonicalTypeSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public class AnnotatedCanonicalTypeSignature extends AnnotatedSignatureImpl implements CanonicalTypeSignature {
	private static final long serialVersionUID = 1L;

	public static final AnnotatedCanonicalTypeSignature INSTANCE_OVERRIDE_JAVA_LANG_STRING;

	private static final Map<String, AnnotatedCanonicalTypeSignature> CACHE_JAVA_LANG_OVERRIDE;
	static {
		TreeMap<String, AnnotatedCanonicalTypeSignature> javalangoverridecache = new TreeMap<>();
		List<SimpleAnnotationSignature> overrideannotlist = ImmutableUtils
				.singletonList(SimpleAnnotationSignature.INSTANCE_JAVA_LANG_OVERRIDE);
		INSTANCE_OVERRIDE_JAVA_LANG_STRING = initCache(javalangoverridecache, "java.lang.String", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Object", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Byte", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Short", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Integer", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Long", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Float", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Double", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Character", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Boolean", overrideannotlist);
		initCache(javalangoverridecache, "java.lang.Void", overrideannotlist);
		CACHE_JAVA_LANG_OVERRIDE = javalangoverridecache;
	}

	private static AnnotatedCanonicalTypeSignature initCache(Map<String, AnnotatedCanonicalTypeSignature> map,
			String canonicalName, List<? extends AnnotationSignature> annots) {
		AnnotatedCanonicalTypeSignature signature = new AnnotatedCanonicalTypeSignature(annots, canonicalName);
		map.put(canonicalName, signature);
		return signature;
	}

	protected String canonicalName;
	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public AnnotatedCanonicalTypeSignature() {
	}

	protected AnnotatedCanonicalTypeSignature(List<? extends AnnotationSignature> annotations, String canonicalName) {
		super(annotations);
		this.canonicalName = canonicalName;
	}

	public static CanonicalTypeSignature create(List<? extends AnnotationSignature> annotations, String canonicalName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return SimpleCanonicalTypeSignature.create(canonicalName);
		}
		return createOrCached(annotations, canonicalName);
	}

	public static CanonicalTypeSignature create(ParserCache cache, List<? extends AnnotationSignature> annotations,
			String canonicalName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return cache.canonicalTypeSignature(canonicalName);
		}
		return createOrCached(annotations, canonicalName);
	}

	private static CanonicalTypeSignature createOrCached(List<? extends AnnotationSignature> annotations,
			String canonicalName) {
		if (annotations.size() == 1) {
			if (SimpleAnnotationSignature.INSTANCE_JAVA_LANG_OVERRIDE.equals(annotations.get(0))) {
				AnnotatedCanonicalTypeSignature cached = CACHE_JAVA_LANG_OVERRIDE.get(canonicalName);
				if (cached != null) {
					return cached;
				}
			}
		}
		return new AnnotatedCanonicalTypeSignature(annotations, canonicalName);
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return canonicalName.substring(canonicalName.indexOf('.') + 1);
	}

	@Override
	public String getCanonicalName() {
		return canonicalName;
	}

	@Override
	public String getName() {
		return canonicalName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(canonicalName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.canonicalName = (String) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
	}

	private Object readResolve() {
		if (annotations.size() == 1) {
			if (SimpleAnnotationSignature.INSTANCE_JAVA_LANG_OVERRIDE.equals(annotations.get(0))) {
				return CACHE_JAVA_LANG_OVERRIDE.getOrDefault(canonicalName, this);
			}
		}
		return this;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(canonicalName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotatedCanonicalTypeSignature other = (AnnotatedCanonicalTypeSignature) obj;
		if (canonicalName == null) {
			if (other.canonicalName != null)
				return false;
		} else if (!canonicalName.equals(other.canonicalName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + (getEnclosingSignature() == null ? getCanonicalName()
				: getEnclosingSignature().toString() + "." + getSimpleName());
	}
}
