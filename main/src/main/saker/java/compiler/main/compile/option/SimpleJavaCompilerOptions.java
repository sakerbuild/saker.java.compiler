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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.compiler.utils.main.CompilationIdentifierTaskOption;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;

public class SimpleJavaCompilerOptions implements JavaCompilerOptions {
	protected CompilationIdentifierTaskOption identifier;
	protected String language;
	protected Collection<JavaSourceDirectoryTaskOption> sourceDirectories;
	protected Collection<JavaClassPathTaskOption> classPath;
	protected Collection<JavaClassPathTaskOption> bootClassPath;
	protected Collection<JavaClassPathTaskOption> modulePath;
	protected SourceVersionTaskOption sourceVersion;
	protected SourceVersionTaskOption targetVersion;
	protected List<String> parameters;
	protected Collection<AnnotationProcessorReferenceTaskOption> annotationProcessors;
	protected Map<String, String> annotationProcessorOptions;
	protected Map<String, SakerPath> processorInputLocations;
	protected Boolean generateNativeHeaders;
	protected Collection<AddExportsPathTaskOption> addExports;
	protected Collection<AddReadsPathTaskOption> addReads;
	protected Collection<String> suppressWarnings;
	protected String moduleMainClass;
	protected String moduleVersion;
	protected Boolean parallelProcessing;
	protected Boolean buildIncremental;
	protected Map<String, SDKDescriptionTaskOption> sdks;
	protected Boolean parameterNames;
	protected Collection<String> debugInfo;
	protected Boolean allowTargetReleaseMismatch;
	protected Boolean patchEnablePreview;

	public SimpleJavaCompilerOptions() {
	}

	public SimpleJavaCompilerOptions(JavaCompilerOptions copy) {
		this.identifier = ObjectUtils.clone(copy.getIdentifier(), CompilationIdentifierTaskOption::clone);
		this.language = copy.getLanguage();
		this.sourceDirectories = ObjectUtils.cloneArrayList(copy.getSourceDirectories(),
				JavaSourceDirectoryTaskOption::clone);
		this.classPath = ObjectUtils.cloneArrayList(copy.getClassPath(), JavaClassPathTaskOption::clone);
		this.modulePath = ObjectUtils.cloneArrayList(copy.getModulePath(), JavaClassPathTaskOption::clone);
		this.bootClassPath = ObjectUtils.cloneArrayList(copy.getBootClassPath(), JavaClassPathTaskOption::clone);
		this.sourceVersion = ObjectUtils.clone(copy.getSourceVersion(), SourceVersionTaskOption::clone);
		this.targetVersion = ObjectUtils.clone(copy.getTargetVersion(), SourceVersionTaskOption::clone);
		this.parameters = ObjectUtils.cloneArrayList(copy.getParameters());
		this.annotationProcessors = ObjectUtils.cloneArrayList(copy.getAnnotationProcessors(),
				AnnotationProcessorReferenceTaskOption::clone);
		this.annotationProcessorOptions = ObjectUtils.cloneTreeMap(copy.getAnnotationProcessorOptions());
		this.processorInputLocations = ObjectUtils.cloneTreeMap(copy.getProcessorInputLocations());
		this.generateNativeHeaders = copy.getGenerateNativeHeaders();
		this.addExports = ObjectUtils.cloneArrayList(copy.getAddExports(), AddExportsPathTaskOption::clone);
		this.addReads = ObjectUtils.cloneArrayList(copy.getAddReads(), AddReadsPathTaskOption::clone);
		this.suppressWarnings = JavaTaskUtils
				.makeImmutableIgnoreCaseNullableStringCollection(copy.getSuppressWarnings());
		this.sdks = ObjectUtils.cloneTreeMap(copy.getSDKs(), Functionals.identityFunction(),
				SDKDescriptionTaskOption::clone);
		this.moduleMainClass = copy.getModuleMainClass();
		this.moduleVersion = copy.getModuleVersion();
		this.parallelProcessing = copy.getParallelProcessing();
		this.buildIncremental = copy.getBuildIncremental();
		this.parameterNames = copy.getParameterNames();
		this.debugInfo = JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(copy.getDebugInfo());
		this.allowTargetReleaseMismatch = copy.getAllowTargetReleaseMismatch();
		this.parallelProcessing = copy.getPatchEnablePreview();
	}

