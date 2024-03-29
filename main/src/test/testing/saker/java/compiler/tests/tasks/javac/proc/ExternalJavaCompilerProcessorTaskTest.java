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
import java.util.Set;
import java.util.TreeMap;

import testing.saker.SakerTest;
import testing.saker.build.tests.EnvironmentTestCaseConfiguration;
import testing.saker.java.compiler.CompilerCollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ExternalJavaCompilerProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		CompilerCollectingTestMetric result = super.createMetricImpl();
		result.setForceExternalCompilation(true);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		
		//to check that we safely handle newly introduced methods on Elements if we compile on Java 8 
		runScriptTask("build8");
	}

	@Override
	protected Set<EnvironmentTestCaseConfiguration> getTestConfigurations() {
		Map<String, String> environmentUserParameters = new TreeMap<>();
		environmentUserParameters.put("saker.java.jre.install.locations",
				this.testParameters.get("saker.java.jre.install.locations"));
		return EnvironmentTestCaseConfiguration.builder(super.getTestConfigurations())
				.setEnvironmentUserParameters(environmentUserParameters).build();
	}
}
