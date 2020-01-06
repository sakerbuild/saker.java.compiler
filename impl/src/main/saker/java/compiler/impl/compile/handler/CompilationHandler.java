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
package saker.java.compiler.impl.compile.handler;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject.Kind;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils.ThreadWorkPool;
import saker.build.util.file.FileOnlyIgnoreCaseExtensionDirectoryVisitPredicate;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.ClassPathIDEConfigurationEntry;
import saker.java.compiler.impl.compile.ModulePathIDEConfigurationEntry;
import saker.java.compiler.impl.compile.VersionKeyUtils;
import saker.java.compiler.impl.compile.file.JavaCompilerFileObject;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticLocation;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticLocationReference;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticPositionTable;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.compile.handler.info.ClassFileData;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;
import saker.java.compiler.impl.compile.handler.info.SourceFileData;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;
import testing.saker.java.compiler.TestFlag;

public abstract class CompilationHandler {
	public static final String URI_SCHEME_INPUT = "saker";
	public static final String URI_SCHEME_GENERATED = "sakergen";

	private static final Map<String, String> SOURCE_VERSION_STRINGS = new TreeMap<>();
	static {
		SOURCE_VERSION_STRINGS.put("RELEASE_0", null);
		SOURCE_VERSION_STRINGS.put("RELEASE_1", "1.1");
		SOURCE_VERSION_STRINGS.put("RELEASE_2", "1.2");
		SOURCE_VERSION_STRINGS.put("RELEASE_3", "1.3");
		SOURCE_VERSION_STRINGS.put("RELEASE_4", "1.4");
		SOURCE_VERSION_STRINGS.put("RELEASE_5", "1.5");
		SOURCE_VERSION_STRINGS.put("RELEASE_6", "1.6");
		SOURCE_VERSION_STRINGS.put("RELEASE_7", "1.7");
		SOURCE_VERSION_STRINGS.put("RELEASE_8", "1.8");
		SOURCE_VERSION_STRINGS.put("RELEASE_9", "9");
		SOURCE_VERSION_STRINGS.put("RELEASE_10", "10");
		SOURCE_VERSION_STRINGS.put("RELEASE_11", "11");
		SOURCE_VERSION_STRINGS.put("RELEASE_12", "12");
		SOURCE_VERSION_STRINGS.put("RELEASE_13", "13");
	}

	public static String sourceVersionToParameterString(String srcversion) {
		if (srcversion == null) {
			return null;
		}
		return SOURCE_VERSION_STRINGS.get(srcversion);
	}

	private static final Map<String, String> IGNORED_VALUE_PARAMETERS = new TreeMap<>();
	static {
		IGNORED_VALUE_PARAMETERS.put("-processorpath", "AnnotationProcessors");
		IGNORED_VALUE_PARAMETERS.put("-processor", "AnnotationProcessors");
		IGNORED_VALUE_PARAMETERS.put("-target", "TargetVersion");
		IGNORED_VALUE_PARAMETERS.put("-source", "SourceVersion");
		IGNORED_VALUE_PARAMETERS.put("-sourcepath", "");
	}

	public static List<String> createOptions(List<String> passparameters, String passsourceversion,
			String passtargetversion, Collection<JavaAddExports> passaddexports,
			Set<? extends SakerPath> bootclasspathfiles, Set<? extends SakerPath> classpathfiles,
			Set<? extends SakerPath> modulepathfiles, boolean parameternames, Set<String> debuginfos,
			boolean[] outnocmdlineclasspath) {
		ArrayList<String> result = new ArrayList<>();
		collectOptions(passparameters, passsourceversion, passtargetversion, passaddexports, bootclasspathfiles,
				classpathfiles, modulepathfiles, result, parameternames, debuginfos, outnocmdlineclasspath);
		return result;
	}

