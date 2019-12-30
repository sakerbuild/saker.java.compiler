package saker.java.compiler.impl.signature.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.type.NameSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface ModuleSignature extends AnnotatedSignature, DocumentedSignature {
	public enum DirectiveSignatureKind {
		REQUIRES,
		EXPORTS,
		OPENS,
		PROVIDES,
		USES;
	}

	public interface DirectiveSignature extends Signature {
		public DirectiveSignatureKind getKind();

		public default boolean signatureEquals(DirectiveSignature other) {
			if (this.getKind() != other.getKind()) {
				return false;
			}
			return true;
		}
	}

	public interface RequiresDirectiveSignature extends DirectiveSignature {
		public boolean isStatic();

		public boolean isTransitive();

		public NameSignature getDependencyModule();

		@Override
		public default DirectiveSignatureKind getKind() {
			return DirectiveSignatureKind.REQUIRES;
		}

		@Override
		public default boolean signatureEquals(DirectiveSignature other) {
			if (!(other instanceof RequiresDirectiveSignature)) {
				return false;
			}
			return signatureEquals((RequiresDirectiveSignature) other);
		}

		public default boolean signatureEquals(RequiresDirectiveSignature other) {
			if (!DirectiveSignature.super.signatureEquals(other)) {
				return false;
			}
			if (this.isStatic() != other.isStatic()) {
				return false;
			}
			if (this.isTransitive() != other.isTransitive()) {
				return false;
			}
			if (!Objects.equals(this.getDependencyModule(), other.getDependencyModule())) {
				return false;
			}
			return true;
		}
	}

	public interface ExportsDirectiveSignature extends DirectiveSignature {
		public NameSignature getExportsPackage();

		public List<? extends NameSignature> getTargetModules();

		@Override
		public default DirectiveSignatureKind getKind() {
			return DirectiveSignatureKind.EXPORTS;
		}

		@Override
		public default boolean signatureEquals(DirectiveSignature other) {
			if (!(other instanceof ExportsDirectiveSignature)) {
				return false;
			}
			return signatureEquals((ExportsDirectiveSignature) other);
		}

		public default boolean signatureEquals(ExportsDirectiveSignature other) {
			if (!DirectiveSignature.super.signatureEquals(other)) {
				return false;
			}
			if (!Objects.equals(getExportsPackage(), other.getExportsPackage())) {
				return false;
			}
			if (!Objects.equals(getTargetModules(), other.getTargetModules())) {
				return false;
			}
			return true;
		}
	}

	public interface OpensDirectiveSignature extends DirectiveSignature {
		public NameSignature getPackageName();

		public List<? extends NameSignature> getTargetModules();

		@Override
		public default DirectiveSignatureKind getKind() {
			return DirectiveSignatureKind.OPENS;
		}

		@Override
		public default boolean signatureEquals(DirectiveSignature other) {
			if (!(other instanceof OpensDirectiveSignature)) {
				return false;
			}
			return signatureEquals((OpensDirectiveSignature) other);
		}

		public default boolean signatureEquals(OpensDirectiveSignature other) {
			if (!DirectiveSignature.super.signatureEquals(other)) {
				return false;
			}
			if (!Objects.equals(getPackageName(), other.getPackageName())) {
				return false;
			}
			if (!Objects.equals(getTargetModules(), other.getTargetModules())) {
				return false;
			}
			return true;
		}

	}

	public interface ProvidesDirectiveSignature extends DirectiveSignature {
		public TypeSignature getService();

		public List<? extends TypeSignature> getImplementationTypes();

		@Override
		public default DirectiveSignatureKind getKind() {
			return DirectiveSignatureKind.PROVIDES;
		}

		@Override
		public default boolean signatureEquals(DirectiveSignature other) {
			if (!(other instanceof ProvidesDirectiveSignature)) {
				return false;
			}
			return signatureEquals((ProvidesDirectiveSignature) other);
		}

		public default boolean signatureEquals(ProvidesDirectiveSignature other) {
			if (!DirectiveSignature.super.signatureEquals(other)) {
				return false;
			}
			if (!ObjectUtils.objectsEquals(getService(), other.getService(), TypeSignature::signatureEquals)) {
				return false;
			}
			if (!ObjectUtils.collectionOrderedEquals(getImplementationTypes(), other.getImplementationTypes(),
					TypeSignature::signatureEquals)) {
				return false;
			}
			return true;
		}
	}

	public interface UsesDirectiveSignature extends DirectiveSignature {
		public TypeSignature getService();

		@Override
		public default DirectiveSignatureKind getKind() {
			return DirectiveSignatureKind.USES;
		}

		@Override
		public default boolean signatureEquals(DirectiveSignature other) {
			if (!(other instanceof UsesDirectiveSignature)) {
				return false;
			}
			return signatureEquals((UsesDirectiveSignature) other);
		}

		public default boolean signatureEquals(UsesDirectiveSignature other) {
			if (!DirectiveSignature.super.signatureEquals(other)) {
				return false;
			}
			if (!ObjectUtils.objectsEquals(getService(), other.getService(), TypeSignature::signatureEquals)) {
				return false;
			}
			return true;
		}
	}

	public String getName();

	public boolean isOpen();

	public List<? extends DirectiveSignature> getDirectives();

	public default boolean isUnnamed() {
		return getName().isEmpty();
	}

	public static boolean signatureEquals(ModuleSignature first, ModuleSignature other) {
		if (first == null) {
			return other == null;
		}
		if (other == null) {
			return false;
		}
		if (!Objects.equals(first.getName(), other.getName())) {
			return false;
		}
		if (first.isOpen() != other.isOpen()) {
			return false;
		}
		if (!directiveSignaturesEquals(first, other)) {
			return false;
		}
		return true;
	}

	public static boolean directiveSignaturesEquals(ModuleSignature first, ModuleSignature other) {
		List<? extends DirectiveSignature> tdirs = first.getDirectives();
		List<? extends DirectiveSignature> odirs = other.getDirectives();
		if (tdirs.size() != odirs.size()) {
			return false;
		}
		odirs = new ArrayList<>(odirs);
		outer:
		for (DirectiveSignature ds : tdirs) {
			for (Iterator<? extends DirectiveSignature> it = odirs.iterator(); it.hasNext();) {
				DirectiveSignature ods = it.next();
				if (ds.signatureEquals(ods)) {
					it.remove();
					continue outer;
				}
			}
			//same directive not found
			return false;
		}
		return true;
	}
}
