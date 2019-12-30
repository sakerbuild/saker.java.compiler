package saker.java.compiler.impl.compile.handler.info;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import saker.build.file.path.SakerPath;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public interface ClassHoldingData extends ClassGenerationInfo, RealizedSignatureHolder {
	public SakerPath getPath();

	public default Collection<? extends ClassSignature> getClassSignatures() {
		return getClasses().values();
	}

	public default Collection<? extends ClassSignature> getRealizedClassSignatures() {
		return getRealizedClasses().values();
	}

	public default GeneratedFileOrigin getOrigin() {
		return null;
	}

	public NavigableMap<String, ? extends ClassSignature> getClasses();

	public PackageSignature getPackageSignature();

	public ModuleSignature getModuleSignature();

	public TopLevelAbiUsage getABIUsage();

	public SortedMap<SakerPath, ClassFileData> getGeneratedClassDatas();

	public default SignatureSourcePositions getSourcePositions() {
		return null;
	}

	public ImportScope getImportScope();

	@Override
	public default String getPackageName() {
		return getImportScope().getPackageName();
	}

	@Override
	public default SortedMap<SakerPath, String> getGeneratedClassBinaryNames() {
		SortedMap<SakerPath, String> result = new TreeMap<>();
		for (Entry<SakerPath, ClassFileData> entry : getGeneratedClassDatas().entrySet()) {
			result.put(entry.getKey(), entry.getValue().getClassBinaryName());
		}
		return result;
	}

	public static void collectAllClassBinaryNames(Set<String> result, Iterable<? extends ClassSignature> classes) {
		for (ClassSignature c : classes) {
			result.add(c.getBinaryName());
			collectAllClassBinaryNames(result, c.getEnclosedClasses());
		}
	}

	public static void collectAllClassBinaryNames(Map<String, ClassSignature> result,
			Collection<? extends ClassSignature> classes) {
		for (ClassSignature c : classes) {
			result.put(c.getBinaryName(), c);
			collectAllClassBinaryNames(result, c.getEnclosedClasses());
		}
	}

	public default Set<String> getAllClassBinaryNames() {
		TreeSet<String> result = new TreeSet<>();
		collectAllClassBinaryNames(result, getClassSignatures());
		return result;
	}

	@Override
	public default SortedMap<String, ClassSignature> getClassesByBinaryNames() {
		SortedMap<String, ClassSignature> result = new TreeMap<>();
		collectAllClassBinaryNames(result, getClassSignatures());
		return result;
	}
}
