package saker.java.compiler.impl.compile.handler.info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface CompilationInfo extends Serializable {
	public static final long serialVersionUID = 1L;

	public static class ExecutionClassPathStateInfo implements Externalizable {
		private static final long serialVersionUID = 1L;

		protected NavigableMap<String, ? extends ClassSignature> classSignatures;
		protected ModuleSignature moduleSignature;
		protected Object abiVersionKey;

		/**
		 * For {@link Externalizable}.
		 */
		public ExecutionClassPathStateInfo() {
		}

		public ExecutionClassPathStateInfo(NavigableMap<String, ? extends ClassSignature> classSignatures,
				ModuleSignature modulesignature, Object abiVersionKey) {
			this.classSignatures = classSignatures;
			this.moduleSignature = modulesignature;
			this.abiVersionKey = abiVersionKey;
		}

		public NavigableMap<String, ? extends ClassSignature> getClassSignatures() {
			return classSignatures;
		}

		public Object getAbiVersionKey() {
			return abiVersionKey;
		}

		public ModuleSignature getModuleSignature() {
			return moduleSignature;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			SerialUtils.writeExternalMap(out, classSignatures);
			out.writeObject(moduleSignature);
			out.writeObject(abiVersionKey);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			classSignatures = SerialUtils.readExternalSortedImmutableNavigableMap(in);
			moduleSignature = (ModuleSignature) in.readObject();
			abiVersionKey = in.readObject();
		}
	}

	public static class LocalClassPathStateInfo implements Externalizable {
		private static final long serialVersionUID = 1L;

		protected NavigableMap<SakerPath, ? extends ContentDescriptor> classPathFileContents;
		protected Object abiVersionKey;

		/**
		 * For {@link Externalizable}.
		 */
		public LocalClassPathStateInfo() {
		}

		public LocalClassPathStateInfo(NavigableMap<SakerPath, ? extends ContentDescriptor> classPathFileContents,
				Object abiVersionKey) {
			this.classPathFileContents = classPathFileContents;
			this.abiVersionKey = abiVersionKey;
		}

		public Object getAbiVersionKey() {
			return abiVersionKey;
		}

		public NavigableMap<SakerPath, ? extends ContentDescriptor> getClassPathFileContents() {
			return classPathFileContents;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			SerialUtils.writeExternalMap(out, classPathFileContents);
			out.writeObject(abiVersionKey);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			classPathFileContents = SerialUtils.readExternalSortedImmutableNavigableMap(in);
			abiVersionKey = in.readObject();
		}

	}

	//TODO include the compiling java JRE version in this info
	public static class ProcessorData implements Externalizable {
		private static final long serialVersionUID = 1L;

		private Set<String> supportedOptions;
		private NavigableMap<SakerPath, ? extends Set<? extends DocReference>> docCommentReferencedSignatures;

		private NavigableMap<SakerPath, ContentDescriptor> readResourceFileContents;

		/**
		 * For {@link Externalizable}.
		 */
		public ProcessorData() {
		}

		public ProcessorData(Set<String> supportedOptions,
				NavigableMap<SakerPath, ? extends Set<? extends DocReference>> docCommentReferencedSignatures,
				NavigableMap<SakerPath, ContentDescriptor> readresourcefilecontents) {
			Objects.requireNonNull(supportedOptions, "supported options");
			Objects.requireNonNull(docCommentReferencedSignatures, "doc comment referenced signatures");
			Objects.requireNonNull(readresourcefilecontents, "read resource file contents");
			this.supportedOptions = supportedOptions;
			this.docCommentReferencedSignatures = docCommentReferencedSignatures;
			this.readResourceFileContents = readresourcefilecontents;
		}

		public Set<String> getSupportedOptions() {
			return supportedOptions;
		}

		public boolean isDocCommentInterested() {
			return !docCommentReferencedSignatures.isEmpty();
		}

		public NavigableMap<SakerPath, ? extends Set<? extends DocReference>> getDocCommentReferencedSignatures() {
			return docCommentReferencedSignatures;
		}

		public NavigableMap<SakerPath, ContentDescriptor> getReadResourceFileContents() {
			return readResourceFileContents;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			SerialUtils.writeExternalCollection(out, supportedOptions);
			SerialUtils.writeExternalMap(out, docCommentReferencedSignatures);
			SerialUtils.writeExternalMap(out, readResourceFileContents);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			supportedOptions = SerialUtils.readExternalCollection(new TreeSet<>(), in);
			docCommentReferencedSignatures = SerialUtils.readExternalSortedImmutableNavigableMap(in);
			readResourceFileContents = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		}

		public interface DocReference {
			@Override
			public int hashCode();

			@Override
			public boolean equals(Object obj);
		}

		public static class ModuleDocReference implements DocReference, Externalizable {
			private static final long serialVersionUID = 1L;

			private String moduleName;

			/**
			 * For {@link Externalizable}.
			 */
			public ModuleDocReference() {
			}

			public ModuleDocReference(String moduleName) {
				this.moduleName = moduleName;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
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
				ModuleDocReference other = (ModuleDocReference) obj;
				if (moduleName == null) {
					if (other.moduleName != null)
						return false;
				} else if (!moduleName.equals(other.moduleName))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "ModuleDocReference [" + moduleName + "]";
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				out.writeUTF(moduleName);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				moduleName = in.readUTF();
			}
		}

		public static class ClassDocReference implements DocReference, Externalizable {
			private static final long serialVersionUID = 1L;

			private String classCanonicalName;

			/**
			 * For {@link Externalizable}.
			 */
			public ClassDocReference() {
			}

			public ClassDocReference(String classCanonicalName) {
				this.classCanonicalName = classCanonicalName;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
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
				ClassDocReference other = (ClassDocReference) obj;
				if (classCanonicalName == null) {
					if (other.classCanonicalName != null)
						return false;
				} else if (!classCanonicalName.equals(other.classCanonicalName))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "ClassDocReference [" + classCanonicalName + "]";
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				out.writeUTF(classCanonicalName);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				classCanonicalName = in.readUTF();
			}
		}

		public static class PackageDocReference implements DocReference, Externalizable {
			private static final long serialVersionUID = 1L;

			private String packageName;

			/**
			 * For {@link Externalizable}.
			 */
			public PackageDocReference() {
			}

			public PackageDocReference(String packageName) {
				this.packageName = packageName;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
				PackageDocReference other = (PackageDocReference) obj;
				if (packageName == null) {
					if (other.packageName != null)
						return false;
				} else if (!packageName.equals(other.packageName))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "PackageDocReference [" + packageName + "]";
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				out.writeUTF(packageName);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				packageName = in.readUTF();
			}
		}

		public static class FieldDocReference implements DocReference, Externalizable {
			private static final long serialVersionUID = 1L;

			private String classCanonicalName;
			private String fieldName;

			/**
			 * For {@link Externalizable}.
			 */
			public FieldDocReference() {
			}

			public FieldDocReference(String classCanonicalName, String fieldName) {
				this.classCanonicalName = classCanonicalName;
				this.fieldName = fieldName;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
				result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
				FieldDocReference other = (FieldDocReference) obj;
				if (classCanonicalName == null) {
					if (other.classCanonicalName != null)
						return false;
				} else if (!classCanonicalName.equals(other.classCanonicalName))
					return false;
				if (fieldName == null) {
					if (other.fieldName != null)
						return false;
				} else if (!fieldName.equals(other.fieldName))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "FieldDocReference [" + classCanonicalName + "." + fieldName + "]";
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				out.writeUTF(classCanonicalName);
				out.writeUTF(fieldName);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				classCanonicalName = in.readUTF();
				fieldName = in.readUTF();
			}
		}

		public static class MethodDocReference implements DocReference, Externalizable {
			private static final long serialVersionUID = 1L;

			private String classCanonicalName;
			private String methodName;

			/**
			 * For {@link Externalizable}.
			 */
			public MethodDocReference() {
			}

			public MethodDocReference(String classCanonicalName, String methodName) {
				this.classCanonicalName = classCanonicalName;
				this.methodName = methodName;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
				result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
				MethodDocReference other = (MethodDocReference) obj;
				if (classCanonicalName == null) {
					if (other.classCanonicalName != null)
						return false;
				} else if (!classCanonicalName.equals(other.classCanonicalName))
					return false;
				if (methodName == null) {
					if (other.methodName != null)
						return false;
				} else if (!methodName.equals(other.methodName))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "MethodDocReference [" + classCanonicalName + "#" + methodName + "]";
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				out.writeUTF(classCanonicalName);
				out.writeUTF(methodName);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				classCanonicalName = in.readUTF();
				methodName = in.readUTF();
			}
		}

	}

	public void setOptions(String sourceversion, String targetversion, List<String> options, String jreVersion);

	public String getSourceVersion();

	public String getTargetVersion();

	public List<String> getOptions();

	public void setSourceFiles(NavigableMap<SakerPath, SourceFileData> sources);

	public void putGeneratedSourceFiles(NavigableMap<SakerPath, GeneratedSourceFileData> sources);

	public void putGeneratedSourceFile(GeneratedSourceFileData source);

	public void setClassFiles(NavigableMap<SakerPath, ClassFileData> classes);

	public void putGeneratedClassFiles(NavigableMap<SakerPath, GeneratedClassFileData> classes);

	public void putGeneratedClassFile(GeneratedClassFileData classfiledata);

	public void putGeneratedResourceFiles(NavigableMap<SakerPath, GeneratedResourceFileData> resources);

	public void putGeneratedResourceFile(GeneratedResourceFileData resourcefiledata);

	public NavigableMap<SakerPath, SourceFileData> getSourceFiles();

	public NavigableMap<SakerPath, GeneratedSourceFileData> getGeneratedSourceFiles();

	public NavigableMap<SakerPath, ClassFileData> getClassFiles();

	public NavigableMap<SakerPath, GeneratedClassFileData> getGeneratedClassFiles();

	public NavigableMap<SakerPath, GeneratedResourceFileData> getGeneratedResourceFiles();

	public Map<? extends ProcessorDetails, ProcessorData> getProcessorDetails();

	public void setProcessorDetails(Map<? extends ProcessorDetails, ProcessorData> processors);

	public Collection<ClassSignature> getRealizedClassSignatures();

	public NavigableMap<SakerPath, Collection<ClassSignature>> getRealizedClassSignaturesByPath();

	public default Collection<PackageSignature> getRealizedPackageSignatures() {
		return getRealizedPackageSignaturesByPath().values();
	}

	public NavigableMap<SakerPath, PackageSignature> getRealizedPackageSignaturesByPath();

	public ModuleSignature getRealizedModuleSignature();

	public SakerPath getRealizedModulesignaturePath();

	public NavigableMap<SakerPath, ? extends ExecutionClassPathStateInfo> getExecutionClassPathStateInfos();

	public NavigableMap<SakerPath, ? extends ExecutionClassPathStateInfo> getExecutionModulePathStateInfos();

	public NavigableMap<SakerPath, ? extends LocalClassPathStateInfo> getLocalClassPathStateInfos();

	public NavigableMap<SakerPath, ? extends LocalClassPathStateInfo> getLocalModulePathStateInfos();

	public void setExecutionClassPathStateInfosSignatures(
			NavigableMap<SakerPath, ExecutionClassPathStateInfo> executionClassPathStateInfos);

	public void setLocalClassPathStateInfosSignatures(
			NavigableMap<SakerPath, LocalClassPathStateInfo> localClassPathStateInfos);

	public void setExecutionModulePathStateInfosSignatures(
			NavigableMap<SakerPath, ExecutionClassPathStateInfo> executionModulePathStateInfos);

	public void setLocalModulePathStateInfosSignatures(
			NavigableMap<SakerPath, LocalClassPathStateInfo> localModulePathStateInfos);

	public void setModuleClassFile(ClassHoldingData fd);

	public ClassHoldingData getModuleClassFile();

	public void setCompilationModuleSet(NavigableSet<String> modulenames);

	public NavigableSet<String> getCompilationModuleSet();

	public void addDiagnostic(DiagnosticEntry entry);

	public void addDiagnostics(Collection<DiagnosticEntry> entries);

	public Set<DiagnosticEntry> getDiagnostics();

	public NavigableSet<String> getUnrecognizedProcessorOptions();

	public void setUnrecognizedProcessorOptions(NavigableSet<String> options);

	public Object getAbiVersionKey();

	public void setAbiVersionKey(Object object);

	public Object getImplementationVersionKey();

	public void setImplementationVersionKey(Object implementationVersionKey);

	public String getJreVersion();

	public String getModuleMainClass();

	public void setModuleMainClass(String moduleMainClass);

	public String getModuleVersion();

	public void setModuleVersion(String moduleVersion);
}