package saker.java.compiler.main;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaClassPathBuilder;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.modulepath.JavaModulePathBuilder;
import saker.java.compiler.impl.compile.CompileFileTags;
import saker.java.compiler.main.compile.option.ClassPathTaskOptionVisitor;
import saker.java.compiler.main.compile.option.JavaClassPathTaskOption;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;

public final class JavaTaskOptionUtils {
	public static JavaClassPath createClassPath(TaskContext taskcontext,
			Collection<? extends JavaClassPathTaskOption> classpathoptions) {
		if (classpathoptions == null) {
			return null;
		}
		JavaClassPathBuilder cpbuilder = JavaClassPathBuilder.newBuilder();
		for (JavaClassPathTaskOption cpoption : classpathoptions) {
			if (cpoption == null) {
				continue;
			}
			cpoption.accept(taskcontext, new ClassPathTaskOptionVisitor() {
				@Override
				public void visitFileLocation(FileLocation fileLocation) {
					cpbuilder.addFileClassPath(fileLocation);
				}

				@Override
				public void visitCompileClassPath(JavaCompilationWorkerTaskIdentifier compiletaskid) {
					cpbuilder.addCompileClassPath(compiletaskid);
				}

				@Override
				public void visitClassPathReference(ClassPathReference classpath) {
					cpbuilder.addClassPath(classpath);
				}

				@Override
				public void visitSDKPath(SDKPathReference sdkpath) {
					cpbuilder.addSDKClassPath(sdkpath);
				}

				@Override
				public void visitWildcard(WildcardPath wildcard) {
					FileCollectionStrategy collectionstrategy = WildcardFileCollectionStrategy
							.create(taskcontext.getTaskWorkingDirectoryPath(), wildcard);
					NavigableMap<SakerPath, SakerFile> cpfiles = taskcontext.getTaskUtilities()
							.collectFilesReportAdditionDependency(CompileFileTags.INPUT_CLASSPATH, collectionstrategy);
					for (Entry<SakerPath, SakerFile> entry : cpfiles.entrySet()) {
						SakerPath path = entry.getKey();
						ExecutionFileLocation filelocation = ExecutionFileLocation.create(path);
						cpbuilder.addFileClassPath(filelocation);
					}
					taskcontext.getTaskUtilities().reportInputFileDependency(CompileFileTags.INPUT_CLASSPATH,
							ObjectUtils.singleValueMap(cpfiles.navigableKeySet(),
									CommonTaskContentDescriptors.PRESENT));
				}
			});
		}
		JavaClassPath classpath = cpbuilder.build();
		return classpath;
	}

	public static JavaModulePath createModulePath(TaskContext taskcontext,
			Collection<? extends JavaClassPathTaskOption> modulepathoptions) {
		if (modulepathoptions == null) {
			return null;
		}
		JavaModulePathBuilder cpbuilder = JavaModulePathBuilder.newBuilder();
		for (JavaClassPathTaskOption cpoption : modulepathoptions) {
			if (cpoption == null) {
				continue;
			}
			cpoption.accept(taskcontext, new ClassPathTaskOptionVisitor() {
				@Override
				public void visitFileLocation(FileLocation fileLocation) {
					cpbuilder.addFileModulePath(fileLocation);
				}

				@Override
				public void visitCompileClassPath(JavaCompilationWorkerTaskIdentifier compiletaskid) {
					cpbuilder.addCompileModulePath(compiletaskid);
				}

				@Override
				public void visitSDKPath(SDKPathReference sdkpath) {
					cpbuilder.addSDKModulePath(sdkpath);
				}

				@Override
				public void visitWildcard(WildcardPath wildcard) {
					FileCollectionStrategy collectionstrategy = WildcardFileCollectionStrategy
							.create(taskcontext.getTaskWorkingDirectoryPath(), wildcard);
					NavigableMap<SakerPath, SakerFile> cpfiles = taskcontext.getTaskUtilities()
							.collectFilesReportAdditionDependency(CompileFileTags.INPUT_MODULEPATH, collectionstrategy);
					for (Entry<SakerPath, SakerFile> entry : cpfiles.entrySet()) {
						SakerPath path = entry.getKey();
						ExecutionFileLocation filelocation = ExecutionFileLocation.create(path);
						cpbuilder.addFileModulePath(filelocation);
					}
					taskcontext.getTaskUtilities().reportInputFileDependency(CompileFileTags.INPUT_MODULEPATH,
							ObjectUtils.singleValueMap(cpfiles.navigableKeySet(),
									CommonTaskContentDescriptors.PRESENT));
				}

				@Override
				public void visitClassPathReference(ClassPathReference classpath) {
					throw new UnsupportedOperationException("Class path reference is unsupported module path.");
				}
			});
		}
		JavaModulePath classpath = cpbuilder.build();
		return classpath;
	}

	private JavaTaskOptionUtils() {
		throw new UnsupportedOperationException();
	}

}
