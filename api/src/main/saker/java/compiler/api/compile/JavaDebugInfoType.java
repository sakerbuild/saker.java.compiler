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
package saker.java.compiler.api.compile;

import saker.build.scripting.model.info.TypeInformationKind;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;

/**
 * Enumeration for specifying the kind of debugging information that should be included in generated class files.
 * <p>
 * The debug info types are interpreted in a case-insensitive manner.
 * <p>
 * Corresponds to the -g option for javac.
 */
@NestInformation("Enumeration for specifying the kind of debugging information that should be included in generated class files.\n"
		+ "The debug info types are interpreted in a case-insensitive manner.\n"
		+ "Corresponds to the -g option for javac.")
@NestTypeInformation(kind = TypeInformationKind.ENUM,
		enumValues = {

				@NestFieldInformation(value = JavaDebugInfoType.lines,
						info = @NestInformation("Includes line number debugging information in the generated class files.")),
				@NestFieldInformation(value = JavaDebugInfoType.vars,
						info = @NestInformation("Includes local variable debugging information in the generated class files.")),
				@NestFieldInformation(value = JavaDebugInfoType.source,
						info = @NestInformation("Includes source file debugging information in the generated class files.")),
				@NestFieldInformation(value = JavaDebugInfoType.all,
						info = @NestInformation("Includes all debugging information in the generated class files.")),
				@NestFieldInformation(value = JavaDebugInfoType.none,
						info = @NestInformation("Includes no debugging information in the generated class files.")),

		})
public final class JavaDebugInfoType {
	/**
	 * Includes line number debugging information in the generated class files.
	 */
	public static final String lines = "lines";
	/**
	 * Includes local variable debugging information in the generated class files.
	 */
	public static final String vars = "vars";
	/**
	 * Includes source file debugging information in the generated class files.
	 */
	public static final String source = "source";
	/**
	 * Includes all debugging information in the generated class files.
	 */
	public static final String all = "all";
	/**
	 * Includes no debugging information in the generated class files.
	 */
	public static final String none = "none";

	private JavaDebugInfoType() {
		throw new UnsupportedOperationException();
	}
}
