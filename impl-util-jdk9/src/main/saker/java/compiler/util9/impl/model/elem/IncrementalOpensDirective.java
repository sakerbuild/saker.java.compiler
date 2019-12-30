package saker.java.compiler.util9.impl.model.elem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.PackageElement;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.signature.element.ModuleSignature.OpensDirectiveSignature;
import saker.java.compiler.impl.signature.type.NameSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalOpensDirective extends IncrementalDirectiveBase<OpensDirectiveSignature>
		implements OpensDirective {
	private static final AtomicReferenceFieldUpdater<IncrementalOpensDirective, PackageElement> ARFU_pack = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalOpensDirective.class, PackageElement.class, "pack");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalOpensDirective, Optional> ARFU_targetModules = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalOpensDirective.class, Optional.class, "targetModules");

	private volatile transient PackageElement pack;
	private volatile transient Optional<List<ModuleElement>> targetModules;

	public IncrementalOpensDirective(IncrementalElementsTypes9 elemTypes, OpensDirectiveSignature signature,
			IncrementalModuleElement module) {
		super(elemTypes, signature, module);
	}

	@Override
	public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
		return v.visitOpens(this, p);
	}

	@Override
	public PackageElement getPackage() {
		PackageElement thispack = this.pack;
		if (thispack != null) {
			return thispack;
		}
		thispack = elemTypes.getCurrentModulePackageElement(signature.getPackageName().toString());
		if (ARFU_pack.compareAndSet(this, null, thispack)) {
			return thispack;
		}
		return this.pack;
	}

	@Override
	public List<? extends ModuleElement> getTargetModules() {
		Optional<List<ModuleElement>> thistargetmodules = this.targetModules;
		if (thistargetmodules != null) {
			return thistargetmodules.orElse(null);
		}
		List<ModuleElement> nval = null;
		List<? extends NameSignature> sigtarget = signature.getTargetModules();
		if (sigtarget != null) {
			nval = new ArrayList<>(sigtarget.size());
			for (NameSignature ns : sigtarget) {
				ModuleElement module = elemTypes.getModuleElement(ns.getName());
				if (module == null) {
					continue;
				}
				nval.add(module);
			}
			nval = ImmutableUtils.makeImmutableList(nval);
		}
		thistargetmodules = Optional.ofNullable(nval);
		if (ARFU_targetModules.compareAndSet(this, null, thistargetmodules)) {
			return nval;
		}
		return this.targetModules.orElse(null);
	}

}
