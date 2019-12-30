package saker.java.compiler.util9.impl.compat.element;

import java.util.List;

import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.PackageElement;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.element.ModuleElementCompat;
import saker.java.compiler.impl.compat.element.OpensDirectiveCompat;

public class OpensDirectiveCompatImpl extends BaseDirectiveCompatImpl<OpensDirective> implements OpensDirectiveCompat {

	public OpensDirectiveCompatImpl(OpensDirective real) {
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
