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
				+ "This path is also used to configure IDE projects.\n"
				+ "This path can also be a wildcard path in which case all matched directories are used."),
		type = @NestTypeUsage(WildcardPath.class))
@NestFieldInformation(value = "Files",
		info = @NestInformation("Specifies the source files that should be matched.\n"
				+ "The value of the field may be one or multiple wildcards which are used to specify the source files.\n"
				+ "The source files should still have the .java extension nonetheless."),
		type = @NestTypeUsage(value = Collection.class, elementTypes = WildcardPath.class))
public interface JavaSourceDirectoryTaskOption {
	public default JavaSourceDirectoryTaskOption clone() {
		return new SimpleJavaSourceDirectoryTaskOption(this);
	}

	//TODO this should be FileLocation
	public WildcardPath getDirectory();

	public Collection<WildcardPath> getFiles();

	public static JavaSourceDirectoryTaskOption valueOf(SakerPath path) {
		return new SimpleJavaSourceDirectoryTaskOption(WildcardPath.valueOf(path), null);
	}

	public static JavaSourceDirectoryTaskOption valueOf(WildcardPath path) {
		return new SimpleJavaSourceDirectoryTaskOption(path, null);
	}

	public static JavaSourceDirectoryTaskOption valueOf(String path) {
		return valueOf(SakerPath.valueOf(path));
	}
}
