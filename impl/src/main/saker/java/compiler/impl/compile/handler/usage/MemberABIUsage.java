package saker.java.compiler.impl.compile.handler.usage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class MemberABIUsage extends AbiUsageImpl {
	private static final long serialVersionUID = 1L;

	private TopLevelABIUsageImpl parent;

	public MemberABIUsage() {
	}

	public MemberABIUsage(TopLevelABIUsageImpl parent) {
		this.parent = parent;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(parent);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		parent = (TopLevelABIUsageImpl) in.readObject();
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
