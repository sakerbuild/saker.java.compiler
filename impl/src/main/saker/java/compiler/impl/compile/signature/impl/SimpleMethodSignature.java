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
package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.type.impl.AnnotatedCanonicalTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.AnnotatedUnresolvedTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.PrimitiveTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.PrimitiveTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class SimpleMethodSignature extends SimpleVoidMethodSignature {
	private static final long serialVersionUID = 1L;

	//cache some common methods that are annotated with @Override
	private static final Map<TypeSignature, SimpleMethodSignature> CACHE_TOSTRING_PUBLIC = new HashMap<>();
	private static final Map<TypeSignature, SimpleMethodSignature> CACHE_TOSTRING_PUBLIC_FINAL = new HashMap<>();
	private static final Map<TypeSignature, SimpleMethodSignature> CACHE_HASHCODE_PUBLIC = new HashMap<>();
	private static final Map<TypeSignature, SimpleMethodSignature> CACHE_HASHCODE_PUBLIC_FINAL = new HashMap<>();
	static {
		final short publicfinal = ImmutableModifierSet.FLAG_PUBLIC | ImmutableModifierSet.FLAG_FINAL;

		initCache(CACHE_TOSTRING_PUBLIC, ImmutableModifierSet.FLAG_PUBLIC,
				AnnotatedCanonicalTypeSignature.INSTANCE_OVERRIDE_JAVA_LANG_STRING, "toString");
		initCache(CACHE_TOSTRING_PUBLIC, ImmutableModifierSet.FLAG_PUBLIC,
				AnnotatedUnresolvedTypeSignature.INSTANCE_OVERRIDE_STRING, "toString");

		initCache(CACHE_TOSTRING_PUBLIC_FINAL, publicfinal,
				AnnotatedCanonicalTypeSignature.INSTANCE_OVERRIDE_JAVA_LANG_STRING, "toString");
		initCache(CACHE_TOSTRING_PUBLIC_FINAL, publicfinal, AnnotatedUnresolvedTypeSignature.INSTANCE_OVERRIDE_STRING,
				"toString");

		PrimitiveTypeSignature intjavalangoverride = PrimitiveTypeSignatureImpl.create(
				ImmutableUtils.singletonList(SimpleAnnotationSignature.INSTANCE_JAVA_LANG_OVERRIDE), TypeKind.INT);
		PrimitiveTypeSignature intoverride = PrimitiveTypeSignatureImpl
				.create(ImmutableUtils.singletonList(SimpleAnnotationSignature.INSTANCE_OVERRIDE), TypeKind.INT);

		initCache(CACHE_HASHCODE_PUBLIC, ImmutableModifierSet.FLAG_PUBLIC, intjavalangoverride, "hashCode");
		initCache(CACHE_HASHCODE_PUBLIC, ImmutableModifierSet.FLAG_PUBLIC, intoverride, "hashCode");

		initCache(CACHE_HASHCODE_PUBLIC_FINAL, publicfinal, intjavalangoverride, "hashCode");
		initCache(CACHE_HASHCODE_PUBLIC_FINAL, publicfinal, intoverride, "hashCode");
	}

	private static void initCache(Map<TypeSignature, SimpleMethodSignature> cachemap, short modifiers,
			TypeSignature returntype, String name) {
		cachemap.put(returntype, new SimpleMethodSignature(modifiers, Collections.emptyList(), returntype, name));
	}

	protected TypeSignature returnType;
	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleMethodSignature() {
	}

	@Override
	public TypeSignature getReturnType() {
		return returnType;
	}

	protected SimpleMethodSignature(Set<Modifier> modifiers, List<? extends MethodParameterSignature> parameters,
			TypeSignature returnType, String name) {
		super(modifiers, parameters, name);
		this.returnType = returnType;
	}

	protected SimpleMethodSignature(short modifierFlags, List<? extends MethodParameterSignature> parameters,
			TypeSignature returnType, String name) {
		super(modifierFlags, parameters, name);
		this.returnType = returnType;
	}

	private static Map<TypeSignature, SimpleMethodSignature> getCacheMap(short modifiers, String methodname) {
		switch (methodname) {
			case "toString": {
				switch (modifiers) {
					case ImmutableModifierSet.FLAG_PUBLIC:
						return CACHE_TOSTRING_PUBLIC;
					case ImmutableModifierSet.FLAG_PUBLIC | ImmutableModifierSet.FLAG_FINAL:
						return CACHE_TOSTRING_PUBLIC_FINAL;
				}
				break;
			}
			case "hashCode": {
				switch (modifiers) {
					case ImmutableModifierSet.FLAG_PUBLIC:
						return CACHE_HASHCODE_PUBLIC;
					case ImmutableModifierSet.FLAG_PUBLIC | ImmutableModifierSet.FLAG_FINAL:
						return CACHE_HASHCODE_PUBLIC_FINAL;
				}
				break;
			}
		}
		return null;
	}

	public static MethodSignature create(Set<Modifier> modifiers, List<? extends MethodParameterSignature> parameters,
			TypeSignature returnType, String name) {
		if (NoTypeSignatureImpl.getVoid().equals(returnType)) {
			return new SimpleVoidMethodSignature(modifiers, parameters, name);
		}
		if (ObjectUtils.isNullOrEmpty(parameters)) {
			//TODO this caching maybe should be moved to ParserCache?
			short modifierFlags = ImmutableModifierSet.getFlag(modifiers);
			if ((modifierFlags
					& ~(ImmutableModifierSet.FLAGS_ACCESS_MODIFIERS | ImmutableModifierSet.FLAG_FINAL)) == 0) {
				//only access modifiers (and final) are set in the modifiers, attempt caching
				Map<TypeSignature, SimpleMethodSignature> cachemap = getCacheMap(modifierFlags, name);
				if (cachemap != null) {
					SimpleMethodSignature cached = cachemap.get(returnType);
					if (cached != null) {
						return cached;
					}
				}
			}
			return new SimpleMethodSignature(modifierFlags, parameters, returnType, name);
		}
		return new SimpleMethodSignature(modifiers, parameters, returnType, name);
	}

	@Override
	public String getSimpleName() {
		return name;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.METHOD;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return returnType.getAnnotations();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(returnType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		this.returnType = (TypeSignature) in.readObject();
	}

	private Object readResolve() {
		if (ObjectUtils.isNullOrEmpty(parameters)) {
			if ((modifierFlags
					& ~(ImmutableModifierSet.FLAGS_ACCESS_MODIFIERS | ImmutableModifierSet.FLAG_FINAL)) == 0) {
				Map<TypeSignature, SimpleMethodSignature> cachemap = getCacheMap(modifierFlags, name);
				if (cachemap != null) {
					return cachemap.getOrDefault(returnType, this);
				}
			}
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleMethodSignature other = (SimpleMethodSignature) obj;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}
}
