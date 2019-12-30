package saker.java.compiler.impl.compile.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.SerializableContentDescriptor;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.compile.signature.jni.NativeSignature;

public class NativeHeaderSakerFile extends SakerFileBase {
	private Collection<? extends NativeSignature> signatures;
	private String classBinaryName;

	public NativeHeaderSakerFile(String name, Collection<? extends NativeSignature> signatures,
			String classbinaryname) {
		super(name);
		this.signatures = signatures;
		this.classBinaryName = classbinaryname;
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return new SerializableContentDescriptor(ImmutableUtils.asUnmodifiableArrayList(signatures, classBinaryName));
	}

	@Override
	public void writeToStreamImpl(OutputStream os) throws IOException {
		generateHeader(signatures, classBinaryName, os);
	}

	public static void generateHeader(Collection<? extends NativeSignature> signatures, String classBinaryName,
			OutputStream os) throws IOException {
		String guard = "JAVA_NATIVE_" + NativeSignature.getJNICompatibleName(classBinaryName) + "_H_";

		write(os, ("/* Compiler generated header for class " + classBinaryName + " */\n"));
		os.write('\n');

		write(os, ("#ifndef " + guard + "\n"));
		write(os, ("#define " + guard + "\n"));

		write(os, "#include <jni.h>\n");

		write(os, ("#ifdef __cplusplus\n"//
				+ "extern \"C\" {\n"//
				+ "#endif /* __cplusplus */\n"));
		os.write('\n');

		for (NativeSignature sig : signatures) {
			write(os, sig.getNativeComment());
			write(os, sig.getNativeString());
			os.write('\n');
			os.write('\n');
		}

		os.write('\n');
		write(os, ("#ifdef __cplusplus\n"//
				+ "}\n"//
				+ "#endif /* __cplusplus */\n"));

		write(os, ("#endif /* " + guard + " */\n"));
	}

	private static void write(OutputStream os, String s) throws IOException {
		os.write(s.getBytes(StandardCharsets.UTF_8));
	}

}
