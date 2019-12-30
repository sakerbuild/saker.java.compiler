package saker.java.compiler.util9.impl.compat.element;

import java.util.List;

import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.PackageElement;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.element.ExportsDirectiveCompat;
import saker.java.compiler.impl.compat.element.ModuleElementCompat;

public class ExportsDirectiveCompatImpl extends BaseDirectiveCompatImpl<ExportsDirective>
		implements ExportsDirectiveCompat {

	public ExportsDirectiveCompatImpl(ExportsDirective real) {
		super(real);
	}

	@Override
	public PackageElement getPackage() {
		return real.getPackage();
	}

	@Override
	public List<? extends ModuleElementCompat> getTargetModules() {
		return JavaTaskUtils.cloneImmutableList(real.getTargetModules(), ModuleElementCompatImpl::new);
	}

}
