package saker.java.compiler.impl.launching;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import saker.build.thirdparty.saker.rmi.connection.RMIConnection;
import saker.build.thirdparty.saker.rmi.connection.RMIOptions;
import saker.build.thirdparty.saker.rmi.connection.RMIServer;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.thirdparty.saker.util.io.ReadWriteBufferOutputStream;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;

public class SakerRMIDaemon {
	public static final String CONTEXT_VARIABLE_BASE_CLASSLOADER = "daemon.base.classloader";

	private int port = 0;
	private ClassLoader baseClassLoader;
	private RMITransferProperties transferProperties;

	public SakerRMIDaemon() {
	}

	public void run() throws IOException {
		//we need to change the streams else the writing and reading might block forever 
		//    if the output is not read on the other side
		PrintStream prevout = System.out;
		PrintStream preverr = System.err;

		try (ReadWriteBufferOutputStream stdout = new ReadWriteBufferOutputStream();
				ReadWriteBufferOutputStream stderr = new ReadWriteBufferOutputStream()) {
			try (PrintStream stdoutps = new PrintStream(stdout);
					PrintStream stderrps = new PrintStream(stderr)) {
				System.setOut(stdoutps);
				System.setErr(stderrps);
				System.setIn(StreamUtils.nullInputStream());

				runServer(prevout, prevout, preverr, stdout, stderr);
			}
		}
	}

	private void runServer(PrintStream portprintout, PrintStream prevout, PrintStream preverr,
			ReadWriteBufferOutputStream stdoutin, ReadWriteBufferOutputStream stderrin) throws IOException {
		try (RMIServer server = new RMIServer(null, port, null) {
			@Override
			protected RMIOptions getRMIOptionsForAcceptedConnection(Socket acceptedsocket, int protocolversion) {
				return new RMIOptions().transferProperties(transferProperties).classLoader(baseClassLoader);
			}

			@Override
			protected void setupConnection(Socket acceptedsocket, RMIConnection connection)
					throws IOException, RuntimeException {
				super.setupConnection(acceptedsocket, connection);
				connection.putContextVariable(CONTEXT_VARIABLE_BASE_CLASSLOADER, baseClassLoader);
			}
		}) {
			portprintout.println(server.getPort());
			portprintout.flush();
			portprintout = null;

			ThreadUtils.startDaemonThread("Stdout printer", () -> {
				try {
					StreamUtils.copyStream(ByteSource.toInputStream(stdoutin), prevout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			ThreadUtils.startDaemonThread("Stderr printer", () -> {
				try {
					StreamUtils.copyStream(ByteSource.toInputStream(stderrin), preverr);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			server.acceptConnections();
		}
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBaseClassLoader(ClassLoader baseClassLoader) {
		this.baseClassLoader = baseClassLoader;
	}

	public void setTransferProperties(RMITransferProperties transferProperties) {
		this.transferProperties = transferProperties;
	}
}
