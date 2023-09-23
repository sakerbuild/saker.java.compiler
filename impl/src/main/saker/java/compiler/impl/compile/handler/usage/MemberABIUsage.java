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
package saker.java.compiler.impl.compile.handler.usage;

import java.io.Externalizable;

public final class MemberABIUsage extends AbiUsageImpl {
	private static final long serialVersionUID = 1L;

	private transient TopLevelABIUsageImpl parent;

	/**
	 * For {@link Externalizable}.
	 */
	public MemberABIUsage() {
	}

	//can be called by TopLevelABIUsageInpl
	MemberABIUsage(TopLevelABIUsageImpl parent) {
		this.parent = parent;
	}

	//called after deserialization
	void setParent(TopLevelABIUsageImpl parent) {
		this.parent = parent;
	}

	@Override
	public String getPackageName() {
		return parent.getPackageName();
	}

	@Override
	public void addWildcardTypeImportPath(String qualifiedpath) {
		parent.addWildcardTypeImportPath(qualifiedpath);
	}

	@Override
	public void addWildcardStaticImportPath(String qualifiedpath) {
		parent.addWildcardStaticImportPath(qualifiedpath);
	}

	@Override
	public boolean hasWildcardTypeImportPath(String path) {
		return parent.hasWildcardTypeImportPath(path);
	}

	@Override
	public boolean hasWildcardStaticImportPath(String path) {
		return parent.hasWildcardStaticImportPath(path);
	}

	@Override
	public void addFieldMemberReference(String typename, String field) {
		super.addFieldMemberReference(typename, field);
		parent.addFieldMemberReference(typename, field);
	}

	@Override
	public void addMethodMemberReference(String typename, String method) {
		super.addMethodMemberReference(typename, method);
		parent.addMethodMemberReference(typename, method);
	}

	@Override
	public void addTypeNameReference(String typename) {
		super.addTypeNameReference(typename);
		parent.addTypeNameReference(typename);
	}

	@Override
	public void addUsedType(String typename) {
		super.addUsedType(typename);
		parent.addUsedType(typename);
	}

	@Override
	public void addPresentSimpleTypeIdentifier(String identifier) {
		super.addPresentSimpleTypeIdentifier(identifier);
		parent.addPresentSimpleTypeIdentifier(identifier);
	}

	@Override
	public void addPresentSimpleVariableIdentifier(String identifier) {
		super.addPresentSimpleVariableIdentifier(identifier);
		parent.addPresentSimpleVariableIdentifier(identifier);
	}

	@Override
	public void addTypeInheritance(String superclass) {
		super.addTypeInheritance(superclass);
		parent.addTypeInheritance(superclass);
	}

}
