package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;

public class SimpleImportScope implements ImportScope, Externalizable {
	private static final long serialVersionUID = 1L;

	private String packageName;
	private NavigableSet<? extends ImportDeclaration> importDeclarations;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleImportScope() {
	}

	public static ImportScope create(String packageName) {
		return new EmptyImportScope(packageName);
	}

	public static ImportScope create(String packageName, NavigableSet<? extends ImportDeclaration> importDeclarations) {
		if (ObjectUtils.isNullOrEmpty(importDeclarations)) {
			return create(packageName);
		}
		return new SimpleImportScope(packageName, importDeclarations);
	}

	public static ImportScope create(ParserCache cache, String packageName) {
		return cache.emptyImportScope(packageName);
	}

	public static ImportScope create(ParserCache cache, String packageName,
			NavigableSet<? extends ImportDeclaration> importDeclarations) {
		if (ObjectUtils.isNullOrEmpty(importDeclarations)) {
			return create(cache, packageName);
		}
		return new SimpleImportScope(packageName, importDeclarations);
	}

	private SimpleImportScope(String packageName, NavigableSet<? extends ImportDeclaration> importDeclarations) {
		this.packageName = packageName;
		this.importDeclarations = importDeclarations;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public NavigableSet<? extends ImportDeclaration> getImportDeclarations() {
		return importDeclarations;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(packageName);
		SerialUtils.writeExternalCollection(out, importDeclarations);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		packageName = (String) in.readObject();
		importDeclarations = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

//	private boolean scopeEquals(ClassMemberSignature l, ClassMemberSignature r) {
//		if (l == r) {
//			return true;
//		}
//		if (l == null || r == null || l.getClass() != r.getClass()) {
//			return false;
//		}
//		if (l instanceof ClassSignature) {
//			ClassSignature lc = (ClassSignature) l;
//			ClassSignature rc = (ClassSignature) r;
//			if (!ObjectUtils.collectionEquals(lc.getTypeParameters(), rc.getTypeParameters(),
//					(ltv, rtv) -> Objects.equals(ltv.getVarName(), rtv.getVarName()))) {
//				return false;
//			}
//			if (!ObjectUtils.collectionEquals(lc.getMembers(), rc.getMembers(), (ltv, rtv) -> Objects.equals(ltv.getSimpleName(), rtv.getSimpleName()))) {
//				return false;
//			}
//		} else if (l instanceof MethodSignature) {
//			MethodSignature lm = (MethodSignature) l;
//			MethodSignature rm = (MethodSignature) r;
//			if (!ObjectUtils.collectionEquals(lm.getTypeParameters(), rm.getTypeParameters(),
//					(ltv, rtv) -> Objects.equals(ltv.getVarName(), rtv.getVarName()))) {
//				return false;
//			}
//		}
//		return true;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((importDeclarations == null) ? 0 : importDeclarations.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
		SimpleImportScope other = (SimpleImportScope) obj;
		if (importDeclarations == null) {
			if (other.importDeclarations != null)
				return false;
		} else if (!importDeclarations.equals(other.importDeclarations))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return packageName + (importDeclarations.isEmpty() ? "" : importDeclarations.toString());
	}

}
