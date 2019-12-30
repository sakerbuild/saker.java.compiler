package saker.java.compiler.util9.impl.compat.element;

import javax.lang.model.element.ModuleElement.RequiresDirective;

import saker.java.compiler.impl.compat.element.ModuleElementCompat;
import saker.java.compiler.impl.compat.element.RequiresDirectiveCompat;

public class RequiresDirectiveCompatImpl extends BaseDirectiveCompatImpl<RequiresDirective>
		implements RequiresDirectiveCompat {

	public RequiresDirectiveCompatImpl(RequiresDirective real) {
		super(real);
	}

	@Override
	public boolean isStatic() {
		return real.isStatic();
	}

	@Override
	public boolean isTransitive() {
		return real.isTransitive();
	}

	@Override
	public ModuleElementCompat getDependency() {
		return new ModuleElementCompatImpl(real.getDependency());
	}

}
