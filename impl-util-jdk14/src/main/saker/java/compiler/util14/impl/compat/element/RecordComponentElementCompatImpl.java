package saker.java.compiler.util14.impl.compat.element;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.RecordComponentElement;

import saker.java.compiler.impl.compat.element.BaseElementCompatImpl;
import saker.java.compiler.impl.compat.element.RecordComponentElementCompat;

public class RecordComponentElementCompatImpl extends BaseElementCompatImpl<RecordComponentElement>
		implements RecordComponentElementCompat {
	public RecordComponentElementCompatImpl(RecordComponentElement real) {
		super(real);
	}

	@Override
	public ExecutableElement getAccessor() {
		return real.getAccessor();
	}

}
