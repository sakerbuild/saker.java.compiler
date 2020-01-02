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
