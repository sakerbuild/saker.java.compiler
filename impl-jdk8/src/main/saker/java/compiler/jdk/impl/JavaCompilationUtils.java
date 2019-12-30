package saker.java.compiler.jdk.impl;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureData;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;
import saker.java.compiler.jdk.impl.parser.usage.AbiUsageParser;
import saker.java.compiler.util8.impl.Java8LanguageUtils;
import saker.java.compiler.util8.impl.file.IncrementalJavaFileManager8;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;

public class JavaCompilationUtils {
	private JavaCompilationUtils() {
		throw new UnsupportedOperationException();
	}

	public static final ElementKind MODULE_ELEMENT_KIND = null;

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
		return Java8LanguageUtils.getRealizedSignatures(unit, trees, filename, cache);
	}

	public static JavaFileManager createFileManager(StandardJavaFileManager stdfilemanager,
			IncrementalDirectoryPaths directorypaths) {
		return new IncrementalJavaFileManager8(stdfilemanager, directorypaths);
	}

	public static void applyRMIProperties(RMITransferProperties.Builder propertiesbuilder) {
		Java8LanguageUtils.applyRMIProperties(propertiesbuilder);
	}

	public static String getModuleNameOf(Element elem) {
		return null;
	}

	public static boolean isModuleElementKind(ElementKind kind) {
		return false;
	}

	public static void addModuleElementKind(Collection<? super ElementKind> coll) {
	}
}
