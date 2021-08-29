/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import saker.java.compiler.impl.compile.handler.incremental.RemoteJavaCompilerCacheKey;
import saker.java.compiler.impl.util.RMICompatUtil;

public class RemoteJavaRMIProcess implements Closeable {
	private Process proc;
	private RMIConnection connection;
	private List<String> commands;
	private Thread shutdownHook = null;
	private InetSocketAddress address = null;

	public RemoteJavaRMIProcess(List<String> commands, String workingdirectory, ClassLoader cloader,
			RMITransferProperties rmiproperties, ThreadGroup connectionThreadGroup) throws IOException {
		this.commands = ImmutableUtils.makeImmutableList(commands);
		System.out.println("Start local process: " + StringUtils.toStringJoin("\"", "\" \"", commands, "\""));
		ProcessBuilder pb = new ProcessBuilder(commands);
		if (workingdirectory != null) {
			pb.directory(new File(workingdirectory));
		}
		proc = pb.start();

		int port = 0;
		try {
			InputStream procin = proc.getInputStream();
			while (true) {
				int r = procin.read();
				if (r < 0) {
					throw new IOException(
							"Failed to read port number from remote Java compiler process, received EOF on standard output.");
				}
				if (r == '\n' || r == '\r') {
					//reached end of port number value
					break;
				}
				if (r < '0' || r > '9') {
					throw new IOException("Invalid character read for port number from remote Java compiler process, port: "
							+ port + " char: 0x" + Integer.toHexString(r));
				}
				port = port * 10 + (r - '0');
				if (port > 0xFFFF) {
					throw new IOException("Invalid port number read from remote Java compiler process: " + port);
				}
			}
			if (port == 0) {
				//XXX reify exception?
				throw new IOException("Failed to read port number from remote Java compiler process.");
			}
			address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
			RMIOptions options = new RMIOptions().classLoader(cloader).transferProperties(rmiproperties)
					.workerThreadGroup(connectionThreadGroup);
			if (RemoteJavaCompilerCacheKey.COLLECT_RMI_STATS) {
				options.collectStatistics(true);
			}
			connection = RMICompatUtil.connectWithRMISocketConfiguration(options, address);
			if (RemoteJavaCompilerCacheKey.COLLECT_RMI_STATS) {
				RMICompatUtil.addDumpCloseListener(connection);
			}
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
			StringBuilder sb = new StringBuilder("Failed to connect to remote Java compiler process.");
			if (address != null) {
				sb.append(" (");
				sb.append(address);
				sb.append(")");
			}
			sb.append(" (Exit code: ");
			sb.append(ProcessUtils.getExitCodeIfExited(proc));
			sb.append(")");
			if (port != 0) {
				sb.append(" (Port: ");
				sb.append(port);
				sb.append(")");
			}
			throw new IOException(sb.toString(), e);
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
		} else {
			System.err.println("Shutdown hook: Terminating Java compiler process.");
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
			if (RemoteJavaCompilerCacheKey.COLLECT_RMI_STATS) {
				printProcessStreams();
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
			System.err.println("Destroying compiler RMI process forcibly. (" + address + ")");
			ProcessUtils.destroyProcessAndPossiblyChildren(proc);
			printProcessStreams();
		}
	}

	public boolean isValid() {
		return proc.isAlive() && connection.isConnected();
	}

	private void printProcessStreams() {
		IOException e1 = null;
		IOException e2 = null;
		String cmd = StringUtils.toStringJoin("\"", "\" \"", commands, "\"");
		synchronized (System.err) {
			System.err.println(" ---- StdOut from command: " + cmd);
			try {
				StreamUtils.copyStream(proc.getInputStream(), System.err);
			} catch (IOException e) {
				e1 = e;
			}
			System.err.println();
			System.err.println(" ---- StdErr from command: " + cmd);
			try {
				StreamUtils.copyStream(proc.getErrorStream(), System.err);
			} catch (IOException e) {
				e2 = e;
			}
			System.err.println();
			System.err.println(" -------- ");
			if (e1 != null) {
				e1.printStackTrace(System.err);
			}
			if (e2 != null) {
				e2.printStackTrace(System.err);
			}
		}

	}

	public RMIConnection getConnection() {
		return connection;
	}

}
