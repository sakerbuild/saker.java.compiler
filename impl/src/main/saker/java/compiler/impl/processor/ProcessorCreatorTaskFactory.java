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
package saker.java.compiler.impl.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.CompileFileTags;
import saker.java.compiler.impl.compile.WorkerJavaCompilerTaskFactoryBase;
import saker.java.compiler.impl.util.ClassPathEntryFileLocationExecutionProperty;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.std.api.file.location.FileLocation;

public final class ProcessorCreatorTaskFactory
		implements TaskFactory<ProcessorCreator>, Task<ProcessorCreator>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private String processorClassName;
	private JavaClassPath classPath;
	private NavigableMap<String, SDKDescription> sdks;

	/**
	 * For {@link Externalizable}.
	 */
	public ProcessorCreatorTaskFactory() {
	}

	public ProcessorCreatorTaskFactory(String processorClassName, JavaClassPath classPath,
			Map<String, SDKDescription> sdks) {
		this.processorClassName = processorClassName;
		this.classPath = classPath;
		if (ObjectUtils.isNullOrEmpty(sdks)) {
			this.sdks = ImmutableUtils.emptyNavigableMap(SDKSupportUtils.getSDKNameComparator());
		} else {
			this.sdks = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
			this.sdks.putAll(sdks);
		}
	}

	@Override
	public ProcessorCreator run(TaskContext taskcontext) throws Exception {
		Map<String, SDKReference> sdkreferences = WorkerJavaCompilerTaskFactoryBase.toSDKReferences(taskcontext, sdks);
		Set<FileLocation> filelocations = collectFileLocations(taskcontext, classPath, sdkreferences);
		return new ClassLoaderProcessorCreator(processorClassName, filelocations);
	}

	private static Set<FileLocation> collectFileLocations(TaskContext taskcontext, JavaClassPath classpath,
			Map<String, SDKReference> sdkreferences) throws IOException {
		return JavaTaskUtils.collectFileLocationsWithImplementationDependencyReporting(taskcontext, classpath,
				CompileFileTags.INPUT_CLASSPATH, sdkreferences, entry -> {
					//we need to add a dependency on the file locations returned from the ClassPathEntry instances
					//if we don't, then the entries can return different file locations while the classpath is still the same
					//in that case this task wouldn't re-run, and the processor creator would fail
					//e.g. if a nest bundle is moved from pending to local, the classpath doesn't change only the file location
					return taskcontext.getTaskUtilities()
							.getReportExecutionDependency(new ClassPathEntryFileLocationExecutionProperty(entry));
				}).keySet();
	}

	@Override
	public Task<? extends ProcessorCreator> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(processorClassName);
		out.writeObject(classPath);
		SerialUtils.writeExternalMap(out, sdks);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		processorClassName = (String) in.readObject();
		classPath = (JavaClassPath) in.readObject();
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classPath == null) ? 0 : classPath.hashCode());
		result = prime * result + ((processorClassName == null) ? 0 : processorClassName.hashCode());
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
		ProcessorCreatorTaskFactory other = (ProcessorCreatorTaskFactory) obj;
		if (classPath == null) {
			if (other.classPath != null)
				return false;
		} else if (!classPath.equals(other.classPath))
			return false;
		if (processorClassName == null) {
			if (other.processorClassName != null)
				return false;
		} else if (!processorClassName.equals(other.processorClassName))
			return false;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (processorClassName != null ? "processorClassName=" + processorClassName + ", " : "")
				+ (classPath != null ? "classPath=" + classPath + ", " : "") + (sdks != null ? "sdks=" + sdks : "")
				+ "]";
	}
}