package saker.java.compiler.impl.compat.element;

import java.util.List;

import javax.lang.model.element.PackageElement;

public interface ExportsDirectiveCompat extends DirectiveCompat {
	@Override
	public default <R, P> R accept(DirectiveVisitorCompat<R, P> v, P p) {
		return v.visitExports(this, p);
	}

	public PackageElement getPackage();

	public List<? extends ModuleElementCompat> getTargetModules();
}
