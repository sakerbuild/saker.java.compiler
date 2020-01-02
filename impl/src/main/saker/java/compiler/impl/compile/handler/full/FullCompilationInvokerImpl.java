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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.util.java.JavaTools;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.impl.compile.VersionKeyUtils;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.JavaCompilerOutputJavaFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.diagnostic.CompilerDiagnosticListener;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticLocation;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticPositionTable;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytes;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytesJavaInputFileObject;
import saker.java.compiler.impl.compile.signature.jni.NativeSignature;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;

public class FullCompilationInvokerImpl implements FullCompilationInvoker {
	private boolean generateNativeHeaders;
	private List<? extends Processor> processors;
	private Collection<String> options;
	private IncrementalDirectoryPaths directoryPaths;

	private NavigableMap<String, Collection<NativeSignature>> outputHeaderNativeSignatures;
	private byte[] implementationHash;
	private byte[] abiVersionKeyHash;
	private String outModuleName;

	public FullCompilationInvokerImpl() {
	}

	@Override
	public void setOptions(boolean generateNativeHeaders, List<Processor> processors, Collection<String> options,
			IncrementalDirectoryPaths directorypaths) {
		this.generateNativeHeaders = generateNativeHeaders;
		this.processors = processors;
		this.options = options;
		this.directoryPaths = directorypaths;
	}

	private static class EmptyDiagnosticPositionTable implements DiagnosticPositionTable {
		public static final EmptyDiagnosticPositionTable INSTANCE = new EmptyDiagnosticPositionTable();

		@Override
		public DiagnosticLocation getForPathSignature(SakerPath path, SignaturePath signature) {
			return null;
		}
	}

