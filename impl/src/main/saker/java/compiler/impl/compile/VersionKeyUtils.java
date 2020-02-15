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
package saker.java.compiler.impl.compile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.java.compiler.jdk.impl.compile.VersionKeyLangUtils;

public class VersionKeyUtils {
	public interface ClassFileHasher {
		public boolean update(ByteArrayRegion classbytes, MessageDigest digest);
	}

	public static boolean updateAbiHashOfClassBytes(ByteArrayRegion classbytes, MessageDigest hasher) {
		return VersionKeyLangUtils.createAbiHashingClassVisitor().update(classbytes, hasher);
	}

	private VersionKeyUtils() {
		throw new UnsupportedOperationException();
	}

	public static MessageDigest getMD5() throws AssertionError {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

	public static class NotAbiClassException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public static final NotAbiClassException INSTANCE = new NotAbiClassException();

		public NotAbiClassException() {
			super(null, null, false, false);
		}
	}

	public static String[] clonedSorted(String[] array) {
		if (array.length <= 1) {
			return array;
		}
		if (array.length == 2) {
			if (array[0].compareTo(array[1]) <= 0) {
				// already sorted
				return array;
			}
			// swapped the items
			return new String[] { array[1], array[0] };
		}
		String[] c = array.clone();
		Arrays.sort(c);
		return c;
	}

}
