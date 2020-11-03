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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.dependencies.RecursiveIgnoreCaseExtensionFileCollectionStrategy;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.JavaDebugInfoType;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.api.option.JavaAddReads;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.options.OutputBytecodeManipulationOption;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.ResolvedSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKDescriptionVisitor;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.UserSDKDescription;
import saker.std.api.environment.qualifier.AnyEnvironmentQualifier;
import saker.std.api.environment.qualifier.EnvironmentQualifier;
import saker.std.api.environment.qualifier.EnvironmentQualifierVisitor;
import saker.std.api.environment.qualifier.PropertyEnvironmentQualifier;

public abstract class WorkerJavaCompilerTaskFactoryBase
		implements TaskFactory<InternalJavaCompilerOutput>, Task<InternalJavaCompilerOutput>, Externalizable {
	private static final NavigableSet<String> ALL_DEBUG_INFO_TYPES = ImmutableUtils.makeImmutableNavigableSet(
			new String[] { JavaDebugInfoType.lines, JavaDebugInfoType.source, JavaDebugInfoType.vars },
			JavaTaskUtils.getIgnoreCaseNullableComparator());
	private static final Set<String> NONE_DEBUG_INFO_TYPES = ImmutableUtils.makeImmutableNavigableSet(
			new String[] { JavaDebugInfoType.none }, JavaTaskUtils.getIgnoreCaseNullableComparator());

	private static final long serialVersionUID = 1L;

	protected Set<JavaSourceDirectory> sourceDirectories;

	protected JavaClassPath classPath;
	protected JavaModulePath modulePath;
	protected JavaClassPath bootClassPath;

	protected List<String> parameters;
	protected String sourceVersionName;
	protected String targetVersionName;

	protected NavigableMap<String, SDKDescription> sdks;

	protected Collection<JavaAnnotationProcessor> annotationProcessors;
	protected NavigableMap<String, SakerPath> processorInputLocations;
	protected NavigableMap<String, String> annotationProcessorOptions;

	protected OutputBytecodeManipulationOption bytecodeManipulation = new OutputBytecodeManipulationOption();
	protected Set<JavaAddExports> addExports;
	protected Set<JavaAddReads> addReads;

	protected boolean generateNativeHeaders;

	protected boolean parameterNames;

	protected NavigableSet<String> debugInfo;

	/**
	 * For {@link Externalizable}.
	 */
	public WorkerJavaCompilerTaskFactoryBase() {
	}

	@Override
	public Task<? extends InternalJavaCompilerOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	public void setSourceDirectories(Set<JavaSourceDirectory> sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}

	public void setClassPath(JavaClassPath classPath) {
		this.classPath = classPath;
	}

	public void setBootClassPath(JavaClassPath bootClassPath) {
		this.bootClassPath = bootClassPath;
	}

	public void setModulePath(JavaModulePath modulePath) {
		this.modulePath = modulePath;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public void setSourceVersionName(String sourceVersionName) {
		this.sourceVersionName = sourceVersionName;
	}

	public void setTargetVersionName(String targetVersionName) {
		this.targetVersionName = targetVersionName;
	}

	public void setAnnotationProcessors(Collection<JavaAnnotationProcessor> annotationProcessors) {
		this.annotationProcessors = annotationProcessors;
	}

	public void setProcessorInputLocations(NavigableMap<String, SakerPath> processorInputLocations) {
		this.processorInputLocations = processorInputLocations;
	}

	public void setAnnotationProcessorOptions(NavigableMap<String, String> annotationProcessorOptions) {
		this.annotationProcessorOptions = annotationProcessorOptions;
	}

	public void setBytecodeManipulation(OutputBytecodeManipulationOption bytecodeManipulation) {
		this.bytecodeManipulation = bytecodeManipulation;
	}

	public void setAddExports(Set<JavaAddExports> addExports) {
		this.addExports = addExports;
	}

	public void setAddReads(Set<JavaAddReads> addReads) {
		this.addReads = addReads;
	}

	public void setGenerateNativeHeaders(boolean generateNativeHeaders) {
		this.generateNativeHeaders = generateNativeHeaders;
	}

	public void setSDKs(NavigableMap<String, SDKDescription> sdks) {
		this.sdks = sdks;
	}

	public void setParameterNames(boolean parameterNames) {
		this.parameterNames = parameterNames;
	}

	/**
	 * @param debugInfo
	 *            Must use ignore-case comparison.
	 */
	public void setDebugInfo(NavigableSet<String> debugInfo) {
		//recreate the set so the equality checks of the task factory doesn't depend on the casing of the infos
		if (debugInfo != null) {
			if (NONE_DEBUG_INFO_TYPES.equals(debugInfo)) {
				this.debugInfo = JavaTaskUtils.newIgnoreCaseNullableStringCollection(null);
			} else if (debugInfo.contains(JavaDebugInfoType.all)) {
				this.debugInfo = ALL_DEBUG_INFO_TYPES;
			} else {
				NavigableSet<String> ncoll = JavaTaskUtils.newIgnoreCaseNullableStringCollection(null);
				for (String dinfo : debugInfo) {
					if (dinfo == null) {
						continue;
					}
					String lc = dinfo.toLowerCase(Locale.ENGLISH);
					if (JavaDebugInfoType.none.equals(lc)) {
						//don't include none
						//if it is specified alongside other types, then those take preference
						continue;
					}
					ncoll.add(lc);
				}
				this.debugInfo = ncoll;
			}
		} else {
			this.debugInfo = debugInfo;
		}
	}

	protected static Set<FileCollectionStrategy> createSorceFileCollectionStrategies(
			Collection<? extends JavaSourceDirectory> sourcedirs) {
		Set<FileCollectionStrategy> sourceadditiondependencies = new HashSet<>();
		for (JavaSourceDirectory srcdir : sourcedirs) {
			SakerPath srcdirsakerpath = srcdir.getDirectory();
			Objects.requireNonNull(srcdirsakerpath, "No Directory specified for sources.");

			Collection<? extends WildcardPath> files = srcdir.getFiles();
			if (files == null) {
				FileCollectionStrategy dep = RecursiveIgnoreCaseExtensionFileCollectionStrategy.create(srcdirsakerpath,
						"." + JavaTaskUtils.EXTENSION_SOURCEFILE);
				sourceadditiondependencies.add(dep);
			} else {
				if (files.isEmpty()) {
					SakerLog.warning().println("No source files specified for directory: " + srcdirsakerpath);
				} else {
					for (WildcardPath wcpath : files) {
						FileCollectionStrategy dep = WildcardFileCollectionStrategy.create(srcdirsakerpath, wcpath);
						sourceadditiondependencies.add(dep);
					}
				}
			}
		}
		return sourceadditiondependencies;
	}

	public static NavigableMap<String, SDKReference> toSDKReferences(TaskContext taskcontext,
			Map<String, SDKDescription> sdks) {
		if (ObjectUtils.isNullOrEmpty(sdks)) {
			return ImmutableUtils.emptyNavigableMap(SDKSupportUtils.getSDKNameComparator());
		}
		TreeMap<String, SDKReference> result = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
		for (Entry<String, SDKDescription> entry : sdks.entrySet()) {
			SDKReference ref = toSDKReference(taskcontext, entry.getValue());
			SDKReference prev = result.put(entry.getKey(), ref);
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate SDKs with name: " + entry.getKey());
			}
		}
		return result;
	}

	protected static SDKReference toSDKReference(TaskContext taskcontext, SDKDescription sdkdescription) {
		SDKReference[] result = { null };
		sdkdescription.accept(new SDKDescriptionVisitor() {
			@Override
			public void visit(EnvironmentSDKDescription description) {
				EnvironmentProperty<? extends SDKReference> envproperty = SDKSupportUtils
						.getEnvironmentSDKDescriptionReferenceEnvironmentProperty(description);
				result[0] = taskcontext.getTaskUtilities().getReportEnvironmentDependency(envproperty);
			}

			@Override
			public void visit(ResolvedSDKDescription description) {
				result[0] = description.getSDKReference();
			}

			@Override
			public void visit(UserSDKDescription description) {
				EnvironmentQualifier qualifier = description.getQualifier();
				if (qualifier != null) {
					qualifier.accept(new EnvironmentQualifierVisitor() {
						@Override
						public void visit(PropertyEnvironmentQualifier qualifier) {
							EnvironmentProperty<?> envproperty = qualifier.getEnvironmentProperty();
							Object currentval = taskcontext.getTaskUtilities()
									.getReportEnvironmentDependency(envproperty);
							Object expectedvalue = qualifier.getExpectedValue();
							if (!Objects.equals(currentval, expectedvalue)) {
								throw new IllegalArgumentException(
										"Unsuitable environment, user SDK qualifier mismatch: " + currentval + " - "
												+ expectedvalue + " for property: " + envproperty);
							}
						}

						@Override
						public void visit(AnyEnvironmentQualifier qualifier) {
						}
					});
				}
				result[0] = UserSDKDescription.createSDKReference(description.getPaths(), description.getProperties());
			}
		});
		return result[0];
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sourceVersionName);
		out.writeObject(targetVersionName);
		out.writeObject(bytecodeManipulation);
		out.writeObject(classPath);
		out.writeObject(bootClassPath);
		out.writeObject(modulePath);
		out.writeBoolean(parameterNames);

		SerialUtils.writeExternalMap(out, annotationProcessorOptions);
		SerialUtils.writeExternalCollection(out, parameters);
		SerialUtils.writeExternalCollection(out, annotationProcessors);
		SerialUtils.writeExternalCollection(out, addExports);
		SerialUtils.writeExternalCollection(out, addReads);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
		SerialUtils.writeExternalCollection(out, debugInfo);

		SerialUtils.writeExternalMap(out, processorInputLocations);
		SerialUtils.writeExternalMap(out, sdks);

		out.writeBoolean(generateNativeHeaders);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sourceVersionName = SerialUtils.readExternalObject(in);
		targetVersionName = SerialUtils.readExternalObject(in);
		bytecodeManipulation = SerialUtils.readExternalObject(in);
		classPath = SerialUtils.readExternalObject(in);
		bootClassPath = SerialUtils.readExternalObject(in);
		modulePath = SerialUtils.readExternalObject(in);
		parameterNames = in.readBoolean();

		annotationProcessorOptions = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		parameters = SerialUtils.readExternalImmutableList(in);
		annotationProcessors = SerialUtils.readExternalImmutableHashSet(in);
		addExports = SerialUtils.readExternalImmutableHashSet(in);
		addReads = SerialUtils.readExternalImmutableHashSet(in);
		sourceDirectories = SerialUtils.readExternalImmutableLinkedHashSet(in);
		debugInfo = SerialUtils.readExternalImmutableNavigableSet(in, JavaTaskUtils.getIgnoreCaseNullableComparator());

		processorInputLocations = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());

		generateNativeHeaders = in.readBoolean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addExports == null) ? 0 : addExports.hashCode());
		result = prime * result + ((addReads == null) ? 0 : addReads.hashCode());
		result = prime * result + ((annotationProcessorOptions == null) ? 0 : annotationProcessorOptions.hashCode());
		result = prime * result + ((annotationProcessors == null) ? 0 : annotationProcessors.hashCode());
		result = prime * result + ((bootClassPath == null) ? 0 : bootClassPath.hashCode());
		result = prime * result + ((bytecodeManipulation == null) ? 0 : bytecodeManipulation.hashCode());
		result = prime * result + ((classPath == null) ? 0 : classPath.hashCode());
		result = prime * result + ((debugInfo == null) ? 0 : debugInfo.hashCode());
		result = prime * result + (generateNativeHeaders ? 1231 : 1237);
		result = prime * result + ((modulePath == null) ? 0 : modulePath.hashCode());
		result = prime * result + (parameterNames ? 1231 : 1237);
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((processorInputLocations == null) ? 0 : processorInputLocations.hashCode());
		result = prime * result + ((sdks == null) ? 0 : sdks.hashCode());
		result = prime * result + ((sourceDirectories == null) ? 0 : sourceDirectories.hashCode());
		result = prime * result + ((sourceVersionName == null) ? 0 : sourceVersionName.hashCode());
		result = prime * result + ((targetVersionName == null) ? 0 : targetVersionName.hashCode());
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
		WorkerJavaCompilerTaskFactoryBase other = (WorkerJavaCompilerTaskFactoryBase) obj;
		if (addExports == null) {
			if (other.addExports != null)
				return false;
		} else if (!addExports.equals(other.addExports))
			return false;
		if (addReads == null) {
			if (other.addReads != null)
				return false;
		} else if (!addReads.equals(other.addReads))
			return false;
		if (annotationProcessorOptions == null) {
			if (other.annotationProcessorOptions != null)
				return false;
		} else if (!annotationProcessorOptions.equals(other.annotationProcessorOptions))
			return false;
		if (annotationProcessors == null) {
			if (other.annotationProcessors != null)
				return false;
		} else if (!annotationProcessors.equals(other.annotationProcessors))
			return false;
		if (bootClassPath == null) {
			if (other.bootClassPath != null)
				return false;
		} else if (!bootClassPath.equals(other.bootClassPath))
			return false;
		if (bytecodeManipulation == null) {
			if (other.bytecodeManipulation != null)
				return false;
		} else if (!bytecodeManipulation.equals(other.bytecodeManipulation))
			return false;
		if (classPath == null) {
			if (other.classPath != null)
				return false;
		} else if (!classPath.equals(other.classPath))
			return false;
		if (debugInfo == null) {
			if (other.debugInfo != null)
				return false;
		} else if (!debugInfo.equals(other.debugInfo))
			return false;
		if (generateNativeHeaders != other.generateNativeHeaders)
			return false;
		if (modulePath == null) {
			if (other.modulePath != null)
				return false;
		} else if (!modulePath.equals(other.modulePath))
			return false;
		if (parameterNames != other.parameterNames)
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (processorInputLocations == null) {
			if (other.processorInputLocations != null)
				return false;
		} else if (!processorInputLocations.equals(other.processorInputLocations))
			return false;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		if (sourceDirectories == null) {
			if (other.sourceDirectories != null)
				return false;
		} else if (!sourceDirectories.equals(other.sourceDirectories))
			return false;
		if (sourceVersionName == null) {
			if (other.sourceVersionName != null)
				return false;
		} else if (!sourceVersionName.equals(other.sourceVersionName))
			return false;
		if (targetVersionName == null) {
			if (other.targetVersionName != null)
				return false;
		} else if (!targetVersionName.equals(other.targetVersionName))
			return false;
		return true;
	}

}
