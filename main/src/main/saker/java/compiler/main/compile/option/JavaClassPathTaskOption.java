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

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.path.WildcardPath.ReducedWildcardPath;
import saker.build.task.TaskContext;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.compile.JavaCompilerTaskFrontendOutput;
import saker.java.compiler.impl.compile.InternalJavaCompilerOutput;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;
import saker.std.main.file.option.FileLocationTaskOption;

@NestInformation("Specifies a Java class or module path.\n"
		+ "Accepts paths to JAR or class directories and wildcards of theirs, outputs from Java compilation tasks, "
		+ "class paths from other tasks, and SDK based paths.\n" + "Manifest attributes in JAR files are ignored.")
@NestTypeInformation(relatedTypes = { @NestTypeUsage(SakerPath.class) })
public interface JavaClassPathTaskOption {
	public JavaClassPathTaskOption clone();

	//TODO remove task context argument
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor);

	public static JavaClassPathTaskOption valueOf(String path) {
		return valueOf(WildcardPath.valueOf(path));
	}

	public static JavaClassPathTaskOption valueOf(SakerPath path) {
		return valueOf(WildcardPath.valueOf(path));
	}

	public static JavaClassPathTaskOption valueOf(WildcardPath path) {
		ReducedWildcardPath reduced = path.reduce();
		if (reduced.getWildcard() == null) {
			SakerPath filepath = reduced.getFile();
			if (filepath.isRelative()) {
				return new RelativePathJavaClassPathTaskOption(filepath);
			}
			return new FileLocationJavaClassPathTaskOption(ExecutionFileLocation.create(filepath));
		}
		return new WildcardPathJavaClassPathTaskOption(path);
	}

	public static JavaClassPathTaskOption valueOf(FileCollection filecollection) {
		return new FileCollectionJavaClassPathTaskOption(filecollection);
	}

	public static JavaClassPathTaskOption valueOf(FileLocation filelocation) {
		FileLocationTaskOption.validateFileLocation(filelocation);
		return new FileLocationJavaClassPathTaskOption(filelocation);
	}

	public static JavaClassPathTaskOption valueOf(InternalJavaCompilerOutput output) {
		return new CompilationOutputJavaClassPathTaskOption(output);
	}

	public static JavaClassPathTaskOption valueOf(JavaCompilerTaskFrontendOutput output) {
		return new CompilationFrontendOutputJavaClassPathTaskOption(output);
	}

	public static JavaClassPathTaskOption valueOf(ClassPathReference classpath) {
		return new ReferenceJavaClassPathTaskOption(classpath);
	}

	public static JavaClassPathTaskOption valueOf(SDKPathReference pathreference) {
		return new SDKPathJavaClassPathTaskOption(pathreference);
	}
}
