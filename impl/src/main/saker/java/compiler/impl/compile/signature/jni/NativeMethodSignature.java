package saker.java.compiler.impl.compile.signature.jni;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.java.compiler.impl.JavaTaskUtils;

public final class NativeMethodSignature implements NativeSignature {
	private static final long serialVersionUID = 1L;

	private String name;
	private List<NativeParameter> params;
	private List<NativeType> typeParameters;
	private NativeType returnType;
	private boolean staticMethod;
	private String classBinaryName;
	private boolean overloaded;
	private String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public NativeMethodSignature() {
	}

	public NativeMethodSignature(String name, List<NativeParameter> params, List<NativeType> typeParameters,
			NativeType returnType, boolean staticMethod, String classBinaryName, boolean overloaded,
			String docComment) {
		this.name = name;
		this.params = params;
		this.typeParameters = typeParameters;
		this.returnType = returnType;
		this.staticMethod = staticMethod;
		this.classBinaryName = classBinaryName;
		this.overloaded = overloaded;
		this.docComment = docComment;
	}

	public NativeMethodSignature(String classbinaryname, ExecutableElement elem, Types types, Elements elements,
			boolean overloaded, String doccomment) {
		this.name = elem.getSimpleName().toString();

		this.params = JavaTaskUtils.cloneImmutableList(elem.getParameters(),
				param -> new NativeParameter(param, types, elements));

		this.returnType = new NativeType(elem.getReturnType(), types, elements, false);
		this.staticMethod = elem.getModifiers().contains(Modifier.STATIC);
		this.classBinaryName = classbinaryname;
		this.overloaded = overloaded;
		this.docComment = doccomment;

		this.typeParameters = JavaTaskUtils.cloneImmutableList(elem.getTypeParameters(),
				tp -> new NativeType(tp.asType(), types, elements, true));
	}

	public final String getName() {
		return name;
	}

	public final List<NativeParameter> getParams() {
		return params;
	}

	public final NativeType getReturnType() {
		return returnType;
	}

	public final boolean isStaticMethod() {
		return staticMethod;
	}

	public final String getClassBinaryName() {
		return classBinaryName;
	}

	public final boolean isOverloaded() {
		return overloaded;
	}

	public final String getDocComment() {
		return docComment;
	}

	@Override
	public String toString() {
		return getNativeString();
	}

	@Override
	public String getNativeString() {
		StringBuilder sb = new StringBuilder();
		sb.append("JNIEXPORT ");
		sb.append(returnType.getNativeType());
		sb.append(" JNICALL Java_");
		NativeSignature.getJNICompatibleName(classBinaryName, sb);
		sb.append("_");
		NativeSignature.getJNICompatibleName(name, sb);
		if (this.overloaded) {
			sb.append("__");
			sb.append(NativeSignature.getJNICompatibleName(String.join("",
					(Iterable<String>) params.stream().map(p -> p.getType().getDescriptorString())::iterator)));
		}
		sb.append("(JNIEnv* env, ");
		sb.append((staticMethod ? "jclass clazz" : "jobject obj"));
		if (!params.isEmpty()) {
			sb.append(", ");
			sb.append(String.join(", ", (Iterable<String>) params.stream()
					.map(p -> p.getType().getNativeType() + " " + p.getName())::iterator));
		}
		sb.append(");");
		return sb.toString();
	}

	private static void deunicodizeName(String signature, StringBuilder sb) {
		for (int cp : (Iterable<Integer>) signature.chars()::iterator) {
			if (cp <= 0xFF) {
				sb.append((char) cp);
			} else {
				sb.append('?');
			}
		}
	}

	public String getMethodNativeSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (NativeParameter p : params) {
			deunicodizeName(p.getType().getDescriptorString(), sb);
		}
		sb.append(")");
		deunicodizeName(returnType.getDescriptorString(), sb);
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
			write(os, (" * Method: " + (staticMethod ? "static " : "") + classBinaryName + "." + name + "\n"));
			if (!typeParameters.isEmpty()) {
				write(os, (" * Type parameters: \n"));
				for (Iterator<NativeType> it = typeParameters.iterator(); it.hasNext();) {
					NativeType param = it.next();
					write(os, (" *            " + param.getTypeString() + (it.hasNext() ? "," : "") + "\n"));
				}
			}
			if (params.isEmpty()) {
				write(os, (" * Arguments: -\n"));
			} else {
				write(os, (" * Arguments: \n"));
				for (Iterator<NativeParameter> it = params.iterator(); it.hasNext();) {
					NativeParameter param = it.next();
					write(os, (" *            " + param.getType().getTypeString() + " " + param.getName()
							+ (it.hasNext() ? "," : "") + "\n"));
				}
			}
			write(os, (" * Return type: " + returnType.getTypeString() + "\n"));
			write(os, (" * Signature: " + getMethodNativeSignature() + "\n"));
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
		result = prime * result + (overloaded ? 1231 : 1237);
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + (staticMethod ? 1231 : 1237);
		result = prime * result + ((typeParameters == null) ? 0 : typeParameters.hashCode());
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
		NativeMethodSignature other = (NativeMethodSignature) obj;
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
		if (overloaded != other.overloaded)
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (staticMethod != other.staticMethod)
			return false;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(classBinaryName);
		out.writeObject(docComment);
		out.writeUTF(name);

		out.writeBoolean(overloaded);
		out.writeBoolean(staticMethod);

		SerialUtils.writeExternalCollection(out, params);
		SerialUtils.writeExternalCollection(out, typeParameters);

		out.writeObject(returnType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.classBinaryName = in.readUTF();
		this.docComment = (String) in.readObject();
		this.name = in.readUTF();

		this.overloaded = in.readBoolean();
		this.staticMethod = in.readBoolean();

		params = SerialUtils.readExternalImmutableList(in);
		typeParameters = SerialUtils.readExternalImmutableList(in);

		returnType = (NativeType) in.readObject();
	}

}