	//the debug infos are already sanitized to lower case values
	private static void collectOptions(List<String> passparameters, String passsourceversion, String passtargetversion,
			Collection<JavaAddExports> passaddexports, Set<? extends SakerPath> bootclasspathfiles,
			Set<? extends SakerPath> classpathfiles, Set<? extends SakerPath> modulepathfiles, List<String> result,
			boolean parameternames, Set<String> debuginfos, boolean[] outnocmdlineclasspath) {
		String presentclasspathparam = null;
		String presentmodulepathparam = null;
		String presentbootclasspathparam = null;
		boolean hadrelease = false;
		boolean parameternamespresent = false;
		int paramssize = passparameters.size();
		for (int i = 0; i < paramssize; i++) {
			String p = passparameters.get(i);
			String ignore = IGNORED_VALUE_PARAMETERS.get(p);
			if (ignore != null) {
				//ignore the argument too
				SakerLog.warning()
						.println("Java compilation parameter " + p
								+ (++i < paramssize ? " " + passparameters.get(i) : "") + " is ignored."
								+ (!ignore.isEmpty() ? " Use " + ignore + " instead." : ""));
				continue;
			}
			if (p.startsWith("-proc:")) {
				SakerLog.warning().println("Java compilation parameter " + p + " is ignored.");
				continue;
			}
			if (p.startsWith("-A")) {
				//dont add the annotation processor option arguments
				//they are handled manually, or added manually
				continue;
			}
			if (p.equals("-g") || p.startsWith("-g:")) {
				SakerLog.warning().println("Java compilation parameter " + p + " is ignored. Use DebugInfo instead.");
				continue;
			}
			if (p.equals("-parameters")) {
				parameternamespresent = true;
			}
			if (p.equals("-classpath") || p.equals("--class-path") || p.equals("-cp")) {
				if (++i < paramssize) {
					//ignore
					SakerLog.warning().println("Java compilation parameter " + p + " argument is missing.");
					continue;
				}
				if (presentclasspathparam != null) {
					presentclasspathparam = presentclasspathparam + File.pathSeparator + passparameters.get(i);
				} else {
					presentclasspathparam = p;
				}
				continue;
			}
			if (p.equals("--module-path") || p.equals("-p")) {
				if (++i < paramssize) {
					//ignore
					SakerLog.warning().println("Java compilation parameter " + p + " argument is missing.");
					continue;
				}
				if (presentmodulepathparam != null) {
					presentmodulepathparam = presentmodulepathparam + File.pathSeparator + passparameters.get(i);
				} else {
					presentmodulepathparam = p;
				}
				continue;
			}
			if (p.equals("-bootclasspath") || p.equals("--boot-class-path")) {
				if (++i < paramssize) {
					//ignore
					SakerLog.warning().println("Java compilation parameter " + p + " argument is missing.");
					continue;
				}
				if (presentbootclasspathparam != null) {
					presentbootclasspathparam = presentbootclasspathparam + File.pathSeparator + passparameters.get(i);
				} else {
					presentbootclasspathparam = p;
				}
				continue;
			}

			if (p.equals("--release")) {
				hadrelease = true;
			}

			result.add(p);
		}

		result.add("-Xlint:all");
		//XXX don't remove processing lint for full compilation
		result.add("-Xlint:-processing");
		if (hadrelease) {
			//--release overrides the -source and -target arguments. this is also ensured with the task builder
			if (passsourceversion != null) {
				String sourceversion = sourceVersionToParameterString(passsourceversion);
				if (sourceversion == null) {
					throw new IllegalArgumentException("Source version not found: " + passsourceversion);
				}
				if (sourceversion != null) {
					result.add("-source");
					result.add(sourceversion);
				}
			}
			if (passtargetversion != null) {
				String targetversion = sourceVersionToParameterString(passtargetversion);
				if (targetversion == null) {
					throw new IllegalArgumentException("Target version not found: " + passtargetversion);
				}
				result.add("-target");
				result.add(targetversion);
			}
		}
		if (!passaddexports.isEmpty()) {
			//use a buffer set to prevent duplicates
			Set<String> addexports = new TreeSet<>();
			for (JavaAddExports ae : passaddexports) {
				for (String cmdline : JavaTaskUtils.toAddExportsCommandLineStrings(ae)) {
					addexports.add(cmdline);
				}
			}
			for (String cmdline : addexports) {
				result.add("--add-exports");
				result.add(cmdline);
			}
		}
		if (debuginfos == null) {
			//use the default, include all debugging information
			result.add("-g");
		} else {
			if (debuginfos.isEmpty()) {
				result.add("-g:none");
			} else {
				StringUtils.toStringJoin("-g:", ",", debuginfos, null);
			}
		}
		if (!parameternamespresent && parameternames) {
			result.add("-parameters");
		}

		if (!ObjectUtils.isNullOrEmpty(bootclasspathfiles)) {
			result.add("-bootclasspath");
			if (presentbootclasspathparam == null) {
				result.add(StringUtils.toStringJoin(File.pathSeparator, bootclasspathfiles));
			} else {
				result.add(presentbootclasspathparam + File.pathSeparator
						+ StringUtils.toStringJoin(File.pathSeparator, bootclasspathfiles));
			}
		} else if (presentbootclasspathparam != null) {
			result.add("-bootclasspath");
			result.add(presentbootclasspathparam);
		}
		if (!ObjectUtils.isNullOrEmpty(classpathfiles)) {
			result.add("-classpath");
			if (presentclasspathparam == null) {
				result.add(StringUtils.toStringJoin(File.pathSeparator, classpathfiles));
			} else {
				result.add(presentclasspathparam + File.pathSeparator
						+ StringUtils.toStringJoin(File.pathSeparator, classpathfiles));
			}
		} else if (presentclasspathparam != null) {
			result.add("-classpath");
			result.add(presentclasspathparam);
		} else {
			// if we dont set any classpath, current directory will be considered
			// current directory should not be ever considered during build execution

			//handle the absence of the classpath in the file managers
			outnocmdlineclasspath[0] = true;
		}
		if (!ObjectUtils.isNullOrEmpty(modulepathfiles)) {
			result.add("--module-path");
			if (presentmodulepathparam == null) {
				result.add(StringUtils.toStringJoin(File.pathSeparator, modulepathfiles));
			} else {
				result.add(presentmodulepathparam + File.pathSeparator
						+ StringUtils.toStringJoin(File.pathSeparator, modulepathfiles));
			}
		} else if (presentmodulepathparam != null) {
			result.add("--module-path");
			result.add(presentmodulepathparam);
		}
	}

