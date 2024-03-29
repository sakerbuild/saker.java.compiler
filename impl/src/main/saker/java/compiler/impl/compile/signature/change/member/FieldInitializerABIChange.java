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
package saker.java.compiler.impl.compile.signature.change.member;

import java.util.Map.Entry;
import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.AbiUsage;
import saker.java.compiler.impl.compile.handler.usage.FieldABIInfo;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;

public class FieldInitializerABIChange implements AbiChange {
	private String classCanonicalName;
	private String field;

	public FieldInitializerABIChange(ClassSignature enclosingclass, FieldSignature var) {
		this.classCanonicalName = enclosingclass.getCanonicalName();
		this.field = var.getSimpleName();
	}

	public FieldInitializerABIChange(String classCanonicalName, String field) {
		this.classCanonicalName = classCanonicalName;
		this.field = field;
	}

	public FieldInitializerABIChange(FieldABIInfo info) {
		this(info.getClassCanonicalName(), info.getFieldName());
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		boolean result = false;
		for (Entry<FieldABIInfo, ? extends AbiUsage> entry : usage.getFields().entrySet()) {
			if (entry.getValue().isReferencesField(classCanonicalName, field)) {
				FieldABIInfo info = entry.getKey();
				foundchanges.accept(new FieldInitializerABIChange(info));
				result = true;
			}
		}
		if (result) {
			return true;
		}
		if (usage.isReferencesField(classCanonicalName, field)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class field initializer changed: " + classCanonicalName + ": " + field;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldInitializerABIChange other = (FieldInitializerABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

}
