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

import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.options.SimpleAddExportsPath;

class SimpleAddExportsPathTaskOption implements AddExportsPathTaskOption {
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

	public SimpleAddExportsPathTaskOption(JavaAddExports copy) {
		this(copy.getModule(), copy.getPackage(), copy.getTarget());
	}

	public static AddExportsPathTaskOption valueOf(String cmdlineoption) {
		//XXX don't  create an intermediate instance
		return new SimpleAddExportsPathTaskOption(SimpleAddExportsPath.valueOf(cmdlineoption));
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
