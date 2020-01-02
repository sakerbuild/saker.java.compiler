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

import saker.build.task.TaskContext;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;

final class FileCollectionJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final FileCollection fileCollection;

	FileCollectionJavaClassPathTaskOption(FileCollection fileCollection) {
		this.fileCollection = fileCollection;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		for (FileLocation fl : fileCollection) {
			visitor.visitFileLocation(fl);
		}
	}
}