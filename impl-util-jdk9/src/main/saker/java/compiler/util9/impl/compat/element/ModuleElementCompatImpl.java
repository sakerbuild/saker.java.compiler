package saker.java.compiler.util9.impl.compat.element;

import java.util.List;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.element.BaseElementCompatImpl;
import saker.java.compiler.impl.compat.element.DirectiveCompat;
import saker.java.compiler.impl.compat.element.ModuleElementCompat;

public class ModuleElementCompatImpl extends BaseElementCompatImpl<ModuleElement> implements ModuleElementCompat {
	public ModuleElementCompatImpl(ModuleElement real) {
		super(real);
	}

	@Override
	public Name getQualifiedName() {
		return real.getQualifiedName();
	}

	@Override
	public boolean isOpen() {
		return real.isOpen();
	}

	@Override
	public boolean isUnnamed() {
		return real.isUnnamed();
	}

	@Override
	public List<? extends DirectiveCompat> getDirectives() {
		return JavaTaskUtils.cloneImmutableList(real.getDirectives(),
				DirectiveCompatCreatorVisitor.INSTANCE::toDirectiveCompat);
	}

}
