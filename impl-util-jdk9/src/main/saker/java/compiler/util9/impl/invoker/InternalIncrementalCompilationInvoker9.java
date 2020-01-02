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
package saker.java.compiler.util9.impl.invoker;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.NestingKind;
import javax.lang.model.util.Elements;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.code.Scope.WriteableScope;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.main.Arguments;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.java.JavaTools;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.handler.info.ClassGenerationInfo;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.invoker.InternalIncrementalCompilationInvokerBase;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilerInvocationDirector;
import saker.java.compiler.impl.compile.handler.invoker.PreviousCompilationClassInfo;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathJavaInputFileObject;
import saker.java.compiler.impl.signature.element.ClassSignatureHeader;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignatureKind;
import saker.java.compiler.impl.signature.element.ModuleSignature.RequiresDirectiveSignature;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;

public class InternalIncrementalCompilationInvoker9 extends InternalIncrementalCompilationInvokerBase {
	private static final AtomicReferenceFieldUpdater<InternalIncrementalCompilationInvoker9, ClassHoldingData> ARFU_moduleHoldingFile = AtomicReferenceFieldUpdater
			.newUpdater(InternalIncrementalCompilationInvoker9.class, ClassHoldingData.class, "moduleHoldingFile");

	private boolean useModules;
	private boolean modulesInited = false;
	private volatile ClassHoldingData moduleHoldingFile;
	private volatile JCCompilationUnit moduleHoldingTree;
	private ModuleSymbol defaultModule;

	@Override
	public void initCompilation(JavaCompilerInvocationDirector director) throws IOException {
		super.initCompilation(director);
		context = new Context();
		context.put(DiagnosticListener.class, getDiagnosticListener());
		context.put(JavaFileManager.class, fileManager);

		Arguments args = Arguments.instance(context);
		java.util.List<String> options = ObjectUtils.newArrayList(director.getOptions());
		if (JavaTools.getCurrentJavaMajorVersion() < 11) {
			options.remove("--enable-preview");
		}
		args.init("saker-with-javac", options, null, null);
		//XXX we may need to call args.validate(), however it is fine to not do it

		final JavaCompiler javac = JavaCompiler.instance(context);
		this.javac = javac;
		javac.keepComments = true;
		javac.genEndPos = true;

		super.sourceVersion = Source.toSourceVersion(Source.instance(context));

		useModules = JavaUtil.isModuleSupportingSourceVersion(getSourceVersionName());

		//XXX set these options for javac?
		// ClassReader.instance(context).saveParameterNames = true
		// as seen in JavaCompiler.initProcessAnnotations
	}

	@Override
	protected void invokeInternalCompilation() {
		JavacTrees trees = JavacTrees.instance(context);
		signatureParser = new CompilationUnitSignatureParser(trees, getSourceVersionName(), cache);
		signatureParser.setElementQueryAPIEnabled(false);
		sourcePositions = trees.getSourcePositions();

		MultiTaskListener mtl = MultiTaskListener.instance(context);
		mtl.add(classGenerationTaskListener);

		Annotate annotate = Annotate.instance(context);
		//block annotation processing
		//not blocking it can cause assertion errors and invalid annotation types in javac
		//this is caused as we are using a hack to call initModules and be able to manually process the annotations
		annotate.blockAnnotations();

		//in case of java 9
		//we need to always parse a module-info if there is any and call init modules with that
		//we need to call enterTrees before any annotation processing
		//else getTypeElement will throw an exception

		while (compilationRound()) {
			//loop
		}

		if (isAnyErrorRaised()) {
			return;
		}
		for (JCCompilationUnit tree : parsedTrees) {
			tree.modle = defaultModule;
		}
		javac.enterTrees(parsedTrees.toList());
		if (isAnyErrorRaised()) {
			return;
		}

		Queue<Env<AttrContext>> atributed = javac.attribute(javac.todo);
		if (isAnyErrorRaised()) {
			return;
		}
		annotate.unblockAnnotations();
		Queue<Env<AttrContext>> flowed = javac.flow(atributed);

		if (isAnyErrorRaised()) {
			return;
		}

		parseABIUsages(signatureParser.getTrees());

		javac.generate(javac.desugar(flowed));

		javac.reportDeferredDiagnostics();
	}

	@Override
	public Collection<ClassHoldingData> parseRoundAddedSources() {
		Collection<ClassHoldingData> result = super.parseRoundAddedSources();
		initModules();
		return result;
	}

	@Override
	public Elements getElements() {
		initModules();
		return super.getElements();
	}

