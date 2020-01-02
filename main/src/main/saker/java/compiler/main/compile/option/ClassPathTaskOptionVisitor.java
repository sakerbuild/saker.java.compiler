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

import saker.build.file.path.WildcardPath;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

public interface ClassPathTaskOptionVisitor {
	public void visitCompileClassPath(JavaCompilationWorkerTaskIdentifier compiletaskid);

	public void visitFileLocation(FileLocation fileLocation);

	public void visitClassPathReference(ClassPathReference classpath);

	public void visitSDKPath(SDKPathReference sdkpath);

	public void visitWildcard(WildcardPath wildcard);
}
