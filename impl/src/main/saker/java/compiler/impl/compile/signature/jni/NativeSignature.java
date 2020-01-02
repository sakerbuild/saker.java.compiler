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
package saker.java.compiler.impl.compile.signature.jni;

import java.io.Externalizable;

public interface NativeSignature extends Externalizable {
	public static final long serialVersionUID = 1L;

	public static void getJNIDefineName(String name, StringBuilder sb) {
		for (int cp : (Iterable<Integer>) name.chars()::iterator) {
			if ((cp >= 'a' && cp <= 'z') || (cp >= 'A' && cp <= 'Z') || cp == '_' || (cp >= '0' && cp <= '9')) {
				sb.append((char) cp);
			} else {
				sb.append('_');
			}
		}
	}

	public static void getJNICompatibleName(String name, StringBuilder sb) {
		for (int cp : (Iterable<Integer>) name.chars()::iterator) {
			if (cp < 127) {
				switch (cp) {
					case '/':
					case '.': {
						sb.append('_');
						break;
					}
					case '$': {
						sb.append("_00024");
						break;
					}
					case '_': {
						sb.append("_1");
						break;
					}
					case ';': {
						sb.append("_2");
						break;
					}
					case '[': {
						sb.append("_3");
						break;
					}
					default: {
						sb.append((char) cp);
						break;
					}
				}
			} else {
				sb.append("_0");
				String hexed = Integer.toHexString(cp);
				for (int i = 0; i < 4 - hexed.length(); i++) {
					sb.append('0');
				}
				sb.append(hexed);
			}
		}
	}

	public static String getJNICompatibleName(String name) {
		StringBuilder sb = new StringBuilder();
		getJNICompatibleName(name, sb);
		return sb.toString();
	}

	public String getNativeString();

	public String getNativeComment();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
