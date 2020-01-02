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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.lang.model.element.NestingKind;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.handler.info.ClassGenerationInfo;
import saker.java.compiler.impl.signature.element.ClassSignatureHeader;

public class PreviousCompilationClassInfo implements Externalizable {
	private static final long serialVersionUID = 1L;

	private static class SimpleClassGenerationInfo implements ClassGenerationInfo {
		private String packageName;
		private SortedMap<String, ? extends ClassSignatureHeader> classesByBinaryNames;
		private SortedMap<SakerPath, String> generatedClassBinaryNames;

		public SimpleClassGenerationInfo() {
		}

		@Override
		public String getPackageName() {
			return packageName;
		}

		@Override
		public SortedMap<String, ? extends ClassSignatureHeader> getClassesByBinaryNames() {
			return classesByBinaryNames;
		}

		@Override
		public SortedMap<SakerPath, String> getGeneratedClassBinaryNames() {
			return generatedClassBinaryNames;
		}
	}

	private Collection<? extends ClassGenerationInfo> classDatas;

	public PreviousCompilationClassInfo() {
	}

	public PreviousCompilationClassInfo(Collection<? extends ClassGenerationInfo> classDatas) {
		this.classDatas = classDatas;
	}

	public Iterable<? extends ClassGenerationInfo> getClassDatas() {
		return classDatas;
	}

	private static void writeClassSignatureHeader(ClassSignatureHeader value, ObjectOutput out) throws IOException {
		while (value != null) {
			out.writeUTF(value.getSimpleName());
			out.writeUTF(value.getBinaryName());
			out.writeObject(value.getNestingKind());
			ClassSignatureHeader enc = value.getEnclosingSignature();
			if (enc == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
			}
			value = enc;
		}
	}

	private ClassSignatureHeader readClassSignatureHeader(ObjectInput in) throws IOException, ClassNotFoundException {
		String simplename = in.readUTF();
		String binaryname = in.readUTF();
		NestingKind nestingkind = (NestingKind) in.readObject();
		ClassSignatureHeader enclosingSignature;

		boolean enc = in.readBoolean();
		if (enc) {
			enclosingSignature = readClassSignatureHeader(in);
		} else {
			enclosingSignature = null;
		}

		return new SimpleClassSignatureHeader(enclosingSignature, simplename, binaryname, nestingkind);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(classDatas.size());
		for (ClassGenerationInfo cd : classDatas) {
			out.writeObject(cd.getPackageName());

			SortedMap<String, ? extends ClassSignatureHeader> cbbn = cd.getClassesByBinaryNames();
			out.writeInt(cbbn.size());
			for (Entry<String, ? extends ClassSignatureHeader> entry : cbbn.entrySet()) {
				out.writeUTF(entry.getKey());

				ClassSignatureHeader value = entry.getValue();
				writeClassSignatureHeader(value, out);
			}
			SerialUtils.writeExternalMap(out, cd.getGeneratedClassBinaryNames());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int size = in.readInt();
		ArrayList<ClassGenerationInfo> cdlist = new ArrayList<>(size);
		this.classDatas = cdlist;
		for (int i = 0; i < size; i++) {
			SimpleClassGenerationInfo info = new SimpleClassGenerationInfo();
			info.packageName = (String) in.readObject();

			int chdsize = in.readInt();
			TreeMap<String, ClassSignatureHeader> cbbnmap = new TreeMap<>();
			info.classesByBinaryNames = cbbnmap;
			for (int j = 0; j < chdsize; j++) {
				String key = in.readUTF();
				ClassSignatureHeader value = readClassSignatureHeader(in);
				cbbnmap.put(key, value);
			}
			info.generatedClassBinaryNames = SerialUtils.readExternalSortedImmutableNavigableMap(in);

			cdlist.add(info);
		}
	}

}
