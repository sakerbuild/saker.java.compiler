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
package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilationConfigurationOutput;

public class JavaCompilationConfigFailedTaskFactory implements TaskFactory<JavaCompilationConfigurationOutput>,
		Task<JavaCompilationConfigurationOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private TaskIdentifier compilationTaskId;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaCompilationConfigFailedTaskFactory() {
	}

	public JavaCompilationConfigFailedTaskFactory(TaskIdentifier compilationTaskId) {
		this.compilationTaskId = compilationTaskId;
	}

	@Override
	public JavaCompilationConfigurationOutput run(TaskContext taskcontext) throws Exception {
		//just get the result. it should throw a task execution failure exception right away
		taskcontext.getTaskResult(compilationTaskId);
		//the following assertion shouldn't be reachable, as the task result retrieval must throw
		throw new AssertionError("Compilation failure configuration task reached an unreachable execution point.");
	}

	@Override
	public Task<? extends JavaCompilationConfigurationOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(compilationTaskId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		compilationTaskId = (TaskIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compilationTaskId == null) ? 0 : compilationTaskId.hashCode());
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
		JavaCompilationConfigFailedTaskFactory other = (JavaCompilationConfigFailedTaskFactory) obj;
		if (compilationTaskId == null) {
			if (other.compilationTaskId != null)
				return false;
		} else if (!compilationTaskId.equals(other.compilationTaskId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaCompilationConfigFailedTaskFactory[" + compilationTaskId + "]";
	}
}
