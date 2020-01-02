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
package saker.java.compiler.impl.compile.handler.invoker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;

public class SakerPathBytes implements Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;
	private ByteArrayRegion bytes;

	public SakerPathBytes() {
	}

	public SakerPath getPath() {
		return path;
	}

	public ByteArrayRegion getBytes() {
		return bytes;
	}

	public SakerPathBytes(SakerPath path, ByteArrayRegion bytes) {
		this.path = path;
		this.bytes = bytes;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(bytes);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		bytes = (ByteArrayRegion) in.readObject();
	}

	@Override
	public String toString() {
		return SakerPathFiles.toRelativeString(path);
	}
}