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

import javax.lang.model.element.ElementKind;

import com.sun.source.tree.Tree;

import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;

public class Java14LanguageUtils {
	private Java14LanguageUtils() {
		throw new UnsupportedOperationException();
	}

	public static void applyRMIProperties(RMITransferProperties.Builder builder) {
	}

	public static void addTreeKindToElementKindMapping(Map<Tree.Kind, ElementKind> map) {
		map.put(Tree.Kind.RECORD, ElementKind.RECORD);
	}
}
