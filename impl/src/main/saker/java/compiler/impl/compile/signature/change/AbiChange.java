package saker.java.compiler.impl.compile.signature.change;

import java.util.Objects;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compile.handler.usage.AbiUsage;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public interface AbiChange {
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges);

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	public static boolean isVisibleFrom(AbiUsage usage, ClassSignature signature) {
		ClassSignature enclosing = signature.getEnclosingSignature();
		return isVisibleFrom(usage, signature, enclosing, signature.getPackageName());
	}

	public static boolean isVisibleFrom(AbiUsage usage, ClassMemberSignature signature, ClassSignature enclosing) {
		return isVisibleFrom(usage, signature, enclosing, enclosing.getPackageName());
	}

	public static boolean isVisibleFrom(AbiUsage usage, ClassMemberSignature signature, ClassSignature enclosing,
			String packagename) {
		Modifier visibility = IncrementalElementsTypes.getVisibilityOfModifiers(signature.getModifiers());
		while (true) {
			switch (visibility) {
				case PRIVATE: {
					//one of the enclosing signature are private
					//the visibility of the member will be private as it cannot be accessed from the outside
					return false;
				}
				case DEFAULT: {
					//package visibility
					if (!Objects.equals(packagename, usage.getPackageName())) {
						return false;
					}
					break;
				}
				case PROTECTED: {
					//package visibility 
					//or
					//inherits from the enclosing class
					if (!Objects.equals(packagename, usage.getPackageName())
							&& (enclosing == null || !usage.isInheritesFromClass(enclosing.getCanonicalName()))) {
						//enclosing == null check just to be sure if a top level class is not marked protected
						//can that happen if the user writes errorneous code?
						return false;
					}
					break;
				}
				case PUBLIC: {
					break;
				}
				default: {
					throw new IllegalArgumentException("Unknown visibility: " + visibility);
				}
			}
			if (enclosing == null) {
				break;
			}
			visibility = IncrementalElementsTypes.getVisibilityOfModifiers(enclosing.getModifiers());
			enclosing = enclosing.getEnclosingSignature();
		}
		return true;
	}

}
