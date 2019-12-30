package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.KindBasedElementVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingAnnotatedConstruct;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public abstract class ForwardingElementBase<E extends Element> extends ForwardingAnnotatedConstruct<E>
		implements ForwardingElement<E> {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, TypeMirror.class, "asType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, Element> ARFU_enclosingElement = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, Element.class, "enclosingElement");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, List> ARFU_enclosedElements = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, List.class, "enclosedElements");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, ElementKind> ARFU_elementKind = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, ElementKind.class, "elementKind");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, Set> ARFU_modifiers = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, Set.class, "modifiers");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, Name> ARFU_simpleName = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, Name.class, "simpleName");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingElementBase, Boolean> ARFU_deprecated = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingElementBase.class, Boolean.class, "deprecated");

	private volatile transient TypeMirror asType;
	private volatile transient Element enclosingElement;
	private volatile transient List<? extends Element> enclosedElements;
	private volatile transient ElementKind elementKind;
	private volatile transient Set<Modifier> modifiers;
	private volatile transient Name simpleName;
	private volatile transient Boolean deprecated;

	public ForwardingElementBase(IncrementalElementsTypesBase elemTypes, E subject) {
		super(elemTypes, subject);
	}

	public void setElementKind(ElementKind elementKind) {
		this.elementKind = elementKind;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return KindBasedElementVisitor.visit(getKind(), this, v, p);
	}

	@Override
	public final TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype != null) {
			return thisastype;
		}
		thisastype = elemTypes.forwardType(subject::asType);
		if (ARFU_asType.compareAndSet(this, null, thisastype)) {
			return thisastype;
		}
		return this.asType;
	}

	@Override
	public final Element getEnclosingElement() {
		Element thisenclosingelement = this.enclosingElement;
		if (thisenclosingelement != null) {
			return thisenclosingelement;
		}
		thisenclosingelement = elemTypes.forwardElement(subject::getEnclosingElement);
		if (ARFU_enclosingElement.compareAndSet(this, null, thisenclosingelement)) {
			return thisenclosingelement;
		}
		return this.enclosingElement;
	}

	@Override
	public final List<? extends Element> getEnclosedElements() {
		List<? extends Element> thisenclosedelements = enclosedElements;
		if (thisenclosedelements != null) {
			return thisenclosedelements;
		}
		thisenclosedelements = elemTypes.forwardElements(subject::getEnclosedElements);
		if (ARFU_enclosedElements.compareAndSet(this, null, thisenclosedelements)) {
			return thisenclosedelements;
		}
		return this.enclosedElements;
	}

	@Override
	public final ElementKind getKind() {
		ElementKind thiselementkind = this.elementKind;
		if (thiselementkind != null) {
			return thiselementkind;
		}
		thiselementkind = elemTypes.javac(subject::getKind);
		if (ARFU_elementKind.compareAndSet(this, null, thiselementkind)) {
			return thiselementkind;
		}
		return this.elementKind;
	}

	@Override
	public final Set<Modifier> getModifiers() {
		Set<Modifier> thismodifiers = this.modifiers;
		if (thismodifiers != null) {
			return thismodifiers;
		}
		thismodifiers = ImmutableModifierSet.get(elemTypes.javac(subject::getModifiers));
		if (ARFU_modifiers.compareAndSet(this, null, thismodifiers)) {
			return thismodifiers;
		}
		return this.modifiers;
	}

	@Override
	public final Name getSimpleName() {
		Name thissimplename = this.simpleName;
		if (thissimplename != null) {
			return thissimplename;
		}
		thissimplename = elemTypes.javac(subject::getSimpleName);
		if (ARFU_simpleName.compareAndSet(this, null, thissimplename)) {
			return thissimplename;
		}
		return this.simpleName;
	}

	@Override
	public final boolean isDeprecated() {
		Boolean thisdeprecated = this.deprecated;
		if (thisdeprecated != null) {
			return thisdeprecated;
		}
		thisdeprecated = elemTypes.isJavacElementDeprecated(subject);
		if (ARFU_deprecated.compareAndSet(this, null, thisdeprecated)) {
			return thisdeprecated;
		}
		return this.deprecated;
	}

	@Override
	public final E getForwardedSubject() {
		return subject;
	}

}
