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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public abstract class AbiUsageImpl implements Externalizable, AbiUsage {
	private static final long serialVersionUID = 1L;

	private NavigableSet<String> presentSimpleTypeIdentifiers = null;
	private NavigableSet<String> presentSimpleVariableIdentifiers = null;
	private NavigableSet<String> presentTypeCanonicalNames = null;

	private NavigableSet<String> usedTypeCanonicalNames = null;
	private NavigableMap<String, NavigableSet<String>> referencedTypeFields = null;
	private NavigableMap<String, NavigableSet<String>> referencedTypeMethods = null;
	/**
	 * Represents all type names, that are being inherited from in this ABIUsage
	 */
	private NavigableSet<String> inheritedTypeCanonicalNames = null;

	public AbiUsageImpl() {
	}

	public abstract void addWildcardTypeImportPath(String qualifiedpath);

	public abstract void addWildcardStaticImportPath(String qualifiedpath);

	@Override
	public abstract boolean hasWildcardTypeImportPath(String path);

	@Override
	public abstract boolean hasWildcardStaticImportPath(String path);

	public void addFieldMemberReference(String typename, String field) {
		if (referencedTypeFields == null) {
			referencedTypeFields = new TreeMap<>();
		}
		referencedTypeFields.computeIfAbsent(typename, Functionals.treeSetComputer()).add(field);
	}

	public void addMethodMemberReference(String typename, String method) {
		if (referencedTypeMethods == null) {
			referencedTypeMethods = new TreeMap<>();
		}
		referencedTypeMethods.computeIfAbsent(typename, Functionals.treeSetComputer()).add(method);
	}

	public void addTypeNameReference(String typename) {
		if (presentTypeCanonicalNames == null) {
			presentTypeCanonicalNames = new TreeSet<>();
		}
		presentTypeCanonicalNames.add(typename);
	}

	public void addUsedType(String typename) {
		if (usedTypeCanonicalNames == null) {
			usedTypeCanonicalNames = new TreeSet<>();
		}
		this.usedTypeCanonicalNames.add(typename);
	}

	public void addPresentSimpleTypeIdentifier(String identifier) {
		if (presentSimpleTypeIdentifiers == null) {
			presentSimpleTypeIdentifiers = new TreeSet<>();
		}
		presentSimpleTypeIdentifiers.add(identifier);
	}

	public void addPresentSimpleVariableIdentifier(String identifier) {
		if (presentSimpleVariableIdentifiers == null) {
			presentSimpleVariableIdentifiers = new TreeSet<>();
		}
		presentSimpleVariableIdentifiers.add(identifier);
	}

	public void addTypeInheritance(String superclass) {
		if (inheritedTypeCanonicalNames == null) {
			inheritedTypeCanonicalNames = new TreeSet<>();
		}
		if (usedTypeCanonicalNames == null) {
			usedTypeCanonicalNames = new TreeSet<>();
		}
		inheritedTypeCanonicalNames.add(superclass);
		usedTypeCanonicalNames.add(superclass);
	}

	@Override
	public boolean isReferencesClass(String canonicaltypenameame) {
		if (presentTypeCanonicalNames != null && presentTypeCanonicalNames.contains(canonicaltypenameame)) {
			return true;
		}
		if (usedTypeCanonicalNames != null && usedTypeCanonicalNames.contains(canonicaltypenameame)) {
			return true;
		}
		if (inheritedTypeCanonicalNames != null && inheritedTypeCanonicalNames.contains(canonicaltypenameame)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReferencesPackageOrSubPackage(String packagename) {
		if (packagename.equals(this.getPackageName())) {
			return true;
		}
		String searchfor = packagename + ".";
		if (isReferencesPackageOrSubPackageInTypeNameMapImpl(searchfor, presentTypeCanonicalNames)) {
			return true;
		}
		if (isReferencesPackageOrSubPackageInTypeNameMapImpl(searchfor, usedTypeCanonicalNames)) {
			return true;
		}
		if (isReferencesPackageOrSubPackageInTypeNameMapImpl(searchfor, inheritedTypeCanonicalNames)) {
			return true;
		}
		if (hasWildcardTypeImportPath(packagename)) {
			return true;
		}
		return false;
	}

	private static boolean isReferencesPackageOrSubPackageInTypeNameMapImpl(String searchfor,
			NavigableSet<String> typenamemap) {
		if (typenamemap != null) {
			String higher = typenamemap.higher(searchfor);
			if (higher != null && higher.startsWith(searchfor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSimpleTypePresent(String simplename) {
		return presentSimpleTypeIdentifiers != null && presentSimpleTypeIdentifiers.contains(simplename);
	}

	@Override
	public boolean isSimpleVariablePresent(String simplename) {
		return presentSimpleVariableIdentifiers != null && presentSimpleVariableIdentifiers.contains(simplename);
	}

	@Override
	public boolean isReferencesField(String canonicaltypename, String member) {
		if (referencedTypeFields == null) {
			return false;
		}
		SortedSet<String> fields = referencedTypeFields.get(canonicaltypename);
		if (fields != null) {
			return fields.contains(member);
		}
		return false;
	}

	@Override
	public boolean isReferencesMethod(String canonicaltypename, String name) {
		if (referencedTypeMethods == null) {
			return false;
		}
		SortedSet<String> fields = referencedTypeMethods.get(canonicaltypename);
		if (fields != null) {
			return fields.contains(name);
		}
		return false;
	}

	@Override
	public boolean isTypeChangeAware(String canonicaltypename) {
		return (inheritedTypeCanonicalNames != null && inheritedTypeCanonicalNames.contains(canonicaltypename))
				|| (usedTypeCanonicalNames != null && usedTypeCanonicalNames.contains(canonicaltypename));
	}

	@Override
	public boolean isInheritanceChangeAffected(String canonicaltypename) {
		return (inheritedTypeCanonicalNames != null && inheritedTypeCanonicalNames.contains(canonicaltypename))
				|| (usedTypeCanonicalNames != null && usedTypeCanonicalNames.contains(canonicaltypename));
	}

	@Override
	public boolean isInheritesFromClass(String canonicalname) {
		return inheritedTypeCanonicalNames != null && inheritedTypeCanonicalNames.contains(canonicalname);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, inheritedTypeCanonicalNames);
		SerialUtils.writeExternalCollection(out, presentSimpleTypeIdentifiers);
		SerialUtils.writeExternalCollection(out, presentSimpleVariableIdentifiers);
		SerialUtils.writeExternalCollection(out, presentTypeCanonicalNames);
		SerialUtils.writeExternalCollection(out, usedTypeCanonicalNames);

		SerialUtils.writeExternalMap(out, referencedTypeFields, ObjectOutput::writeUTF,
				SerialUtils::writeExternalCollection);
		SerialUtils.writeExternalMap(out, referencedTypeMethods, ObjectOutput::writeUTF,
				SerialUtils::writeExternalCollection);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inheritedTypeCanonicalNames = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		presentSimpleTypeIdentifiers = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		presentSimpleVariableIdentifiers = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		presentTypeCanonicalNames = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		usedTypeCanonicalNames = SerialUtils.readExternalSortedImmutableNavigableSet(in);

		referencedTypeFields = SerialUtils.readExternalSortedImmutableNavigableMap(in, ObjectInput::readUTF,
				SerialUtils::readExternalSortedImmutableNavigableSet);
		referencedTypeMethods = SerialUtils.readExternalSortedImmutableNavigableMap(in, ObjectInput::readUTF,
				SerialUtils::readExternalSortedImmutableNavigableSet);
	}

	@Override
	public String toString() {
		return "ABIUsage ["
				+ (presentSimpleTypeIdentifiers != null
						? "presentSimpleTypeIdentifiers=" + presentSimpleTypeIdentifiers + ", "
						: "")
				+ (presentSimpleVariableIdentifiers != null
						? "presentSimpleVariableIdentifiers=" + presentSimpleVariableIdentifiers + ", "
						: "")
				+ (presentTypeCanonicalNames != null ? "presentTypeCanonicalNames=" + presentTypeCanonicalNames + ", "
						: "")
				+ (usedTypeCanonicalNames != null ? "usedTypeCanonicalNames=" + usedTypeCanonicalNames + ", " : "")
				+ (referencedTypeFields != null ? "referencedTypeFields=" + referencedTypeFields + ", " : "")
				+ (referencedTypeMethods != null ? "referencedTypeMethods=" + referencedTypeMethods + ", " : "")
				+ (inheritedTypeCanonicalNames != null ? "inheritedTypeCanonicalNames=" + inheritedTypeCanonicalNames
						: "")
				+ "]";
	}

}