	public abstract void build() throws Exception, JavaCompilationFailedException;

	public abstract Collection<? extends ClassPathIDEConfigurationEntry> getClassPathIDEConfigurationEntries();

	public abstract Collection<? extends ClassPathIDEConfigurationEntry> getBootClassPathIDEConfigurationEntries();

	public abstract Collection<? extends ModulePathIDEConfigurationEntry> getModulePathIDEConfigurationEntries();

	public abstract String getModuleName();

	public static boolean isNameCompatible(String simpleName, Kind kind, Kind thiskind, String thisfilename) {
		if (thiskind != kind) {
			return false;
		}
		//check if starts with the name and check the extension with ignored case
		if (thisfilename.length() != simpleName.length() + kind.extension.length()) {
			return false;
		}
		if (!thisfilename.startsWith(simpleName)) {
			return false;
		}
		if (!StringUtils.endsWithIgnoreCase(thisfilename, kind.extension)) {
			return false;
		}
		return true;
	}

	public static void putResultClassFilesToIncrementalInfo(NavigableMap<String, ? extends SakerFile> outputclasses,
			NavigableMap<String, SakerPath> classsourcefiles, NavigableMap<SakerPath, SourceFileData> sourcefiles,
			Map<SakerPath, ClassFileData> outclassfiledatas) {
//		ThreadUtils.runParallel(outputclasses.entrySet(), e -> {
		MessageDigest hasher = VersionKeyUtils.getMD5();
		for (Entry<String, ? extends SakerFile> entry : outputclasses.entrySet()) {
			String classname = entry.getKey();
			SakerFile cf = entry.getValue();
			SakerPath sourcefilepath = classsourcefiles.get(classname);

			ContentDescriptor contentdesc = cf.getContentDescriptor();
			SakerPath path = cf.getSakerPath();

			//we dont call getBytes, as that would need a synchronization to occurr. we don't want that yet
			//as this getBytesImpl call may require RMI calls, we could consider paralellizing this loop
			ByteArrayRegion cfbytes;
			try {
				cfbytes = cf.getBytesImpl();
			} catch (IOException e) {
				// XXX reify exception
				throw new UncheckedIOException(e);
			}

			byte[] abihash;
			if (VersionKeyUtils.updateAbiHashOfClassBytes(cfbytes, hasher)) {
				abihash = hasher.digest();
			} else {
				abihash = ObjectUtils.EMPTY_BYTE_ARRAY;
			}
			hasher.update(cfbytes.getArray(), cfbytes.getOffset(), cfbytes.getLength());
			byte[] implementationhash = hasher.digest();
			ClassFileData classdata = new ClassFileData(path, contentdesc, null, classname, abihash,
					implementationhash);
			if (sourcefilepath != null) {
				//sourcefile can be null if a class file was directly generated by a processor
				SourceFileData sfd = sourcefiles.get(sourcefilepath);
				sfd.addGeneratedClass(classdata);
				classdata.setSourceFile(sfd);
			}

			outclassfiledatas.put(path, classdata);
		}
//		});
	}

