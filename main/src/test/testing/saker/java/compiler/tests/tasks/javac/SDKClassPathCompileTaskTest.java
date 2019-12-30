package testing.saker.java.compiler.tests.tasks.javac;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class SDKClassPathCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private Path sdkClassPathPath;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.sdk.classpath", sdkClassPathPath.toString());
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		sdkClassPathPath = getBuildDirectory().resolve("sdkclasspath.jar");
		LocalFileProvider.getInstance().createDirectories(getBuildDirectory());
		FileUtils.writeStreamEqualityCheckTo(new UnsyncByteArrayInputStream(getInitialJarBytes()), sdkClassPathPath);

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("second"));
	}

	private ByteArrayRegion getInitialJarBytes() throws IOException {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {

				jaros.putNextEntry(
						new ZipEntry("firstpkg/FirstClass.class").setLastModifiedTime(FileTime.fromMillis(0)));
				LocalFileProvider.getInstance().writeTo(
						getWorkingDirectory().resolve("jarfiles/firstpkg/FirstClass.class"), ByteSink.valueOf(jaros));
			}
			jarbytes = baos.toByteArrayRegion();
		}
		return jarbytes;
	}
}
