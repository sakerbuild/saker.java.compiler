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
