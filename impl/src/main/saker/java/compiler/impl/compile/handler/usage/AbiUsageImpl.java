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
import java.util.Objects;
import java.util.TreeMap;

import saker.build.thirdparty.saker.util.io.SerialUtils;

public abstract class AbiUsageImpl implements Externalizable, AbiUsage {
	private static final long serialVersionUID = 1L;

	private static final byte FLAG_PRESENT_SIMPLE_TYPE_IDENTIFIER = 1 << 0;
	private static final byte FLAG_PRESENT_SIMPLE_VARIABLE_IDENTIFIER = 1 << 1;
	private static final byte FLAG_PRESENT_TYPE_CANONICAL_NAME = 1 << 2;
	private static final byte FLAG_USED_TYPE_CANONICAL_NAME = 1 << 3;
	private static final byte FLAG_INHERITED_TYPE_CANONICAL_NAME = 1 << 4;
	private static final byte FLAG_WILDCARD_TYPE_IMPORT_PATH = 1 << 5;
	private static final byte FLAG_WILDCARD_STATIC_IMPORT_PATH = 1 << 6;

	private static final byte FLAGS_RESERVED = (byte) (1 << 7);

	private static final byte REFERENCED_TYPE_FLAG_FIELD = 1 << 0;
	private static final byte REFERENCED_TYPE_FLAG_METHOD = 1 << 1;

	//cached boxed Byte instances to avoid auto boxing in compute methods
	private static final Byte BOXED_REFERENCED_TYPE_FLAG_FIELD = 1 << 0;
	private static final Byte BOXED_REFERENCED_TYPE_FLAG_METHOD = 1 << 1;

	private NavigableMap<String, ReferenceInfo> referenceInfos = null;

	private transient byte presentSimpleFlags;

	public AbiUsageImpl() {
	}

	private NavigableMap<String, ReferenceInfo> getReferenceInfos() {
		return referenceInfos;
	}

	private NavigableMap<String, ReferenceInfo> getReferenceInfosCreate() {
		NavigableMap<String, ReferenceInfo> map = referenceInfos;
		if (map != null) {
			return map;
		}
		map = new TreeMap<>();
		referenceInfos = map;
		return map;
	}

	private ReferenceInfo getReferenceInfoCreate(String identifier) {
		return getReferenceInfosCreate().computeIfAbsent(identifier, x -> new ReferenceInfo());
	}

	private ReferenceInfo getReferenceInfo(String identifier) {
		NavigableMap<String, ReferenceInfo> refinfos = getReferenceInfos();
		if (refinfos == null) {
			return null;
		}
		return refinfos.get(identifier);
	}

	public void addWildcardTypeImportPath(String qualifiedpath) {
		getReferenceInfoCreate(qualifiedpath).flags |= FLAG_WILDCARD_TYPE_IMPORT_PATH;
	}

	public void addWildcardStaticImportPath(String qualifiedpath) {
		getReferenceInfoCreate(qualifiedpath).flags |= FLAG_WILDCARD_STATIC_IMPORT_PATH;
	}

	@Override
	public boolean hasWildcardTypeImportPath(String path) {
		if (Objects.equals(getPackageName(), path) || "java.lang".equals(path)) {
			return true;
		}
		ReferenceInfo ri = getReferenceInfo(path);
		if (ri != null) {
			return (ri.flags & (FLAG_WILDCARD_TYPE_IMPORT_PATH)) != 0;
		}
		return false;
	}

	@Override
	public boolean hasWildcardStaticImportPath(String path) {
		ReferenceInfo ri = getReferenceInfo(path);
		if (ri != null) {
			return (ri.flags & (FLAG_WILDCARD_STATIC_IMPORT_PATH)) != 0;
		}
		return false;
	}

	public void addFieldMemberReference(String typename, String field) {
		getReferenceInfoCreate(typename).getMemberFlagsCreate().compute(field,
				(k, v) -> v == null ? BOXED_REFERENCED_TYPE_FLAG_FIELD
						: (byte) (v.byteValue() | REFERENCED_TYPE_FLAG_FIELD));
	}

