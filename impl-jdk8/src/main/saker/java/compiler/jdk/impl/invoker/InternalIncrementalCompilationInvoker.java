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
package saker.java.compiler.jdk.impl.invoker;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.java.JavaTools;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.info.ClassGenerationInfo;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.invoker.InternalIncrementalCompilationInvokerBase;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilerInvocationDirector;
import saker.java.compiler.impl.compile.handler.invoker.PreviousCompilationClassInfo;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathJavaInputFileObject;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;

public class InternalIncrementalCompilationInvoker extends InternalIncrementalCompilationInvokerBase {
	@Override
	public void initCompilation(JavaCompilerInvocationDirector director) throws IOException {
		super.initCompilation(director);

		context = new Context();
		context.put(DiagnosticListener.class, getDiagnosticListener());
		context.put(JavaFileManager.class, fileManager);

		java.util.List<String> options = ObjectUtils.newArrayList(director.getOptions());
		String sourceversionname = CompilationHandler
				.sourceVersionToParameterString(director.getSourceVersionOptionName());
		String targetversionname = CompilationHandler
				.sourceVersionToParameterString(director.getTargetVersionOptionName());
		int currentmajor = JavaTools.getCurrentJavaMajorVersion();
		if (currentmajor < 11) {
			options.remove("--enable-preview");
			if (currentmajor < 9) {
				CompilationHandler.removeNonModularArgumentsFromOptionsList(options);
				int releaseidx = options.indexOf("--release");
				if (releaseidx >= 0) {
					//remove the release argument and its value
					options.remove(releaseidx);
					String releaseval = options.remove(releaseidx);

					//set these for compatibility, but only if they aren't set to other values
					if (sourceversionname == null) {
						sourceversionname = releaseval;
					}
					if (targetversionname == null) {
						targetversionname = releaseval;
					}
				}
			}
		}

		Options opt = Options.instance(context);
		//put the version options and query the corresponding instances
		//as they cache themselves with other keys
		//remove them as well to avoid validation conflicts with --release
		if (sourceversionname != null) {
			opt.put(Option.SOURCE, sourceversionname);
			Source.instance(context);
			opt.remove(Option.SOURCE.text);
		}
		if (targetversionname != null) {
			opt.put(Option.TARGET, targetversionname);
			Target.instance(context);
			opt.remove(Option.TARGET.text);
		}
		JavacTool.processOptions(context, fileManager, options);
		//put the source and target options again as they might've been overwritten
		if (sourceversionname != null) {
			opt.put(Option.SOURCE, sourceversionname);
		}
		if (targetversionname != null) {
			opt.put(Option.TARGET, targetversionname);
		}

		final JavaCompiler javac = JavaCompiler.instance(context);
		this.javac = javac;
		javac.keepComments = true;
		javac.genEndPos = true;

		super.sourceVersion = Source.toSourceVersion(Source.instance(context));
	}

	@Override
	protected void invokeInternalCompilation() {
		//XXX set these options for javac?
		// ClassReader.instance(context).saveParameterNames = true
		// as seen in JavaCompiler.initProcessAnnotations

		MultiTaskListener mtl = MultiTaskListener.instance(context);
		mtl.add(classGenerationTaskListener);

		JavacTrees trees = JavacTrees.instance(context);
		signatureParser = new CompilationUnitSignatureParser(trees, getSourceVersionName(), cache);
		signatureParser.setElementQueryAPIEnabled(false);
		sourcePositions = trees.getSourcePositions();

		parsedTrees = new ListBuffer<>();

		while (compilationRound()) {
			//loop
		}

		if (isAnyErrorRaised()) {
			return;
		}
		List<JCCompilationUnit> parsedtreelist = parsedTrees.toList();
		javac.enterTrees(parsedtreelist);
		if (isAnyErrorRaised()) {
			return;
		}

		Queue<Env<AttrContext>> attributed = javac.attribute(javac.todo);
		if (isAnyErrorRaised()) {
			return;
		}

		Queue<Env<AttrContext>> flowed = javac.flow(attributed);

		if (isAnyErrorRaised()) {
			return;
		}

		parseABIUsages(signatureParser.getTrees());

		javac.generate(javac.desugar(flowed));

		javac.reportDeferredDiagnostics();
	}

	@Override
	protected void compilationUnitSignatureParsed(JCCompilationUnit tree, ClassHoldingData sfd) {
		if (sfd.getModuleSignature() != null) {
			throw new UnsupportedOperationException("Modules are not supported.");
		}
	}

	@Override
	public void addClassFilesFromPreviousCompilation(PreviousCompilationClassInfo previoussources) {
		ClassReader reader = ClassReader.instance(context);
		Names names = Names.instance(context);
		Symtab symtab = Symtab.instance(context);

		String prevpackname = null;
		PackageSymbol p = symtab.unnamedPackage;
		if (p.members_field == null) {
			//as seen in ClassReader.fillIn(PackageSymbol)
			//assigned during completion if necessary
			p.members_field = new Scope(p);
		}

		//TODO we could gain some performance by not entering private, 
		//     anonymous classes, or classes which are not visible from changed packages
		for (ClassGenerationInfo sfd : previoussources.getClassDatas()) {
			String spackname = sfd.getPackageName();

			if (!Objects.equals(spackname, prevpackname)) {
				Name pname = spackname == null ? names.empty : names.fromString(spackname);
				p = reader.enterPackage(pname);
				prevpackname = spackname;
				if (p.members_field == null) {
					//as seen in ClassReader.fillIn(PackageSymbol)
					//assigned during completion if necessary
					p.members_field = new Scope(p);
				}
			}

			Set<String> signatureclasses = sfd.getClassesByBinaryNames().keySet();

			for (Entry<SakerPath, String> entry : sfd.getGeneratedClassBinaryNames().entrySet()) {
				String classname = entry.getValue();
				if (!signatureclasses.contains(classname)) {
					//we dont have to add class files for anyonymous or local inner classes
					continue;
				}
				//name will never be package-info
				//adding that from the previous compilation has no effect so might as well ignore it
				JavaFileObject fileobj = new SakerPathJavaInputFileObject(director.getDirectoryPaths(), entry.getKey(),
						Kind.CLASS, classname);

				Name cnamename = names.fromString(classname);
				//enterClass might throw an assertion error if a class is present on both the classpath and among the sources
				ClassSymbol csym = reader.enterClass(cnamename, fileobj);
				p.members_field.enter(csym);
			}
		}
	}

	@Override
	public NavigableSet<String> getCompilationModuleSet() {
		return null;
	}
}
