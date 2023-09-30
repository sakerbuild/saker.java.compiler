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
package saker.java.compiler.impl.compile.handler.full;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.annotation.processing.Processor;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.TransformingNavigableMap;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.file.JavaCompilerDirectories;
import saker.java.compiler.impl.compile.file.JavaCompilerDirectories.IncrementalDirectoryFileRMIWrapper;
import saker.java.compiler.impl.compile.file.JavaCompilerDirectories.IncrementalDirectoryLocationRMIWrapper;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.NativeHeaderSakerFile;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytes;
import saker.java.compiler.impl.compile.signature.jni.NativeSignature;
import saker.java.compiler.impl.options.OutputBytecodeManipulationOption;

public class FullCompilationDirector {
	private FullCompilationInvoker invoker;

	private TaskContext taskContext;
	private SakerDirectory outputClassDirectory;
	private SakerDirectory outputNativeHeaderDirectory;
	private SakerDirectory outputSourceDirectory;
	private SakerDirectory outputResourceDirectory;
	private boolean generateNativeHeaders;
	private Collection<String> options;
	private List<Processor> processors;
	private NavigableMap<SakerPath, SakerFile> sourceFiles;

	private NavigableMap<SakerPath, ContentDescriptor> outputFileContents;
	private NavigableMap<SakerPath, ContentDescriptor> processorAccessedFileContents;

	private NavigableMap<String, SakerPath> processorInputLocations;

	private boolean noCommandLineClasspath = false;
	//true by default
	private boolean allowCommandLineBootClassPath = true;

	private OutputBytecodeManipulationOption bytecodeManipulation;

	public FullCompilationDirector(FullCompilationInvoker invoker) {
		this.invoker = invoker;
	}

	public void setOptions(TaskContext taskContext, SakerDirectory outputClassDirectory,
			SakerDirectory outputNativeHeaderDirectory, SakerDirectory outputSourceDirectory,
			SakerDirectory outputResourceDirectory, boolean generateNativeHeaders, Collection<String> options,
			List<Processor> processors, NavigableMap<SakerPath, SakerFile> sourceFiles,
			NavigableMap<String, SakerPath> processorInputLocations,
			OutputBytecodeManipulationOption bytecodeManipulation, boolean nocmdlineclasspath,
			boolean allowcommandlinebootclasspath) {
		this.taskContext = taskContext;
		this.outputClassDirectory = outputClassDirectory;
		this.outputNativeHeaderDirectory = outputNativeHeaderDirectory;
		this.outputSourceDirectory = outputSourceDirectory;
		this.outputResourceDirectory = outputResourceDirectory;
		this.generateNativeHeaders = generateNativeHeaders;
		this.options = options;
		this.processors = processors;
		this.sourceFiles = sourceFiles;
		this.processorInputLocations = processorInputLocations;
		this.bytecodeManipulation = bytecodeManipulation;
		this.noCommandLineClasspath = nocmdlineclasspath;
		this.allowCommandLineBootClassPath = allowcommandlinebootclasspath;
	}

	public void invokeCompilation() throws IOException, JavaCompilationFailedException {
		InputTrackingJavaCompilerDirectories directorypaths = new InputTrackingJavaCompilerDirectories(taskContext);
		directorypaths.setNoCommandLineClassPath(noCommandLineClasspath);
		directorypaths.setAllowCommandLineBootClassPath(allowCommandLineBootClassPath);
		directorypaths.setBytecodeManipulation(bytecodeManipulation);
		directorypaths.addDirectory(ExternalizableLocation.LOCATION_CLASS_OUTPUT, outputClassDirectory);
		directorypaths.addDirectory(ExternalizableLocation.LOCATION_SOURCE_OUTPUT, outputSourceDirectory);
		directorypaths.addDirectory(ExternalizableLocation.LOCATION_NATIVE_HEADER_OUTPUT, outputNativeHeaderDirectory);

		directorypaths.setResourceOutputDirectory(outputResourceDirectory);
		if (!ObjectUtils.isNullOrEmpty(processorInputLocations)) {
			TaskExecutionUtilities taskutils = taskContext.getTaskUtilities();
			for (Entry<String, SakerPath> entry : processorInputLocations.entrySet()) {
				String locname = entry.getKey();
				if (!JavaUtil.isValidProcessorInputLocationName(locname)) {
					throw new IllegalArgumentException("Invalid processor input location name: " + locname);
				}
				SakerDirectory dir = taskutils.resolveDirectoryAtPath(entry.getValue());
				if (dir != null) {
					directorypaths.addDirectory(new ExternalizableLocation(locname, false), dir);
				} else {
					SakerLog.warning().println(
							"Processor input location is not a directory: " + locname + " for " + entry.getValue());
				}
			}
		}
		invoker.setOptions(generateNativeHeaders, processors, options, directorypaths);
		SakerPathBytes[] sources = new SakerPathBytes[sourceFiles.size()];
		int i = 0;
		for (Entry<SakerPath, SakerFile> entry : sourceFiles.entrySet()) {
			sources[i++] = new SakerPathBytes(entry.getKey(), entry.getValue().getBytes());
		}
		invoker.compile(sources);
		ConcurrentNavigableMap<SakerPath, ? extends SakerFile> alloutputfiles = directorypaths.getAllOutputFiles();
		outputFileContents = new TreeMap<>(new FileContentDecriptorTransformingNavigableMap(alloutputfiles));

		NavigableMap<String, Collection<NativeSignature>> nativesignatures = invoker.getOutputHeaderNativeSignatures();
		if (!nativesignatures.isEmpty()) {
			SakerPath outputheaderdirsakerpath = outputNativeHeaderDirectory.getSakerPath();
			for (Entry<String, Collection<NativeSignature>> entry : nativesignatures.entrySet()) {
				String classbinaryname = entry.getKey();
				String fname = classbinaryname.replace('.', '_').replace('$', '_') + ".h";
				NativeHeaderSakerFile headerfile = new NativeHeaderSakerFile(fname, entry.getValue(), classbinaryname);
				outputNativeHeaderDirectory.add(headerfile);
				outputFileContents.put(outputheaderdirsakerpath.resolve(fname), headerfile.getContentDescriptor());
			}
		}
		processorAccessedFileContents = directorypaths.readContents;
	}

