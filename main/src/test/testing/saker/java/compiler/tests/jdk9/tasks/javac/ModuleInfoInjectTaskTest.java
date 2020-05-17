package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import saker.build.thirdparty.org.objectweb.asm.ClassReader;
import saker.build.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.build.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;

import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ModuleInfoInjectTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	private String mainClass;
	private String version;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.mainclass", mainClass);
		result.put("test.version", version);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		mainClass = "firstpkg.FirstClass";
		version = "1.2";

		runScriptTask("build");
		assertModuleInfoAttributes();

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdResults());
		assertModuleInfoAttributes();

		version = "1.3";
		runScriptTask("build");
		assertModuleInfoAttributes();

		mainClass = "test.MyClass";
		runScriptTask("build");
		assertModuleInfoAttributes();
	}

	private void assertModuleInfoAttributes() throws IOException, AssertionError {
		boolean[] hadmodule = { false };
		boolean[] hadmain = { false };

		new ClassReader(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("saker.java.compile/main/bin/module-info.class")).copy())
						.accept(new ClassVisitor(Opcodes.ASM7) {
							@Override
							public ModuleVisitor visitModule(String name, int access, String version) {
								assertEquals(version, ModuleInfoInjectTaskTest.this.version);
								hadmodule[0] = true;
								return new ModuleVisitor(api, super.visitModule(name, access, version)) {
									@Override
									public void visitMainClass(String mainClass) {
										assertEquals(mainClass, mainClass);
										hadmain[0] = true;
										super.visitMainClass(mainClass);
									}
								};
							}
						}, 0);
		assertEquals(hadmain[0], mainClass != null);
		assertEquals(hadmodule[0], version != null);
	}

}
