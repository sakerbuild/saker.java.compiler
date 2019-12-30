package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.DocumentedSignature;

public interface DocumentedIncrementalElement<Sig extends AnnotatedSignature & DocumentedSignature>
		extends DocumentedElement<Sig> {

}
