package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.exception.TaskResultWaitingFailedException;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.util.property.IDEConfigurationRequiredExecutionProperty;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.ide.configuration.JavaIDEConfigurationBuilder;
import saker.java.compiler.impl.ide.configuration.JavaIDEConfigurationBuilder.ClassPathConfigurationBuilder;
import saker.java.compiler.impl.ide.configuration.JavaIDEConfigurationBuilder.SourceDirectoryConfigurationBuilder;
import saker.java.compiler.impl.options.SimpleAddExportsPath;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;

public class JavaIDEConfigurationReportingTaskFactory implements TaskFactory<Void>, Task<Void>, Externalizable {
	private static final long serialVersionUID = 1L;

	//TODO add gen directories from transitive classpaths

	protected String compilationId;
	protected Set<JavaAddExports> addExports = new LinkedHashSet<>();
	protected String compilerJavaVersion;
	protected Set<JavaSourceDirectory> sourceDirectories = new LinkedHashSet<>();
	protected SakerPath processorGenDirectory;
	protected SakerPath outputBinDirectory;
	protected List<String> parameters;
	protected SakerPath compilerInstallLocation;
	protected Set<? extends ClassPathIDEConfigurationEntry> classPathEntries;
	protected Set<? extends ClassPathIDEConfigurationEntry> bootClassPathEntries;
	protected Set<? extends ModulePathIDEConfigurationEntry> modulePathEntries;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaIDEConfigurationReportingTaskFactory() {
	}

	public JavaIDEConfigurationReportingTaskFactory(String compilationId) {
		this.compilationId = compilationId;
	}

	public void setAddExports(Set<JavaAddExports> addExports) {
		this.addExports = addExports;
	}

	public void setCompilerJavaVersion(String compilerJavaVersion) {
		this.compilerJavaVersion = compilerJavaVersion;
	}

	public void setSourceDirectories(Set<JavaSourceDirectory> sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}

	public void setProcessorGenDirectory(SakerPath processorGenDirectory) {
		this.processorGenDirectory = processorGenDirectory;
	}

