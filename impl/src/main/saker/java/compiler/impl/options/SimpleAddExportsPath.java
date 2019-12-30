package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.option.JavaAddExports;

public class SimpleAddExportsPath implements Externalizable, JavaAddExports {
	private static final long serialVersionUID = 1L;

	private static final Pattern PATTERN_SPLIT_COMMA = Pattern.compile("[,]+");

	private String module;
	private Set<String> packages;
	private Set<String> target;

	public SimpleAddExportsPath() {
	}

	public SimpleAddExportsPath(String module, Collection<String> packages, Collection<String> target) {
		Objects.requireNonNull(module, "add exports module");
		Objects.requireNonNull(packages, "add exports pacakges");
		if (packages.isEmpty()) {
			throw new IllegalArgumentException("No packages specified for add-exports.");
		}
		this.module = module;
		this.packages = ImmutableUtils.makeImmutableNavigableSet(packages);
		this.target = target == null ? Collections.emptySet() : ImmutableUtils.makeImmutableNavigableSet(target);
	}

	public SimpleAddExportsPath(JavaAddExports copy) {
		this(copy.getModule(), copy.getPackage(), copy.getTarget());
	}

	public static SimpleAddExportsPath valueOf(String cmdlineoption) {
		int slashidx = cmdlineoption.indexOf('/');
		if (slashidx < 0) {
			throw new IllegalArgumentException(
					"Invalid format: " + cmdlineoption + " expected: 'module/package=other-module(,other-module)*'."
							+ " If '=other-module...' is not provided, ALL-UNNAMED is used.");
		}

		int eqidx = cmdlineoption.indexOf('=');
		String module = cmdlineoption.substring(0, slashidx);

		String modulepackage;
		NavigableSet<String> restarget;
		if (eqidx >= 0) {
			modulepackage = cmdlineoption.substring(slashidx + 1, eqidx);
			String[] target = PATTERN_SPLIT_COMMA.split(cmdlineoption.substring(eqidx + 1));
			restarget = ImmutableUtils.makeImmutableNavigableSet(target);
		} else {
			modulepackage = cmdlineoption.substring(slashidx + 1);
			restarget = null;
		}
		return new SimpleAddExportsPath(module, ImmutableUtils.singletonNavigableSet(modulepackage), restarget);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(module);
		SerialUtils.writeExternalCollection(out, packages);
		SerialUtils.writeExternalCollection(out, target);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		module = (String) in.readObject();
		packages = SerialUtils.readExternalImmutableLinkedHashSet(in);
		target = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public Set<String> getPackage() {
		return packages;
	}

	@Override
	public Set<String> getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((packages == null) ? 0 : packages.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		SimpleAddExportsPath other = (SimpleAddExportsPath) obj;
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module))
			return false;
		if (packages == null) {
			if (other.packages != null)
				return false;
		} else if (!packages.equals(other.packages))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleAddExportsPath [" + (module != null ? "module=" + module + ", " : "")
				+ (packages != null ? "packages=" + packages + ", " : "") + (target != null ? "target=" + target : "")
				+ "]";
	}

}
