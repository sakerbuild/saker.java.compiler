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

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.tasks.javac.util.BundleClasspathTaskTestDepClass;
import testing.saker.java.compiler.tests.tasks.javac.util.BundleClasspathTaskTestSimpleClass;
import testing.saker.nest.util.NestIntegrationTestUtils;

@SakerTest
public class BundleClasspathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		TreeMap<String, Set<Class<?>>> bundleclasses;
		bundleclasses = TestUtils.<String, Set<Class<?>>>treeMapBuilder()//
				.put("simple.bundle-v1", ObjectUtils.newHashSet(BundleClasspathTaskTestSimpleClass.class))//
				.put("dep.bundle-v1", ObjectUtils.newHashSet(BundleClasspathTaskTestDepClass.class))//
				.build();

		String classsubdirpath = getClass().getName().replace('.', '/');
		Path workdir = getTestingBaseWorkingDirectory().resolve(classsubdirpath);
		Path bundleoutdir = getTestingBaseBuildDirectory().resolve(classsubdirpath);
		NestIntegrationTestUtils.createAllJarsFromDirectoriesWithClasses(LocalFileProvider.getInstance(),
				SakerPath.valueOf(workdir).resolve("bundles"), bundleoutdir, bundleclasses);

		NestIntegrationTestUtils.appendToUserParam(parameters, "nest.params.bundles",
				";" + NestIntegrationTestUtils.createParameterBundlesParameter(bundleclasses.keySet(), bundleoutdir));

		runScriptTask("build");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		runScriptTask("resolveclasspath");

		runScriptTask("resolveclasspath");
		assertEmpty(getMetric().getRunTaskIdFactories());

		runScriptTask("downloadclasspath");

		runScriptTask("downloadclasspath");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}
}
