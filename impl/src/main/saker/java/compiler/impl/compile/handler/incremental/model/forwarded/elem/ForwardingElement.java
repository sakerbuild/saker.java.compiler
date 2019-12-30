package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingObject;

public interface ForwardingElement<T extends Element> extends ForwardingObject<T>, CommonElement {
}
