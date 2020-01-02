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
package saker.java.compiler.main.sdk;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.java.compiler.impl.sdk.CurrentJavaSDKReferenceEnvironmentProperty;
import saker.java.compiler.impl.sdk.JavaSDKReferenceEnvironmentProperty;
import saker.java.compiler.main.TaskDocs.JavaVersionOption;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.main.TaskDocs.DocSDKDescription;

@NestTaskInformation(returnType = @NestTypeUsage(DocSDKDescription.class))
@NestInformation("Locates a Java Runtime with a given Version and presents it as an SDK.\n"
		+ "The task attempts to locate a Java Runtime that matches the requirements placed via the parameters.\n"
		+ "The returned object can be used as an SDK input to compilation and other tasks. (It can be used "
		+ "as the SDK input for Java in the " + JavaCompilerTaskFactory.TASK_NAME + "() task.)\n"
		+ "Without any parameters, the task will return a reference to the JRE that the build execution is running on. "
		+ "In other cases it will attempt to find a matching JRE by checking the JRE paths in the "
		+ JavaSDKReferenceEnvironmentProperty.INSTALL_LOCATIONS_ENV_PARAMETER_NAME + " environment property. "
		+ "The property holds a semicolon separated paths to the local JRE installations on the execution machine.")

@NestParameterInformation(value = "Version",
		aliases = { "", "Versions" },
		type = @NestTypeUsage(JavaVersionOption.class),
		info = @NestInformation("Specifies the version of the JRE that the task should look for. It will attempt to match the java.version "
				+ "system property of the possible JRE, or the Java release major version number. (In this order)\n"
				+ "If no version is specified, the task will return the reference to the JRE that runs the build execution."))
public class JavaSDKTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;
	
	public static final String TASK_NAME = "saker.java.sdk";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Version", "Versions" })
			public Optional<Collection<String>> versionOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (this.versionOption == null) {
					return EnvironmentSDKDescription.create(CurrentJavaSDKReferenceEnvironmentProperty.INSTANCE);
				}
				Collection<String> versions = this.versionOption.get();
				if (versions == null) {
					taskcontext.abortExecution(new NullPointerException("Version argument is null."));
					return null;
				}
				NavigableSet<String> suitableversions = new TreeSet<>();
				for (String v : versions) {
					if (v == null) {
						continue;
					}
					suitableversions.add(v);
				}
				if (suitableversions.isEmpty()) {
					taskcontext.abortExecution(new IllegalArgumentException("No suitable versions specified."));
					return null;
				}
				return EnvironmentSDKDescription.create(new JavaSDKReferenceEnvironmentProperty(suitableversions));
			}
		};
	}

}
