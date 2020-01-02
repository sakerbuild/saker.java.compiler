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
package saker.java.compiler.impl.compile.handler.info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class SourceFileData extends BasicFileData implements ClassHoldingFileData {
	private static final long serialVersionUID = 1L;

	protected TopLevelAbiUsage abiUsage;

	protected ImportScope importScope;
	protected NavigableMap<String, ? extends ClassSignature> classes;
	protected PackageSignature packageSignature;
	protected ModuleSignature moduleSignature;

	protected NavigableMap<String, ? extends ClassSignature> realizedClasses;
	protected PackageSignature realizedPackageSignature;
	protected ModuleSignature realizedModuleSignature;

	protected NavigableMap<SakerPath, ClassFileData> generatedClassDatas;

	protected SignatureSourcePositions sourcePositions;

	/**
	 * For Externalizable implementation only.
	 */
	public SourceFileData() {
	}

	public SourceFileData(SourceFileData data) {
		super(data);
		this.classes = new TreeMap<>(data.classes);
		this.realizedClasses = new TreeMap<>(data.realizedClasses);
		this.abiUsage = data.abiUsage;
		this.generatedClassDatas = new TreeMap<>(data.generatedClassDatas);
		this.packageSignature = data.packageSignature;
		this.realizedPackageSignature = data.realizedPackageSignature;
		this.importScope = data.importScope;
	}

	public SourceFileData(SakerPath path, ContentDescriptor contentdescriptor,
			NavigableMap<String, ? extends ClassSignature> classes, TopLevelAbiUsage abiusage,
			PackageSignature packagesignature, ImportScope importscope) {
		super(path, contentdescriptor);
		this.classes = classes;
		this.abiUsage = abiusage;
		this.packageSignature = packagesignature;
		this.importScope = importscope;

		this.generatedClassDatas = new ConcurrentSkipListMap<>();
	}

	public SourceFileData(SakerPath path, ContentDescriptor contentdescriptor,
			NavigableMap<String, ? extends ClassSignature> classes, PackageSignature packagesignature,
			ImportScope importscope) {
		super(path, contentdescriptor);
		this.classes = classes;
		this.packageSignature = packagesignature;
		this.importScope = importscope;

		this.generatedClassDatas = new ConcurrentSkipListMap<>();
	}

	@Override
	public ImportScope getImportScope() {
		return importScope;
	}

	@Override
	public FileDataKind getKind() {
		return FileDataKind.SOURCE;
	}

	@Override
	public NavigableMap<SakerPath, ClassFileData> getGeneratedClassDatas() {
		return generatedClassDatas;
	}

	public void addGeneratedClass(ClassFileData cfd) {
		generatedClassDatas.put(cfd.getPath(), cfd);
	}

	public void setABIUsage(TopLevelAbiUsage abiusage) {
		this.abiUsage = abiusage;
	}

	@Override
	public TopLevelAbiUsage getABIUsage() {
		return abiUsage;
	}

	public NavigableSet<String> getClassCanonicalNames() {
		return classes.navigableKeySet();
	}

	@Override
	public NavigableMap<String, ? extends ClassSignature> getClasses() {
		return classes;
	}

	@Override
	public NavigableMap<String, ? extends ClassSignature> getRealizedClasses() {
		return realizedClasses;
	}

	@Override
	public PackageSignature getPackageSignature() {
		return packageSignature;
	}

	@Override
	public PackageSignature getRealizedPackageSignature() {
		return realizedPackageSignature;
	}

	@Override
	public ModuleSignature getModuleSignature() {
		return moduleSignature;
	}

	@Override
	public ModuleSignature getRealizedModuleSignature() {
		return realizedModuleSignature;
	}

	@Override
	public SignatureSourcePositions getSourcePositions() {
		return sourcePositions;
	}

	public void setRealizedPackageSignature(PackageSignature realizedPackageSignature) {
		this.realizedPackageSignature = realizedPackageSignature;
	}

	public void setRealizedModuleSignature(ModuleSignature realizedModuleSignature) {
		this.realizedModuleSignature = realizedModuleSignature;
	}

	public void setRealizedClasses(NavigableMap<String, ? extends ClassSignature> realizedClasses) {
		this.realizedClasses = realizedClasses;
	}

	public void setModuleSignature(ModuleSignature moduleSignature) {
		this.moduleSignature = moduleSignature;
	}

	public void setRealizedSignatures(RealizedSignatureHolder realizedsignatures) {
		this.realizedClasses = realizedsignatures.getRealizedClasses();
		this.realizedPackageSignature = realizedsignatures.getRealizedPackageSignature();
		this.realizedModuleSignature = realizedsignatures.getRealizedModuleSignature();
	}

	public void setSourcePositions(SignatureSourcePositions sourcePositions) {
		this.sourcePositions = sourcePositions;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(importScope);
		SerialUtils.writeExternalMap(out, classes);
		SerialUtils.writeExternalMap(out, realizedClasses);

		out.writeObject(abiUsage);
		out.writeObject(packageSignature);
		out.writeObject(realizedPackageSignature);
		out.writeObject(moduleSignature);
		out.writeObject(realizedModuleSignature);

		SerialUtils.writeExternalMap(out, generatedClassDatas);

		out.writeObject(sourcePositions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		importScope = (ImportScope) in.readObject();
		classes = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		realizedClasses = SerialUtils.readExternalSortedImmutableNavigableMap(in);

		abiUsage = (TopLevelAbiUsage) in.readObject();
		packageSignature = (PackageSignature) in.readObject();
		realizedPackageSignature = (PackageSignature) in.readObject();
		moduleSignature = (ModuleSignature) in.readObject();
		realizedModuleSignature = (ModuleSignature) in.readObject();

		generatedClassDatas = SerialUtils.readExternalSortedImmutableNavigableMap(in);

		sourcePositions = (SignatureSourcePositions) in.readObject();
	}

	//inherited hashCode

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceFileData other = (SourceFileData) obj;
		if (abiUsage == null) {
			if (other.abiUsage != null)
				return false;
		} else if (!abiUsage.equals(other.abiUsage))
			return false;
		if (classes == null) {
			if (other.classes != null)
				return false;
		} else if (!classes.equals(other.classes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}

}