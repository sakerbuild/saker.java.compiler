package saker.java.compiler.impl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketAddress;

import saker.build.thirdparty.saker.rmi.connection.RMIConnection;
import saker.build.thirdparty.saker.rmi.connection.RMIOptions;
import saker.build.thirdparty.saker.rmi.connection.RMIServer;
import saker.build.thirdparty.saker.rmi.connection.RMISocketConfiguration;
import saker.build.thirdparty.saker.util.io.StreamUtils;

public class RMICompatUtil {
	/**
	 * The default {@link RMIServer} connection timeout that should be used.
	 * <p>
	 * This value should be somewhat greater than a few seconds, as connecting and handshake could take longer under
	 * heavy CPU load during builds.
	 */
	public static final int DEFAULT_RMI_SERVER_CONNECTION_TIMEOUT = 30000;

	/**
	 * Negative: don't set as saker.rmi version doesn't support it.
	 * <p>
	 * Zero: infinite. <br>
	 * Positive: The default value as in {@link #DEFAULT_RMI_SERVER_CONNECTION_TIMEOUT} or the property value.
	 */
	public static final int RMI_SERVER_CONNECTION_TIMEOUT_PROPERTY_VALUE;
	static {
		int result;
		if (saker.build.meta.Versions.THIRDPARTY_SAKER_RMI_VERSION_FULL_COMPOUND >= 8_002) {
			result = DEFAULT_RMI_SERVER_CONNECTION_TIMEOUT;
			final String propertyname = "saker.java.compiler.rmi.server.connection.timeout";
			String val = System.getProperty(propertyname);
			if (val != null) {
				try {
					result = Integer.parseInt(val);
				} catch (NumberFormatException e) {
					// XXX: include in build trace somehow
					synchronized (System.err) {
						System.err.println(
								"Failed to parse property: " + propertyname + " with value: " + val + " caused by:");
						e.printStackTrace(System.err);
					}
				}
			}
		} else {
			result = -1;
		}
		RMI_SERVER_CONNECTION_TIMEOUT_PROPERTY_VALUE = result;
	}

	private RMICompatUtil() {
		throw new UnsupportedOperationException();
	}

	public static RMIConnection connectWithRMISocketConfiguration(RMIOptions options, SocketAddress address)
			throws IOException {
		if (saker.build.meta.Versions.THIRDPARTY_SAKER_RMI_VERSION_FULL_COMPOUND >= 8_002) {
			RMISocketConfiguration socketconfig = new RMISocketConfiguration();
			if (RMI_SERVER_CONNECTION_TIMEOUT_PROPERTY_VALUE >= 0) {
				socketconfig.setConnectionTimeout(RMI_SERVER_CONNECTION_TIMEOUT_PROPERTY_VALUE);
			}
			socketconfig.setConnectionInterruptible(true);
			return options.connect(address, socketconfig);
		}
		return options.connect(address);
	}

	public static void addDumpCloseListener(OutputStream stderrin, RMIConnection connection) {
		connection.addCloseListener(new RMIConnection.CloseListener() {
			@Override
			public void onConnectionClosed() {
				try (OutputStreamWriter writer = new OutputStreamWriter(
						StreamUtils.closeProtectedOutputStream(stderrin))) {
					connection.getStatistics().dumpSummary(writer, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void addDumpCloseListener(RMIConnection conn) {
		conn.addCloseListener(new RMIConnection.CloseListener() {
			@Override
			public void onConnectionClosed() {
				conn.getStatistics().dumpSummary(System.err, null);
			}
		});
	}

	public static void setRMIServerConnectionTimeout(RMIServer server, int timeout) {
		if (saker.build.meta.Versions.THIRDPARTY_SAKER_RMI_VERSION_FULL_COMPOUND >= 8_002) {
			server.setConnectionTimeout(timeout);
		}
	}

	public static void setRMIServerConnectionTimeoutToPropertyOrDefault(RMIServer server) {
		if (RMI_SERVER_CONNECTION_TIMEOUT_PROPERTY_VALUE >= 0) {
			server.setConnectionTimeout(RMI_SERVER_CONNECTION_TIMEOUT_PROPERTY_VALUE);
		}
	}
}