	public SimpleJavaCompilerOptions(JavaCompilerOptions base, JavaCompilerOptions merge)
			throws IllegalArgumentException {
		this(base);

		Collection<JavaSourceDirectoryTaskOption> osourcedirs = merge.getSourceDirectories();
		if (!ObjectUtils.isNullOrEmpty(osourcedirs)) {
			this.setSourceDirectories(ObjectUtils.newArrayList(this.getSourceDirectories(), osourcedirs));
		}

		Collection<JavaClassPathTaskOption> oclasspath = merge.getClassPath();
		if (!ObjectUtils.isNullOrEmpty(oclasspath)) {
			Collection<JavaClassPathTaskOption> thisclasspath = this.getClassPath();
			this.setClassPath(ObjectUtils.newArrayList(thisclasspath, oclasspath));
		}
		Collection<JavaClassPathTaskOption> omodulepath = merge.getModulePath();
		if (!ObjectUtils.isNullOrEmpty(omodulepath)) {
			Collection<JavaClassPathTaskOption> thismodulepath = this.getModulePath();
			this.setModulePath(ObjectUtils.newArrayList(thismodulepath, omodulepath));
		}
		Collection<JavaClassPathTaskOption> obootclasspath = merge.getBootClassPath();
		if (obootclasspath != null) {
			//its valid if its empty
			this.setBootClassPath(ObjectUtils.newArrayList(this.getBootClassPath(), obootclasspath));
		}

		SourceVersionTaskOption osrc = merge.getSourceVersion();
		if (osrc != null) {
			SourceVersionTaskOption src = this.getSourceVersion();
			if (!Objects.equals(osrc, src)) {
				if (src != null) {
					throw new IllegalArgumentException("Failed to merge SourceVersion with " + src + " and " + osrc);
				}
				this.setSourceVersion(osrc);
			}
		}
		SourceVersionTaskOption otar = merge.getTargetVersion();
		if (otar != null) {
			SourceVersionTaskOption tar = this.getTargetVersion();
			if (!Objects.equals(otar, tar)) {
				if (tar != null) {
					throw new IllegalArgumentException("Failed to merge TargetVersion with " + tar + " and " + otar);
				}
				this.setTargetVersion(otar);
			}
		}
		List<String> oparams = merge.getParameters();
		if (!ObjectUtils.isNullOrEmpty(oparams)) {
			this.setParameters(ObjectUtils.newArrayList(this.getParameters(), oparams));
		}
		Map<String, String> oannotprocoptions = merge.getAnnotationProcessorOptions();
		if (!ObjectUtils.isNullOrEmpty(oannotprocoptions)) {
			Map<String, String> thisannotprocoptions = this.getAnnotationProcessorOptions();
			LinkedHashMap<String, String> nmap = new LinkedHashMap<>(
					thisannotprocoptions == null ? Collections.emptyMap() : thisannotprocoptions);
			for (Entry<String, String> entry : oannotprocoptions.entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if (nmap.containsKey(k)) {
					String presentv = nmap.get(k);
					if (!Objects.equals(presentv, v)) {
						throw new IllegalArgumentException("Merge conflict on AnnotationProcessorOptions for key: " + k
								+ " with " + presentv + " and " + v);
					}
				}
				nmap.put(k, v);
			}
			this.setAnnotationProcessorOptions(nmap);
		}
		Collection<AnnotationProcessorReferenceTaskOption> oannotprocessors = merge.getAnnotationProcessors();
		if (!ObjectUtils.isNullOrEmpty(oannotprocessors)) {
			this.setAnnotationProcessors(ObjectUtils.newArrayList(this.getAnnotationProcessors(), oannotprocessors));
		}
		Map<String, SakerPath> oprocinputlocations = merge.getProcessorInputLocations();
		if (!ObjectUtils.isNullOrEmpty(oprocinputlocations)) {
			Map<String, SakerPath> thisprocinputlocations = this.getProcessorInputLocations();
			if (ObjectUtils.isNullOrEmpty(thisprocinputlocations)) {
				this.setProcessorInputLocations(new TreeMap<>(oprocinputlocations));
			} else {
				//merge the locations
				TreeMap<String, SakerPath> nmap = new TreeMap<>(thisprocinputlocations);
				for (Entry<String, SakerPath> entry : oprocinputlocations.entrySet()) {
					//always put. will properly overwrite if null is present
					String k = entry.getKey();
					SakerPath v = entry.getValue();
					SakerPath prev = nmap.put(k, v);
					if (prev != null && !prev.equals(v)) {
						throw new IllegalArgumentException("Merge conflict on ProcessorInputLocations for key: " + k
								+ " with " + prev + " and " + v);
					}
				}
				this.setProcessorInputLocations(nmap);
			}
		}
		if (Boolean.TRUE.equals(merge.getGenerateNativeHeaders())) {
			this.setGenerateNativeHeaders(true);
		}
		Collection<AddExportsPathTaskOption> oaddexports = merge.getAddExports();
		if (!ObjectUtils.isNullOrEmpty(oaddexports)) {
			//we dont care about the duplicates, as the option users should filter them
			this.setAddExports(ObjectUtils.newArrayList(this.getAddExports(), oaddexports));
		}
		Collection<AddReadsPathTaskOption> oaddreads = merge.getAddReads();
		if (!ObjectUtils.isNullOrEmpty(oaddreads)) {
			//we dont care about the duplicates, as the option users should filter them
			this.setAddReads(ObjectUtils.newArrayList(this.getAddReads(), oaddreads));
		}

		Collection<String> osuppresswarnings = merge.getSuppressWarnings();
		if (!ObjectUtils.isNullOrEmpty(osuppresswarnings)) {
			NavigableSet<String> nsuppresswarnings = JavaTaskUtils
					.newIgnoreCaseNullableStringCollection(this.getSuppressWarnings());
			nsuppresswarnings.addAll(osuppresswarnings);
			this.setSuppressWarnings(nsuppresswarnings);
		}

		Map<String, SDKDescriptionTaskOption> osdks = merge.getSDKs();
		if (!ObjectUtils.isNullOrEmpty(osdks)) {
			Map<String, SDKDescriptionTaskOption> sdks = this.getSDKs();
			if (ObjectUtils.isNullOrEmpty(sdks)) {
				this.setSDKs(ImmutableUtils.makeImmutableNavigableMap(osdks));
			} else {
				TreeMap<String, SDKDescriptionTaskOption> nsdks = ObjectUtils.cloneTreeMap(sdks);
				for (Entry<String, SDKDescriptionTaskOption> entry : osdks.entrySet()) {
					if (entry.getValue() == null) {
						continue;
					}
					SDKDescriptionTaskOption prev = nsdks.putIfAbsent(entry.getKey(), entry.getValue());
					if (prev != null) {
						throw new IllegalArgumentException("Failed to merge SDKs, multiple definition for: "
								+ entry.getKey() + " with " + prev + " and " + entry.getValue());
					}
				}
			}
		}
		String omodulemainclass = merge.getModuleMainClass();
		if (omodulemainclass != null) {
			String targetmodulemainclass = this.getModuleMainClass();
			if (targetmodulemainclass == null) {
				this.setModuleMainClass(omodulemainclass);
			} else {
				//both have module main class
				if (!targetmodulemainclass.equals(omodulemainclass)) {
					throw new IllegalArgumentException("Failed to merge ModuleMainClass with " + targetmodulemainclass
							+ " and " + omodulemainclass);
				}
			}
		}
		String omoduleversion = merge.getModuleVersion();
		if (omoduleversion != null) {
			String targetmoduleversion = this.getModuleVersion();
			if (targetmoduleversion == null) {
				this.setModuleVersion(omoduleversion);
			} else {
				//both have module version
				if (!targetmoduleversion.equals(omoduleversion)) {
					throw new IllegalArgumentException(
							"Failed to merge ModuleVersion with " + targetmoduleversion + " and " + omoduleversion);
				}
			}
		}
		if (Boolean.FALSE.equals(merge.getParallelProcessing())) {
			this.setParallelProcessing(false);
		}
		if (Boolean.FALSE.equals(merge.getBuildIncremental())) {
			this.setBuildIncremental(false);
		}
		if (Boolean.FALSE.equals(merge.getParameterNames())) {
			this.setParameterNames(false);
		}
		Collection<String> odebuginfo = merge.getDebugInfo();
		if (odebuginfo != null) {
			NavigableSet<String> ndebuginfo = JavaTaskUtils.newIgnoreCaseNullableStringCollection(this.getDebugInfo());
			ndebuginfo.addAll(odebuginfo);
			this.setDebugInfo(ndebuginfo);
		}

		if (Boolean.TRUE.equals(merge.getAllowTargetReleaseMismatch())) {
			this.setAllowTargetReleaseMismatch(true);
		}
		if (Boolean.TRUE.equals(merge.getPatchEnablePreview())) {
			this.setPatchEnablePreview(true);
		}
	}

