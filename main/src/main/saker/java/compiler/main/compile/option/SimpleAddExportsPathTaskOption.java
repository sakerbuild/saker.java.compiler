/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.main.compile.option;

import java.util.Collection;
import java.util.regex.Pattern;

import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.options.SimpleAddExportsPath;

class SimpleAddExportsPathTaskOption implements AddExportsPathTaskOption {
	private static final Pattern PATTERN_SPLIT_COMMA = Pattern.compile("[,]+");

	private String module;
	private Collection<String> packages;
	private Collection<String> target;

	public SimpleAddExportsPathTaskOption(String module, Collection<String> packages, Collection<String> target) {
		this.module = module;
		this.packages = packages;
		this.target = target;
	}

	public SimpleAddExportsPathTaskOption(AddExportsPathTaskOption copy) {
		this(copy.getModule(), ObjectUtils.cloneTreeSet(copy.getPackage()), ObjectUtils.cloneTreeSet(copy.getTarget()));
	}

	public static AddExportsPathTaskOption valueOf(String cmdlineoption) {
		int slashidx = cmdlineoption.indexOf('/');
		if (slashidx < 0) {
			throw new IllegalArgumentException(
					"Invalid format: " + cmdlineoption + " expected: 'module/package=other-module(,other-module)*'."
							+ " If '=other-module...' is not provided, ALL-UNNAMED is used.");
		}

		int eqidx = cmdlineoption.indexOf('=');
		String module = cmdlineoption.substring(0, slashidx);

		String modulepackage;
		Collection<String> restarget;
		if (eqidx >= 0) {
			modulepackage = cmdlineoption.substring(slashidx + 1, eqidx);
			String[] target = PATTERN_SPLIT_COMMA.split(cmdlineoption.substring(eqidx + 1));
			restarget = ImmutableUtils.makeImmutableNavigableSet(target);
		} else {
			modulepackage = cmdlineoption.substring(slashidx + 1);
			restarget = null;
		}
		return new SimpleAddExportsPathTaskOption(module, ImmutableUtils.singletonNavigableSet(modulepackage),
				restarget);
	}

	@Override
	public JavaAddExports toAddExportsPath(TaskContext taskcontext) {
		return new SimpleAddExportsPath(module, packages, target);
	}

	@Override
	public AddExportsPathTaskOption clone() {
		return this;
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public Collection<String> getPackage() {
		return packages;
	}

	@Override
	public Collection<String> getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (module != null ? "module=" + module + ", " : "")
				+ (packages != null ? "packages=" + packages + ", " : "") + (target != null ? "target=" + target : "")
				+ "]";
	}

}
