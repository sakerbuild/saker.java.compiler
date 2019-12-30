package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskFactory;
import saker.build.task.TaskName;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.CompilerCollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.TestJarCreatingTaskFactory;

@SakerTest
public class ClassPathProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath extractprocsrcpath = PATH_WORKING_DIRECTORY
			.resolve("proc/test/ExtractProcessor.java");

	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		CompilerCollectingTestMetric result = super.createMetricImpl();
		Map<TaskName, TaskFactory<?>> injectedfactories = ObjectUtils.newTreeMap(result.getInjectedTaskFactories());
		injectedfactories.put(TaskName.valueOf("test.jar.create"), new TestJarCreatingTaskFactory());
		result.setInjectedTaskFactories(injectedfactories);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		testWithTarget("build");
		testWithTarget("jarbuild");
	}

	private void testWithTarget(String targetname) throws Throwable {
		runScriptTask(targetname);
		assertTrue(getMetric().getInitializedProcessors().contains("test.ExtractProcessor"));

		runScriptTask(targetname);
		assertEmpty(getMetric().getCompiledFiles());
		assertMap(getMetric().getRunTaskIdResults()).noRemaining();
		assertTrue(getMetric().getInitializedProcessors().isEmpty());

		files.putFile(extractprocsrcpath, files.getAllBytes(extractprocsrcpath).toString().replace("\n", "\n\n"));
		runScriptTask(targetname);
		assertTrue(getMetric().getInitializedProcessors().contains("test.ExtractProcessor"));
	}

}
