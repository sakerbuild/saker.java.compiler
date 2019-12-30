package testing.saker.java.compiler;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import testing.saker.SakerTestCase;
import testing.saker.nest.util.NestRepositoryCachingEnvironmentTestCase;

public abstract class JavaCompilerVariablesMetricEnvironmentTaskTestCase extends NestRepositoryCachingEnvironmentTestCase {
	public static final SakerPath SRC_PATH_BASE = PATH_WORKING_DIRECTORY.resolve("src");

	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		return new CompilerCollectingTestMetric();
	}

	@Override
	protected CompilerCollectingTestMetric getMetric() {
		return (CompilerCollectingTestMetric) super.getMetric();
	}

	protected static SakerPath getJavacBuildDirectory(String... dirnames) {
		return PATH_BUILD_DIRECTORY.resolve("saker.java.compile")
				.resolve(StringUtils.toStringJoin("-", ObjectUtils.newTreeSet(dirnames)));
	}

	protected static SakerPath getJavacBuildBinDirectory(String... dirnames) {
		return getJavacBuildDirectory(dirnames).resolve("bin");
	}

	protected static SakerPath getJavacBuildGeneratedSourceDirectory(String... dirnames) {
		return getJavacBuildDirectory(dirnames).resolve("gen");
	}

	protected static SakerPath getJavacBuildResourcesDirectory(String... dirnames) {
		return getJavacBuildDirectory(dirnames).resolve("res");
	}

	protected static SakerPath getJavacBuildHeaderDirectory(String... dirnames) {
		return getJavacBuildDirectory(dirnames).resolve("nativeh");
	}

	public void assertCompiled(SakerPath... files) throws AssertionError {
		if (files.length == 0) {
			SakerTestCase.assertEmpty(getMetric().getCompiledFiles());
		} else {
			SakerTestCase.assertEquals(getMetric().getCompiledFiles(), SakerTestCase.setOf(files));
		}
	}

	public void assertReused(SakerPath... files) throws AssertionError {
		if (files.length == 0) {
			SakerTestCase.assertEmpty(getMetric().getReusedFiles());
		} else {
			SakerTestCase.assertEquals(getMetric().getReusedFiles(), SakerTestCase.setOf(files));
		}
	}

}
