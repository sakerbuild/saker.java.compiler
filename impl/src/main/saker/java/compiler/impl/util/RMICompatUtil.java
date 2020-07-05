package saker.java.compiler.impl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import saker.build.thirdparty.saker.rmi.connection.RMIConnection;
import saker.build.thirdparty.saker.util.io.StreamUtils;

public class RMICompatUtil {
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
}