	private void initModules() {
		if (!modulesInited) {
			modulesInited = true;
			if (useModules) {
				if (moduleHoldingTree == null) {
					javac.initModules(List.nil());
					defaultModule = Symtab.instance(context).unnamedModule;
				} else {
					List<JCCompilationUnit> mlist = List.of(moduleHoldingTree);
					javac.initModules(mlist);
					//this is a nasty hack
					//we dont want to enter the trees, but the enter has to be done in order to call Elements methods
					// enterTrees call will enter the trees later
					javac.enterDone();

					defaultModule = moduleHoldingTree.modle;
				}
			} else {
				if (moduleHoldingTree != null) {
					throw new IllegalStateException(
							"Source version " + getSourceVersionName() + " doesn't support modules.");
				}
				javac.initModules(List.nil());
				defaultModule = Symtab.instance(context).noModule;
			}
			ModuleSymbol javacdefault = Modules.instance(context).getDefaultModule();
			if (defaultModule != javacdefault) {
				throw new IllegalStateException("Failed to determine default module. " + defaultModule + " - "
						+ javacdefault + "\n" + getSourceVersionName() + " use modules: " + useModules + " - "
						+ ObjectUtils.classOf(defaultModule) + " - " + ObjectUtils.classOf(javacdefault)
						+ " defaultnom " + defaultModule.isNoModule() + " javacnom " + javacdefault.isNoModule()
						+ " errclass " + Symtab.instance(context).errModule.getClass());
			}
		}
	}

	@Override
	protected void compilationUnitSignatureParsed(JCCompilationUnit tree, ClassHoldingData sfd) {
		ModuleSignature modulesig = sfd.getModuleSignature();
		if (modulesig != null) {
			if (!ARFU_moduleHoldingFile.compareAndSet(this, null, sfd)) {
				throw new IllegalStateException(
						"Multiple module signatures found: " + moduleHoldingFile.getPath() + " and " + sfd.getPath());
			}
			moduleHoldingTree = tree;
		}
	}

	@Override
	public void addClassFilesFromPreviousCompilation(PreviousCompilationClassInfo previoussources) {
		initModules();
		Names names = Names.instance(context);
		Symtab symtab = Symtab.instance(context);

		//TODO set the current module if not unnamed
		ModuleSymbol module = defaultModule;

		String prevpackname = null;
		PackageSymbol p = module.unnamedPackage;
		if (p.members_field == null) {
			//as seen in ClassReader.fillIn(PackageSymbol)
			//assigned during completion if necessary
			p.members_field = WriteableScope.create(p);
		}

		for (ClassGenerationInfo sfd : previoussources.getClassDatas()) {
			String spackname = sfd.getPackageName();
			if (!Objects.equals(spackname, prevpackname)) {
				Name pname = spackname == null ? names.empty : names.fromString(spackname);
				p = symtab.enterPackage(module, pname);
				prevpackname = spackname;
				if (p.members_field == null) {
					//as seen in ClassReader.fillIn(PackageSymbol)
					//assigned during completion if necessary
					p.members_field = WriteableScope.create(p);
				}
			}

			Map<String, ? extends ClassSignatureHeader> signatureclasses = sfd.getClassesByBinaryNames();

			for (Entry<SakerPath, String> entry : sfd.getGeneratedClassBinaryNames().entrySet()) {
				String classname = entry.getValue();
				ClassSignatureHeader classsig = signatureclasses.get(classname);
				if (classsig == null) {
					//we dont have to add class files for anyonymous or local inner classes
					continue;
				}
				//name will never be package-info
				//adding that from the previous compilation has no effect so might as well ignore it

				JavaFileObject fileobj = new SakerPathJavaInputFileObject(director.getDirectoryPaths(), entry.getKey(),
						Kind.CLASS, classname);

				ClassSymbol csym = getClassSymbol(module, symtab, p, names, classsig);
				csym.classfile = fileobj;
				p.members_field.enter(csym);
			}
		}
	}

	private static ClassSymbol getClassSymbol(ModuleSymbol module, Symtab symtab, PackageSymbol p, Names names,
			ClassSignatureHeader signature) {
		Name classname = names.fromString(signature.getSimpleName());
		if (signature.getNestingKind() == NestingKind.TOP_LEVEL) {
			return symtab.enterClass(module, classname, p);
		}
		Name flatname = names.fromString(signature.getBinaryName());
		ClassSymbol gotclass = symtab.getClass(module, flatname);
		if (gotclass != null) {
			return gotclass;
		}
		ClassSignatureHeader enclosing = signature.getEnclosingSignature();
		ClassSymbol result = symtab.enterClass(module, classname, getClassSymbol(module, symtab, p, names, enclosing));
		return result;
	}

	@Override
	public NavigableSet<String> getCompilationModuleSet() {
		initModules();
		if (moduleHoldingFile == null) {
			return null;
		}
		NavigableSet<String> result = new TreeSet<>();

		Elements elems = getElements();
		ModuleSignature msig = moduleHoldingFile.getModuleSignature();
		result.add(msig.getName());
		for (DirectiveSignature ds : msig.getDirectives()) {
			if (ds.getKind() == DirectiveSignatureKind.REQUIRES) {
				String depname = ((RequiresDirectiveSignature) ds).getDependencyModule().toString();
				if (result.add(depname)) {
					ModuleElement depmod = elems.getModuleElement(depname);
					if (depmod != null) {
						addCompilationModuleSet(depmod, result);
					}
				}
			}
		}

		return result;
	}

	private void addCompilationModuleSet(ModuleElement me, SortedSet<String> result) {
		for (Directive d : me.getDirectives()) {
			if (d.getKind() == DirectiveKind.REQUIRES) {
				ModuleElement depme = ((RequiresDirective) d).getDependency();
				String depmename = depme.getQualifiedName().toString();
				if (result.add(depmename)) {
					addCompilationModuleSet(depme, result);
				}
			}
		}
	}
}
