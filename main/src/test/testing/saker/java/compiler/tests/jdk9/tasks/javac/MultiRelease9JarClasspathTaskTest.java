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
package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

//@SakerTest
/**
 * This test is disabled for now, as the javac implementation doesn't work.
 * <p>
 * In order to have multi-release JARs on the classpath, the <i>hidden</i> <code>--multi-release</code> option needs to
 * be added with the version number as its argument to javac.
 * <p>
 * Then, it will use <code>JarFileSystem</code> to access the JAR files. However, it fails to properly list the
 * versioned entries, therefore the version 9 of the given class will not be enumerated.
 * <p>
 * This results in that the versioned classes are not accessible to the compiler, and "cannot find symbol" errors will
 * be thrown.
 * <p>
 * Working around this bug requires our manual opening of JAR files in the compilers. This is also beneficial as we can
 * manage our own caching.
 * 
 * @see com.sun.tools.javac.main.Option#MULTIRELEASE
 */
public class MultiRelease9JarClasspathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//TODO implement self opening JAR files for the compiler, and verify this test

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("mpath.jar"), getInitialJarBytes());
		runScriptTask("build");
	}

	private ByteArrayRegion getInitialJarBytes() throws IOException {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().putValue("Multi-Release", "true");

			try (JarOutputStream jaros = new JarOutputStream(baos, manifest)) {
				jaros.putNextEntry(new ZipEntry("firstpkg/FirstClass8.class"));
				LocalFileProvider.getInstance().writeTo(
						getWorkingDirectory().resolve("jarfiles/firstpkg/FirstClass8.class"), ByteSink.valueOf(jaros));

				jaros.putNextEntry(new ZipEntry("META-INF/versions/9/firstpkg/FirstClass9.class"));
				LocalFileProvider.getInstance().writeTo(
						getWorkingDirectory().resolve("jarfiles/firstpkg/FirstClass9.class"), ByteSink.valueOf(jaros));
			}
			jarbytes = baos.toByteArrayRegion();
		}
		return jarbytes;
	}
}