	@Override
	public void compile(SakerPathBytes[] sources) throws IOException, JavaCompilationFailedException {
		System.out.println("Compiling " + sources.length + " source files.");
		Iterable<? extends JavaFileObject> units = toCompilationSourceFileUnits(sources);

		JavaCompiler compiler = JavaTools.getSystemJavaCompiler();
		try (StandardJavaFileManager standardfilemanager = compiler.getStandardFileManager(null, null,
				StandardCharsets.UTF_8);
				JavaFileManager manager = JavaCompilationUtils.createFileManager(standardfilemanager, directoryPaths)) {

			CompilerDiagnosticListener diagnosticlistener = new CompilerDiagnosticListener();
			JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnosticlistener, options, null,
					units);
			JavacTask javac = (JavacTask) task;
			task.setProcessors(this.processors);

			Iterable<? extends CompilationUnitTree> compilationunits = javac.parse();

			if (diagnosticlistener.hadError()) {
				CompilationHandler.printDiagnosticEntries(diagnosticlistener.getDiagnostics(), Collections.emptySet(),
						EmptyDiagnosticPositionTable.INSTANCE);
				throw new JavaCompilationFailedException("Java compilation failed.");
			}

			Iterable<? extends Element> analyzedelements = javac.analyze();

			if (diagnosticlistener.hadError()) {
				CompilationHandler.printDiagnosticEntries(diagnosticlistener.getDiagnostics(), Collections.emptySet(),
						EmptyDiagnosticPositionTable.INSTANCE);
				throw new JavaCompilationFailedException("Java compilation failed.");
			}

			//find the module name if any that was compiled
			for (Element elem : analyzedelements) {
				if (!JavaCompilationUtils.isModuleElementKind(elem.getKind())) {
					continue;
				}
				outModuleName = ((QualifiedNameable) elem).getQualifiedName().toString();
				break;
			}

			if (generateNativeHeaders) {
				System.out.println("Generating native headers.");
				Trees trees = Trees.instance(javac);
				Types types = javac.getTypes();
				Elements elementsutil = javac.getElements();

				NativeHeaderCompilationUnitSignatureVisitor headervisitor = new NativeHeaderCompilationUnitSignatureVisitor(
						trees, types, elementsutil);
				NavigableMap<String, Collection<NativeSignature>> nativesignatures = new TreeMap<>();
				for (CompilationUnitTree unit : compilationunits) {
					Map<String, Collection<NativeSignature>> unitnativesignatures = headervisitor
							.getNativeSignatures(unit);
					if (!unitnativesignatures.isEmpty()) {
						nativesignatures.putAll(unitnativesignatures);
					}
				}
				outputHeaderNativeSignatures = nativesignatures;
			} else {
				outputHeaderNativeSignatures = Collections.emptyNavigableMap();
			}

			Iterable<? extends JavaFileObject> outputfiles = javac.generate();

			CompilationHandler.printDiagnosticEntries(diagnosticlistener.getDiagnostics(), Collections.emptySet(),
					EmptyDiagnosticPositionTable.INSTANCE);
			if (diagnosticlistener.hadError()) {
				throw new JavaCompilationFailedException("Java compilation failed.");
			}
			implementationHash = generateImplementationHashFromOutputFiles(outputfiles);
			abiVersionKeyHash = generateAbiHashFromOutputFiles(outputfiles);
		}
	}

	@Override
	public byte[] getAbiVersionKeyHash() {
		return abiVersionKeyHash;
	}

	@Override
	public byte[] getImplementationVersionKeyHash() {
		return implementationHash;
	}

	@Override
	public String getModuleName() {
		return outModuleName;
	}

	private static byte[] generateAbiHashFromOutputFiles(Iterable<? extends JavaFileObject> outputfiles) {
		Iterator<? extends JavaFileObject> it = outputfiles.iterator();
		if (!it.hasNext()) {
			return ObjectUtils.EMPTY_BYTE_ARRAY;
		}
		MessageDigest hasher = VersionKeyUtils.getMD5();
		TreeMap<String, byte[]> classhashes = new TreeMap<>();
		while (true) {
			JavaFileObject jfo = it.next();
			JavaCompilerOutputJavaFileObject compileroutfile = ((JavaCompilerOutputJavaFileObject) jfo);
			ByteArrayRegion outbytes = compileroutfile.getOutputBytes();

			byte[] hash;
			if (VersionKeyUtils.updateAbiHashOfClassBytes(outbytes, hasher)) {
				hash = hasher.digest();
			} else {
				hash = ObjectUtils.EMPTY_BYTE_ARRAY;
			}
			classhashes.put(compileroutfile.getClassName(), hash);

			if (!it.hasNext()) {
				break;
			}
		}

		for (byte[] h : classhashes.values()) {
			hasher.update(h);
		}

		return hasher.digest();
	}

	private static byte[] generateImplementationHashFromOutputFiles(Iterable<? extends JavaFileObject> outputfiles) {
		Iterator<? extends JavaFileObject> it = outputfiles.iterator();
		if (!it.hasNext()) {
			return ObjectUtils.EMPTY_BYTE_ARRAY;
		}
		MessageDigest hasher = VersionKeyUtils.getMD5();
		TreeMap<String, byte[]> classhashes = new TreeMap<>();
		while (true) {
			JavaFileObject jfo = it.next();
			JavaCompilerOutputJavaFileObject compileroutfile = ((JavaCompilerOutputJavaFileObject) jfo);
			ByteArrayRegion outbytes = compileroutfile.getOutputBytes();

			hasher.update(outbytes.getArray(), outbytes.getOffset(), outbytes.getLength());

			classhashes.put(compileroutfile.getClassName(), hasher.digest());
			if (!it.hasNext()) {
				break;
			}
		}

		for (byte[] h : classhashes.values()) {
			hasher.update(h);
		}

		return hasher.digest();
	}

	@Override
	public NavigableMap<String, Collection<NativeSignature>> getOutputHeaderNativeSignatures() {
		return outputHeaderNativeSignatures;
	}

	private static Iterable<? extends JavaFileObject> toCompilationSourceFileUnits(SakerPathBytes[] sources) {
		JavaFileObject[] sourcefileobjects = new JavaFileObject[sources.length];
		for (int i = 0; i < sources.length; i++) {
			sourcefileobjects[i] = new SakerPathBytesJavaInputFileObject(sources[i], Kind.SOURCE, null);
		}
		Iterable<? extends JavaFileObject> units = ImmutableUtils.unmodifiableArrayList(sourcefileobjects);
		return units;
	}
}
