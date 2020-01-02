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
