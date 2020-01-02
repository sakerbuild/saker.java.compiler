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
package saker.java.compiler.main.compile.option;

import javax.lang.model.SourceVersion;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestTypeInformation(relatedTypes = { @NestTypeUsage(SourceVersion.class), @NestTypeUsage(int.class) })
@NestInformation("A specific version of Java release.")
public final class SourceVersionTaskOption {
	private int version;

	public SourceVersionTaskOption(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

	@Override
	public SourceVersionTaskOption clone() {
		return this;
	}

	public static SourceVersionTaskOption valueOf(SourceVersion sv) {
		return new SourceVersionTaskOption(JavaUtil.getSourceVersionNumber(sv));
	}

	public static SourceVersionTaskOption valueOf(int v) {
		if (v < 0) {
			throw new IllegalArgumentException("Version must be greater than 0. (" + v + ")");
		}
		return new SourceVersionTaskOption(v);
	}

	public static SourceVersionTaskOption valueOf(String s) {
		try {
			int sval = Integer.parseInt(s);
			if (sval < 0) {
				throw new IllegalArgumentException("Version must be greater than 0. (" + s + ")");
			}
			return new SourceVersionTaskOption(sval);
		} catch (NumberFormatException e) {
			if (!StringUtils.startsWithIgnoreCase(s, "release_")) {
				IllegalArgumentException exc = new IllegalArgumentException(
						"Version must have RELEASE_<num> or <num> format.(" + s + ")");
				exc.addSuppressed(e);
				throw exc;
			}
			String verstr = s.substring(8);
			try {
				int sval = Integer.parseInt(verstr);
				if (sval < 0) {
					IllegalArgumentException exc = new IllegalArgumentException(
							"Version must be greater than 0. (" + s + ")");
					exc.addSuppressed(e);
					throw exc;
				}
				return new SourceVersionTaskOption(sval);
			} catch (NumberFormatException e2) {
				IllegalArgumentException exc = new IllegalArgumentException(
						"Failed to parse source version name: " + s + ". Expected format: RELEASE_<num>, or <num>.",
						e2);
				exc.addSuppressed(e);
				throw exc;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceVersionTaskOption other = (SourceVersionTaskOption) obj;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[version=" + version + "]";
	}

}