	public void setOutputBinDirectory(SakerPath outputBinDirectory) {
		this.outputBinDirectory = outputBinDirectory;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public void setCompilerInstallLocation(SakerPath compilerInstallLocation) {
		this.compilerInstallLocation = compilerInstallLocation;
	}

	public void setClassPathEntries(Collection<? extends ClassPathIDEConfigurationEntry> classPathEntries) {
		this.classPathEntries = ImmutableUtils.makeImmutableLinkedHashSet(classPathEntries);
	}

	public void setBootClassPathEntries(Collection<? extends ClassPathIDEConfigurationEntry> bootClassPathEntries) {
		this.bootClassPathEntries = ImmutableUtils.makeImmutableLinkedHashSet(bootClassPathEntries);
	}

	public void setModulePathEntries(Collection<? extends ModulePathIDEConfigurationEntry> modulePathEntries) {
		this.modulePathEntries = ImmutableUtils.makeImmutableLinkedHashSet(modulePathEntries);
	}

	@Override
	public Void run(TaskContext taskcontext) throws Exception {
		if (!taskcontext.getTaskUtilities()
				.getReportExecutionDependency(IDEConfigurationRequiredExecutionProperty.INSTANCE)) {
			return null;
		}
		JavaIDEConfigurationBuilder ideconfigbuilder = new JavaIDEConfigurationBuilder();

		Collection<JavaSourceDirectory> sourcedirs = sourceDirectories;
		if (!ObjectUtils.isNullOrEmpty(sourcedirs)) {
			for (JavaSourceDirectory srcdiropt : sourcedirs) {
				ideconfigbuilder.addSourceDirectory(new SourceDirectoryConfigurationBuilder(srcdiropt));
			}
		}

		Collection<? extends ClassPathIDEConfigurationEntry> cpentries = classPathEntries;
		if (!ObjectUtils.isNullOrEmpty(cpentries)) {
			for (ClassPathIDEConfigurationEntry cpe : cpentries) {
				ClassPathConfigurationBuilder cpbuilder = buildClassPathConfiguration(taskcontext, cpe);

				ideconfigbuilder.addClassPath(cpbuilder);
			}
		}
		if (processorGenDirectory != null) {
			ideconfigbuilder.addProcessorGenDirectory(processorGenDirectory.toString());
		}
		if (outputBinDirectory != null) {
			ideconfigbuilder.setOutputBinDirectory(outputBinDirectory.toString());
		}

		Collection<JavaAddExports> exports = this.addExports;
		addExportsFromParameters(parameters, exports);
		if (!ObjectUtils.isNullOrEmpty(exports)) {
			Collection<String> exportsstrings = new TreeSet<>();
			for (JavaAddExports exp : exports) {
				exportsstrings.addAll(JavaTaskUtils.toAddExportsCommandLineStrings(exp));
			}
			ideconfigbuilder.setAddExports(exportsstrings);
		}

		Collection<? extends ClassPathIDEConfigurationEntry> bootclasspath = this.bootClassPathEntries;
		if (!ObjectUtils.isNullOrEmpty(bootclasspath)) {
			for (ClassPathIDEConfigurationEntry cpe : bootclasspath) {
				ClassPathConfigurationBuilder cpbuilder = buildClassPathConfiguration(taskcontext, cpe);

				ideconfigbuilder.addBootClassPath(cpbuilder);
			}
		}
		Set<? extends ModulePathIDEConfigurationEntry> modulepath = this.modulePathEntries;
		if (!ObjectUtils.isNullOrEmpty(modulepath)) {
			for (ModulePathIDEConfigurationEntry mpe : modulepath) {
				ClassPathConfigurationBuilder mpbuilder = buildModulePathConfiguration(taskcontext, mpe);

				ideconfigbuilder.addModulePath(mpbuilder);
			}
		}
		if (compilerInstallLocation != null) {
			ideconfigbuilder.setCompilerInstallLocation(compilerInstallLocation.toString());
		}
		String compilerjavaversion = this.compilerJavaVersion;
		if (!ObjectUtils.isNullOrEmpty(compilerjavaversion)) {
			ideconfigbuilder.setCompilerJavaVersion(compilerjavaversion);
		}

		taskcontext.reportIDEConfiguration(ideconfigbuilder.build(compilationId));
		return null;
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(compilationId);
		out.writeObject(compilerInstallLocation);
		out.writeObject(compilerJavaVersion);
		out.writeObject(processorGenDirectory);
		out.writeObject(outputBinDirectory);

		SerialUtils.writeExternalCollection(out, parameters);
		SerialUtils.writeExternalCollection(out, addExports);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
		SerialUtils.writeExternalCollection(out, classPathEntries);
		SerialUtils.writeExternalCollection(out, bootClassPathEntries);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		compilationId = (String) in.readObject();
		compilerInstallLocation = (SakerPath) in.readObject();
		compilerJavaVersion = (String) in.readObject();
		processorGenDirectory = (SakerPath) in.readObject();
		outputBinDirectory = (SakerPath) in.readObject();

		parameters = SerialUtils.readExternalImmutableList(in);
		addExports = SerialUtils.readExternalImmutableHashSet(in);
		sourceDirectories = SerialUtils.readExternalImmutableLinkedHashSet(in);
		classPathEntries = SerialUtils.readExternalImmutableLinkedHashSet(in);
		bootClassPathEntries = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(compilationId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaIDEConfigurationReportingTaskFactory other = (JavaIDEConfigurationReportingTaskFactory) obj;
		if (addExports == null) {
			if (other.addExports != null)
				return false;
		} else if (!addExports.equals(other.addExports))
			return false;
		if (bootClassPathEntries == null) {
			if (other.bootClassPathEntries != null)
				return false;
		} else if (!bootClassPathEntries.equals(other.bootClassPathEntries))
			return false;
		if (classPathEntries == null) {
			if (other.classPathEntries != null)
				return false;
		} else if (!classPathEntries.equals(other.classPathEntries))
			return false;
		if (compilationId == null) {
			if (other.compilationId != null)
				return false;
		} else if (!compilationId.equals(other.compilationId))
			return false;
		if (compilerInstallLocation == null) {
			if (other.compilerInstallLocation != null)
				return false;
		} else if (!compilerInstallLocation.equals(other.compilerInstallLocation))
			return false;
		if (compilerJavaVersion == null) {
			if (other.compilerJavaVersion != null)
				return false;
		} else if (!compilerJavaVersion.equals(other.compilerJavaVersion))
			return false;
		if (outputBinDirectory == null) {
			if (other.outputBinDirectory != null)
				return false;
		} else if (!outputBinDirectory.equals(other.outputBinDirectory))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (processorGenDirectory == null) {
			if (other.processorGenDirectory != null)
				return false;
		} else if (!processorGenDirectory.equals(other.processorGenDirectory))
			return false;
		if (sourceDirectories == null) {
			if (other.sourceDirectories != null)
				return false;
		} else if (!sourceDirectories.equals(other.sourceDirectories))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + compilationId + "]";
	}

	public static TaskIdentifier createTaskIdentifier(String compilationid) {
		return new IDEConfigurationReportingTaskIdentifier(compilationid);
	}

	static void addExportsFromParameters(List<String> parameters, Collection<JavaAddExports> result) {
		if (ObjectUtils.isNullOrEmpty(parameters)) {
			return;
		}
		for (Iterator<String> it = parameters.iterator(); it.hasNext();) {
			String param = it.next();
			if ("--add-exports".equals(param) && it.hasNext()) {
				String path = it.next();
				result.add(SimpleAddExportsPath.valueOf(path));
			}
		}
	}

	private static ClassPathConfigurationBuilder buildModulePathConfiguration(TaskContext taskcontext,
			ModulePathIDEConfigurationEntry mpe) {
		ClassPathConfigurationBuilder mpbuilder = new ClassPathConfigurationBuilder();

		FileLocation filelocation = mpe.getFileLocation();
		setClassPathPath(mpbuilder, filelocation);

		setClassPathSourceDirectories(mpbuilder, mpe.getSourceDirectories());

		setClassPathSourceAttachment(taskcontext, mpbuilder, mpe.getSourceAttachment());

		return mpbuilder;
	}

	private static ClassPathConfigurationBuilder buildClassPathConfiguration(TaskContext taskcontext,
			ClassPathIDEConfigurationEntry cpe) {
		ClassPathConfigurationBuilder cpbuilder = new ClassPathConfigurationBuilder();

		FileLocation filelocation = cpe.getFileLocation();
		setClassPathPath(cpbuilder, filelocation);

		Collection<? extends JavaSourceDirectory> cpsrcdirs = cpe.getSourceDirectories();
		setClassPathSourceDirectories(cpbuilder, cpsrcdirs);
		SakerPath srcgendir = cpe.getSourceGenDirectory();
		if (srcgendir != null) {
			cpbuilder.setSourceGenDirectory(srcgendir.toString());
		}
		StructuredTaskResult srcattachment = cpe.getSourceAttachment();
		setClassPathSourceAttachment(taskcontext, cpbuilder, srcattachment);

		StructuredTaskResult docattachment = cpe.getDocumentationAttachment();
		setClassPathDocAttachment(taskcontext, cpbuilder, docattachment);
		return cpbuilder;
	}

	private static void setClassPathSourceDirectories(ClassPathConfigurationBuilder cpbuilder,
			Collection<? extends JavaSourceDirectory> cpsrcdirs) {
		if (ObjectUtils.isNullOrEmpty(cpsrcdirs)) {
			return;
		}
		for (JavaSourceDirectory srcdir : cpsrcdirs) {
			cpbuilder.addSourceDirectory(new SourceDirectoryConfigurationBuilder(srcdir));
		}
	}

	private static void setClassPathDocAttachment(TaskContext taskcontext, ClassPathConfigurationBuilder cpbuilder,
			StructuredTaskResult docattachment) {
		if (docattachment == null) {
			return;
		}
		try {
			Object docresult = docattachment.toResult(taskcontext);
			if (docresult instanceof SakerPath) {
				cpbuilder.setDocumentationAttachmentPath(docresult.toString());
			} else if (docresult instanceof FileLocation) {
				((FileLocation) docresult).accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						cpbuilder.setSourceAttachmentPath(loc.getPath().toString());
					}

					@Override
					public void visit(LocalFileLocation loc) {
						cpbuilder.setSourceAttachmentLocalPath(loc.getLocalPath().toString());
					}
				});
			}
		} catch (TaskResultWaitingFailedException e) {
			//dont try to recover from deadlock and other waiting related exceptions
			throw e;
		} catch (Exception e) {
			SakerLog.warning().verbose().println("Failed to retrieve documentation attachment: " + docattachment);
			taskcontext.getTaskUtilities().reportIgnoredException(e);
		}
	}

	private static void setClassPathSourceAttachment(TaskContext taskcontext, ClassPathConfigurationBuilder cpbuilder,
			StructuredTaskResult srcattachment) {
		if (srcattachment == null) {
			return;
		}
		try {
			Object srcresult = srcattachment.toResult(taskcontext);
			if (srcresult instanceof SakerPath) {
				cpbuilder.setSourceAttachmentPath(srcresult.toString());
			} else if (srcresult instanceof FileLocation) {
				((FileLocation) srcresult).accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						cpbuilder.setSourceAttachmentPath(loc.getPath().toString());
					}

					@Override
					public void visit(LocalFileLocation loc) {
						cpbuilder.setSourceAttachmentLocalPath(loc.getLocalPath().toString());
					}
				});
			}
		} catch (TaskResultWaitingFailedException e) {
			//dont try to recover from deadlock and other waiting related exceptions
			throw e;
		} catch (Exception e) {
			SakerLog.warning().verbose().println("Failed to retrieve source attachment: " + srcattachment);
			taskcontext.getTaskUtilities().reportIgnoredException(e);
		}
	}

	private static void setClassPathPath(ClassPathConfigurationBuilder cpbuilder, FileLocation filelocation) {
		if (filelocation == null) {
			return;
		}
		try {
			filelocation.accept(new FileLocationVisitor() {
				@Override
				public void visit(ExecutionFileLocation loc) {
					cpbuilder.setPath(loc.getPath().toString());
				}

				@Override
				public void visit(LocalFileLocation loc) {
					cpbuilder.setLocalPath(loc.getLocalPath().toString());
				}
			});
		} catch (UnsupportedOperationException e) {
			//ignore
		}
	}

	private static class IDEConfigurationReportingTaskIdentifier implements TaskIdentifier, Externalizable {
		private static final long serialVersionUID = 1L;

		private String compilationId;

		/**
		 * For {@link Externalizable}.
		 */
		public IDEConfigurationReportingTaskIdentifier() {
		}

		public IDEConfigurationReportingTaskIdentifier(String compilationId) {
			this.compilationId = compilationId;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(compilationId);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			compilationId = (String) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((compilationId == null) ? 0 : compilationId.hashCode());
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
			IDEConfigurationReportingTaskIdentifier other = (IDEConfigurationReportingTaskIdentifier) obj;
			if (compilationId == null) {
				if (other.compilationId != null)
					return false;
			} else if (!compilationId.equals(other.compilationId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + (compilationId != null ? "compilationId=" + compilationId : "")
					+ "]";
		}
	}
}
