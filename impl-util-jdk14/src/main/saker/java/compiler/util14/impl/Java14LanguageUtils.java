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
package saker.java.compiler.util14.impl;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.sun.source.tree.Tree;

import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.util.function.TriFunction;
import saker.java.compiler.impl.JavaUtil;

public class Java14LanguageUtils {
	private Java14LanguageUtils() {
		throw new UnsupportedOperationException();
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
		JavaUtil.addCommonElementClassRMIProperties(builder, RecordComponentElement.class);
	}

	public static void addTreeKindToElementKindMapping(Map<Tree.Kind, ElementKind> map) {
		map.put(Tree.Kind.RECORD, ElementKind.RECORD);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJava14KindBasedElementVisitorFunctions(
			Map<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> result) {
		result.put(ElementKind.RECORD_COMPONENT,
				(TriFunction<ElementVisitor, RecordComponentElement, Object, Object>) ElementVisitor::visitRecordComponent);
		result.put(ElementKind.RECORD,
				(TriFunction<ElementVisitor, TypeElement, Object, Object>) ElementVisitor::visitType);
		result.put(ElementKind.BINDING_VARIABLE,
				(TriFunction<ElementVisitor, VariableElement, Object, Object>) ElementVisitor::visitVariable);
	}
}
