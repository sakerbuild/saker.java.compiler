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
package saker.java.compiler.api.classpath;

import java.util.Objects;

import saker.java.compiler.impl.options.FileLocationClassAndModulePathReferenceOption;
import saker.java.compiler.impl.options.SDKClassAndModulePathReferenceOption;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

public interface ClassPathEntryInputFile {
	public void accept(ClassPathEntryInputFileVisitor visitor) throws NullPointerException;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	public static ClassPathEntryInputFile create(FileLocation filelocation) throws NullPointerException{
		Objects.requireNonNull(filelocation, "file location");
		return new FileLocationClassAndModulePathReferenceOption(filelocation);
	}

	public static ClassPathEntryInputFile create(SDKPathReference sdkpathreference) throws NullPointerException{
		Objects.requireNonNull(sdkpathreference, "sdk path reference");
		return new SDKClassAndModulePathReferenceOption(sdkpathreference);
	}
}
