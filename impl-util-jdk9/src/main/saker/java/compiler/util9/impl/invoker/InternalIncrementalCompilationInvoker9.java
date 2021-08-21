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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
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
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

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
import com.sun.tools.javac.jvm.Target;
import com.sun.tools.javac.main.Arguments;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.platform.PlatformDescription;
import com.sun.tools.javac.platform.PlatformUtils;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.util.java.JavaTools;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.incremental.JavacPrivateAPIError;
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

	private java.util.List<String> options;
	private Set<String> addReadsReferencedModules;

	@Override
	public void initCompilation(JavaCompilerInvocationDirector director) throws IOException {
		super.initCompilation(director);
		context = new Context();
		context.put(DiagnosticListener.class, getDiagnosticListener());

		java.util.List<String> options = ObjectUtils.newArrayList(director.getOptions());
		String sourceversionname = CompilationHandler
				.sourceVersionToParameterString(director.getSourceVersionOptionName());
		String targetversionname = CompilationHandler
				.sourceVersionToParameterString(director.getTargetVersionOptionName());
		if (JavaTools.getCurrentJavaMajorVersion() < 11) {
			options.remove("--enable-preview");
		}

		JavaFileManager putfilemanager = fileManager;

		int releaseidx = options.indexOf("--release");
		if (releaseidx >= 0) {
			options.remove(releaseidx);
			String releaseval = options.remove(releaseidx);

			PlatformDescription platformdesc = PlatformUtils.lookupPlatformDescription(releaseval);
			if (platformdesc == null) {
				throw new IllegalArgumentException("Platform not found for --release " + releaseval);
			}
			context.put(PlatformDescription.class, platformdesc);
			if (sourceversionname == null) {
				sourceversionname = platformdesc.getSourceVersion();
			}
			if (targetversionname == null) {
				targetversionname = platformdesc.getTargetVersion();
			}
			int release = Integer.parseInt(releaseval);
			try {
				Method getFileManagerMethod = PlatformDescription.class.getMethod("getFileManager");
				JavaFileManager fm = (JavaFileManager) getFileManagerMethod.invoke(platformdesc);
				//we are running on java 10 or later
				//use the file manager
				putfilemanager = fileManager instanceof StandardJavaFileManager
						? new DelegatingStandardJavaFileManager(fm, (StandardJavaFileManager) fileManager, release)
						: new DelegatingJavaFileManager(fm, fileManager, release);
			} catch (NoSuchMethodException e) {
				//we're running on Java 9
				if (release == 9) {
					//do nothing with the file manager
				} else if (release < 9) {
					//release is 8 or lower
					//set the platform class path
					try {
						Method getPlatformPathMethod = PlatformDescription.class.getMethod("getPlatformPath");
						@SuppressWarnings("unchecked")
						Collection<? extends Path> platformpaths = (Collection<? extends Path>) getPlatformPathMethod
								.invoke(platformdesc);
						((StandardJavaFileManager) fileManager)
								.setLocationFromPaths(StandardLocation.PLATFORM_CLASS_PATH, platformpaths);
						putfilemanager = new ForwardingJavaFileManager<JavaFileManager>(fileManager) {
							@Override
							public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
									boolean recurse) throws IOException {
								if (location instanceof StandardLocation) {
									return super.list(location, packageName, kinds, recurse);
								}
								//fall back to the platform class path for any module related things
								Iterable<JavaFileObject> superresult = super.list(StandardLocation.PLATFORM_CLASS_PATH,
										packageName, kinds, recurse);
								//override the Kind of the returned objects as the caller doesn't handle sig files properly on JDK9
								ArrayList<JavaFileObject> result = new ArrayList<>();
								for (JavaFileObject jfo : superresult) {
									result.add(new ClassKindOverridingForwardingJavaFileObject(jfo));
								}
								return result;
							}

							@Override
							public String inferBinaryName(Location location, JavaFileObject file) {
								if (file instanceof ClassKindOverridingForwardingJavaFileObject) {
									return super.inferBinaryName(location,
											((ClassKindOverridingForwardingJavaFileObject) file).getFileObject());
								}
								return super.inferBinaryName(location, file);
							}

							@Override
							public boolean isSameFile(FileObject a, FileObject b) {
								if (a instanceof ClassKindOverridingForwardingJavaFileObject) {
									a = ((ClassKindOverridingForwardingJavaFileObject) a).getFileObject();
								}
								if (b instanceof ClassKindOverridingForwardingJavaFileObject) {
									b = ((ClassKindOverridingForwardingJavaFileObject) a).getFileObject();
								}
								return super.isSameFile(a, b);
							}

						};
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e1) {
						e1.addSuppressed(e);
						throw new JavacPrivateAPIError(e1);
					}
				} else {
					//release is 10 or above
					//this should not happen as we're running on Java 9
					//  because the getFileManager method was not found
					throw new JavacPrivateAPIError(e);
				}
			} catch (Exception e) {
				throw new JavacPrivateAPIError(e);
			}
		}

		context.put(JavaFileManager.class, putfilemanager);

		Options opt = Options.instance(context);

		//put the version options and query the corresponding instances
		//as they cache themselves with other keys
		//remove them as well to avoid validation conflicts with --release
		if (sourceversionname != null) {
			opt.put(Option.SOURCE, sourceversionname);
			Source.instance(context);
			opt.remove(Option.SOURCE.primaryName);
		}
		if (targetversionname != null) {
			opt.put(Option.TARGET, targetversionname);
			Target.instance(context);
			opt.remove(Option.TARGET.primaryName);
		}

		this.addReadsReferencedModules = CompilationHandler.getAddReadsReferencedModules(options);
		if (!ObjectUtils.isNullOrEmpty(addReadsReferencedModules)) {
			//add the --add-reads modules via --add-modules otherwise javac won't resolve them
			//this is better than requiring the user to manualy specify them
			//duplicate --add-modules values in the parameters are ignored.
			options.add("--add-modules");
			options.add(StringUtils.toStringJoin(",", addReadsReferencedModules));
		}
		this.options = options;
		Arguments args = Arguments.instance(context);
		args.init("saker-with-javac", options, null, null);

		//put the source and target options again as they might've been overwritten
		if (sourceversionname != null) {
			opt.put(Option.SOURCE, sourceversionname);
		}
		if (targetversionname != null) {
			opt.put(Option.TARGET, targetversionname);
		}

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

		//in case of java 9
		//we need to always parse a module-info if there is any and call init modules with that
		//we need to call enterTrees before any annotation processing
		//else getTypeElement will throw an exception

		while (compilationRound()) {
			//loop
		}

		Annotate annotate = Annotate.instance(context);
		//block annotation processing
		//not blocking it can cause assertion errors and invalid annotation types in javac
		//this is caused as we are using a hack to call initModules and be able to manually process the annotations
		annotate.blockAnnotations();

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
				String depname = ((RequiresDirectiveSignature) ds).getDependencyModule().getName();
				if (result.add(depname)) {
					ModuleElement depmod = elems.getModuleElement(depname);
					if (depmod != null) {
						addCompilationModuleSet(depmod, result);
					}
				}
			}
		}

		for (String m : addReadsReferencedModules) {
			if (result.add(m)) {
				ModuleElement me = elems.getModuleElement(m);
				if (me != null) {
					addCompilationModuleSet(me, result);
				}
			}
		}

		for (Iterator<String> it = this.options.iterator(); it.hasNext();) {
			String arg = it.next();
			if ("--add-modules".equals(arg)) {
				//must have an argument, otherwise we would've failed earlier 
				String readsval;
				try {
					readsval = it.next();
				} catch (NoSuchElementException e) {
					throw new IllegalArgumentException("Failed to retrieve --add-modules argument from javac options.",
							e);
				}
				Iterator<? extends CharSequence> mit = StringUtils.splitCharSequenceIterator(readsval, ',');
				while (mit.hasNext()) {
					String m = mit.next().toString();
					if (m.isEmpty()) {
						//ignore
						continue;
					}
					if (result.add(m)) {
						ModuleElement me = elems.getModuleElement(m);
						if (me != null) {
							addCompilationModuleSet(me, result);
						}
					}
				}
				continue;
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

	private static final class ClassKindOverridingForwardingJavaFileObject
			extends ForwardingJavaFileObject<JavaFileObject> {
		private ClassKindOverridingForwardingJavaFileObject(JavaFileObject fileObject) {
			super(fileObject);
		}

		@Override
		public Kind getKind() {
			return Kind.CLASS;
		}

		public JavaFileObject getFileObject() {
			return fileObject;
		}
	}

	private static class DelegatingJavaFileManager implements JavaFileManager {

		private final JavaFileManager releaseFM;
		private final JavaFileManager baseFM;
		private final int release;

		public DelegatingJavaFileManager(JavaFileManager releaseFM, JavaFileManager baseFM, int release) {
			this.releaseFM = releaseFM;
			this.baseFM = baseFM;
			this.release = release;
		}

		private JavaFileManager delegate(Location location) {
			if (releaseFM.hasLocation(location)) {
				return releaseFM;
			}
			return baseFM;
		}

		@Override
		public ClassLoader getClassLoader(Location location) {
			return delegate(location).getClassLoader(location);
		}

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
				throws IOException {
			if (release > 8) {
				return delegate(location).list(location, packageName, kinds, recurse);
			}
			//the release is 8 or earlier 
			// non-standard (i.e. module oriented) locations should be delegated to release fm
			if (location instanceof StandardLocation) {
				return delegate(location).list(location, packageName, kinds, recurse);
			}
			return releaseFM.list(StandardLocation.PLATFORM_CLASS_PATH, packageName, kinds, recurse);
		}

		@Override
		public String inferBinaryName(Location location, JavaFileObject file) {
			return delegate(location).inferBinaryName(location, file);
		}

		@Override
		public boolean isSameFile(FileObject a, FileObject b) {
			return baseFM.isSameFile(a, b);
		}

		@Override
		public boolean handleOption(String current, Iterator<String> remaining) {
			return baseFM.handleOption(current, remaining);
		}

		@Override
		public boolean hasLocation(Location location) {
			return releaseFM.hasLocation(location) || baseFM.hasLocation(location);
		}

		@Override
		public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
			return delegate(location).getJavaFileForInput(location, className, kind);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
				throws IOException {
			return delegate(location).getJavaFileForOutput(location, className, kind, sibling);
		}

		@Override
		public FileObject getFileForInput(Location location, String packageName, String relativeName)
				throws IOException {
			return delegate(location).getFileForInput(location, packageName, relativeName);
		}

		@Override
		public FileObject getFileForOutput(Location location, String packageName, String relativeName,
				FileObject sibling) throws IOException {
			return delegate(location).getFileForOutput(location, packageName, relativeName, sibling);
		}

		@Override
		public void flush() throws IOException {
			releaseFM.flush();
			baseFM.flush();
		}

		@Override
		public void close() throws IOException {
			releaseFM.close();
			baseFM.close();
		}

		@Override
		public Location getLocationForModule(Location location, String moduleName) throws IOException {
			return delegate(location).getLocationForModule(location, moduleName);
		}

		@Override
		public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
			return delegate(location).getLocationForModule(location, fo);
		}

		@Override
		public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws IOException {
			return delegate(location).getServiceLoader(location, service);
		}

		@Override
		public String inferModuleName(Location location) throws IOException {
			return delegate(location).inferModuleName(location);
		}

		@Override
		public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
			return delegate(location).listLocationsForModules(location);
		}

		@Override
		public boolean contains(Location location, FileObject fo) throws IOException {
			return delegate(location).contains(location, fo);
		}

		@Override
		public int isSupportedOption(String option) {
			return baseFM.isSupportedOption(option);
		}

		public JavaFileManager getBaseFileManager() {
			return baseFM;
		}

	}

	private static final class DelegatingStandardJavaFileManager extends DelegatingJavaFileManager
			implements StandardJavaFileManager {

		private final StandardJavaFileManager baseSJFM;

		private DelegatingStandardJavaFileManager(JavaFileManager releaseFM, StandardJavaFileManager baseSJFM,
				int release) {
			super(releaseFM, baseSJFM, release);
			this.baseSJFM = baseSJFM;
		}

		@Override
		public boolean isSameFile(FileObject a, FileObject b) {
			return baseSJFM.isSameFile(a, b);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
			return baseSJFM.getJavaFileObjectsFromFiles(files);
		}

		//not available on jdk 12
		//@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(Collection<? extends Path> paths) {
			return baseSJFM.getJavaFileObjectsFromPaths(paths);
		}

		@Deprecated(since = "13")
		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(Iterable<? extends Path> paths) {
			return baseSJFM.getJavaFileObjectsFromPaths(paths);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
			return baseSJFM.getJavaFileObjects(files);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjects(Path... paths) {
			return baseSJFM.getJavaFileObjects(paths);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
			return baseSJFM.getJavaFileObjectsFromStrings(names);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
			return baseSJFM.getJavaFileObjects(names);
		}

		@Override
		public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
			baseSJFM.setLocation(location, files);
		}

		@Override
		public void setLocationFromPaths(Location location, Collection<? extends Path> paths) throws IOException {
			baseSJFM.setLocationFromPaths(location, paths);
		}

		@Override
		public void setLocationForModule(Location location, String moduleName, Collection<? extends Path> paths)
				throws IOException {
			baseSJFM.setLocationForModule(location, moduleName, paths);
		}

		@Override
		public Iterable<? extends File> getLocation(Location location) {
			return baseSJFM.getLocation(location);
		}

		@Override
		public Iterable<? extends Path> getLocationAsPaths(Location location) {
			return baseSJFM.getLocationAsPaths(location);
		}

		@Override
		public Path asPath(FileObject file) {
			return baseSJFM.asPath(file);
		}

		@Override
		public void setPathFactory(PathFactory f) {
			baseSJFM.setPathFactory(f);
		}

	}
}
