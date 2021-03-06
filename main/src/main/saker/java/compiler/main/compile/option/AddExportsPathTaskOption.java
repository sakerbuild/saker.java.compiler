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
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.main.TaskDocs.AddExportsModuleOption;
import saker.java.compiler.main.TaskDocs.AddExportsPackageOption;
import saker.java.compiler.main.TaskDocs.AddExportsTargetOption;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Describes additional module export definitions to be used during Java compilation with modules.\n"
		+ "The values correspond to the --add-exports javac parameter. The value can be specified either by using a string "
		+ "formatted as module/package=other-module(,other-module)* or by passing the Module, Package, and Target fields. "
		+ "If the target module is omitted, ALL-UNNAMED is used.\n"
		+ "The value accepts configuration using the enclosed fields as well.")
@NestTypeInformation(relatedTypes = @NestTypeUsage(AddExportsModuleOption.class))
@NestFieldInformation(value = "Module",
		type = @NestTypeUsage(AddExportsModuleOption.class),
		info = @NestInformation("Specifies the module that the export definitions should be added to. "
				+ "Corresponds to the module part of the option."))
@NestFieldInformation(value = "Package",
		type = @NestTypeUsage(value = Collection.class, elementTypes = AddExportsPackageOption.class),
		info = @NestInformation("Specifies the packages that should be exported from the given module."))
@NestFieldInformation(value = "Target",
		type = @NestTypeUsage(value = Collection.class, elementTypes = AddExportsTargetOption.class),
		info = @NestInformation("Specifies the target modules of the export addition. If not specified, ALL-UNNAMED is used."))
public interface AddExportsPathTaskOption {
	public default AddExportsPathTaskOption clone() {
		return new SimpleAddExportsPathTaskOption(this);
	}

	@Deprecated
	public JavaAddExports toAddExportsPath(TaskContext taskcontext);

	public String getModule();

	public Collection<String> getPackage();

	public Collection<String> getTarget();

	public static AddExportsPathTaskOption valueOf(String cmdlineoption) {
		return SimpleAddExportsPathTaskOption.valueOf(cmdlineoption);
	}
}
