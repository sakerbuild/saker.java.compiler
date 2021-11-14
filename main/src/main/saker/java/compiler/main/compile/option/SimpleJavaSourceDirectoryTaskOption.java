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

import saker.build.file.path.WildcardPath;
import saker.build.thirdparty.saker.util.ObjectUtils;

final class SimpleJavaSourceDirectoryTaskOption implements JavaSourceDirectoryTaskOption {
	private final WildcardPath path;
	private final Collection<WildcardPath> files;

	public SimpleJavaSourceDirectoryTaskOption(WildcardPath path, Collection<WildcardPath> files) {
		this.path = path;
		this.files = files;
	}

	public SimpleJavaSourceDirectoryTaskOption(JavaSourceDirectoryTaskOption copy) {
		this.path = copy.getDirectory();
		this.files = ObjectUtils.cloneLinkedHashSet(copy.getFiles());
	}

	@Override
	public JavaSourceDirectoryTaskOption clone() {
		return this;
	}

	@Override
	public Collection<WildcardPath> getFiles() {
		return files;
	}

	@Override
	public WildcardPath getDirectory() {
		return path;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}

}