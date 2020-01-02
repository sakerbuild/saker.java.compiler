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

public interface AbiUsage {
	public String getPackageName();

	public boolean hasWildcardTypeImportPath(String path);

	public boolean hasWildcardStaticImportPath(String path);

	public boolean isReferencesClass(String canonicaltypenameame);

	public boolean isSimpleTypePresent(String simplename);

	public boolean isSimpleVariablePresent(String simplename);

	public boolean isReferencesField(String canonicaltypename, String member);

	public boolean isReferencesMethod(String canonicaltypename, String name);

	public boolean isTypeChangeAware(String canonicaltypename);

	public boolean isInheritanceChangeAffected(String canonicaltypename);

	public boolean isInheritesFromClass(String canonicalname);
	
	public boolean isReferencesPackageOrSubPackage(String packagename);

}