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
package testing.saker.java.compiler.tests.jdk11.tasks.javac;

import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.JavaCompilerTestUtils;

@SakerTest
public class PatchEnablePreviewTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private boolean patch;

	private static final SakerPath PATH_MAIN_CLASS = PATH_BUILD_DIRECTORY
			.resolve("saker.java.compile/src/bin/test/Main.class");

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.shouldpatch", patch);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		patch = false;
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		JavaCompilerTestUtils.assertClassBytesMinorVersion(files.getAllBytes(PATH_MAIN_CLASS), 0xFFFF);

		patch = true;
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		JavaCompilerTestUtils.assertClassBytesMinorVersion(files.getAllBytes(PATH_MAIN_CLASS), 0x0000);
	}

}
