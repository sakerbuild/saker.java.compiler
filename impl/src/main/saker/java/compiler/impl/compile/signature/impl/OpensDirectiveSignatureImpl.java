package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.ModuleSignature.OpensDirectiveSignature;
import saker.java.compiler.impl.signature.type.NameSignature;

public class OpensDirectiveSignatureImpl implements OpensDirectiveSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private NameSignature packageName;
	private List<NameSignature> targetModules;

	/**
	 * For {@link Externalizable}.
	 */
	public OpensDirectiveSignatureImpl() {
	}

	public OpensDirectiveSignatureImpl(NameSignature exportsPackage, List<NameSignature> targetModules) {
		this.packageName = exportsPackage;
		this.targetModules = targetModules;
	}

	@Override
	public NameSignature getPackageName() {
		return packageName;
	}

	@Override
	public List<? extends NameSignature> getTargetModules() {
		return targetModules;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(packageName);
		SerialUtils.writeExternalCollection(out, targetModules);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		packageName = (NameSignature) in.readObject();
		targetModules = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((targetModules == null) ? 0 : targetModules.hashCode());
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
		OpensDirectiveSignatureImpl other = (OpensDirectiveSignatureImpl) obj;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (targetModules == null) {
			if (other.targetModules != null)
				return false;
		} else if (!targetModules.equals(other.targetModules))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "opens " + packageName
				+ (targetModules == null ? "" : " to " + StringUtils.toStringJoin(", ", targetModules));
	}
}
