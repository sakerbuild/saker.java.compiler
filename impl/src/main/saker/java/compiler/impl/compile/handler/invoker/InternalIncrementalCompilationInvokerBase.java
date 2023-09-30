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
package saker.java.compiler.impl.compile.handler.invoker;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ConcurrentPrependAccumulator;
import saker.build.thirdparty.saker.util.ConcurrentPrependEntryAccumulator;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.IOUtils;
import saker.build.thirdparty.saker.util.io.ResourceCloser;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils.ThreadWorkPool;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.incremental.JavacPrivateAPIError;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.signature.parser.CompilationUnitSignatureParserBase.ParseContextBase;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;
import testing.saker.java.compiler.TestFlag;

public abstract class InternalIncrementalCompilationInvokerBase extends AbstractJavaCompilationInvoker {
	protected Context context;
	protected JavaCompiler javac;
	protected SourceVersion sourceVersion;
	protected ListBuffer<JCCompilationUnit> parsedTrees;
	protected ConcurrentPrependAccumulator<JavaCompilerJavaFileObject> roundAddedSources;
	protected ConcurrentPrependEntryAccumulator<String, SakerPath> roundAddedClassFiles;

	/**
	 * The file manager provided by javac.
	 */
	protected StandardJavaFileManager javacFileManager;
	/**
	 * The file manager that we actually use and give to javac.
	 */
	protected JavaFileManager fileManager;

	protected CompilationUnitSignatureParser signatureParser;
	protected SourcePositions sourcePositions;

	protected final ResourceCloser resourceCloser = new ResourceCloser();

	protected TaskListener classGenerationTaskListener = new TaskListener() {
		@Override
		public void started(TaskEvent e) {
		}

		@Override
		public void finished(TaskEvent e) {
			switch (e.getKind()) {
				case GENERATE: {
					TypeSymbol typeelem = (TypeSymbol) e.getTypeElement();
					String binaryname;
					if (typeelem.getSimpleName().contentEquals("module-info")) {
						binaryname = "module-info";
					} else {
						binaryname = typeelem.flatName().toString();
					}
					JavaFileObject srcfile = e.getSourceFile();
					addGeneratedClassFileForSourceFile(binaryname, srcfile);
					break;
				}
				default: {
					break;
				}
			}
		}
	};

	@Override
	public CompilationInitResultData initCompilation(JavaCompilerInvocationDirector director,
			IncrementalDirectoryPaths directorypaths, String[] options, String sourceversionoptionname,
			String targetversionoptionname) throws IOException {
		super.initCompilation(director, directorypaths, options, sourceversionoptionname, targetversionoptionname);
		context = new Context();
		context.put(DiagnosticListener.class, getDiagnosticListener());
		context.put(Locale.class, (Locale) null);

		//result is created by subclass
		return null;
	}

	@Override
	public void invokeCompilationImpl(JavaCompilerJavaFileObject[] units) throws IOException {
		try {
			roundAddedSources = new ConcurrentPrependAccumulator<>(units);
			roundAddedClassFiles = new ConcurrentPrependEntryAccumulator<>();
			parsedTrees = new ListBuffer<>();

			invokeInternalCompilation();

			JavaFileManager fm = fileManager;
			if (fm != null) {
				//shouldn't be null, but check so we don't get surprised
				fm.flush();
			}
		} catch (LinkageError | AssertionError e) {
			//in case of a method access error or others
			throw new JavacPrivateAPIError(e);
		} finally {
			closeJavaCompiler(this.javac);
		}
	}

	@Override
	public void close() throws IOException {
		//close the resources
		IOUtils.close(resourceCloser);
	}

	@Override
	public String getSourceVersionName() {
		return sourceVersion.name();
	}

	private static void closeJavaCompiler(JavaCompiler jc) {
		if (jc == null) {
			return;
		}
		if (TestFlag.ENABLED) {
			TestFlag.metric().javacAddCompilationFinishClosing(jc::close);
			return;
		}
		jc.close();
	}

	protected abstract void invokeInternalCompilation();

	@Override
	public Elements getElements() {
		return JavacElements.instance(context);
	}

	@Override
	public Types getTypes() {
		return JavacTypes.instance(context);
	}

	@Override
	public void addSourceForCompilation(String sourcename, SakerPath file) {
		roundAddedSources.add(new SakerPathJavaInputFileObject(directoryPaths, file, Kind.SOURCE, sourcename));
	}

	@Override
	public void addClassFileForCompilation(String classname, SakerPath file) {
		roundAddedClassFiles.add(classname, file);
	}

	@Override
	public Collection<ClassHoldingData> parseRoundAddedSources() {
		ConcurrentSkipListMap<SakerPath, ClassHoldingData> result = new ConcurrentSkipListMap<>();
		addRoundAddedSourceFiles(result);
		return ImmutableUtils.makeImmutableList(result.values());
	}

	@Override
	public Collection<ClassHoldingData> parseRoundAddedClassFiles() {
		ConcurrentSkipListMap<SakerPath, ClassHoldingData> result = new ConcurrentSkipListMap<>();
		addRoundAddedClassFiles(result);
		return ImmutableUtils.makeImmutableList(result.values());
	}

