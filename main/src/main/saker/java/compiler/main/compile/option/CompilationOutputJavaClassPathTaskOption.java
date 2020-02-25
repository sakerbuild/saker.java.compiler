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
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;

final class CompilationOutputJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final JavaCompilerWorkerTaskOutput output;

	CompilationOutputJavaClassPathTaskOption(JavaCompilerWorkerTaskOutput output) {
		this.output = output;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitCompileClassPath(output.getCompilationTaskIdentifier());
	}
}