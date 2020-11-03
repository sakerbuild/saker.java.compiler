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
import saker.java.compiler.api.option.JavaAddReads;
import saker.java.compiler.impl.options.SimpleAddReadsPath;

public class SimpleAddReadsPathTaskOption implements AddReadsPathTaskOption {
	private String module;
	private Collection<String> requires;

	public SimpleAddReadsPathTaskOption(String module, Collection<String> requires) {
		this.module = module;
		this.requires = requires;
	}

	public SimpleAddReadsPathTaskOption(AddReadsPathTaskOption copy) {
		this(copy.getModule(), ObjectUtils.cloneTreeSet(copy.getRequires()));
	}

	public SimpleAddReadsPathTaskOption(JavaAddReads copy) {
		this(copy.getModule(), copy.getRequires());
	}

	public static AddReadsPathTaskOption valueOf(String cmdlineoption) {
		//XXX don't  create an intermediate instance
		return new SimpleAddReadsPathTaskOption(SimpleAddReadsPath.valueOf(cmdlineoption));
	}

	@Override
	public JavaAddReads toAddReadsPath(TaskContext taskcontext) {
		return new SimpleAddReadsPath(module, requires);
	}

	@Override
	public AddReadsPathTaskOption clone() {
		return this;
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public Collection<String> getRequires() {
		return requires;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (module != null ? "module=" + module + ", " : "")
				+ (requires != null ? "requires=" + requires : "") + "]";
	}

}
