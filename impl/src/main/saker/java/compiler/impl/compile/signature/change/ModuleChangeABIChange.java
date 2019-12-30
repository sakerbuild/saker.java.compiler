package saker.java.compiler.impl.compile.signature.change;

import java.util.function.Consumer;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;

public class ModuleChangeABIChange implements AbiChange {
	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		//a module ABI change results in recompilation of all files
		// change in a module declaration can cause new errors to surface, e.g. a package is no longer visible
		return true;
	}

	@Override
	public String toString() {
		return "Compilation module changed";
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return ObjectUtils.isSameClass(this, o);
	}
}