	public void addMethodMemberReference(String typename, String method) {
		getReferenceInfoCreate(typename).getMemberFlagsCreate().compute(method,
				(k, v) -> v == null ? BOXED_REFERENCED_TYPE_FLAG_METHOD
						: (byte) (v.byteValue() | REFERENCED_TYPE_FLAG_METHOD));
	}

	public void addTypeNameReference(String typename) {
		getReferenceInfoCreate(typename).flags |= FLAG_PRESENT_TYPE_CANONICAL_NAME;
	}

	public void addUsedType(String typename) {
		getReferenceInfoCreate(typename).flags |= FLAG_USED_TYPE_CANONICAL_NAME;
	}

	public void addPresentSimpleTypeIdentifier(String identifier) {
		presentSimpleFlags |= FLAG_PRESENT_SIMPLE_TYPE_IDENTIFIER;
		getReferenceInfoCreate(identifier).flags |= FLAG_PRESENT_SIMPLE_TYPE_IDENTIFIER;
	}

	public void addPresentSimpleVariableIdentifier(String identifier) {
		presentSimpleFlags |= FLAG_PRESENT_SIMPLE_VARIABLE_IDENTIFIER;
		getReferenceInfoCreate(identifier).flags |= FLAG_PRESENT_SIMPLE_VARIABLE_IDENTIFIER;
	}

	public void addTypeInheritance(String superclass) {
		getReferenceInfoCreate(superclass).flags |= FLAG_USED_TYPE_CANONICAL_NAME | FLAG_INHERITED_TYPE_CANONICAL_NAME;
	}

	@Override
	public boolean isReferencesClass(String canonicaltypename) {
		ReferenceInfo ri = getReferenceInfo(canonicaltypename);
		if (ri != null) {
			return (ri.flags & (FLAG_PRESENT_TYPE_CANONICAL_NAME | FLAG_USED_TYPE_CANONICAL_NAME
					| FLAG_INHERITED_TYPE_CANONICAL_NAME)) != 0;
		}
		return false;
	}

	@Override
	public boolean isReferencesPackageOrSubPackage(String packagename) {
		if (packagename.equals(this.getPackageName())) {
			return true;
		}
		String searchfor = packagename + ".";
		if (isReferencesPackageOrSubPackageInTypeNameMapImpl(searchfor, referenceInfos)) {
			return true;
		}
		if (hasWildcardTypeImportPath(packagename)) {
			return true;
		}
		return false;
	}

