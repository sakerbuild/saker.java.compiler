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
package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalAnnotationMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalDeclaredType;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface IncrementalElementsTypesBase extends SakerElementsTypes {
	public List<TypeMirror> erasure(List<? extends TypeMirror> types);

	public DeclaredType getJavaLangObjectTypeMirror();
	
	public DeclaredType getJavaLangRecordTypeMirror();

	public DeclaredType getJavaLangCloneableTypeMirror();

	public DeclaredType getJavaIoSerializableTypeMirror();

	public TypeElement getJavaLangObjectTypeElement();

	public TypeElement getJavaLangEnumTypeElement();

	public TypeElement getJavaIoSerializableTypeElement();

	public TypeElement getJavaLangCloneableTypeElement();

	public TypeMirror erasureImpl(TypeMirror t);

	public boolean hasSuperType(TypeElement subject, Element base);

	public DeclaredType captureImpl(DeclaredType declaredType);

	public Object getAnnotationValue(Value value, TypeMirror targettype, Element enclosingresolutionelement);

	public <E extends Element> List<? extends E> forwardElements(List<? extends E> list);

	public <E extends Element> List<? extends E> forwardElements(Supplier<List<? extends E>> javaclistsupplier);

	public <E extends Element> E forwardElementElems(Function<Elements, E> javacelementsupplier);

	public <E extends Element> E forwardElement(Supplier<E> javacelementsupplier);

	public VariableElement forwardElement(VariableElement element);

	public ExecutableElement forwardElement(ExecutableElement element);

	public Element forwardElement(Element element);

	public <T extends TypeMirror> List<? extends T> forwardTypes(List<? extends T> list);

	public <T extends TypeMirror> List<? extends T> forwardTypes(Supplier<List<? extends T>> javaclistsupplier);

	public List<? extends TypeMirror> forwardTypeArguments(Supplier<List<? extends TypeMirror>> javaclistsupplier,
			ForwardingDeclaredType enclosingtype);

	public List<? extends TypeMirror> forwardTypeArguments(List<? extends TypeMirror> list,
			ForwardingDeclaredType enclosingtype);

	public <T extends TypeMirror> T forwardType(T mirror, TypeParameterElement correspondingtypeparameter);

	public <T extends TypeMirror> T forwardType(Supplier<T> javacmirrorsupplier,
			TypeParameterElement correspondingtypeparameter);

	public <T extends TypeMirror> T forwardType(Supplier<T> javacmirrorsupplier);

	public TypeMirror forwardType(TypeMirror mirror);

	public AnnotationMirror forward(AnnotationMirror a);

	public AnnotationValue forward(AnnotationValue av);

	public <T> T javac(Supplier<T> function);

	public <T> T javacElements(Function<Elements, T> function);

	public DeclaredType getAnnotationDeclaredType(AnnotationSignature signature, Element enclosingresolutionelement);

	public Map<? extends ExecutableElement, ? extends AnnotationValue> getAnnotationValues(
			IncrementalAnnotationMirror a, boolean includedefaults, Element enclosingresolutionelement);

	@Deprecated
	public default TypeElement getTypeElement(TypeSignature sig) {
		throw new UnsupportedOperationException();
	}

	public String getCanonicalName(TypeSignature signature, Element enclosingelement);

	public TypeElement getTypeElement(TypeSignature sig, Element enclosingelement);

	public TypeMirror getTypeMirror(TypeSignature signature, Element enclosingelement);

	public List<? extends TypeMirror> getDeclaredTypeArguments(IncrementalDeclaredType type);

	public Element getCapturesEnclosingElement();

	public List<TypeMirror> createCaptureWildcardElementBounds(TypeMirror type);

	public DeclaredType getSuperCorrectParameterizedTypeMirror(DeclaredType type, TypeElement supertype);

	public boolean isWildcardConstrainedToSingleType(CommonWildcardType wc);

	public boolean isJavacElementDeprecated(Element elem);

	public TypeElement getTypeElementFromRealElements(String name);

	public Elements getRealElements();

	public PackagesTypesContainer getLocalPackagesTypesContainer();

	public TypeMirror forwardTypeOrNone(Supplier<? extends TypeMirror> javacmirrorsupplier);

	public boolean isJavacElementBridge(ExecutableElement ee);

	public TypeMirror captureTypeParameter(TypeMirror type);

	public Name getJavacTypeBinaryName(TypeElement type);

	public ResolutionScope createResolutionScope(Element resolutionelement);

	public IncrementalElement<?> createRecordComponentElement(FieldSignature m);
}
