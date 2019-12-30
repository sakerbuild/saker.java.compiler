package saker.java.compiler.impl.compile.file;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.EmptyContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.file.content.NullContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.java.compiler.impl.compile.handler.CompilationHandler;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class CompilationResultSakerFileObject extends SakerFileBase implements JavaCompilerFileObject {
	private final class ContentDescriptorSetterByteArrayOutputStream extends UnsyncByteArrayOutputStream {
		@Override
		public void close() {
			try {
				contentDescriptor = HashContentDescriptor.hash(this::writeTo);
			} catch (IOException e) {
				//failed to hash
				//print the stack trace for info as this should not happen
				e.printStackTrace();
				contentDescriptor = NullContentDescriptor.getInstance();
			} finally {
				super.close();
			}
		}

		@Override
		public void reset() {
			contentDescriptor = EmptyContentDescriptor.INSTANCE;
			super.reset();
		}
	}

	protected UnsyncByteArrayOutputStream bytes;
	private ContentDescriptor contentDescriptor = EmptyContentDescriptor.INSTANCE;

	private transient URI uri;

	public CompilationResultSakerFileObject(String name) {
		super(name);
		bytes = createByteArrayOutputStream();
	}

	private UnsyncByteArrayOutputStream createByteArrayOutputStream() {
		contentDescriptor = EmptyContentDescriptor.INSTANCE;
		return new ContentDescriptorSetterByteArrayOutputStream();
	}

	@Override
	public void writeToStreamImpl(OutputStream os) throws IOException {
		bytes.writeTo(os);
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

	@Override
	public URI toUri() {
		if (uri == null) {
			uri = URI.create(CompilationHandler.URI_SCHEME_GENERATED + ":"
					+ this.getSakerPath().toString().replace(" ", "%20"));
		}
		return uri;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		bytes.reset();
		return bytes;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream());
	}

	@Override
	public String getContentImpl() throws IOException {
		return bytes.toString();
	}

	@Override
	public ByteArrayRegion getBytesImpl() {
		return bytes.toByteArrayRegion();
	}

	@Override
	public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return bytes.toString();
	}

	@Override
	public Writer openWriter() throws IOException {
		return new OutputStreamWriter(openOutputStream(), StandardCharsets.UTF_8);
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public boolean delete() {
		return false;
	}

	@Override
	public SakerPath getFileObjectSakerPath() {
		return getSakerPath();
	}

}
