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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.compile.signature.change.ClassChangedABIChange;
import saker.java.compiler.impl.compile.signature.change.PackageAnnotationsChangeABIChange;
import saker.java.compiler.impl.compile.signature.change.member.FieldChangedABIChange;
import saker.java.compiler.impl.compile.signature.change.member.MethodChangedABIChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;

public class TopLevelABIUsageImpl extends AbiUsageImpl implements TopLevelAbiUsage {
	private static final long serialVersionUID = 1L;

	private String packageName;

	private NavigableMap<ClassABIInfo, MemberABIUsage> classes = new TreeMap<>();
	private NavigableMap<MethodABIInfo, Collection<MemberABIUsage>> methods = new TreeMap<>();
	private NavigableMap<FieldABIInfo, MemberABIUsage> fields = new TreeMap<>();

	private MemberABIUsage packageUsage;

	/**
	 * For {@link Externalizable}.
	 */
	public TopLevelABIUsageImpl() {
	}

	public TopLevelABIUsageImpl(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public Map<FieldABIInfo, MemberABIUsage> getFields() {
		return fields;
	}

	public void setPackageUsage(MemberABIUsage packageUsage) {
		this.packageUsage = packageUsage;
	}

	@Override
	public MemberABIUsage getPackageUsage() {
		return packageUsage;
	}

	public void addMember(ClassSignature enclosingclass, FieldSignature field, MemberABIUsage usage) {
		Objects.requireNonNull(enclosingclass, "class signature");
		Objects.requireNonNull(field, "field");
		Objects.requireNonNull(usage, "abi usage");
		FieldABIInfo abiinfo = FieldABIInfo.create(enclosingclass, field);
		MemberABIUsage prev = fields.putIfAbsent(abiinfo, usage);
		if (prev != null) {
			throw new AssertionError("Member usage defined multiple times: " + enclosingclass.getClass().getName()
					+ ": " + enclosingclass + " field: " + field.getClass().getName() + ": " + field + " for "
					+ abiinfo);
		}
	}

	public void addMember(ClassSignature enclosingclass, MethodSignature method, MemberABIUsage usage) {
		Objects.requireNonNull(enclosingclass, "class signature");
		Objects.requireNonNull(method, "method");
		Objects.requireNonNull(usage, "abi usage");
		methods.computeIfAbsent(new MethodABIInfo(enclosingclass, method), Functionals.arrayListComputer()).add(usage);
		//not checked for duplicates, as the key only contains the method name, not the method signature
	}

	public void addMember(ClassSignature clazz, MemberABIUsage usage) {
		Objects.requireNonNull(clazz, "class signature");
		Objects.requireNonNull(usage, "abi usage");
		ClassABIInfo abiinfo = new ClassABIInfo(clazz);
		MemberABIUsage prev = classes.putIfAbsent(abiinfo, usage);
		if (prev != null) {
			throw new AssertionError("Member usage defined multiple times: " + clazz.getClass().getName() + ": " + clazz
					+ " for " + abiinfo);
		}
	}

//	@Override
//	public boolean addABIChangeForEachClassMember(BiFunction<ClassMemberSignature, ABIUsage, Boolean> predicate, Consumer<ABIChange> foundchanges) {
//		boolean result = false;
//		for (Entry<ClassSignature, MemberABIUsage> entry : getClasses().entrySet()) {
//			MemberABIUsage memberusage = entry.getValue();
//			ClassSignature sig = entry.getKey();
//			if (predicate.apply(sig, memberusage)) {
//				foundchanges.accept(new ClassChangedABIChange(sig));
//				result = true;
//			}
//		}
//		for (Entry<FieldSignature, MemberABIUsage> entry : getFields().entrySet()) {
//			MemberABIUsage memberusage = entry.getValue();
//			FieldSignature sig = entry.getKey();
//			if (predicate.apply(sig, memberusage)) {
//				foundchanges.accept(new FieldChangedABIChange(sig));
//				result = true;
//			}
//		}
//		for (Entry<MethodSignature, MemberABIUsage> entry : getMethods().entrySet()) {
//			MemberABIUsage memberusage = entry.getValue();
//			MethodSignature sig = entry.getKey();
//			if (predicate.apply(sig, memberusage)) {
//				foundchanges.accept(new MethodChangedABIChange(sig));
//				result = true;
//			}
//		}
//		return result;
//	}

	@Override
	public boolean addABIChangeForEachMember(Function<AbiUsage, Boolean> predicate, Consumer<AbiChange> foundchanges) {
		boolean result = false;
		MemberABIUsage packusage = getPackageUsage();
		if (packusage != null && predicate.apply(packusage)) {
			foundchanges.accept(new PackageAnnotationsChangeABIChange(getPackageName()));
			result = true;
		}
		for (Entry<ClassABIInfo, MemberABIUsage> entry : classes.entrySet()) {
			MemberABIUsage memberusage = entry.getValue();
			if (predicate.apply(memberusage)) {
				ClassABIInfo info = entry.getKey();
				foundchanges.accept(new ClassChangedABIChange(info.getCanonicalName()));
				result = true;
			}
		}
		for (Entry<FieldABIInfo, MemberABIUsage> entry : fields.entrySet()) {
			MemberABIUsage memberusage = entry.getValue();
			if (predicate.apply(memberusage)) {
				FieldABIInfo info = entry.getKey();
				foundchanges.accept(new FieldChangedABIChange(info.getClassCanonicalName(), info.getFieldName()));
				result = true;
			}
		}
		for (Entry<MethodABIInfo, Collection<MemberABIUsage>> entry : methods.entrySet()) {
//			MemberABIUsage memberusage = entry.getValue();
			for (MemberABIUsage memberusage : entry.getValue()) {
				if (predicate.apply(memberusage)) {
					MethodABIInfo info = entry.getKey();
					foundchanges.accept(new MethodChangedABIChange(info.getClassCanonicalName(), info.getMethodName()));
					result = true;
				}
			}
		}
		return result;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(packageName);

		SerialUtils.writeExternalMap(out, classes, (o, v) -> v.writeExternal(o), ObjectOutput::writeObject);
		SerialUtils.writeExternalMap(out, methods, (o, v) -> v.writeExternal(o), SerialUtils::writeExternalCollection);

		int size = fields.size();
		out.writeInt(size);
		Iterator<Entry<FieldABIInfo, MemberABIUsage>> it = fields.entrySet().iterator();
		while (size-- > 0) {
			Entry<FieldABIInfo, MemberABIUsage> entry = it.next();
			FieldABIInfo info = entry.getKey();
			MemberABIUsage usage = entry.getValue();
			//we use this trick to differentiate between constant and non constant FieldABIInfos
			//serialize in different order to signal const and non-const
			if (info.hasConstantValue()) {
				out.writeObject(info.getClassCanonicalName());
				out.writeObject(info.getFieldName());
				out.writeObject(usage);
			} else {
				out.writeObject(usage);
				out.writeObject(info.getClassCanonicalName());
				out.writeObject(info.getFieldName());
			}
		}
		if (it.hasNext()) {
			throw new ConcurrentModificationException("fields modified during serialization");
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		packageName = (String) in.readObject();

		classes = SerialUtils.readExternalSortedImmutableNavigableMap(in, ClassABIInfo::createExternal,
				SerialUtils::readExternalObject);
		methods = SerialUtils.readExternalSortedImmutableNavigableMap(in, MethodABIInfo::createExternal,
				SerialUtils::readExternalImmutableList);

		int fieldssize = in.readInt();
		FieldABIInfo[] keys = new FieldABIInfo[fieldssize];
		MemberABIUsage[] vals = new MemberABIUsage[fieldssize];
		for (int i = 0; i < fieldssize; ++i) {
			Object first = in.readObject();
			FieldABIInfo info;
			MemberABIUsage usage;
			if (first instanceof String) {
				//const branch
				String fieldname = (String) in.readObject();
				usage = (MemberABIUsage) in.readObject();
				info = FieldABIInfo.createConstant((String) first, fieldname);
			} else {
				//non-const branch
				usage = (MemberABIUsage) first;
				String classname = (String) in.readObject();
				String fieldname = (String) in.readObject();
				info = FieldABIInfo.create(classname, fieldname);
			}
			keys[i] = info;
			vals[i] = usage;
		}
		fields = ImmutableUtils.unmodifiableNavigableMap(keys, vals);
	}
}
