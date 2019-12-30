package saker.java.compiler.impl.compile.handler.info;

import java.util.SortedMap;

import saker.build.file.path.SakerPath;
import saker.java.compiler.impl.signature.element.ClassSignatureHeader;

public interface ClassGenerationInfo {
	public String getPackageName();

	public SortedMap<String, ? extends ClassSignatureHeader> getClassesByBinaryNames();

	public SortedMap<SakerPath, String> getGeneratedClassBinaryNames();
}
