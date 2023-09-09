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
package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.compile.JavaCompilationConfigurationOutput;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.sdk.support.api.SDKDescription;

public class InternalJavaCompilerOutputImpl implements InternalJavaCompilerOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private JavaCompilationConfigurationOutput outputConfig;

	private Collection<JavaSourceDirectory> sourceDirectories;
	private JavaClassPath classPath;
	private JavaModulePath modulePath;

	private Object abiVersionKey;
	private Object implementationVersionKey;

	private boolean hadAnnotationProcessors;

	private CompilationInfo compilationInfo;

	/**
	 * For {@link Externalizable}.
	 */
	public InternalJavaCompilerOutputImpl() {
	}

	public InternalJavaCompilerOutputImpl(Collection<JavaSourceDirectory> sourcedirs, JavaClassPath classPath,
			JavaModulePath modulepath, Object abiVersionKey, Object implementationVersionKey,
			JavaCompilationConfigurationOutput outputconfig, boolean hadAnnotationProcessors) {
		this(sourcedirs, classPath, modulepath, abiVersionKey, implementationVersionKey, outputconfig,
				hadAnnotationProcessors, null);
	}

	public InternalJavaCompilerOutputImpl(Collection<JavaSourceDirectory> sourcedirs, JavaClassPath classPath,
			JavaModulePath modulepath, Object abiVersionKey, Object implementationVersionKey,
			JavaCompilationConfigurationOutput outputconfig, boolean hadAnnotationProcessors,
			CompilationInfo compilationInfo) {
		this.sourceDirectories = sourcedirs;
		this.classPath = classPath;
		this.modulePath = modulepath;
		this.abiVersionKey = abiVersionKey;
		this.implementationVersionKey = implementationVersionKey;
		this.outputConfig = outputconfig;
		this.hadAnnotationProcessors = hadAnnotationProcessors;
		this.compilationInfo = compilationInfo;
	}

	@Override
	public boolean hadAnnotationProcessors() {
		return hadAnnotationProcessors;
	}

	@Override
	public SakerPath getClassDirectory() {
		return outputConfig.getClassDirectory();
	}

	@Override
	public SakerPath getHeaderDirectory() {
		return outputConfig.getHeaderDirectory();
	}

	@Override
	public SakerPath getResourceDirectory() {
		return outputConfig.getResourceDirectory();
	}

	@Override
	public SakerPath getSourceGenDirectory() {
		return outputConfig.getSourceGenDirectory();
	}

	@Override
	public Collection<ClassSignature> getClassSignatures() {
		return compilationInfo == null ? null : compilationInfo.getRealizedClassSignatures();
	}

	@Override
	public Collection<PackageSignature> getPackageSignatures() {
		return compilationInfo == null ? null : compilationInfo.getRealizedPackageSignatures();
	}

	@Override
	public ModuleSignature getModuleSignature() {
		return compilationInfo == null ? null : compilationInfo.getRealizedModuleSignature();
	}

	@Override
	public String getModuleName() {
		return outputConfig.getModuleName();
	}

	@Override
	public Collection<JavaSourceDirectory> getSourceDirectories() {
		return sourceDirectories;
	}

	@Override
	public JavaClassPath getClassPath() {
		return classPath;
	}

	@Override
	public JavaModulePath getModulePath() {
		return modulePath;
	}

	@Override
	public JavaCompilationWorkerTaskIdentifier getCompilationTaskIdentifier() {
		return outputConfig.getCompilationTaskIdentifier();
	}

	@Override
	public Object getAbiVersionKey() {
		return abiVersionKey;
	}

	@Override
	public Object getImplementationVersionKey() {
		return implementationVersionKey;
	}

	@Override
	public SDKDescription getJavaSDK() {
		return outputConfig.getJavaSDK();
	}

	@Override
	public NavigableMap<String, SDKDescription> getSDKs() {
		return outputConfig.getSDKs();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputConfig);
		out.writeObject(abiVersionKey);
		out.writeObject(implementationVersionKey);
		out.writeObject(classPath);
		out.writeObject(modulePath);
		out.writeBoolean(hadAnnotationProcessors);

		out.writeObject(compilationInfo);
		SerialUtils.writeExternalCollection(out, sourceDirectories);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputConfig = (JavaCompilationConfigurationOutput) in.readObject();
		abiVersionKey = in.readObject();
		implementationVersionKey = in.readObject();
		classPath = (JavaClassPath) in.readObject();
		modulePath = (JavaModulePath) in.readObject();
		hadAnnotationProcessors = in.readBoolean();

		compilationInfo = (CompilationInfo) in.readObject();
		sourceDirectories = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public String toString() {
		return "InternalJavaCompilerOutput [" + outputConfig.getCompilationTaskIdentifier().getPassIdentifier() + "]";
	}

}