	@Override
	public CompilationIdentifierTaskOption getIdentifier() {
		return identifier;
	}

	public void setIdentifier(CompilationIdentifierTaskOption identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public Collection<JavaSourceDirectoryTaskOption> getSourceDirectories() {
		return sourceDirectories;
	}

	public void setSourceDirectories(Collection<JavaSourceDirectoryTaskOption> sourceDirectories) {
		this.sourceDirectories = sourceDirectories;
	}

	@Override
	public Collection<JavaClassPathTaskOption> getClassPath() {
		return classPath;
	}

	public void setClassPath(Collection<JavaClassPathTaskOption> classPath) {
		this.classPath = classPath;
	}

	@Override
	public Collection<JavaClassPathTaskOption> getModulePath() {
		return modulePath;
	}

	public void setModulePath(Collection<JavaClassPathTaskOption> modulePath) {
		this.modulePath = modulePath;
	}

	@Override
	public Collection<JavaClassPathTaskOption> getBootClassPath() {
		return bootClassPath;
	}

	public void setBootClassPath(Collection<JavaClassPathTaskOption> bootClassPath) {
		this.bootClassPath = bootClassPath;
	}

	@Override
	public SourceVersionTaskOption getSourceVersion() {
		return sourceVersion;
	}

	public void setSourceVersion(SourceVersionTaskOption sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	@Override
	public SourceVersionTaskOption getTargetVersion() {
		return targetVersion;
	}

	public void setTargetVersion(SourceVersionTaskOption targetVersion) {
		this.targetVersion = targetVersion;
	}

	@Override
	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Map<String, String> getAnnotationProcessorOptions() {
		return annotationProcessorOptions;
	}

	public void setAnnotationProcessorOptions(Map<String, String> annotationProcessorOptions) {
		this.annotationProcessorOptions = annotationProcessorOptions;
	}

	@Override
	public Collection<AnnotationProcessorReferenceTaskOption> getAnnotationProcessors() {
		return annotationProcessors;
	}

	public void setAnnotationProcessors(Collection<AnnotationProcessorReferenceTaskOption> annotationProcessors) {
		this.annotationProcessors = annotationProcessors;
	}

	@Override
	public Map<String, SakerPath> getProcessorInputLocations() {
		return processorInputLocations;
	}

	public void setProcessorInputLocations(Map<String, SakerPath> processorInputLocations) {
		this.processorInputLocations = processorInputLocations;
	}

	@Override
	public Boolean getGenerateNativeHeaders() {
		return generateNativeHeaders;
	}

	public void setGenerateNativeHeaders(Boolean generateNativeHeaders) {
		this.generateNativeHeaders = generateNativeHeaders;
	}

	@Override
	public Collection<AddExportsPathTaskOption> getAddExports() {
		return addExports;
	}

	public void setAddExports(Collection<AddExportsPathTaskOption> addExports) {
		this.addExports = addExports;
	}

	@Override
	public Collection<AddReadsPathTaskOption> getAddReads() {
		return addReads;
	}

	public void setAddReads(Collection<AddReadsPathTaskOption> addReads) {
		this.addReads = addReads;
	}

	@Override
	public Collection<String> getSuppressWarnings() {
		return suppressWarnings;
	}

	public void setSuppressWarnings(Collection<String> suppressWarnings) {
		this.suppressWarnings = suppressWarnings;
	}

	@Override
	public Map<String, SDKDescriptionTaskOption> getSDKs() {
		return sdks;
	}

	public void setSDKs(Map<String, SDKDescriptionTaskOption> sdks) {
		this.sdks = sdks;
	}

	@Override
	public String getModuleMainClass() {
		return moduleMainClass;
	}

	public void setModuleMainClass(String moduleMainClass) {
		this.moduleMainClass = moduleMainClass;
	}

	@Override
	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	@Override
	public Boolean getParallelProcessing() {
		return parallelProcessing;
	}

	public void setParallelProcessing(Boolean parallelProcessing) {
		this.parallelProcessing = parallelProcessing;
	}

	@Override
	public Boolean getBuildIncremental() {
		return buildIncremental;
	}

	public void setBuildIncremental(Boolean buildIncremental) {
		this.buildIncremental = buildIncremental;
	}

	@Override
	public Boolean getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(Boolean parameterNames) {
		this.parameterNames = parameterNames;
	}

	@Override
	public Collection<String> getDebugInfo() {
		return debugInfo;
	}

	public void setDebugInfo(Collection<String> debugInfo) {
		this.debugInfo = debugInfo;
	}

	@Override
	public Boolean getAllowTargetReleaseMismatch() {
		return allowTargetReleaseMismatch;
	}

	public void setAllowTargetReleaseMismatch(Boolean allowTargetReleaseMismatch) {
		this.allowTargetReleaseMismatch = allowTargetReleaseMismatch;
	}

	@Override
	public Boolean getPatchEnablePreview() {
		return patchEnablePreview;
	}

	public void setPatchEnablePreview(Boolean patchEnablePreview) {
		this.patchEnablePreview = patchEnablePreview;
	}

	@Override
	public String toString() {
		return "{" + (identifier != null ? "identifier: " + identifier + ", " : "")
				+ (language != null ? "language: " + language + ", " : "")
				+ (sourceDirectories != null ? "sourceDirectories: " + sourceDirectories + ", " : "")
				+ (classPath != null ? "classPath: " + classPath + ", " : "")
				+ (bootClassPath != null ? "bootClassPath: " + bootClassPath + ", " : "")
				+ (modulePath != null ? "modulePath: " + modulePath + ", " : "")
				+ (sourceVersion != null ? "sourceVersion: " + sourceVersion + ", " : "")
				+ (targetVersion != null ? "targetVersion: " + targetVersion + ", " : "")
				+ (parameters != null ? "parameters: " + parameters + ", " : "")
				+ (annotationProcessors != null ? "annotationProcessors: " + annotationProcessors + ", " : "")
				+ (annotationProcessorOptions != null
						? "annotationProcessorOptions: " + annotationProcessorOptions + ", "
						: "")
				+ (processorInputLocations != null ? "processorInputLocations: " + processorInputLocations + ", " : "")
				+ (generateNativeHeaders != null ? "generateNativeHeaders: " + generateNativeHeaders + ", " : "")
				+ (addExports != null ? "addExports: " + addExports + ", " : "")
				+ (addReads != null ? "addReads: " + addReads + ", " : "")
				+ (suppressWarnings != null ? "suppressWarnings: " + suppressWarnings + ", " : "")
				+ (moduleMainClass != null ? "moduleMainClass: " + moduleMainClass + ", " : "")
				+ (moduleVersion != null ? "moduleVersion: " + moduleVersion + ", " : "")
				+ (parallelProcessing != null ? "parallelProcessing: " + parallelProcessing + ", " : "")
				+ (buildIncremental != null ? "buildIncremental: " + buildIncremental + ", " : "")
				+ (sdks != null ? "sdks: " + sdks + ", " : "")
				+ (parameterNames != null ? "parameterNames: " + parameterNames + ", " : "")
				+ (debugInfo != null ? "debugInfo: " + debugInfo + ", " : "")
				+ (allowTargetReleaseMismatch != null
						? "allowTargetReleaseMismatch: " + allowTargetReleaseMismatch + ", "
						: "")
				+ (patchEnablePreview != null ? "patchEnablePreview: " + patchEnablePreview : "") + "}";
	}

}
