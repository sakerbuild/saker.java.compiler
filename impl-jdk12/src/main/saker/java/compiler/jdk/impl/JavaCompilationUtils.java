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
package saker.java.compiler.jdk.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureData;
import saker.java.compiler.impl.compile.handler.invoker.CompilationContextInformation;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;
import saker.java.compiler.jdk.impl.parser.usage.AbiUsageParser;
import saker.java.compiler.util12.impl.Java12LanguageUtils;
import saker.java.compiler.util8.impl.Java8LanguageUtils;
import saker.java.compiler.util9.impl.Java9LanguageUtils;
import saker.java.compiler.util9.impl.file.IncrementalJavaFileManager9;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;

public class JavaCompilationUtils {
	private JavaCompilationUtils() {
		throw new UnsupportedOperationException();
	}

	public static CompilationUnitSignatureParser createSignatureParser(Trees trees, String srcver, ParserCache cache) {
		return new CompilationUnitSignatureParser(trees, srcver, cache);
	}

	public static AbiUsageParser createAbiUsageParser(Trees trees, String srcver, ParserCache cache) {
		return new AbiUsageParser(trees, srcver, cache);
	}

	public static IncrementalElementsTypes createElementsTypes(Elements realelements, Object javacsync,
			CompilationContextInformation context, ParserCache cache) {
		return new IncrementalElementsTypes(realelements, javacsync, cache, context);
	}

	public static RealizedSignatureData getRealizedSignatures(CompilationUnitTree unit, Trees trees, String filename,
			ParserCache cache) {
		return Java9LanguageUtils.getRealizedSignatures(unit, trees, filename, cache);
	}

	public static JavaFileManager createFileManager(StandardJavaFileManager stdfilemanager,
			IncrementalDirectoryPaths directorypaths) {
		return new IncrementalJavaFileManager9(stdfilemanager, directorypaths);
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
		Java8LanguageUtils.applyRMIProperties(builder);
		Java9LanguageUtils.applyRMIProperties(builder);
		Java12LanguageUtils.applyRMIProperties(builder);
	}

	public static String getModuleNameOf(Element elem) {
		return Java9LanguageUtils.getModuleNameOf(elem);
	}

	public static List<? extends TypeMirror> getPermittedSubclasses(TypeElement elem) {
		return Collections.emptyList();
	}
}
