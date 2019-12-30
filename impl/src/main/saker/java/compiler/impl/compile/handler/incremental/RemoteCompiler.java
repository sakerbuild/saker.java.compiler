package saker.java.compiler.impl.compile.handler.incremental;

import saker.java.compiler.impl.RemoteJavaRMIProcess;

public class RemoteCompiler {
	private RemoteJavaRMIProcess rmiProcess;

	public RemoteCompiler(RemoteJavaRMIProcess rmiProcess) {
		this.rmiProcess = rmiProcess;
	}

	public RemoteJavaRMIProcess getRmiProcess() {
		return rmiProcess;
	}
}