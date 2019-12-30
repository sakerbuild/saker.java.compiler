package saker.java.compiler.impl.compile.signature.change.member;

import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compile.handler.usage.AbiUsage;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage.FieldABIInfo;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;

public class FieldAddedABIChange implements AbiChange {
	private String classCanonicalName;
	private FieldSignature field;

	private transient ClassSignature enclosingClass;

	public FieldAddedABIChange(ClassSignature enclosingclass, FieldSignature var) {
		this.enclosingClass = enclosingclass;
		this.classCanonicalName = enclosingclass.getCanonicalName();
		this.field = var;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (!AbiChange.isVisibleFrom(usage, field, enclosingClass)) {
			return false;
		}
		Set<Modifier> modifiers = field.getModifiers();
		boolean isstatic = modifiers.contains(Modifier.STATIC);

		String fieldname = field.getSimpleName();
		boolean issimplevisible = (isstatic && usage.hasWildcardStaticImportPath(classCanonicalName))
				|| usage.isInheritesFromClass(classCanonicalName);
		boolean isreferenced = usage.isReferencesField(classCanonicalName, fieldname);
		if (!issimplevisible && !isreferenced) {
			return false;
		}

		if (isstatic) {
			//can only affect constant initializers if the field is static
			for (Entry<FieldABIInfo, ? extends AbiUsage> entry : usage.getFields().entrySet()) {
				FieldABIInfo field = entry.getKey();
				if (field.hasConstantValue()) {
					//field can possibly be a constant value
					AbiUsage entryusage = entry.getValue();
					if (entryusage.isReferencesField(classCanonicalName, fieldname)
							|| (issimplevisible && entryusage.isSimpleVariablePresent(fieldname))) {
						foundchanges.accept(new FieldInitializerABIChange(field));
					}
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "Class field added: " + classCanonicalName + ": " + field;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldAddedABIChange other = (FieldAddedABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!FieldSignature.signatureEquals(field, other.field))
			return false;
		return true;
	}

}
