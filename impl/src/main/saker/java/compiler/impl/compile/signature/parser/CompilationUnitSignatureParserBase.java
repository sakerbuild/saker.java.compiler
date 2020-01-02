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
