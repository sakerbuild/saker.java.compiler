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
package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.api.classpath.ClassPathVisitor;
import saker.java.compiler.api.classpath.CompilationClassPath;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.modulepath.CompilationModulePath;
import saker.java.compiler.api.modulepath.ModulePathVisitor;
import saker.java.compiler.impl.compile.InternalJavaCompilerOutput;

public class CompilePassClassAndModulePathReferenceOption implements ClassPathReferenceOption,
		ModulePathReferenceOption, CompilationModulePath, CompilationClassPath, Externalizable {
	private static final long serialVersionUID = 1L;

	private JavaCompilationWorkerTaskIdentifier compilePassTaskIdentifier;

	/**
	 * For {@link Externalizable}.
	 */
	public CompilePassClassAndModulePathReferenceOption() {
	}

	public CompilePassClassAndModulePathReferenceOption(InternalJavaCompilerOutput output) {
		JavaCompilationWorkerTaskIdentifier taskid = output.getCompilationTaskIdentifier();
		Objects.requireNonNull(taskid, "java compilation task identifier");
		this.compilePassTaskIdentifier = taskid;
	}

	public CompilePassClassAndModulePathReferenceOption(JavaCompilationWorkerTaskIdentifier compilePassTaskIdentifier) {
		Objects.requireNonNull(compilePassTaskIdentifier, "task id");
		this.compilePassTaskIdentifier = compilePassTaskIdentifier;
	}

	@Override
	public void accept(ClassPathVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(ModulePathVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public JavaCompilationWorkerTaskIdentifier getCompilationWorkerTaskIdentifier() {
		return compilePassTaskIdentifier;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(compilePassTaskIdentifier);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		compilePassTaskIdentifier = (JavaCompilationWorkerTaskIdentifier) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compilePassTaskIdentifier == null) ? 0 : compilePassTaskIdentifier.hashCode());
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
		CompilePassClassAndModulePathReferenceOption other = (CompilePassClassAndModulePathReferenceOption) obj;
		if (compilePassTaskIdentifier == null) {
			if (other.compilePassTaskIdentifier != null)
				return false;
		} else if (!compilePassTaskIdentifier.equals(other.compilePassTaskIdentifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + compilePassTaskIdentifier + "]";
	}

}