	public static void printDiagnosticEntry(DiagnosticEntry entry, DiagnosticLocation location) {
		if (location.getPath() == null && entry.getMessage() == null) {
			//no useful information in this
			return;
		}
		if (TestFlag.ENABLED) {
			TestFlag.metric().javacDiagnosticReported(entry.getMessage(), entry.getWarningType());
		}
		final SakerLog printer;
		switch (entry.getKind()) {
			case ERROR: {
				printer = SakerLog.error();
				break;
			}
			case MANDATORY_WARNING:
			case WARNING: {
				printer = SakerLog.warning();
				break;
			}
			case OTHER:
			case NOTE: {
				printer = SakerLog.info();
				break;
			}
			default: {
				printer = SakerLog.log();
				break;
			}
		}
		if (location.getPath() != null) {
			printer.path(location.getPath());
		}
		if (location.getLineNumber() >= 0) {
			printer.line(location.getLineNumber());
			if (location.getLinePositionStart() >= 0) {
				//subtract 1 from the position end to make it inclusive ending.
				printer.position(location.getLinePositionStart(), location.getLinePositionEnd() - 1);
			}
		}
		if (entry.getOrigin() != null) {
			printer.println(
					"(" + entry.getOrigin().getProcessorDetails().getProcessorName() + "): " + entry.getMessage());
		} else {
			printer.println(entry.getMessage());
		}
	}

	public static void printDiagnosticEntries(Collection<DiagnosticEntry> entries,
			DiagnosticPositionTable positiontable) {
		printDiagnosticEntries(entries, Collections.emptySet(), Collections.emptyMap(), positiontable);
	}

	public static void printDiagnosticEntries(Collection<DiagnosticEntry> entries, Collection<String> suppresswarnings,
			DiagnosticPositionTable positiontable) {
		printDiagnosticEntries(entries, suppresswarnings, Collections.emptyMap(), positiontable);
	}

	public static void printDiagnosticEntries(CompilationInfo info, Collection<String> suppresswarnings,
			Map<ProcessorDetails, Collection<String>> processorsuppresswarnings,
			DiagnosticPositionTable positiontable) {
		printDiagnosticEntries(info.getDiagnostics(), suppresswarnings, processorsuppresswarnings, positiontable);
	}

