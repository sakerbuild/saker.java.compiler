package saker.java.compiler.util9.impl.model.elem;

import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignature;
import saker.java.compiler.util9.impl.Java9LanguageUtils;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public abstract class IncrementalDirectiveBase<Sig extends DirectiveSignature>
		implements Directive, IncrementallyModelled {
	protected IncrementalElementsTypes9 elemTypes;
	protected IncrementalModuleElement module;
	protected Sig signature;
	private DirectiveKind kind;

	public IncrementalDirectiveBase(IncrementalElementsTypes9 elemTypes, Sig signature,
			IncrementalModuleElement module) {
		this.elemTypes = elemTypes;
		this.signature = signature;
		this.module = module;
		this.kind = Java9LanguageUtils.toDirectiveKind(signature.getKind());
	}

	@Override
	public DirectiveKind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return signature.toString();
	}
}