	private static boolean isReferencesPackageOrSubPackageInTypeNameMapImpl(String searchfor,
			NavigableMap<String, ?> typenamemap) {
		if (typenamemap != null) {
			String higher = typenamemap.higherKey(searchfor);
			if (higher != null && higher.startsWith(searchfor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSimpleTypePresent(String simplename) {
		ReferenceInfo ri = getReferenceInfo(simplename);
		if (ri != null) {
			return (ri.flags & (FLAG_PRESENT_SIMPLE_TYPE_IDENTIFIER)) != 0;
		}
		return false;
	}

	@Override
	public boolean hasAnySimpleTypeIdentifier() {
		if ((presentSimpleFlags & FLAG_PRESENT_SIMPLE_TYPE_IDENTIFIER) != 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isSimpleVariablePresent(String simplename) {
		ReferenceInfo ri = getReferenceInfo(simplename);
		if (ri != null) {
			return (ri.flags & (FLAG_PRESENT_SIMPLE_VARIABLE_IDENTIFIER)) != 0;
		}
		return false;
	}

	@Override
	public boolean hasAnySimpleVariableIdentifier() {
		if ((presentSimpleFlags & FLAG_PRESENT_SIMPLE_VARIABLE_IDENTIFIER) != 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReferencesField(String canonicaltypename, String member) {
		ReferenceInfo refinfo = getReferenceInfo(canonicaltypename);
		if (refinfo == null) {
			return false;
		}
		Byte flags = refinfo.getMemberFlag(member);
		if (flags == null) {
			return false;
		}
		return (flags & REFERENCED_TYPE_FLAG_FIELD) != 0;
	}

	@Override
	public boolean isReferencesMethod(String canonicaltypename, String name) {
		ReferenceInfo refinfo = getReferenceInfo(canonicaltypename);
		if (refinfo == null) {
			return false;
		}
		Byte flags = refinfo.getMemberFlag(name);
		if (flags == null) {
			return false;
		}
		return (flags & REFERENCED_TYPE_FLAG_METHOD) != 0;
	}

	@Override
	public boolean isTypeChangeAware(String canonicaltypename) {
		ReferenceInfo ri = getReferenceInfo(canonicaltypename);
		if (ri != null) {
			return (ri.flags & (FLAG_USED_TYPE_CANONICAL_NAME | FLAG_INHERITED_TYPE_CANONICAL_NAME)) != 0;
		}
		return false;
	}

	@Override
	public boolean isInheritanceChangeAffected(String canonicaltypename) {
		ReferenceInfo ri = getReferenceInfo(canonicaltypename);
		if (ri != null) {
			return (ri.flags & (FLAG_USED_TYPE_CANONICAL_NAME | FLAG_INHERITED_TYPE_CANONICAL_NAME)) != 0;
		}
		return false;
	}

	@Override
	public boolean isInheritesFromClass(String canonicalname) {
		ReferenceInfo ri = getReferenceInfo(canonicalname);
		if (ri != null) {
			return (ri.flags & (FLAG_INHERITED_TYPE_CANONICAL_NAME)) != 0;
		}
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, referenceInfos, ObjectOutput::writeUTF, (o, info) -> info.writeExternal(o));
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		referenceInfos = SerialUtils.readExternalSortedImmutableNavigableMap(in, ObjectInput::readUTF, i -> {
			ReferenceInfo ri = new ReferenceInfo();
			ri.readExternal(i);
			presentSimpleFlags |= ri.flags;
			return ri;
		});
	}

	protected static void writeStringByteMap(ObjectOutput out, NavigableMap<String, Byte> map) throws IOException {
		SerialUtils.writeExternalMap(out, map, ObjectOutput::writeUTF, (ObjectOutput o, Byte b) -> o.writeByte(b));
	}

	protected static NavigableMap<String, Byte> readStringByteMap(ObjectInput in)
			throws ClassNotFoundException, IOException {
		return SerialUtils.readExternalSortedImmutableNavigableMap(in, ObjectInput::readUTF, i -> i.readByte());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + referenceInfos + "]";
	}

	private static final class ReferenceInfo implements Externalizable {
		private static final long serialVersionUID = 1L;

		protected byte flags;
		protected NavigableMap<String, Byte> memberFlags;

		public ReferenceInfo() {
		}

		public NavigableMap<String, Byte> getMemberFlags() {
			return memberFlags;
		}

		public Byte getMemberFlag(String member) {
			NavigableMap<String, Byte> memflags = this.memberFlags;
			if (memflags == null) {
				return null;
			}
			return memflags.get(member);
		}

		public NavigableMap<String, Byte> getMemberFlagsCreate() {
			NavigableMap<String, Byte> map = this.memberFlags;
			if (map != null) {
				return map;
			}
			map = new TreeMap<>();
			this.memberFlags = map;
			return map;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			if (memberFlags == null) {
				//use the reserved flag to convey the presence of the member map, this lets us write a few bytes less to the output
				out.writeByte(flags);
			} else {
				out.writeByte(flags | FLAGS_RESERVED);
				writeStringByteMap(out, memberFlags);
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			flags = in.readByte();
			if ((flags & FLAGS_RESERVED) != 0) {
				memberFlags = readStringByteMap(in);
				flags = (byte) (flags & ~FLAGS_RESERVED);
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getClass().getSimpleName());
			sb.append("[flags=");
			sb.append(flags);
			if (memberFlags != null) {
				sb.append(", memberFlags=");
				sb.append(memberFlags);
			}
			sb.append("]");
			return sb.toString();
		}

	}
}