	public NavigableMap<SakerPath, ContentDescriptor> getProcessorAccessedFileContents() {
		return processorAccessedFileContents;
	}

	public NavigableMap<SakerPath, ContentDescriptor> getOutputFileContents() {
		return outputFileContents;
	}

	public byte[] getAbiVersionKeyHash() {
		return invoker.getAbiVersionKeyHash();
	}

	public byte[] getImplementationVersionKeyHash() {
		return invoker.getImplementationVersionKeyHash();
	}

	public String getModuleName() {
		return invoker.getModuleName();
	}

	private static class InputTrackingJavaCompilerDirectories extends JavaCompilerDirectories {
		protected ConcurrentSkipListMap<SakerPath, ContentDescriptor> readContents = new ConcurrentSkipListMap<>();

		public InputTrackingJavaCompilerDirectories(TaskContext taskContext) {
			super(taskContext);
		}

		@Override
		protected DirectoryLocationImpl createDirectoryLocation() {
			return new InputTrackingDirectoryLocationImpl();
		}

		@RMIWrap(IncrementalDirectoryLocationRMIWrapper.class)
		private class InputTrackingDirectoryLocationImpl extends DirectoryLocationImpl {
			@Override
			protected IncrementalDirectoryFile findFileWithResourcePath(SakerPath resourcepath) {
				for (Entry<SakerPath, SakerDirectory> entry : directories.entrySet()) {
					SakerDirectory dir = entry.getValue();
					SakerPath dirpath = entry.getKey();

					SakerFile file = taskUtils.resolveFileAtRelativePath(dir, resourcepath);
					if (file == null) {
						readContents.putIfAbsent(dirpath.resolve(resourcepath),
								CommonTaskContentDescriptors.IS_NOT_FILE);
						continue;
					}
					SakerPath fpath = dirpath.resolve(resourcepath);
					readContents.putIfAbsent(fpath, CommonTaskContentDescriptors.PRESENT);
					return new InputTrackingDirectoryResourceFileImpl(file, fpath);
				}
				return null;
			}
		}

		@RMIWrap(IncrementalDirectoryFileRMIWrapper.class)
		private class InputTrackingDirectoryResourceFileImpl extends DirectoryFileImpl {
			private SakerPath path;

			public InputTrackingDirectoryResourceFileImpl(SakerFile file, SakerPath fpath) {
				super(file, null);
				this.path = fpath;
			}

			@Override
			public SakerPath getPath() {
				return path;
			}

			@Override
			public ByteArrayRegion getBytes() throws IOException {
				readContents.replace(path, CommonTaskContentDescriptors.PRESENT, file.getContentDescriptor());
				return super.getBytes();
			}

		}
	}

	private static final class FileContentDecriptorTransformingNavigableMap
			extends TransformingNavigableMap<SakerPath, SakerFile, SakerPath, ContentDescriptor> {
		private FileContentDecriptorTransformingNavigableMap(
				NavigableMap<? extends SakerPath, ? extends SakerFile> map) {
			super(map);
		}

		protected static ContentDescriptor transformValue(SakerFile value) {
			return value.getContentDescriptor();
		}

		@Override
		protected Entry<SakerPath, ContentDescriptor> transformEntry(SakerPath key, SakerFile value) {
			return ImmutableUtils.makeImmutableMapEntry(key, transformValue(value));
		}
	}

}
