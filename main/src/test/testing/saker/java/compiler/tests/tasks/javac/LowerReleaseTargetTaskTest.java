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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.util.java.JavaTools;
import testing.saker.SakerTest;
import testing.saker.build.tests.EnvironmentTestCaseConfiguration;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.JavaCompilerTestUtils;

/**
 * <h1>BINARY INCOMPATIBILITY MAY OCCURR IF THE SOURCE VERSION AND THE RELEASE VERSION DON'T BOTH SUPPORT OR DON'T
 * SUPPORT MODULES.</h1>
 * <p>
 * That is, the following should be true for the source and --release versions:
 * 
 * <pre>
 * supportsModules(sourceVersion) == supportsModules(releaseVersion)
 * </pre>
 * <p>
 * Otherwise binary incompatibility may occur. E.g. if the release is 8, and source version is 9, then the
 * {@link ByteBuffer#flip()} method will resolve to the version in Java 9 rather than in Java 8.
 */
@SakerTest
public class LowerReleaseTargetTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected Set<EnvironmentTestCaseConfiguration> getTestConfigurations() {
		return EnvironmentTestCaseConfiguration.builder(super.getTestConfigurations()).setUseProject(true).build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		String buildfilestr = files.getAllBytes(PATH_WORKING_DIRECTORY.resolve("saker.build")).toString();

		final int runtimemajor = JavaTools.getCurrentJavaMajorVersion();
		for (int release = runtimemajor; release >= 8; --release) {
			for (int source = Math.min(release + 1, runtimemajor); source >= release - 1; --source) {
				for (int target = Math.min(Math.max(source + 1, release + 1), runtimemajor); target >= Math
						.min(source - 1, release - 1); --target) {
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

					//seems like the actual release is determined by taking the greater of source and release
					if (Math.max(release, source) >= 9) {
						assertTrue(cfstr.contains("()Ljava/nio/ByteBuffer;"), cfstr);
						assertFalse(cfstr.contains("()Ljava/nio/Buffer;"), cfstr);
					} else {
						assertTrue(cfstr.contains("()Ljava/nio/Buffer;"), cfstr);
						assertFalse(cfstr.contains("()Ljava/nio/ByteBuffer;"), cfstr);
					}

					JavaCompilerTestUtils.assertClassBytesVersion(maincfallbytes, expectedversion);
				}
			}
		}
	}

}
