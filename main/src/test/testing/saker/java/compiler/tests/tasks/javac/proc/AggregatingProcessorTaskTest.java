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
package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class AggregatingProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private boolean include = true;

	@Override
	protected Map<String, ?> getTaskVariables() {
		return TestUtils.<String, Object>treeMapBuilder().put("test.Include", include).build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");
		include = true;

		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);

		SakerPath srcpath = SakerPath.valueOf("wd:/src/test/Main.java");
		files.setFileBytes(srcpath,
				files.getAllBytes(srcpath).toString().replace("/*ANNOT_PLACEHOLDER*/", "@GroupBy(1)").getBytes());
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class", "groups",
				"groups/Group1.class");
		assertDirectoryRecursiveContents(genbuilddir, "groups", "groups/Group1.java");

		include = false;
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);

		include = true;
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class", "groups",
				"groups/Group1.class");
		assertDirectoryRecursiveContents(genbuilddir, "groups", "groups/Group1.java");

		files.putFile(SakerPath.valueOf("wd:/src/test/Second.java"),
				"package test; @GroupBy(1) public class Second { }");
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class",
				"test/Second.class", "groups", "groups/Group1.class");
		assertDirectoryRecursiveContents(genbuilddir, "groups", "groups/Group1.java");
		assertCompiled(SRC_PATH_BASE.resolve("test/Second.java"),
				genbuilddir.resolve("groups/Group1.java"));

		files.putFile(SakerPath.valueOf("wd:/src/test/Third.java"), "package test; @GroupBy(2) public class Third { }");
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class",
				"test/Second.class", "test/Third.class", "groups", "groups/Group1.class", "groups/Group2.class");
		assertDirectoryRecursiveContents(genbuilddir, "groups", "groups/Group1.java", "groups/Group2.java");
		assertCompiled(SRC_PATH_BASE.resolve("test/Third.java"), genbuilddir.resolve("groups/Group2.java"));

		runScriptTask("build");
		assertCompiled();

		files.touch(SRC_PATH_BASE.resolve("test/Second.java"));
		runScriptTask("build");
		assertCompiled(SRC_PATH_BASE.resolve("test/Second.java"));
		assertEmpty(getMetric().getInitializedProcessors());

		include = false;
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/GroupBy.class", "test/Main.class",
				"test/Second.class", "test/Third.class");
		assertDirectoryRecursiveContents(genbuilddir);
	}

}
