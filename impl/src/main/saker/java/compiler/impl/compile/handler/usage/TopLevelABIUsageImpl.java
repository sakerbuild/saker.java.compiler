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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

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

	private NavigableSet<String> wildcardTypeImportPaths = null;
	private NavigableSet<String> wildcardStaticImportPaths = null;

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
	public Map<ClassABIInfo, MemberABIUsage> getClasses() {
		return classes;
	}

	@Override
	public Map<MethodABIInfo, Collection<MemberABIUsage>> getMethods() {
		return methods;
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
		MemberABIUsage prev = fields.put(new FieldABIInfo(enclosingclass, field), usage);
		if (prev != null) {
			throw new AssertionError("Member usage defined multiple times: " + field);
		}
	}

	public void addMember(ClassSignature enclosingclass, MethodSignature method, MemberABIUsage usage) {
		methods.computeIfAbsent(new MethodABIInfo(enclosingclass, method), Functionals.arrayListComputer()).add(usage);
//		MemberABIUsage prev = methods.put(new MethodABIInfo(method), usage);
//		if (prev != null) {
//			throw new AssertionError("Member usage defined multiple times: " + method);
//		}
	}

	public void addMember(ClassSignature clazz, MemberABIUsage usage) {
		MemberABIUsage prev = classes.put(new ClassABIInfo(clazz), usage);
		if (prev != null) {
			throw new AssertionError("Member usage defined multiple times: " + clazz);
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
	public void addWildcardTypeImportPath(String qualifiedpath) {
		if (wildcardTypeImportPaths == null) {
			wildcardTypeImportPaths = new TreeSet<>();
		}
		this.wildcardTypeImportPaths.add(qualifiedpath);
	}

	@Override
	public void addWildcardStaticImportPath(String qualifiedpath) {
		if (wildcardStaticImportPaths == null) {
			wildcardStaticImportPaths = new TreeSet<>();
		}
		this.wildcardStaticImportPaths.add(qualifiedpath);
	}

	@Override
	public boolean hasWildcardTypeImportPath(String path) {
		return (wildcardTypeImportPaths != null && wildcardTypeImportPaths.contains(path))
				|| Objects.equals(packageName, path) || "java.lang".equals(path);
	}

	@Override
	public boolean hasWildcardStaticImportPath(String path) {
		return wildcardStaticImportPaths != null && wildcardStaticImportPaths.contains(path);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(packageName);

		SerialUtils.writeExternalCollection(out, wildcardTypeImportPaths);
		SerialUtils.writeExternalCollection(out, wildcardStaticImportPaths);

		SerialUtils.writeExternalMap(out, classes);
		SerialUtils.writeExternalMap(out, fields);
		SerialUtils.writeExternalMap(out, methods, ObjectOutput::writeObject, SerialUtils::writeExternalCollection);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		packageName = (String) in.readObject();

		wildcardTypeImportPaths = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		wildcardStaticImportPaths = SerialUtils.readExternalSortedImmutableNavigableSet(in);

		classes = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		fields = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		methods = SerialUtils.readExternalSortedImmutableNavigableMap(in, SerialUtils::readExternalObject,
				SerialUtils::readExternalImmutableList);
	}
}
