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
package saker.java.compiler.util8.impl;

import java.util.NavigableMap;
import java.util.TreeMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.info.RealizedSignatureData;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class Java8LanguageUtils {
	private Java8LanguageUtils() {
		throw new UnsupportedOperationException();
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
	}

	public static RealizedSignatureData getRealizedSignatures(CompilationUnitTree unit, Trees trees, String filename,
			ParserCache cache) {
		TreePath unitpath = new TreePath(unit);
		NavigableMap<String, ClassSignature> realizedclasses = new TreeMap<>();
		PackageSignature packsig = null;
		ModuleSignature modulesig = null;
		for (Tree typetrees : unit.getTypeDecls()) {
			//getTypeDecls(): The list may also include empty statements resulting from extraneous semicolons.
			if (typetrees.getKind() == Tree.Kind.EMPTY_STATEMENT) {
				continue;
			}

			Element typeelem = trees.getElement(new TreePath(unitpath, typetrees));
			TypeElement element = (TypeElement) typeelem;
			ClassSignature c = (ClassSignature) IncrementalElementsTypes.createSignatureFromJavacElement(element,
					cache);
			realizedclasses.put(c.getCanonicalName(), c);
		}

		if (JavaTaskUtils.isPackageInfoSource(filename)) {
			Element unitelem = trees.getElement(unitpath);
			if (unitelem != null) {
				PackageElement elem = (PackageElement) unitelem;
				packsig = (PackageSignature) IncrementalElementsTypes.createSignatureFromJavacElement(elem, cache);
			}
		}
		//we cannot create realized module signature, as we are running on jdk 8
		return new RealizedSignatureData(realizedclasses, packsig, modulesig);
	}
}
