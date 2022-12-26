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
package testing.saker.java.compiler;

import java.util.Map.Entry;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import testing.saker.SakerTestCase;
import testing.saker.nest.util.NestRepositoryCachingEnvironmentTestCase;

public abstract class JavaCompilerVariablesMetricEnvironmentTaskTestCase
		extends NestRepositoryCachingEnvironmentTestCase {
	public static final SakerPath SRC_PATH_BASE = PATH_WORKING_DIRECTORY.resolve("src");

	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		return new CompilerCollectingTestMetric();
	}

	@Override
	public void executeRunning() throws Exception {
		try {
			super.executeRunning();
		} catch (Throwable e) {
			//print the system properties in case of a test failure
			//this is to help us diagnose issues that seem to only occurr on devices
			//that we don't have access to (like CI)
			try {
				StringBuilder sb = new StringBuilder("System properties:\n");
				for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
					sb.append(entry);
					sb.append('\n');
				}
				System.err.println(sb);
			} catch (Throwable e2) {
				e.addSuppressed(e2);
			}
			throw e;
		}
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