	public static void printDiagnosticEntries(Collection<DiagnosticEntry> entries, Collection<String> suppresswarnings,
			Map<ProcessorDetails, Collection<String>> processorsuppresswarnings,
			DiagnosticPositionTable positiontable) {
		if (entries.isEmpty()) {
			return;
		}
		if (suppresswarnings == null) {
			suppresswarnings = Collections.emptySet();
		}
		if (processorsuppresswarnings == null) {
			processorsuppresswarnings = Collections.emptyMap();
		}
		//sort the entries, and do not print duplicates
		NavigableMap<DiagnosticLocation, NavigableSet<DiagnosticEntry>> printentries = new TreeMap<>(
				DiagnosticLocation::compare);
//		NavigableSet<DiagnosticEntry> printentries = new TreeSet<>(DiagnosticEntry::compareWithoutOrigins);
		for (DiagnosticEntry entry : entries) {
			if (entry.getKind() != Diagnostic.Kind.ERROR) {
				//errors should not be suppressable, so only check the suppressions if they are less severe
				if (suppresswarnings.contains(entry.getWarningType())) {
					continue;
				}
				if (entry.getOrigin() != null) {
					Collection<String> sup = processorsuppresswarnings.get(entry.getOrigin().getProcessorDetails());
					if (sup != null && sup.contains(entry.getWarningType())) {
						continue;
					}
				}
			}

			DiagnosticLocationReference locref = entry.getLocationReference();
			DiagnosticLocation location;
			if (locref == null) {
				location = DiagnosticLocation.EMPTY_INSTANCE;
			} else {
				location = locref.getLocation(positiontable);
				if (location == null) {
					location = DiagnosticLocation.EMPTY_INSTANCE;
				}
			}

			printentries.computeIfAbsent(location, x -> new TreeSet<>(DiagnosticEntry::compareContents)).add(entry);
		}
		for (Entry<DiagnosticLocation, NavigableSet<DiagnosticEntry>> entry : printentries.entrySet()) {
			DiagnosticLocation location = entry.getKey();
			for (DiagnosticEntry diag : entry.getValue()) {
				printDiagnosticEntry(diag, location);
			}
		}
	}

