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
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

/**
 * Reproduces an encountered error, where the additional compilation classpath wasn't added properly.
 */
@SakerTest
public class ReleaseWithClasspathCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN1_JAVA_PATH = PATH_WORKING_DIRECTORY.resolve("src1").resolve("test1/Main1.java");
	private static final SakerPath MAIN2_JAVA_PATH = PATH_WORKING_DIRECTORY.resolve("src2").resolve("test2/Main2.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("mpath.jar"), getInitialJarBytes());
		runScriptTask("build");
		assertCompiled(MAIN1_JAVA_PATH, MAIN2_JAVA_PATH);
	}

	private ByteArrayRegion getInitialJarBytes() throws IOException {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {

				jaros.putNextEntry(new ZipEntry("firstpkg/FirstClass.class"));
				LocalFileProvider.getInstance().writeTo(
						getWorkingDirectory().resolve("jarfiles/firstpkg/FirstClass.class"), ByteSink.valueOf(jaros));
			}
			jarbytes = baos.toByteArrayRegion();
		}
		return jarbytes;
	}
}
