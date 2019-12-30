package saker.java.compiler.main.compile.option;

import java.util.Collection;
import java.util.regex.Pattern;

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
		+ "The values correspond to the --add-exports javac parameter. The value can be specified either by usin a string "
		+ "formatted as module/package=other-module(,other-module)*. If the target module is omitted, ALL-UNNAMED is used.\n"
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
	public static final Pattern PATTERN_SPLIT_COMMA = Pattern.compile("[,]+");

	public default AddExportsPathTaskOption clone() {
		return new SimpleAddExportsPathTaskOption(this);
	}

	public JavaAddExports toAddExportsPath(TaskContext taskcontext);

	public String getModule();

	public Collection<String> getPackage();

	public Collection<String> getTarget();

	public static AddExportsPathTaskOption valueOf(String cmdlineoption) {
		return SimpleAddExportsPathTaskOption.valueOf(cmdlineoption);
	}
}
