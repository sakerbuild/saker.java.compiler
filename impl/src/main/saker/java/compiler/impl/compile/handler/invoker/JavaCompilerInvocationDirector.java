package saker.java.compiler.impl.compile.handler.invoker;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.FileHandle;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeySerializeValueWrapper;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingFileData;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;

public interface JavaCompilerInvocationDirector {

	public CompilationInfo invokeCompilation(NavigableMap<SakerPath, ? extends FileHandle> units,
			NavigableMap<SakerPath, ? extends ClassHoldingFileData> removedsourcefiles)
			throws JavaCompilationFailedException, IOException;

	public void setErrorRaised();

	@RMICacheResult
	public IncrementalDirectoryPaths getDirectoryPaths();

	public void addGeneratedClassFilesForSourceFiles(
			@RMIWrap(RMITreeMapSerializeKeySerializeValueWrapper.class) Map<String, SakerPath> classbinarynamesourcefilepaths);

	@RMICacheResult
	public String[] getOptions();

	public boolean compilationRound();

	public boolean isAnyErrorRaised();

	public void reportDiagnostic(DiagnosticEntry entry);
}
