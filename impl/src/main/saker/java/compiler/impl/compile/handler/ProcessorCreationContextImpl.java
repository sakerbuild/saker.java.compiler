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

import java.io.IOException;
import java.nio.file.Path;

import saker.build.exception.FileMirroringUnavailableException;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerFile;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.TaskContext;
import saker.java.compiler.api.processor.ProcessorCreationContext;

public class ProcessorCreationContextImpl implements ProcessorCreationContext {
	private TaskContext taskContext;

	public ProcessorCreationContextImpl(TaskContext taskContext) {
		this.taskContext = taskContext;
	}

	@Deprecated
	public ExecutionContext getExecutionContext() {
		return taskContext.getExecutionContext();
	}

	/**
	 * @see TaskContext#mirror(SakerFile, DirectoryVisitPredicate)
	 */
	@Deprecated
	public Path mirror(SakerFile file, DirectoryVisitPredicate synchpredicate)
			throws IOException, NullPointerException, FileMirroringUnavailableException {
		return taskContext.mirror(file, synchpredicate);
	}

	/**
	 * @see TaskContext#mirror(SakerFile)
	 */
	@Deprecated
	public Path mirror(SakerFile file) throws IOException, NullPointerException, FileMirroringUnavailableException {
		return mirror(file, DirectoryVisitPredicate.everything());
	}

	@Override
	public SakerEnvironment getEnvironment() {
		return taskContext.getExecutionContext().getEnvironment();
	}

}