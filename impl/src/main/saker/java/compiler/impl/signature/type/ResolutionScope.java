package saker.java.compiler.impl.signature.type;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import saker.java.compiler.api.processing.SakerElementsTypes;

public interface ResolutionScope {
	public ResolutionScope getEnclosingScope();

	public Element asElement(SakerElementsTypes elemtypes);

	public TypeElement asType(SakerElementsTypes elemtypes);

	public ExecutableElement asExecutable(SakerElementsTypes elemtypes);

	/**
	 * Returns the resolved {@link TypeElement} or {@link TypeParameterElement}. Null if not found.
	 * 
	 * @param elemtypes
	 * @param simplename
	 * @return
	 */
	public Element resolveType(SakerElementsTypes elemtypes, String simplename);

	public VariableElement resolveVariable(SakerElementsTypes elemtypes, String simplename);

	public TypeSignature resolveTypeSignature(SakerElementsTypes elemtypes, String qualifiedname,
			List<? extends TypeSignature> typeparameters);
}
