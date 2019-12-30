package saker.java.compiler.impl.compile.handler.info;

public interface ProcessorGeneratedClassHoldingFileData extends ProcessorGeneratedFileData, ClassHoldingFileData {
	@Override
	public GeneratedFileOrigin getOrigin();
}
