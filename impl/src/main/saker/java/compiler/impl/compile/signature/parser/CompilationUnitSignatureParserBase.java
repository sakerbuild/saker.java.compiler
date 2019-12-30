package saker.java.compiler.impl.compile.signature.parser;

import java.util.Map;
import java.util.NavigableMap;

import com.sun.source.tree.Tree;

import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface CompilationUnitSignatureParserBase {
	public interface ParseContextBase {
		public PackageSignature getPackageSignature();

		public ModuleSignature getModuleSignature();

		public NavigableMap<String, ClassSignature> getClasses();

		public Map<? extends Tree, ? extends Signature> getTreeSignatures();

		public Map<? extends Tree, ? extends SignaturePath> getTreeSignaturePaths();

		public String getPackageName();
		
		public ImportScope getImportScope();
	}
}