	private void addRoundAddedSourceFiles(ConcurrentSkipListMap<SakerPath, ? super ParsedFileClassHoldingData> result) {
		if (roundAddedSources.isEmpty()) {
			return;
		}
		Iterator<JavaCompilerJavaFileObject> it = roundAddedSources.clearAndIterator();
		if (it.hasNext()) {
			//TODO test if using thread pools is actually faster
			try (ThreadWorkPool wp = ThreadUtils.newFixedWorkPool()) {
				do {
					JavaCompilerJavaFileObject unit = it.next();
					JCCompilationUnit cunittree = javac.parse(unit);
					parsedTrees.add(cunittree);

					wp.offer(() -> {
						ParsedFileClassHoldingData sfd = parseCompilationUnitSignature(unit, cunittree);

						result.put(sfd.getPath(), sfd);

						compilationUnitSignatureParsed(cunittree, sfd);
					});
				} while (it.hasNext());
			}
		}
//		ThreadUtils.smartParallel(it, unit -> {
//			JCCompilationUnit cunittree = javac.parse(unit);
//			parsedTrees.add(cunittree);
//
//			return () -> {
//				ClassHoldingData sfd = parseCompilationUnitSignature(unit, cunittree);
//
//				result.put(sfd.getPath(), sfd);
//
//				compilationUnitSignatureParsed(cunittree, sfd);
//			};
//		});
	}

	protected void compilationUnitSignatureParsed(JCCompilationUnit tree, ClassHoldingData sfd) {
	}

	private void addRoundAddedClassFiles(Map<SakerPath, ClassHoldingData> result) {
		//TODO support generating class files
		if (roundAddedClassFiles.isEmpty()) {
			return;
		}
		throw new UnsupportedOperationException("Generating class files is not yet supported.");
//		ClassReader reader = ClassReader.instance(context);
//		Names names = Names.instance(context);
//
//		Map<SakerFile, TypeSymbol> createdsymbols = new IdentityHashMap<>();
//
//		for (Iterator<Entry<String, SakerFile>> it = roundAddedClassFiles.clearAndIterator(); it.hasNext();) {
//			Entry<String, SakerFile> entry = it.next();
//			String classname = entry.getKey();
//			SakerFile file = entry.getValue();
//
//			SakerFileWrapperJavaFileObject fileobj = new SakerFileWrapperJavaFileObject(file, Kind.CLASS, classname);
//
//			Name cname = names.fromString(classname);
//			ClassSymbol csym = reader.enterClass(cname, fileobj);
//
//			TypeSymbol tsym = csym;
//			if (fileobj.isNameCompatible("package-info", Kind.CLASS)) {
//				PackageSymbol p = reader.enterPackage(names.fromString(Convert.packagePart(classname)));
//				if (p.package_info == null) {
//					p.package_info = csym;
//				}
//				tsym = p;
//			}
//
//			System.out.println("InternalIncrementalCompilationInvoker.parseRoundAddedSources() " + csym + " - " + csym.flatName());
//
//			createdsymbols.put(file, tsym);
//		}
//
//		for (Iterator<Entry<SakerFile, TypeSymbol>> it = createdsymbols.entrySet().iterator(); it.hasNext();) {
//			Entry<SakerFile, TypeSymbol> entry = it.next();
//			SakerFile file = entry.getKey();
//			TypeSymbol tsym = entry.getValue();
//			tsym.complete();
//			Signature signature = null;
//			if (tsym.kind == Kinds.TYP) {
//				ClassSymbol csym = (ClassSymbol) tsym;
//				NestingKind nestkind = csym.getNestingKind();
//				if (nestkind == NestingKind.TOP_LEVEL) {
//					signature = IncrementalElementsTypes.createSignatureFromJavacElement(tsym);
//				} else {
//					continue;
//				}
//			} else {
//				signature = IncrementalElementsTypes.createSignatureFromJavacElement(tsym);
//			}
//			it.remove();
//			GeneratedClassFileData cfd = new GeneratedClassFileData(file.getSakerPath(), file.getContentDescriptor(), null, tsym.flatName().toString(),
//					signature);
//
//			result.put(cfd.getPath(), cfd);
//
//			handleAnalyzedGeneratedClassFile(cfd);
//		}
//		for (Entry<SakerFile, TypeSymbol> entry : createdsymbols.entrySet()) {
//			//only member, anonymous and local classes
//			System.out.println("InternalIncrementalCompilationInvoker.parseRoundAddedSources() " + entry.getKey());
//			ClassSymbol csym = (ClassSymbol) entry.getValue();
//			System.out.println("InternalIncrementalCompilationInvoker.parseRoundAddedSources() " + csym.flatName() + " - " + csym.getQualifiedName() + " - "
//					+ csym.getSimpleName());
//		}
	}

	private ParsedFileClassHoldingData parseCompilationUnitSignature(JavaCompilerJavaFileObject unit,
			CompilationUnitTree cunittree) {
		ParseContextBase parsedsignature = signatureParser.parse(cunittree);

		SakerPath filepath = unit.getFileObjectSakerPath();

		return handleParsedFileSignature(cunittree, parsedsignature, filepath, unit, sourcePositions,
				parsedsignature.getImportScope());
	}

}
