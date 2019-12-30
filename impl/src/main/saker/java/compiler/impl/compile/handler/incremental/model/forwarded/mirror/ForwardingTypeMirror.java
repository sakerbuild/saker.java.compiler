package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingObject;

public interface ForwardingTypeMirror<T extends TypeMirror> extends ForwardingObject<T>, CommonTypeMirror {
}
