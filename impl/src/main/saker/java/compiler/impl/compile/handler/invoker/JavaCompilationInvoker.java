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
import java.util.Collection;
import java.util.NavigableMap;
import java.util.NavigableSet;

import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMISerialize;
import saker.build.util.java.JavaTools;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureHolder;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface JavaCompilationInvoker {
	public interface ABIParseInfo extends RealizedSignatureHolder {
		public TopLevelAbiUsage getUsage();
	}

	public static class ABIParseInfoImpl implements ABIParseInfo, Externalizable {
		private static final long serialVersionUID = 1L;

		private TopLevelAbiUsage usage;
		private RealizedSignatureHolder realizedSignatures;

		public ABIParseInfoImpl() {
		}

		public ABIParseInfoImpl(TopLevelAbiUsage usage, RealizedSignatureHolder realizedSignatures) {
			this.usage = usage;
			this.realizedSignatures = realizedSignatures;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(usage);
			out.writeObject(realizedSignatures);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			usage = (TopLevelAbiUsage) in.readObject();
			realizedSignatures = (RealizedSignatureHolder) in.readObject();
		}

		@Override
		public TopLevelAbiUsage getUsage() {
			return usage;
		}

		@Override
		public NavigableMap<String, ? extends ClassSignature> getRealizedClasses() {
			return realizedSignatures.getRealizedClasses();
		}

		@Override
		public PackageSignature getRealizedPackageSignature() {
			return realizedSignatures.getRealizedPackageSignature();
		}

		@Override
		public ModuleSignature getRealizedModuleSignature() {
			return realizedSignatures.getRealizedModuleSignature();
		}
	}

	public void initCompilation(JavaCompilerInvocationDirector director) throws IOException;

	public void invokeCompilation(SakerPathBytes[] units) throws IOException;

	public void addSourceForCompilation(String sourcename, SakerPath file) throws IOException;

	public void addClassFileForCompilation(String classname, SakerPath file) throws IOException;

	@RMISerialize
	//This method result is serialized as it has identity reference comparions related logic in it. (Source positions) 
	public Collection<? extends ClassHoldingData> parseRoundAddedSources();

	@RMISerialize
	public Collection<? extends ClassHoldingData> parseRoundAddedClassFiles();

	public Elements getElements();

	public Types getTypes();

	public void addClassFilesFromPreviousCompilation(PreviousCompilationClassInfo previoussources);

	@RMISerialize
	public NavigableMap<SakerPath, ABIParseInfo> getParsedSourceABIUsages();

	public JavaFileManager getJavaFileManager();

	@RMISerialize
	public NavigableSet<String> getCompilationModuleSet();

	@RMICacheResult
	public default String getJavaVersionProperty() {
		return JavaTools.getCurrentJavaVersionProperty();
	}

	/**
	 * Gets the name of the {@link SourceVersion} enumeration that is being accepted by this compilation.
	 * 
	 * @return The source version name.
	 */
	@RMICacheResult
	public String getSourceVersionName();
}
