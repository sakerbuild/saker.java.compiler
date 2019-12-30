package saker.java.compiler.impl.compile.signature.change;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;

public class PackageAnnotationsChangeABIChange implements AbiChange {
	private String packagePath;

	public PackageAnnotationsChangeABIChange(String packagePath) {
		this.packagePath = packagePath;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (usage.isReferencesPackageOrSubPackage(packagePath)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Package annotations changed: " + packagePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packagePath == null) ? 0 : packagePath.hashCode());
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
		PackageAnnotationsChangeABIChange other = (PackageAnnotationsChangeABIChange) obj;
		if (packagePath == null) {
			if (other.packagePath != null)
				return false;
		} else if (!packagePath.equals(other.packagePath))
			return false;
		return true;
	}

}
