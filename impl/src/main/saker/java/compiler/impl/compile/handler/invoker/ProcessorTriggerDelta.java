package saker.java.compiler.impl.compile.handler.invoker;

import java.util.concurrent.ConcurrentSkipListSet;

import saker.build.file.path.SakerPath;

public class ProcessorTriggerDelta {
	private ConcurrentSkipListSet<SakerPath> triggeredUnitPaths = new ConcurrentSkipListSet<>();
	private boolean readResourceTriggered;

	public ProcessorTriggerDelta() {
	}

	public ConcurrentSkipListSet<SakerPath> getTriggeredUnitPaths() {
		return triggeredUnitPaths;
	}

	public boolean isReadResourceTriggered() {
		return readResourceTriggered;
	}

	public void setReadResourceTriggered(boolean readResourceTriggered) {
		this.readResourceTriggered = readResourceTriggered;
	}
}
