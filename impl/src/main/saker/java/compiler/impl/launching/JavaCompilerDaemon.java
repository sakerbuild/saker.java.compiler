package saker.java.compiler.impl.launching;

import java.io.IOException;

public class JavaCompilerDaemon {
	private static class Arguments {
		private int port = 0;

		public Arguments(String[] args) {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "-port": {
						this.port = Integer.parseInt(args[++i]);
						if (port < 0 || port > 0xFFFF) {
							throw new IllegalArgumentException("Invalid port: " + port);
						}
						break;
					}
					default: {
						throw new IllegalArgumentException("unknown argument: " + args[i]);
					}
				}
			}
		}
	}

	public static void main(String[] progarguments) throws IOException {
		Arguments args = new Arguments(progarguments);
		SakerRMIDaemon daemon = new SakerRMIDaemon();
		daemon.setBaseClassLoader(JavaCompilerDaemon.class.getClassLoader());
		daemon.setPort(args.port);
		daemon.run();
	}

}
