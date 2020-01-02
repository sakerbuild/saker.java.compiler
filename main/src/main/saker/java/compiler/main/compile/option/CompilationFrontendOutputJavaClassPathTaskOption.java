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
import saker.build.task.TaskDependencyFuture;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerTaskFrontendOutput;
import saker.java.compiler.impl.JavaTaskUtils;

final class CompilationFrontendOutputJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final JavaCompilerTaskFrontendOutput output;

	CompilationFrontendOutputJavaClassPathTaskOption(JavaCompilerTaskFrontendOutput output) {
		this.output = output;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		//TODO this should be done in the visitor instead
		JavaCompilationWorkerTaskIdentifier compiletaskid = output.getTaskIdentifier();
		TaskDependencyFuture<?> depres = taskcontext.getTaskDependencyFuture(compiletaskid);
		//wait for the task in order for the consumer to be able to use getFinished
		depres.get();
		depres.setTaskOutputChangeDetector(JavaTaskUtils.IS_INSTANCE_OF_JAVA_COMPILER_OUTPUT);

		visitor.visitCompileClassPath(compiletaskid);
	}
}