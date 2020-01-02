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

import saker.build.thirdparty.saker.util.ReflectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleClass;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleSecondClass;

@SakerTest
public class WildcardClassPathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(
				PATH_WORKING_DIRECTORY.resolve("cp1").resolve(SimpleClass.class.getName().replace('.', '/') + ".class"),
				ReflectUtils.getClassBytesUsingClassLoader(SimpleClass.class));
		files.putFile(
				PATH_WORKING_DIRECTORY.resolve("cp2")
						.resolve(SimpleSecondClass.class.getName().replace('.', '/') + ".class"),
				ReflectUtils.getClassBytesUsingClassLoader(SimpleSecondClass.class));

		runScriptTask("build");
	}

}
