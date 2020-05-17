package testing.saker.java.compiler.tests;

import java.io.DataInputStream;
import java.io.IOException;

import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTestCase;

public class JavaCompilerTestUtils {
	private JavaCompilerTestUtils() {
		throw new UnsupportedOperationException();
	}

	public static void assertClassBytesMajorVersion(ByteArrayRegion bytes, int major) {
		try (DataInputStream dis = new DataInputStream(new UnsyncByteArrayInputStream(bytes.getArray()))) {
			SakerTestCase.assertEquals(dis.readInt(), 0xcafebabe);
			short cfminor = dis.readShort();
			short cfmajor = dis.readShort();
			SakerTestCase.assertEquals(cfmajor, (short) major, "Major");
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public static void assertClassBytesMinorVersion(ByteArrayRegion bytes, int minor) {
		try (DataInputStream dis = new DataInputStream(new UnsyncByteArrayInputStream(bytes.getArray()))) {
			SakerTestCase.assertEquals(dis.readInt(), 0xcafebabe);
			short cfminor = dis.readShort();
			short cfmajor = dis.readShort();
			SakerTestCase.assertEquals(cfminor, (short) minor, "Minor");
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public static void assertClassBytesVersion(ByteArrayRegion bytes, int minor, int major) {
		try (DataInputStream dis = new DataInputStream(new UnsyncByteArrayInputStream(bytes.getArray()))) {
			SakerTestCase.assertEquals(dis.readInt(), 0xcafebabe);
			short cfminor = dis.readShort();
			short cfmajor = dis.readShort();
			SakerTestCase.assertEquals(cfminor, (short) minor, "Minor");
			SakerTestCase.assertEquals(cfmajor, (short) major, "Major");
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

}
