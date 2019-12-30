package saker.java.compiler.impl.compat.element;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;

public interface ModuleElementCompat {
	public Element getRealObject();

	public Name getQualifiedName();

	public boolean isOpen();

	public boolean isUnnamed();

	public List<? extends DirectiveCompat> getDirectives();
}
