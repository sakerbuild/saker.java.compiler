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
package saker.java.compiler.impl.ide.configuration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import saker.build.file.path.WildcardPath;
import saker.build.ide.configuration.IDEConfiguration;
import saker.build.ide.configuration.SimpleIDEConfiguration;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.classpath.JavaSourceDirectory;

public final class JavaIDEConfigurationBuilder {
	public static final String TYPE = "saker.java.compile.ide.configuration";

	public static final String FIELD_SOURCE_DIRECTORIES = "java.source.directories";
	public static final String FIELD_SOURCEDIRECTORY_PATH = "path";
	public static final String FIELD_SOURCEDIRECTORY_FILES = "files";

	public static final String FIELD_CLASSPATHS = "java.classpaths";

	public static final String FIELD_CLASSPATH_PATH = "path";
	public static final String FIELD_CLASSPATH_SOURCEPATH = "sourcepath";
	public static final String FIELD_CLASSPATH_DOCPATH = "docpath";
	public static final String FIELD_CLASSPATH_LOCAL_PATH = "path.local";
	public static final String FIELD_CLASSPATH_LOCAL_SOURCEPATH = "sourcepath.local";
	public static final String FIELD_CLASSPATH_LOCAL_DOCPATH = "docpath.local";
	public static final String FIELD_CLASSPATH_SOURCEDIRECTORIES = "sourcedirectories";
	public static final String FIELD_CLASSPATH_SOURCEGENDIRECTORY = "sourcegendirectory";
	public static final String FIELD_CLASSPATH_DISPLAY_NAME = "display.name";

	public static final String FIELD_BOOT_CLASSPATHS = "java.bootclasspaths";

	public static final String FIELD_MODULEPATHS = "java.modulepaths";

	public static final String FIELD_ADDEXPORTS = "java.addexports";
	public static final String FIELD_ADDREADS = "java.addreads";

	public static final String FIELD_COMPILER_JDKHOME = "java.compiler.install.location";

	public static final String FIELD_COMPILER_JAVAVERSION = "java.compiler.java.version";

	public static final String FIELD_PROCESSOR_GEN_DIRECTORIES = "java.processor.gendirectories";

	public static final String FIELD_OUTPUT_BIN_DIRECTORY = "java.output.directory.bin";

	private Collection<Object> sourceDirectories = new LinkedHashSet<>();
	private Collection<Object> classPaths = new LinkedHashSet<>();
	private Collection<Object> bootClassPaths = new LinkedHashSet<>();
	private Collection<Object> modulePaths = new LinkedHashSet<>();
	private Collection<Object> processorGenDirectories = new LinkedHashSet<>();
	private Collection<?> addExports = new LinkedHashSet<>();
	private Collection<?> addReads = new LinkedHashSet<>();
	private String compilerInstallLocation;
	private String compilerJavaVersion;
	private String outputBinDirectory;

	public JavaIDEConfigurationBuilder setAddExports(Collection<String> addExports) {
		this.addExports = addExports;
		return this;
	}

	public JavaIDEConfigurationBuilder setAddReads(Collection<String> addReads) {
		this.addReads = addReads;
		return this;
	}

	public JavaIDEConfigurationBuilder setCompilerInstallLocation(String compilerInstallLocation) {
		this.compilerInstallLocation = compilerInstallLocation;
		return this;
	}

	public JavaIDEConfigurationBuilder setCompilerJavaVersion(String javaVersion) {
		this.compilerJavaVersion = javaVersion;
		return this;
	}

	public JavaIDEConfigurationBuilder addClassPath(ClassPathConfigurationBuilder cpbuilder) {
		this.classPaths.add(cpbuilder.toClassPathObject());
		return this;
	}

	public JavaIDEConfigurationBuilder addBootClassPath(ClassPathConfigurationBuilder cpbuilder) {
		this.bootClassPaths.add(cpbuilder.toClassPathObject());
		return this;
	}

