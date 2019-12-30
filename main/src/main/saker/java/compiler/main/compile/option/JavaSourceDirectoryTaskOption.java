package saker.java.compiler.main.compile.option;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestTypeInformation(relatedTypes = { @NestTypeUsage(SakerPath.class) })
@NestInformation("Source directory containing Java source files.\n"
		+ "The source files should have .java extension and be located under their respective package name subdirectories.\n"
		+ "The Directory and Files fields can also be used to specify or limit the source files.\n"
		+ "A single directory path can be used in place of this type in which case all .java files are used.")
@NestFieldInformation(value = "Directory",
		info = @NestInformation("Path to the source directory.\n"
				+ "The wildcards specified in the Files field are resolved against this path.\n"
				+ "This path is also used to configure IDE projects."),
		type = @NestTypeUsage(SakerPath.class))
@NestFieldInformation(value = "Files",
		info = @NestInformation("Specifies the source files that should be matched.\n"
				+ "The value of the field may be one or multiple wildcards which are used to specify the source files.\n"
				+ "The source files still should have the .java extension nonetheless."),
		type = @NestTypeUsage(value = Collection.class, elementTypes = WildcardPath.class))
public interface JavaSourceDirectoryTaskOption {
	public default JavaSourceDirectoryTaskOption clone() {
		return new SimpleJavaSourceDirectoryTaskOption(this);
	}

	//TODO this should be FileLocation
	public SakerPath getDirectory();

	public Collection<WildcardPath> getFiles();

	public static JavaSourceDirectoryTaskOption valueOf(SakerPath path) {
		return new SimpleJavaSourceDirectoryTaskOption(path, null);
	}

	public static JavaSourceDirectoryTaskOption valueOf(String path) {
		return valueOf(SakerPath.valueOf(path));
	}
}
