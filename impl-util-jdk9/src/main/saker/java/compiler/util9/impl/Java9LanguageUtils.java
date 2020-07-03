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
package saker.java.compiler.util9.impl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.ModuleElement.UsesDirective;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.build.thirdparty.saker.rmi.connection.MethodTransferProperties;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.rmi.io.writer.WrapperRMIObjectWriteHandler;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.function.TriFunction;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIIdentityHashSetRemoteElementWrapper;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureData;
import saker.java.compiler.impl.compile.signature.impl.ExportsDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.ModuleSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.NameSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.OpensDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.ProvidesDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.RequiresDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.UsesDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignatureKind;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.impl.signature.type.NameSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class Java9LanguageUtils {
	private static final Map<DirectiveSignatureKind, DirectiveKind> SIGNATURE_TO_DIRECTIVE_KIND_MAP = new EnumMap<>(
			DirectiveSignatureKind.class);
	static {
		SIGNATURE_TO_DIRECTIVE_KIND_MAP.put(DirectiveSignatureKind.REQUIRES, DirectiveKind.REQUIRES);
		SIGNATURE_TO_DIRECTIVE_KIND_MAP.put(DirectiveSignatureKind.EXPORTS, DirectiveKind.EXPORTS);
		SIGNATURE_TO_DIRECTIVE_KIND_MAP.put(DirectiveSignatureKind.OPENS, DirectiveKind.OPENS);
		SIGNATURE_TO_DIRECTIVE_KIND_MAP.put(DirectiveSignatureKind.PROVIDES, DirectiveKind.PROVIDES);
		SIGNATURE_TO_DIRECTIVE_KIND_MAP.put(DirectiveSignatureKind.USES, DirectiveKind.USES);
	}

	private Java9LanguageUtils() {
		throw new UnsupportedOperationException();
	}

	public static String getModuleNameOf(Element elem) {
		return IncrementalElementsTypes9.getModuleOfImpl(elem).getQualifiedName().toString();
	}

	public static boolean isModuleElementKind(ElementKind kind) {
		return kind == ElementKind.MODULE;
	}

	public static void addModuleElementKind(Collection<? super ElementKind> coll) {
		coll.add(ElementKind.MODULE);
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(RoundEnvironment.class, "getElementsAnnotatedWithAny",
						TypeElement[].class))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class)).build());
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(RoundEnvironment.class, "getElementsAnnotatedWithAny", Set.class))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class)).build());

		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(Elements.class, "getAllPackageElements", CharSequence.class))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class)).build());
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(Elements.class, "getAllTypeElements", CharSequence.class))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class)).build());
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(Elements.class, "getAllModuleElements"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class)).build());

		JavaUtil.addCommonElementClassRMIProperties(builder, ModuleElement.class);
		JavaUtil.addCommonQualifiedNameableRMIProperties(builder, ModuleElement.class);

		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(ExportsDirective.class, "getTargetModules"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class)).build());
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(OpensDirective.class, "getTargetModules"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class)).build());
		builder.add(MethodTransferProperties
				.builder(ReflectUtils.getMethodAssert(ProvidesDirective.class, "getImplementations"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class)).build());
	}

	public static RealizedSignatureData getRealizedSignatures(CompilationUnitTree unit, Trees trees, String filename,
			ParserCache cache) {
		TreePath unitpath = new TreePath(unit);
		NavigableMap<String, ClassSignature> realizedclasses = new TreeMap<>();
		PackageSignature packsig = null;
		ModuleSignature modulesig = null;
		for (Tree typetrees : unit.getTypeDecls()) {
			//getTypeDecls(): The list may also include empty statements resulting from extraneous semicolons.
			if (typetrees.getKind() == Tree.Kind.EMPTY_STATEMENT) {
				continue;
			}

			Element elem = trees.getElement(new TreePath(unitpath, typetrees));
			if (elem.getKind() == ElementKind.MODULE) {
				if (modulesig != null) {
					throw new AssertionError(
							"Multiple module declaration found in compilation unit. " + unit.getSourceFile());
				}
				modulesig = (ModuleSignature) IncrementalElementsTypes.createSignatureFromJavacElement(elem, cache);
			} else {
				ClassSignature c = (ClassSignature) IncrementalElementsTypes.createSignatureFromJavacElement(elem,
						cache);
				realizedclasses.put(c.getCanonicalName(), c);
			}
		}

		if (JavaTaskUtils.isPackageInfoSource(filename)) {
			PackageTree unitpack = unit.getPackage();
			if (unitpack != null) {
				Element unitelem = trees.getElement(new TreePath(unitpath, unitpack));
				if (unitelem != null) {
					PackageElement elem = (PackageElement) unitelem;
					packsig = ((PackageSignature) IncrementalElementsTypes.createSignatureFromJavacElement(elem,
							cache));
				}
			}
		}
		return new RealizedSignatureData(realizedclasses, packsig, modulesig);
	}

	public static ModuleSignature moduleElementToSignature(Element elem, ParserCache cache) {
		ModuleElement moduleelem = (ModuleElement) elem;
		List<? extends Directive> modelemdirectives = moduleelem.getDirectives();
		List<DirectiveSignature> moddirectives = JavaTaskUtils.cloneImmutableList(modelemdirectives,
				DirectiveToSignatureVisitor::toSignature);

		//the doc comment is not added to the signature
		ModuleSignatureImpl result = new ModuleSignatureImpl(
				IncrementalElementsTypes.getAnnotationSignaturesForAnnotatedConstruct(moduleelem, cache),
				moduleelem.getQualifiedName().toString(), moduleelem.isOpen(), moddirectives, null);
		return result;
	}

	public static DirectiveKind toDirectiveKind(DirectiveSignatureKind kind) {
		return SIGNATURE_TO_DIRECTIVE_KIND_MAP.get(kind);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJavaKindBasedElementVisitorFunctions(
			Map<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> result) {
		result.put(ElementKind.MODULE,
				(TriFunction<ElementVisitor, ModuleElement, Object, Object>) ElementVisitor::visitModule);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJava9KindBasedTypeVisitorFunctions(
			Map<TypeKind, TriFunction<TypeVisitor, ? extends TypeMirror, Object, Object>> result) {
		result.put(TypeKind.MODULE, (TriFunction<TypeVisitor, NoType, Object, Object>) TypeVisitor::visitNoType);
	}

	private static class DirectiveToSignatureVisitor implements DirectiveVisitor<DirectiveSignature, Void> {
		private static final DirectiveToSignatureVisitor INSTANCE = new DirectiveToSignatureVisitor();

		public static DirectiveSignature toSignature(Directive d) {
			return d.accept(INSTANCE, null);
		}

		@Override
		public DirectiveSignature visitExports(ExportsDirective d, Void p) {
			List<? extends ModuleElement> tmodules = d.getTargetModules();
			List<NameSignature> targetmodulenames = JavaTaskUtils.cloneImmutableList(tmodules,
					me -> new NameSignatureImpl(me.getQualifiedName().toString()));

			return new ExportsDirectiveSignatureImpl(
					new NameSignatureImpl(d.getPackage().getQualifiedName().toString()), targetmodulenames);
		}

		@Override
		public DirectiveSignature visitOpens(OpensDirective d, Void p) {
			List<? extends ModuleElement> tmodules = d.getTargetModules();
			List<NameSignature> targetmodulenames = JavaTaskUtils.cloneImmutableList(tmodules,
					me -> new NameSignatureImpl(me.getQualifiedName().toString()));
			return new OpensDirectiveSignatureImpl(new NameSignatureImpl(d.getPackage().getQualifiedName().toString()),
					targetmodulenames);
		}

		@Override
		public DirectiveSignature visitProvides(ProvidesDirective d, Void p) {
			List<TypeSignature> impltypes = JavaTaskUtils.cloneImmutableList(d.getImplementations(),
					impl -> CanonicalTypeSignatureImpl.create(impl.getQualifiedName().toString()));
			return new ProvidesDirectiveSignatureImpl(
					CanonicalTypeSignatureImpl.create(d.getService().getQualifiedName().toString()), impltypes);
		}

		@Override
		public DirectiveSignature visitRequires(RequiresDirective d, Void p) {
			return new RequiresDirectiveSignatureImpl(d.isStatic(), d.isTransitive(),
					new NameSignatureImpl(d.getDependency().getQualifiedName().toString()));
		}

		@Override
		public DirectiveSignature visitUses(UsesDirective d, Void p) {
			return new UsesDirectiveSignatureImpl(
					CanonicalTypeSignatureImpl.create(d.getService().getQualifiedName().toString()));
		}
	}
}
