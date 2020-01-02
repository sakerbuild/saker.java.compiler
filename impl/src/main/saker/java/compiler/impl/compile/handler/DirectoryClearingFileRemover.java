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
package saker.java.compiler.impl.compile.handler;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;

public class DirectoryClearingFileRemover {
	//TODO maybe store the SakerDirectory instances and operate on those instead?
	private ConcurrentSkipListSet<SakerPath> removedPathDirectories = new ConcurrentSkipListSet<>();

	public void remove(SakerPath path, SakerFile file) {
		if (path.isRelative()) {
			return;
		}
		file.remove();
		removedPathDirectories.add(path.getParent());
	}

	public void remove(SakerFile file) {
		this.remove(file.getSakerPath(), file);
	}

	public void clearDirectories(ExecutionContext executioncontext, SakerPath directory) {
		NavigableSet<SakerPath> subdirs = SakerPathFiles.getPathSubSetDirectoryChildren(removedPathDirectories,
				directory, false);
		SakerPath first;
		while ((first = subdirs.pollFirst()) != null) {
			SakerDirectory dir = SakerPathFiles.resolveDirectoryAtAbsolutePath(executioncontext, first);
			if (dir == null) {
				continue;
			}
			if (!dir.isEmpty()) {
				continue;
			}
			dir.remove();
			removedPathDirectories.add(first.getParent());
		}
	}
}
