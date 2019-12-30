package saker.java.compiler.impl.compile.handler.incremental;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URI;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;

public abstract class ByteArrayBufferingJavaFileObject implements JavaFileObject {
	private static final AtomicIntegerFieldUpdater<ByteArrayBufferingJavaFileObject> AIFU_state = AtomicIntegerFieldUpdater
			.newUpdater(ByteArrayBufferingJavaFileObject.class, "state");

	/**
	 * -1: unopened <br>
	 * 0: opened <br>
	 * 1: closed <br>
	 */
	@SuppressWarnings("unused")
	private volatile int state = -1;

	@Override
	public URI toUri() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public InputStream openInputStream() throws IOException, IllegalStateException {
		throw openedForWriting();
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException, IllegalStateException {
		throw openedForWriting();
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException, IllegalStateException {
		throw openedForWriting();
	}

	private static IllegalStateException openedForWriting() {
		return new IllegalStateException("File was opened for writing.");
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		if (!AIFU_state.compareAndSet(ByteArrayBufferingJavaFileObject.this, -1, 0)) {
			//the output stream was already closed. closing against is no-op
			throw new IllegalStateException("Output stream was already opened.");
		}
		return new UnsyncByteArrayOutputStream() {
			@Override
			public void close() {
				if (!AIFU_state.compareAndSet(ByteArrayBufferingJavaFileObject.this, 0, 1)) {
					//the output stream was already closed. closing again is no-op
					return;
				}
				try {
					closeOutputStream(this);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} finally {
					super.close();
				}
			}
		};
	}

	@Override
	public Writer openWriter() throws IOException {
		return new OutputStreamWriter(openOutputStream());
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
	public Kind getKind() {
		return null;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NestingKind getNestingKind() {
		return null;
	}

	@Override
	public Modifier getAccessLevel() {
		return null;
	}

	protected abstract void closeOutputStream(UnsyncByteArrayOutputStream baos) throws IOException;

}
