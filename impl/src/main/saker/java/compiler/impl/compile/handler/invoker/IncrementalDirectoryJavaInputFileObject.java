package saker.java.compiler.impl.compile.handler.invoker;

import java.util.function.Supplier;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class IncrementalDirectoryJavaInputFileObject extends IncrementalDirectoryFileInputFileObject
		implements JavaCompilerJavaFileObject {
	private Supplier<Kind> kind;
	private Supplier<String> inferredBinaryName;

	public IncrementalDirectoryJavaInputFileObject(IncrementalDirectoryFile file, Kind kind,
			String inferredBinaryName) {
		super(file);
		this.kind = Functionals.valSupplier(kind);
		this.inferredBinaryName = Functionals.valSupplier(inferredBinaryName);
	}

	public IncrementalDirectoryJavaInputFileObject(IncrementalDirectoryFile file) {
		super(file);
		this.kind = LazySupplier.of(() -> JavaUtil.getKindFromName(file.getPath().getFileName()));
		this.inferredBinaryName = file::getInferredBinaryName;
	}

	@Override
	public Kind getKind() {
		return kind.get();
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return CompilationHandler.isNameCompatible(simpleName, kind, this.getKind(),
				getFileObjectSakerPath().getFileName());
	}

	@Override
	public NestingKind getNestingKind() {
		return null;
	}

	@Override
	public Modifier getAccessLevel() {
		return null;
	}

	@Override
	public int[] getLineIndexMap() {
		return StringUtils.getLineIndexMap(bytes.get().toString());
	}

	@Override
	public String getInferredBinaryName() {
		return inferredBinaryName.get();
	}

}