	public static boolean hasDiagnosticError(Collection<DiagnosticEntry> entries) {
		for (DiagnosticEntry e : entries) {
			if (e.getKind() == Diagnostic.Kind.ERROR) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNativeMethodOverloaded(ExecutableElement method, Iterable<? extends Element> methods) {
		for (Element m : methods) {
			if (m.getKind() != ElementKind.METHOD || m == method || !m.getModifiers().contains(Modifier.NATIVE)) {
				continue;
			}
			if (m.getSimpleName().equals(method.getSimpleName())) {
				return true;
			}
		}
		return false;
	}

	public static SakerPath uriToFilePath(URI uri) {
		//XXX is SakerPath.valueOf okay here? (to determine the path from URI)

		String scheme = uri.getScheme();
		switch (scheme) {
			case "jar": {
				String specific = uri.getSchemeSpecificPart();
				if (specific.startsWith("file:/")) {
					int idx = specific.indexOf('!');
					return SakerPath.valueOf(specific.substring(6, idx));
				}
				throw new RuntimeException("Unknown jar protocol: " + specific);
			}
			case CompilationHandler.URI_SCHEME_GENERATED:
			case CompilationHandler.URI_SCHEME_INPUT: {
				return SakerPath.valueOf(uri.getSchemeSpecificPart());
			}
			default: {
				throw new IllegalArgumentException("Failed to convert URI to path: " + uri);
			}
		}
	}

	public static SakerPath getFileObjectPath(FileObject file) {
		if (file instanceof JavaCompilerFileObject) {
			return ((JavaCompilerFileObject) file).getFileObjectSakerPath();
		}
		if (file == null) {
			return null;
		}
		return CompilationHandler.uriToFilePath(file.toUri());
	}

	public static boolean isURIGenerated(URI uri) {
		return CompilationHandler.URI_SCHEME_GENERATED.equals(uri.getScheme());
	}

	public static String createUnrecognizedGeneralProcessorOptionsMessage(Set<String> options) {
		return "The following options were not recognized by any processor: \'["
				+ StringUtils.toStringJoin(", ", options) + "]\'";
	}

	public static String createUnrecognizedDirectProcessorOptionsMessage(Set<String> directunrecognizeds,
			String processorname) {
		return "The following direct options were not recognized by the processor " + processorname + ": \'["
				+ StringUtils.toStringJoin(", ", directunrecognizeds) + "]\'";
	}

	@Deprecated
	public static NavigableMap<SakerPath, SakerFile> getSourceFiles(TaskContext taskcontext,
			Collection<? extends JavaSourceDirectory> directories) {
		ConcurrentSkipListMap<SakerPath, SakerFile> result = new ConcurrentSkipListMap<>();
		Iterator<? extends JavaSourceDirectory> it = directories.iterator();
		if (it.hasNext()) {
			try (ThreadWorkPool wp = ThreadUtils.newFixedWorkPool()) {
				do {
					JavaSourceDirectory srcdir = it.next();
					SakerPath dir = srcdir.getDirectory();
					if (dir == null) {
						throw new IllegalArgumentException("Directory is null: " + srcdir);
					}
					SakerFile mf = SakerPathFiles.resolveAtPath(taskcontext, dir);
					if (mf == null || !(mf instanceof SakerDirectory)) {
						throw new IllegalArgumentException("Not a directory: " + dir + " in " + srcdir);
					}
					wp.offer(() -> {
						SakerDirectory mdir = (SakerDirectory) mf;
						Collection<? extends WildcardPath> files = srcdir.getFiles();
						if (files == null) {
							SakerPath dirpath = mdir.getSakerPath();
							result.putAll(mdir.getFilesRecursiveByPath(dirpath,
									new FileOnlyIgnoreCaseExtensionDirectoryVisitPredicate(".java")));
//							result.putAll(mdir.getFilesRecursiveByPath(f -> !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".java")));
//							mdir.collectFilesRecursive(result, f -> !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".java"));
						} else {
							for (WildcardPath wcpath : files) {
								SortedMap<SakerPath, SakerFile> gotfiles = wcpath.getFiles(null, mdir);
								//WildcardPath.getFiles(null, wcpath, mdir);
								if (gotfiles.isEmpty()) {
									SakerLog.warning().path(dir)
											.println("No source files found with pattern: " + wcpath);
								} else {
									result.putAll(gotfiles);
								}
							}
						}
					});
				} while (it.hasNext());
			}
		}
//		ThreadUtils.smartParallel(directories, srcdir -> {
//			SakerPath dir = srcdir.getDirectory();
//			if (dir == null) {
//				throw new IllegalArgumentException("Directory is null: " + srcdir);
//			}
//			SakerFile mf = SakerPathFiles.resolveAtPath(taskcontext, dir);
//			if (mf == null || !(mf instanceof SakerDirectory)) {
//				throw new IllegalArgumentException("Not a directory: " + dir + " in " + srcdir);
//			}
//			return () -> {
//				SakerDirectory mdir = (SakerDirectory) mf;
//				Collection<String> files = srcdir.getFiles();
//				if (files == null) {
//					SakerPath dirpath = mdir.getSakerPath();
//					result.putAll(mdir.getFilesRecursiveByPath(dirpath, new FileOnlyIgnoreCaseExtensionDirectoryVisitPredicate(".java")));
////					result.putAll(mdir.getFilesRecursiveByPath(f -> !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".java")));
////					mdir.collectFilesRecursive(result, f -> !f.isDirectory() && StringUtils.endsWithIgnoreCase(f.getName(), ".java"));
//				} else {
//					for (String wcpath : files) {
//						SortedMap<SakerPath, SakerFile> gotfiles = WildcardPath.valueOf(wcpath).getFiles(null, mdir);
//						//WildcardPath.getFiles(null, wcpath, mdir);
//						if (gotfiles.isEmpty()) {
//							SakerLog.warning().path(dir).println("No source files found with pattern: " + wcpath);
//						} else {
//							result.putAll(gotfiles);
//						}
//					}
//				}
//			};
//		});
		return result;
	}

	public static List<? extends Element> getPackageEnclosedElements(String packname,
			SortedMap<String, ? extends IncrementalTypeElement> canonicalTypeElements) {
		SortedMap<String, ? extends IncrementalTypeElement> map = canonicalTypeElements.tailMap(packname);
		List<Element> result = new ArrayList<>();
		for (Entry<String, ? extends IncrementalTypeElement> entry : map.entrySet()) {
			if (!entry.getKey().startsWith(packname)) {
				break;
			}
			IncrementalTypeElement te = entry.getValue();
			if (te.getNestingKind() != NestingKind.TOP_LEVEL) {
				continue;
			}
			if (te.getSignature().getPackageName().equals(packname)) {
				result.add(entry.getValue());
			}
		}
		return result;
	}

}
