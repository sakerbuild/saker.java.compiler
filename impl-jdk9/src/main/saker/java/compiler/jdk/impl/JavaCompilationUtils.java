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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureData;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;
import saker.java.compiler.jdk.impl.parser.usage.AbiUsageParser;
import saker.java.compiler.util8.impl.Java8LanguageUtils;
import saker.java.compiler.util9.impl.Java9LanguageUtils;
import saker.java.compiler.util9.impl.file.IncrementalJavaFileManager9;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation;
import saker.java.compiler.util8.impl.file.IncrementalJavaFileManager8;
import saker.build.thirdparty.saker.util.ConcatIterable;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;

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

	public static IncrementalElementsTypes createElementsTypes(Elements realelements, Object javacsync, String srcver,
			ParserCache cache) {
		return new IncrementalElementsTypes(realelements, javacsync, cache);
	}

	public static RealizedSignatureData getRealizedSignatures(CompilationUnitTree unit, Trees trees, String filename,
			ParserCache cache) {
		return Java9LanguageUtils.getRealizedSignatures(unit, trees, filename, cache);
	}

	public static JavaFileManager createFileManager(StandardJavaFileManager stdfilemanager,
			IncrementalDirectoryPaths directorypaths) {
		return new StandardIncrementalJavaFileManager(stdfilemanager, directorypaths);
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
		Java8LanguageUtils.applyRMIProperties(builder);
		Java9LanguageUtils.applyRMIProperties(builder);
	}

	public static String getModuleNameOf(Element elem) {
		return IncrementalElementsTypes9.getModuleOfImpl(elem).getQualifiedName().toString();
	}

	public static boolean isModuleElementKind(ElementKind kind) {
		return kind == ElementKind.MODULE;
	}

	public static boolean isRecordElementKind(ElementKind kind) {
		return false;
	}

	public static boolean isRecordComponentElementKind(ElementKind kind) {
		return false;
	}

	public static ElementKind getModuleElementKind() {
		return ElementKind.MODULE;
	}

	public static ElementKind getRecordElementKind() {
		return null;
	}

	public static ElementKind getRecordComponentElementKind() {
		return null;
	}

	public static void addTreeKindToElementKindMapping(Map<Tree.Kind, ElementKind> map) {
	}

	//when --release option is used, only Java 9 requires the file manager to implement StandardJavaFileManager
	private static class StandardIncrementalJavaFileManager extends IncrementalJavaFileManager9
			implements StandardJavaFileManager {
		public StandardIncrementalJavaFileManager(StandardJavaFileManager fileManager,
				IncrementalDirectoryPaths directorypaths) {
			super(fileManager, directorypaths);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
			return fileManager.getJavaFileObjectsFromFiles(files);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
			return fileManager.getJavaFileObjects(files);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
			return fileManager.getJavaFileObjectsFromStrings(names);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
			return fileManager.getJavaFileObjects(names);
		}

		@Override
		public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
			fileManager.setLocation(location, files);
		}

		@Override
		public Iterable<? extends File> getLocation(Location location) {
			return fileManager.getLocation(location);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(Iterable<? extends Path> paths) {
			return fileManager.getJavaFileObjectsFromPaths(paths);
		}

		@Override
		public Iterable<? extends JavaFileObject> getJavaFileObjects(Path... paths) {
			return fileManager.getJavaFileObjects(paths);
		}

		@Override
		public void setLocationFromPaths(Location location, Collection<? extends Path> paths) throws IOException {
			fileManager.setLocationFromPaths(location, paths);
		}

		@Override
		public void setLocationForModule(Location location, String moduleName, Collection<? extends Path> paths)
				throws IOException {
			fileManager.setLocationForModule(location, moduleName, paths);
		}

		@Override
		public Iterable<? extends Path> getLocationAsPaths(Location location) {
			return fileManager.getLocationAsPaths(location);
		}

		@Override
		public Path asPath(FileObject file) {
			return fileManager.asPath(file);
		}

		@Override
		public void setPathFactory(PathFactory f) {
			fileManager.setPathFactory(f);
		}

		@Override
		public boolean handleOption(String current, Iterator<String> remaining) {
			if ("--system".equals(current)) {
				//DONT ALLOW SETTING THE --system OPTION
				//this is called on JDK 9 in the Arguments.handleReleaseOptions function automatically
				//and messes up the system module path
				return false;
			}
			return super.handleOption(current, remaining);
		}
	}
}