	public JavaIDEConfigurationBuilder addModulePath(ClassPathConfigurationBuilder cpbuilder) {
		this.modulePaths.add(cpbuilder.toClassPathObject());
		return this;
	}

	public JavaIDEConfigurationBuilder addSourceDirectory(SourceDirectoryConfigurationBuilder srcdirbuilder) {
		this.sourceDirectories.add(srcdirbuilder.toSourceDirectoryObject());
		return this;
	}

	public JavaIDEConfigurationBuilder addProcessorGenDirectory(String dirpath) {
		processorGenDirectories.add(dirpath);
		return this;
	}

	public JavaIDEConfigurationBuilder setOutputBinDirectory(String outputBinDirectory) {
		this.outputBinDirectory = outputBinDirectory;
		return this;
	}

	public IDEConfiguration build(String identifier) {
		NavigableMap<String, Object> fields = new TreeMap<>();
		if (!ObjectUtils.isNullOrEmpty(sourceDirectories)) {
			fields.put(FIELD_SOURCE_DIRECTORIES, sourceDirectories);
		}
		if (!ObjectUtils.isNullOrEmpty(classPaths)) {
			fields.put(FIELD_CLASSPATHS, classPaths);
		}
		if (!ObjectUtils.isNullOrEmpty(bootClassPaths)) {
			fields.put(FIELD_BOOT_CLASSPATHS, bootClassPaths);
		}
		if (!ObjectUtils.isNullOrEmpty(modulePaths)) {
			fields.put(FIELD_MODULEPATHS, modulePaths);
		}
		if (!ObjectUtils.isNullOrEmpty(addExports)) {
			fields.put(FIELD_ADDEXPORTS, addExports);
		}
		if (!ObjectUtils.isNullOrEmpty(addReads)) {
			fields.put(FIELD_ADDREADS, addReads);
		}
		if (!ObjectUtils.isNullOrEmpty(processorGenDirectories)) {
			fields.put(FIELD_PROCESSOR_GEN_DIRECTORIES, processorGenDirectories);
		}
		if (outputBinDirectory != null) {
			fields.put(FIELD_OUTPUT_BIN_DIRECTORY, outputBinDirectory);
		}
		if (compilerInstallLocation != null) {
			fields.put(FIELD_COMPILER_JDKHOME, compilerInstallLocation);
		}
		if (compilerJavaVersion != null) {
			fields.put(FIELD_COMPILER_JAVAVERSION, compilerJavaVersion);
		}
		return new SimpleIDEConfiguration(TYPE, identifier, fields);
	}

	public static final class SourceDirectoryConfigurationBuilder {
		protected String path;
		protected Set<WildcardPath> files = new LinkedHashSet<>();

		public SourceDirectoryConfigurationBuilder() {
		}

		public SourceDirectoryConfigurationBuilder(JavaSourceDirectory srcdir) {
			this.path = Objects.toString(srcdir.getDirectory(), null);
			ObjectUtils.addAll(files, srcdir.getFiles());
		}

		public SourceDirectoryConfigurationBuilder setPath(String path) {
			this.path = path;
			return this;
		}

		public SourceDirectoryConfigurationBuilder addFiles(WildcardPath files) {
			this.files.add(files);
			return this;
		}

		protected Object toSourceDirectoryObject() {
			TreeMap<String, Object> result = new TreeMap<>();
			if (!ObjectUtils.isNullOrEmpty(path)) {
				result.put(FIELD_SOURCEDIRECTORY_PATH, path);
			}
			if (!ObjectUtils.isNullOrEmpty(files)) {
				result.put(FIELD_SOURCEDIRECTORY_FILES, ImmutableUtils.makeImmutableLinkedHashSet(files));
			}
			return ImmutableUtils.unmodifiableMap(result);
		}
	}

