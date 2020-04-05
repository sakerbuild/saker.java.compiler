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
package saker.java.compiler.impl.compile.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.identifier.TaskIdentifier;

@Deprecated
//TODO use saker.standard 0.8.2
public class LocalPathFileContentDescriptorExecutionProperty
		implements ExecutionProperty<ContentDescriptor>, Externalizable {
	private static final long serialVersionUID = 1L;

	//associatedTask field is necessary for the incremental behaviours of the build system to work
	private TaskIdentifier associatedTask;
	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalPathFileContentDescriptorExecutionProperty() {
	}

	public LocalPathFileContentDescriptorExecutionProperty(TaskIdentifier associatedTask, SakerPath path) {
		this.associatedTask = associatedTask;
		this.path = path;
	}

	@Override
	public ContentDescriptor getCurrentValue(ExecutionContext executioncontext) {
		try {
			ContentDescriptor result = executioncontext
					.getContentDescriptor(LocalFileProvider.getInstance().getPathKey(path));
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(associatedTask);
		out.writeObject(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		associatedTask = (TaskIdentifier) in.readObject();
		path = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((associatedTask == null) ? 0 : associatedTask.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalPathFileContentDescriptorExecutionProperty other = (LocalPathFileContentDescriptorExecutionProperty) obj;
		if (associatedTask == null) {
			if (other.associatedTask != null)
				return false;
		} else if (!associatedTask.equals(other.associatedTask))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[path=" + path + "]";
	}

}
