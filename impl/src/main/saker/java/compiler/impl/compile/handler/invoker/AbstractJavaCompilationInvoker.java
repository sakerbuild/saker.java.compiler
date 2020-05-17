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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTool;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.diagnostic.CompilerDiagnosticListener;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.info.ClassFileData;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureData;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.compile.handler.info.SignatureSourcePositions;
import saker.java.compiler.impl.compile.handler.info.SourceFileData;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.parser.CompilationUnitSignatureParserBase.ParseContextBase;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;
import saker.java.compiler.jdk.impl.parser.usage.AbiUsageParser;

public abstract class AbstractJavaCompilationInvoker implements JavaCompilationInvoker {

	protected JavaCompilerInvocationDirector director;

	private Map<CompilationUnitTree, Map<? extends Tree, ? extends Signature>> unitTreeSignatures = new ConcurrentHashMap<>();
	private NavigableMap<SakerPath, ABIParseInfo> abiUsages = new ConcurrentSkipListMap<>();

	private ConcurrentNavigableMap<String, SakerPath> classBinaryNameSourcePaths = new ConcurrentSkipListMap<>();

	protected JavaFileManager fileManager;
	private boolean errorDiagnosticReported = false;

	protected ParserCache cache = new ParserCache();

	private DiagnosticListener<JavaFileObject> diagnosticListener = new DiagnosticListener<JavaFileObject>() {
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
				errorDiagnosticReported = true;
			}
			director.reportDiagnostic(CompilerDiagnosticListener.convertToDiagnosticEntry(diagnostic));
		}
	};

	public AbstractJavaCompilationInvoker() {
	}

	public ParserCache getParserCache() {
		return cache;
	}

	@Override
	public void initCompilation(JavaCompilerInvocationDirector director) throws IOException {
		this.director = director;
		this.fileManager = JavaCompilationUtils.createFileManager(
				JavacTool.create().getStandardFileManager(getDiagnosticListener(), null, StandardCharsets.UTF_8),
				director.getDirectoryPaths());
	}

	@Override
	public void invokeCompilation(SakerPathBytes[] units) throws IOException {
		JavaCompilerJavaFileObject[] unitobjects = new JavaCompilerJavaFileObject[units.length];
		for (int i = 0; i < unitobjects.length; i++) {
			unitobjects[i] = new SakerPathBytesJavaInputFileObject(units[i], Kind.SOURCE, null);
		}
		invokeCompilationImpl(unitobjects);
		director.addGeneratedClassFilesForSourceFiles(classBinaryNameSourcePaths);
	}

	@Override
	public JavaFileManager getJavaFileManager() {
		return fileManager;
	}

	protected abstract void invokeCompilationImpl(JavaCompilerJavaFileObject[] units) throws IOException;

	protected DiagnosticListener<JavaFileObject> getDiagnosticListener() {
		return diagnosticListener;
	}

	protected boolean isAnyErrorRaised() {
		if (errorDiagnosticReported) {
			return true;
		}
		errorDiagnosticReported = director.isAnyErrorRaised();
		return errorDiagnosticReported;
	}

	public boolean compilationRound() {
		return director.compilationRound();
	}

	@Override
	public NavigableMap<SakerPath, ABIParseInfo> getParsedSourceABIUsages() {
		return abiUsages;
	}

	protected void addGeneratedClassFileForSourceFile(String classbinaryname, JavaFileObject sourcefile) {
		SakerPath sourcefilepath = CompilationHandler.getFileObjectPath(sourcefile);
		classBinaryNameSourcePaths.put(classbinaryname, sourcefilepath);
	}

	private static String getFileNameOfCompilationUnit(CompilationUnitTree unit) {
		String name = unit.getSourceFile().getName();
		int idx = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return idx < 0 ? name : name.substring(0, idx);
	}

	protected void parseABIUsages(Trees trees) {
		AbiUsageParser abiusageparser = JavaCompilationUtils.createAbiUsageParser(trees, getSourceVersionName(), cache);

		ThreadUtils.runParallelItems(unitTreeSignatures.entrySet(), entry -> {
			CompilationUnitTree unit = entry.getKey();
			Map<? extends Tree, ? extends Signature> treesignatures = entry.getValue();

			TopLevelAbiUsage parsedusage = abiusageparser.parse(unit, treesignatures);

			String filename = getFileNameOfCompilationUnit(unit);
			ABIParseInfo info = new ABIParseInfoImpl(parsedusage, getRealizedSignatures(unit, trees, filename, cache));

			abiUsages.put(CompilationHandler.getFileObjectPath(unit.getSourceFile()), info);
		});
	}

	public static RealizedSignatureData getRealizedSignatures(CompilationUnitTree unit, Trees trees, String filename,
			ParserCache cache) {
		return JavaCompilationUtils.getRealizedSignatures(unit, trees, filename, cache);
	}

	public static void setRealizedSignatures(CompilationUnitTree unit, Trees trees, SourceFileData sfd,
			ParserCache cache) {
		sfd.setRealizedSignatures(getRealizedSignatures(unit, trees, sfd.getPath().getFileName(), cache));
	}

	protected ClassHoldingData handleParsedFileSignature(CompilationUnitTree unit, ParseContextBase parsedsignature,
			SakerPath filepath, JavaCompilerJavaFileObject fileobject, SourcePositions javacsourcepositions,
			ImportScope importscope) {
		PackageSignature packagesignature = parsedsignature.getPackageSignature();
		ModuleSignature modulesignatures = parsedsignature.getModuleSignature();
		NavigableMap<String, ClassSignature> classes = parsedsignature.getClasses();
		String packagename = parsedsignature.getPackageName();

		Map<? extends Tree, ? extends Signature> treesignatures = parsedsignature.getTreeSignatures();
		unitTreeSignatures.put(unit, treesignatures);

		Map<? extends Tree, ? extends SignaturePath> treesignaturepaths = parsedsignature.getTreeSignaturePaths();

		SignatureSourcePositions sourcepositions = null;
		if (javacsourcepositions != null) {
			int[] lineindices = fileobject.getLineIndexMap();
			if (lineindices != null) {
				sourcepositions = new SignatureSourcePositions();
				for (Entry<? extends Tree, ? extends SignaturePath> entry : treesignaturepaths.entrySet()) {
					Tree tree = entry.getKey();
					int startpos = (int) javacsourcepositions.getStartPosition(unit, tree);
					if (startpos != Diagnostic.NOPOS) {
						int endpos = (int) javacsourcepositions.getEndPosition(unit, tree);
						int lineidx = StringUtils.getLineIndex(lineindices, startpos);
						int linepositionidx = StringUtils.getLinePositionIndex(lineindices, lineidx, startpos);

						SignaturePath sig = entry.getValue();
						sourcepositions.putPosition(sig, startpos, endpos, lineidx, linepositionidx);
					}
				}
			}
		}
		return new ParsedFileClassHoldingData(filepath, packagename, classes, packagesignature, modulesignatures,
				sourcepositions, importscope);
	}

	public static final class ParsedFileClassHoldingData implements ClassHoldingData, Externalizable {
		private static final long serialVersionUID = 1L;

		private SakerPath filePath;
		private NavigableMap<String, ? extends ClassSignature> classes;
		private PackageSignature packageSignature;
		private ModuleSignature moduleSignature;
		private SignatureSourcePositions sourcePositions;
		private ImportScope importScope;

		/**
		 * For externalizable.
		 */
		public ParsedFileClassHoldingData() {
		}

		public ParsedFileClassHoldingData(SakerPath filePath, String packageName,
				NavigableMap<String, ? extends ClassSignature> classes, PackageSignature packageSignature,
				ModuleSignature moduleSignature, SignatureSourcePositions sourcePositions, ImportScope importScope) {
			this.filePath = filePath;
			this.classes = classes;
			this.packageSignature = packageSignature;
			this.moduleSignature = moduleSignature;
			this.sourcePositions = sourcePositions;
			this.importScope = importScope;
		}

		@Override
		public ImportScope getImportScope() {
			return importScope;
		}

		@Override
		public SakerPath getPath() {
			return filePath;
		}

		@Override
		public PackageSignature getRealizedPackageSignature() {
			return null;
		}

		@Override
		public NavigableMap<String, ? extends ClassSignature> getRealizedClasses() {
			return Collections.emptyNavigableMap();
		}

		@Override
		public PackageSignature getPackageSignature() {
			return packageSignature;
		}

		@Override
		public String getPackageName() {
			return importScope.getPackageName();
		}

		@Override
		public ModuleSignature getModuleSignature() {
			return moduleSignature;
		}

		@Override
		public ModuleSignature getRealizedModuleSignature() {
			return null;
		}

		@Override
		public NavigableMap<SakerPath, ClassFileData> getGeneratedClassDatas() {
			return Collections.emptyNavigableMap();
		}

		@Override
		public NavigableMap<String, ? extends ClassSignature> getClasses() {
			return classes;
		}

		@Override
		public TopLevelAbiUsage getABIUsage() {
			return null;
		}

		@Override
		public SignatureSourcePositions getSourcePositions() {
			return sourcePositions;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(filePath);
			out.writeObject(packageSignature);
			out.writeObject(moduleSignature);
			out.writeObject(sourcePositions);
			out.writeObject(importScope);

			SerialUtils.writeExternalMap(out, classes);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			filePath = (SakerPath) in.readObject();
			packageSignature = (PackageSignature) in.readObject();
			moduleSignature = (ModuleSignature) in.readObject();
			sourcePositions = (SignatureSourcePositions) in.readObject();
			importScope = (ImportScope) in.readObject();

			classes = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		}
	}
}
