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