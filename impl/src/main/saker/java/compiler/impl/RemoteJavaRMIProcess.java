package saker.java.compiler.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import saker.build.thirdparty.saker.rmi.connection.RMIConnection;
import saker.build.thirdparty.saker.rmi.connection.RMIOptions;
import saker.build.thirdparty.saker.rmi.connection.RMIServer;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.IOUtils;
import saker.build.thirdparty.saker.util.io.ProcessUtils;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;

public class RemoteJavaRMIProcess implements Closeable {
	private Process proc;
	private RMIConnection connection;
	private List<String> commands;
	private Thread shutdownHook = null;
	private InetSocketAddress address = null;

	public RemoteJavaRMIProcess(List<String> commands, String workingdirectory, ClassLoader cloader,
			RMITransferProperties rmiproperties, ThreadGroup connectionThreadGroup) throws IOException {
		this.commands = ImmutableUtils.makeImmutableList(commands);
		System.out.println(
				"Start local process: " + StringUtils.toStringJoin("\"", "\" \"", commands, "\""));
		ProcessBuilder pb = new ProcessBuilder(commands);
		if (workingdirectory != null) {
			pb.directory(new File(workingdirectory));
		}
		proc = pb.start();

		final int port;

		try {
			InputStream procin = proc.getInputStream();
			try (UnsyncByteArrayOutputStream portnumbuf = new UnsyncByteArrayOutputStream()) {
				while (true) {
					int r = procin.read();
					if (r < 0 || r == '\n' || r == '\r') {
						break;
					}
					portnumbuf.write(r);
				}
				if (portnumbuf.isEmpty()) {
					//XXX reify exception?
					throw new IOException("Failed to read port number.");
				}
				port = Integer.parseInt(portnumbuf.toString());
			}
			address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
			connection = new RMIOptions().classLoader(cloader).transferProperties(rmiproperties)
					.workerThreadGroup(connectionThreadGroup).connect(address);
			shutdownHook = new Thread(connectionThreadGroup, "RMI process shutdown hook") {
				@Override
				public void run() {
					close();
				}
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch (Exception e) {
			waitDestroyForcibly();
			printProcessStreams();
			throw new IOException(
					"Failed to connect to remote Java process." + (address == null ? "" : " (" + address + ")")
							+ " (Exit code: " + ProcessUtils.getExitCodeIfExited(proc) + ")",
					e);
		}
	}

	public RemoteJavaRMIProcess(List<String> commands, ClassLoader cloader, RMITransferProperties rmiproperties,
			ThreadGroup connectionThreadGroup) throws IOException {
		this(commands, null, cloader, rmiproperties, connectionThreadGroup);
	}

	@Override
	public void close() {
		if (Thread.currentThread() != shutdownHook) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
		if (!proc.isAlive()) {
			//we're good
			return;
		}
		try {
			try {
				//swallow exception
				IOUtils.closePrint(connection);

				RMIServer.shutdownServer(address);
			} finally {
				waitDestroyForcibly();
			}
		} catch (IOException e) {
			e.printStackTrace();
			printProcessStreams();
		}
	}

	private void waitDestroyForcibly() {
		boolean finished = false;
		try {
			finished = proc.waitFor(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			finished = false;
		}
		if (!finished) {
			System.out.println("Destroying RMI process forcibly. " + address);
			ProcessUtils.destroyProcessAndPossiblyChildren(proc);
			printProcessStreams();
		}
	}

	public boolean isValid() {
		return proc.isAlive() && connection.isConnected();
	}

	private void printProcessStreams() {
		try {
			String cmd = StringUtils.toStringJoin("\"", "\" \"", commands, "\"");
			System.out.println(" ---- StdOut from command: " + cmd);
			StreamUtils.copyStream(proc.getInputStream(), System.out);
			System.out.println();
			System.out.println(" ---- StdErr from command: " + cmd);
			StreamUtils.copyStream(proc.getErrorStream(), System.out);
			System.out.println();
			System.out.println(" -------- ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RMIConnection getConnection() {
		return connection;
	}

}
