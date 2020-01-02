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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class NativeConstantSignature implements NativeSignature {
	private static final Map<Class<?>, String> PRIMITIVE_CLASSMAP = new HashMap<>();
	static {
		PRIMITIVE_CLASSMAP.put(String.class, "String");
		PRIMITIVE_CLASSMAP.put(Long.class, "long");
		PRIMITIVE_CLASSMAP.put(Integer.class, "int");
		PRIMITIVE_CLASSMAP.put(Short.class, "short");
		PRIMITIVE_CLASSMAP.put(Byte.class, "byte");
		PRIMITIVE_CLASSMAP.put(Character.class, "char");
		PRIMITIVE_CLASSMAP.put(Double.class, "double");
		PRIMITIVE_CLASSMAP.put(Float.class, "float");
		PRIMITIVE_CLASSMAP.put(Boolean.class, "boolean");
	}
	private static final long serialVersionUID = 1L;

	private String name;
	private Object value;
	private String docComment;
	private String classBinaryName;

	/**
	 * For {@link Externalizable}.
	 */
	public NativeConstantSignature() {
	}

	public NativeConstantSignature(String name, Object value, String docComment, String classBinaryName) {
		this.name = name;
		this.value = value;
		this.docComment = docComment;
		this.classBinaryName = classBinaryName;
	}

	public final String getName() {
		return name;
	}

	public final Object getValue() {
		return value;
	}

	public final String getDocComment() {
		return docComment;
	}

	public final String getClassBinaryName() {
		return classBinaryName;
	}

	@Override
	public String toString() {
		return getNativeString();
	}

	private String getConstantValueString() {
		if (value instanceof String) {
			// XXX further examine how special characters should be escaped for C/C++ code
			return IncrementalElementsTypes.getConstantExpression((String) value);
		}
		return value.toString();
	}

	private String getTypeString() {
		return PRIMITIVE_CLASSMAP.get(value.getClass());
	}

	private String getNativeCast() {
		if (value instanceof String) {
			return "";
		}
		return "(j" + getTypeString() + ") ";
	}

	@Override
	public String getNativeString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#define ");
		sb.append("Java_const_");
		NativeSignature.getJNIDefineName(classBinaryName, sb);
		sb.append("_");
		NativeSignature.getJNIDefineName(name, sb);
		sb.append(" (");
		sb.append(getNativeCast());
		sb.append(getConstantValueString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getNativeComment() {
		try (UnsyncByteArrayOutputStream os = new UnsyncByteArrayOutputStream()) {
			write(os, ("/**\n"));
			if (docComment != null) {
				for (String line : docComment.split("\n")) {
					write(os, (" * " + line + "\n"));
				}
				write(os, (" * \n"));
			}
			write(os, (" * Type:     " + getTypeString() + "\n"));
			write(os, (" * Constant: " + classBinaryName + "." + name + "\n"));
			write(os, (" * Value:    " + getConstantValueString() + "\n"));
			write(os, (" */\n"));
			return os.toString();
		}
	}

	private static void write(UnsyncByteArrayOutputStream os, String s) {
		os.write(s.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classBinaryName == null) ? 0 : classBinaryName.hashCode());
		result = prime * result + ((docComment == null) ? 0 : docComment.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		NativeConstantSignature other = (NativeConstantSignature) obj;
		if (classBinaryName == null) {
			if (other.classBinaryName != null)
				return false;
		} else if (!classBinaryName.equals(other.classBinaryName))
			return false;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(classBinaryName);
		out.writeObject(docComment);
		out.writeUTF(name);

		out.writeObject(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.classBinaryName = in.readUTF();
		this.docComment = (String) in.readObject();
		this.name = in.readUTF();

		this.value = in.readObject();
	}

}
