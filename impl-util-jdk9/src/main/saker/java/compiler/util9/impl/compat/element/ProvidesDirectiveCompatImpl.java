package saker.java.compiler.util9.impl.compat.element;

import java.util.List;

import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.compat.element.ProvidesDirectiveCompat;

public class ProvidesDirectiveCompatImpl extends BaseDirectiveCompatImpl<ProvidesDirective>
		implements ProvidesDirectiveCompat {

	public ProvidesDirectiveCompatImpl(ProvidesDirective real) {
		super(real);
	}

	@Override
	public TypeElement getService() {
		return real.getService();
	}

	@Override
	public List<? extends TypeElement> getImplementations() {
		return real.getImplementations();
	}
}