	public static final class ClassPathConfigurationBuilder {
		protected String path;
		protected String localPath;
		protected String sourceAttachmentPath;
		protected String sourceAttachmentLocalPath;
		protected String documentationAttachmentPath;
		protected String documentationAttachmentLocalPath;
		protected Set<Object> sourceDirectories = new LinkedHashSet<>();
		protected String sourceGenDirectory;
		protected String displayName;

		public ClassPathConfigurationBuilder() {
		}

		public ClassPathConfigurationBuilder setPath(String path) {
			this.path = path;
			return this;
		}

		public ClassPathConfigurationBuilder setSourceGenDirectory(String sourceGenDirectory) {
			this.sourceGenDirectory = sourceGenDirectory;
			return this;
		}

		public ClassPathConfigurationBuilder setDocumentationAttachmentPath(String documentationAttachmentPath) {
			this.documentationAttachmentPath = documentationAttachmentPath;
			return this;
		}

		public ClassPathConfigurationBuilder setSourceAttachmentPath(String sourceAttachmentPath) {
			this.sourceAttachmentPath = sourceAttachmentPath;
			return this;
		}

		public ClassPathConfigurationBuilder setLocalPath(String localPath) {
			this.localPath = localPath;
			return this;
		}

		public ClassPathConfigurationBuilder setDocumentationAttachmentLocalPath(
				String documentationAttachmentLocalPath) {
			this.documentationAttachmentLocalPath = documentationAttachmentLocalPath;
			return this;
		}

		public ClassPathConfigurationBuilder setSourceAttachmentLocalPath(String sourceAttachmentLocalPath) {
			this.sourceAttachmentLocalPath = sourceAttachmentLocalPath;
			return this;
		}

		public ClassPathConfigurationBuilder addSourceDirectory(SourceDirectoryConfigurationBuilder sourcedirbuilder) {
			this.sourceDirectories.add(sourcedirbuilder.toSourceDirectoryObject());
			return this;
		}

		public ClassPathConfigurationBuilder setDisplayName(String displayname) {
			this.displayName = displayname;
			return this;
		}

		public Object toClassPathObject() {
			TreeMap<String, Object> result = new TreeMap<>();
			if (!ObjectUtils.isNullOrEmpty(path)) {
				result.put(FIELD_CLASSPATH_PATH, path);
			}
			if (!ObjectUtils.isNullOrEmpty(sourceAttachmentPath)) {
				result.put(FIELD_CLASSPATH_SOURCEPATH, sourceAttachmentPath);
			}
			if (!ObjectUtils.isNullOrEmpty(documentationAttachmentPath)) {
				result.put(FIELD_CLASSPATH_DOCPATH, documentationAttachmentPath);
			}
			if (!ObjectUtils.isNullOrEmpty(localPath)) {
				result.put(FIELD_CLASSPATH_LOCAL_PATH, localPath);
			}
			if (!ObjectUtils.isNullOrEmpty(sourceAttachmentLocalPath)) {
				result.put(FIELD_CLASSPATH_LOCAL_SOURCEPATH, sourceAttachmentLocalPath);
			}
			if (!ObjectUtils.isNullOrEmpty(documentationAttachmentLocalPath)) {
				result.put(FIELD_CLASSPATH_LOCAL_DOCPATH, documentationAttachmentLocalPath);
			}
			if (!ObjectUtils.isNullOrEmpty(sourceDirectories)) {
				result.put(FIELD_CLASSPATH_SOURCEDIRECTORIES,
						ImmutableUtils.makeImmutableLinkedHashSet(sourceDirectories));
			}
			if (!ObjectUtils.isNullOrEmpty(sourceGenDirectory)) {
				result.put(FIELD_CLASSPATH_SOURCEGENDIRECTORY, sourceGenDirectory);
			}
			if (!ObjectUtils.isNullOrEmpty(displayName)) {
				result.put(FIELD_CLASSPATH_DISPLAY_NAME, displayName);
			}
			return ImmutableUtils.unmodifiableMap(result);
		}

	}
}
