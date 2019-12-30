package saker.java.compiler.impl.compile.handler.info;

public interface ClassHoldingFileData extends ClassHoldingData, FileData {
	@Override
	public default GeneratedFileOrigin getOrigin() {
		return null;
	}
}
