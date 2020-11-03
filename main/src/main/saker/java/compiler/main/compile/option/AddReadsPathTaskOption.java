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
import saker.java.compiler.api.option.JavaAddReads;
import saker.java.compiler.main.TaskDocs.AddExportsModuleOption;
import saker.java.compiler.main.TaskDocs.AddReadsModuleOption;
import saker.java.compiler.main.TaskDocs.AddReadsRequiresOption;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Describes additional module reads definitions to be used during Java compilation with modules.\n"
		+ "The values correspond to the --add-reads javac parameter. The value can be specified either by using a string "
		+ "formatted as module=other-module(,other-module)* or by passing the Module, and Requires fields.")
@NestTypeInformation(relatedTypes = @NestTypeUsage(AddExportsModuleOption.class))
@NestFieldInformation(value = "Module",
		type = @NestTypeUsage(AddReadsModuleOption.class),
		info = @NestInformation("Specifies the module that reads the specified modules."))
@NestFieldInformation(value = "Requires",
		type = @NestTypeUsage(value = Collection.class, elementTypes = AddReadsRequiresOption.class),
		info = @NestInformation("Specifies the modules that are required/read."))
public interface AddReadsPathTaskOption {
	public default AddReadsPathTaskOption clone() {
		return new SimpleAddReadsPathTaskOption(this);
	}

	@Deprecated
	public JavaAddReads toAddReadsPath(TaskContext taskcontext);

	public String getModule();

	public Collection<String> getRequires();

	public static AddReadsPathTaskOption valueOf(String cmdlineoption) {
		return SimpleAddReadsPathTaskOption.valueOf(cmdlineoption);
	}
}
