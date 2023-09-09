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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.type.TypeKind;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.SimpleAnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.PrimitiveTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class PrimitiveTypeSignatureImpl extends AnnotatedSignatureImpl implements PrimitiveTypeSignature {
	private static final long serialVersionUID = 1L;

	public static final PrimitiveTypeSignature INSTANCE_BOOLEAN = new PrimitiveTypeSignatureImpl(
			Collections.emptyList(), TypeKind.BOOLEAN);
	public static final PrimitiveTypeSignature INSTANCE_BYTE = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.BYTE);
	public static final PrimitiveTypeSignature INSTANCE_SHORT = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.SHORT);
	public static final PrimitiveTypeSignature INSTANCE_INT = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.INT);
	public static final PrimitiveTypeSignature INSTANCE_LONG = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.LONG);
	public static final PrimitiveTypeSignature INSTANCE_CHAR = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.CHAR);
	public static final PrimitiveTypeSignature INSTANCE_FLOAT = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.FLOAT);
	public static final PrimitiveTypeSignature INSTANCE_DOUBLE = new PrimitiveTypeSignatureImpl(Collections.emptyList(),
			TypeKind.DOUBLE);

	private static final EnumMap<TypeKind, PrimitiveTypeSignature> SIMPLE_PRIMTIVE_SIGNATURES = new EnumMap<>(
			TypeKind.class);
	static {
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.BOOLEAN, INSTANCE_BOOLEAN);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.BYTE, INSTANCE_BYTE);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.SHORT, INSTANCE_SHORT);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.INT, INSTANCE_INT);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.LONG, INSTANCE_LONG);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.CHAR, INSTANCE_CHAR);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.FLOAT, INSTANCE_FLOAT);
		SIMPLE_PRIMTIVE_SIGNATURES.put(TypeKind.DOUBLE, INSTANCE_DOUBLE);
	}

	private static final Map<TypeSignature, EnumMap<TypeKind, PrimitiveTypeSignature>> SIMPLE_ANNOTATED_SIGNATURES = new HashMap<>();
	static {
		initAnnotatedCacheMap(SimpleAnnotationSignature.INSTANCE_JAVA_LANG_OVERRIDE);
		initAnnotatedCacheMap(SimpleAnnotationSignature.INSTANCE_OVERRIDE);
		initAnnotatedCacheMap(SimpleAnnotationSignature.INSTANCE_JAVA_LANG_DEPRECATED);
		initAnnotatedCacheMap(SimpleAnnotationSignature.INSTANCE_DEPRECATED);
	}

	private static void initAnnotatedCacheMap(SimpleAnnotationSignature sig) {
		EnumMap<TypeKind, PrimitiveTypeSignature> typemap = new EnumMap<>(TypeKind.class);
		List<SimpleAnnotationSignature> annotlist = ImmutableUtils.singletonList(sig);
		typemap.put(TypeKind.BOOLEAN, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.BOOLEAN));
		typemap.put(TypeKind.BYTE, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.BYTE));
		typemap.put(TypeKind.SHORT, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.SHORT));
		typemap.put(TypeKind.INT, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.INT));
		typemap.put(TypeKind.LONG, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.LONG));
		typemap.put(TypeKind.CHAR, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.CHAR));
		typemap.put(TypeKind.FLOAT, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.FLOAT));
		typemap.put(TypeKind.DOUBLE, new PrimitiveTypeSignatureImpl(annotlist, TypeKind.DOUBLE));
		SIMPLE_ANNOTATED_SIGNATURES.put(sig.getAnnotationType(), typemap);
	}

	private TypeKind typeKind;

	public PrimitiveTypeSignatureImpl() {
	}

	public static PrimitiveTypeSignature create(TypeKind kind) {
		return SIMPLE_PRIMTIVE_SIGNATURES.get(kind);
	}

	public static PrimitiveTypeSignature create(List<? extends AnnotationSignature> annotations, TypeKind kind) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(kind);
		}
		if (annotations.size() == 1) {
			AnnotationSignature annot = annotations.get(0);
			PrimitiveTypeSignature cached = getSimpleAnnotatedCached(kind, annot);
			if (cached != null) {
				return cached;
			}
		}
		return new PrimitiveTypeSignatureImpl(annotations, kind);
	}

	private static PrimitiveTypeSignature getSimpleAnnotatedCached(TypeKind kind, AnnotationSignature annot) {
		if (ObjectUtils.isNullOrEmpty(annot.getValues())) {
			Map<TypeKind, PrimitiveTypeSignature> cachemap = SIMPLE_ANNOTATED_SIGNATURES.get(annot.getAnnotationType());
			return ObjectUtils.getMapValue(cachemap, kind);
		}
		return null;
	}

	private PrimitiveTypeSignatureImpl(List<? extends AnnotationSignature> annotations, TypeKind typeKind) {
		super(annotations);
		this.typeKind = typeKind;
	}

	@Override
	public TypeKind getTypeKind() {
		return typeKind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((typeKind == null) ? 0 : typeKind.hashCode());
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
		PrimitiveTypeSignatureImpl other = (PrimitiveTypeSignatureImpl) obj;
		if (typeKind != other.typeKind)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + Objects.toString(typeKind).toLowerCase(Locale.ENGLISH);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(typeKind);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.typeKind = (TypeKind) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
	}

	private Object readResolve() {
		Collection<? extends AnnotationSignature> annots = getAnnotations();
		if (ObjectUtils.isNullOrEmpty(annots)) {
			return SIMPLE_PRIMTIVE_SIGNATURES.get(this.typeKind);
		}
		if (annots.size() == 1) {
			AnnotationSignature annot = annotations.get(0);
			PrimitiveTypeSignature cached = getSimpleAnnotatedCached(this.typeKind, annot);
			if (cached != null) {
				return cached;
			}
		}
		return this;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return Objects.toString(typeKind).toLowerCase(Locale.ENGLISH);
	}

}
