package saker.java.compiler.util9.impl.compat.element;

import javax.lang.model.element.ModuleElement.UsesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.compat.element.UsesDirectiveCompat;

public class UsesDirectiveCompatImpl extends BaseDirectiveCompatImpl<UsesDirective> implements UsesDirectiveCompat {

	public UsesDirectiveCompatImpl(UsesDirective real) {
		super(real);
	}

	@Override
	public TypeElement getService() {
		return real.getService();
	}
}
