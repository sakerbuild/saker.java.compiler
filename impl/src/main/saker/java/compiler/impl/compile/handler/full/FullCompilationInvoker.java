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
package saker.java.compiler.impl.compile.handler.full;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;

import javax.annotation.processing.Processor;

import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListStringElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeySerializeValueWrapper;
import saker.build.util.java.JavaTools;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytes;
import saker.java.compiler.impl.compile.signature.jni.NativeSignature;

public interface FullCompilationInvoker {
	public void compile(SakerPathBytes[] sources) throws IOException, JavaCompilationFailedException;

	public void setOptions(boolean generateNativeHeaders,
			@RMIWrap(RMIArrayListRemoteElementWrapper.class) List<Processor> processors,
			@RMIWrap(RMIArrayListStringElementWrapper.class) Collection<String> options,
			IncrementalDirectoryPaths directorypaths);

	@RMIWrap(RMITreeMapSerializeKeySerializeValueWrapper.class)
	public NavigableMap<String, Collection<NativeSignature>> getOutputHeaderNativeSignatures();

	public byte[] getImplementationVersionKeyHash();

	public byte[] getAbiVersionKeyHash();

	@RMICacheResult
	public default String getJavaVersionProperty() {
		return JavaTools.getCurrentJavaVersionProperty();
	}

	public String getModuleName();
}
