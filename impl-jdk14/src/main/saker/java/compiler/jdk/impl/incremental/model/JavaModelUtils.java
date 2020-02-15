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
package saker.java.compiler.jdk.impl.incremental.model;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.function.TriFunction;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.util14.impl.Java14LanguageUtils;
import saker.java.compiler.util9.impl.Java9LanguageUtils;

public class JavaModelUtils {
	private JavaModelUtils() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "rawtypes" })
	public static void addJavaKindBasedElementVisitorFunctions(
			Map<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> result) {
		Java9LanguageUtils.addJavaKindBasedElementVisitorFunctions(result);
		Java14LanguageUtils.addJava14KindBasedElementVisitorFunctions(result);
	}

	@SuppressWarnings({ "rawtypes" })
	public static void addJava9KindBasedTypeVisitorFunctions(
			Map<TypeKind, TriFunction<TypeVisitor, ? extends TypeMirror, Object, Object>> result) {
		Java9LanguageUtils.addJava9KindBasedTypeVisitorFunctions(result);
	}

	public static ModuleSignature moduleElementToSignature(Element elem, ParserCache cache) {
		return Java9LanguageUtils.moduleElementToSignature(elem, cache);
	}

}
