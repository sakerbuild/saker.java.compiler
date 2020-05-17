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
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.build.util.java.JavaTools;
import testing.saker.SakerTest;
import testing.saker.build.tests.EnvironmentTestCaseConfiguration;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.JavaCompilerTestUtils;

/**
 * <h1>TARGET SHOULDNT BE GREATER THAN RELEASE.</h1>
 * <p>
 * Otherwise errors may be thrown such as <code>Unable to find method makeConcatWithConstants</code> when target is 9
 * and release is 8.
 */
@SakerTest
public class LowerReleaseTargetTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//the test also tests that the ClassPath parameter still works correctly with all the file manager machination

	@Override
	protected Set<EnvironmentTestCaseConfiguration> getTestConfigurations() {
		return EnvironmentTestCaseConfiguration.builder(super.getTestConfigurations()).setUseProject(true).build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("mpath.jar"), getInitialJarBytes());

		String buildfilestr = files.getAllBytes(PATH_WORKING_DIRECTORY.resolve("saker.build")).toString();

		final int runtimemajor = JavaTools.getCurrentJavaMajorVersion();
		final int minmajor = 7;
		for (int release = runtimemajor; release >= 8; --release) {
			for (int source = Math.min(release + 1, runtimemajor); source >= minmajor; --source) {
				for (int target = Math.min(source + 1, Math.min(runtimemajor, release)); target >= minmajor; --target) {
					int expectedversion = target + 44;
					Integer actualsrc = source;
					StringBuilder dirsbuilder = new StringBuilder();
					for (int i = source; i >= 6; i--) {
						String dirname = "src-" + i + ", ";
						dirsbuilder.append(dirname);
						files.createDirectories(PATH_WORKING_DIRECTORY.resolve(dirname));
					}
					String bfsrc = buildfilestr.replace("RELEASE", String.valueOf(release))
							.replace("SOURCE", String.valueOf(actualsrc)).replace("TARGET", String.valueOf(target))
							.replace("DIRS", dirsbuilder);
					SakerPath bfpath = PATH_WORKING_DIRECTORY
							.resolve("bf_" + release + "_" + actualsrc + "_" + target + ".build");
					files.putFile(bfpath, bfsrc);

					String compilationid = "c_r" + release + "_s" + actualsrc + "_t" + target;
					System.out.println("Run:" + " Release: " + release + " Source: " + actualsrc + " Target: " + target
							+ " for " + compilationid);

					runScriptTask("build", bfpath);
					SakerPath maincfpath = PATH_BUILD_DIRECTORY
							.resolve("saker.java.compile/" + compilationid + "/bin/test/FlipTest.class");
					ByteArrayRegion maincfallbytes = files.getAllBytes(maincfpath);
					String cfstr = new String(maincfallbytes.copy(), StandardCharsets.US_ASCII);

					JavaCompilerTestUtils.assertClassBytesMajorVersion(maincfallbytes, expectedversion);

					if (release >= 9) {
						assertTrue(cfstr.contains("()Ljava/nio/ByteBuffer;"), cfstr);
						assertFalse(cfstr.contains("()Ljava/nio/Buffer;"), cfstr);
					} else {
						assertTrue(cfstr.contains("()Ljava/nio/Buffer;"), cfstr);
						assertFalse(cfstr.contains("()Ljava/nio/ByteBuffer;"), cfstr);
					}

				}
			}
		}
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
