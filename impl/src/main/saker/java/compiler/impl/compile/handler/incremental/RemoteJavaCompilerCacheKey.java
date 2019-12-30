package saker.java.compiler.impl.compile.handler.incremental;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saker.build.runtime.environment.SakerEnvironment;
import saker.build.runtime.repository.RepositoryEnvironment;
import saker.build.thirdparty.saker.util.DateUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.cache.CacheKey;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.RemoteJavaRMIProcess;
import saker.java.compiler.impl.launching.JavaCompilerDaemon;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.utils.NestUtils;
import testing.saker.java.compiler.TestFlag;

public class RemoteJavaCompilerCacheKey implements CacheKey<RemoteCompiler, RemoteJavaRMIProcess> {
	private static final Class<JavaCompilerDaemon> JAVA_COMPILER_DAEMON_MAIN_CLASS = saker.java.compiler.impl.launching.JavaCompilerDaemon.class;
	private static final String JAVA_COMPILER_DAEMON_MAIN_CLASS_NAME = JAVA_COMPILER_DAEMON_MAIN_CLASS.getName();

	private SakerEnvironment environment;
	private String javaExe;
	private Path sakerJar;
	private final Map<String, String> executionUserParameters;

	public RemoteJavaCompilerCacheKey(SakerEnvironment environment, String javaExe, Path sakerJar) {
		this.environment = environment;
		this.javaExe = javaExe;
		this.sakerJar = sakerJar;

		NestBundleClassLoader cl = (NestBundleClassLoader) IncrementalCompilationHandler.class.getClassLoader();
		Map<String, String> userparams = cl.getBundleStorageConfiguration().getBundleLookup()
				.getLocalConfigurationUserParameters(null);
		this.executionUserParameters = userparams;
	}

	@Override
	public void close(RemoteCompiler data, RemoteJavaRMIProcess resource) throws Exception {
		resource.close();
	}

	@Override
	public RemoteJavaRMIProcess allocate() throws Exception {
		List<String> commands = ObjectUtils.newArrayList(javaExe, "-cp",
				sakerJar.toAbsolutePath().normalize().toString());
		NestBundleClassLoader cl = (NestBundleClassLoader) IncrementalCompilationHandler.class.getClassLoader();
		RepositoryEnvironment repoenv = cl.getRepository().getRepositoryEnvironment();
		commands.add(saker.build.launching.Main.class.getName());
		commands.add("action");
		commands.add("-storage-dir");
		commands.add(repoenv.getEnvironmentStorageDirectory().toString());
		commands.add("-direct-repo");
		commands.add(repoenv.getRepositoryClassPathLoadDirectory().toString());
		commands.add("main");
		for (Entry<String, String> entry : executionUserParameters.entrySet()) {
			commands.add("-U" + entry.getKey().replace("=", "\\=") + "=" + entry.getValue());
		}
		commands.add("-class");
		commands.add(JAVA_COMPILER_DAEMON_MAIN_CLASS_NAME);
		commands.add("-bundle");
		commands.add(NestUtils.getClassBundleIdentifier(JAVA_COMPILER_DAEMON_MAIN_CLASS).toString());
		return new RemoteJavaRMIProcess(commands, IncrementalCompilationHandler.class.getClassLoader(),
				JavaUtil.getCompilationRMIProperties(), environment.getEnvironmentThreadGroup());
	}

	@Override
	public RemoteCompiler generate(RemoteJavaRMIProcess resource) throws Exception {
		return new RemoteCompiler(resource);
	}

	@Override
	public boolean validate(RemoteCompiler data, RemoteJavaRMIProcess resource) {
		boolean valid = resource.isValid();
		if (!valid) {
			resource.close();
		}
		return valid;
	}

	@Override
	public long getExpiry() {
		if (TestFlag.ENABLED) {
			//if testing, expire ASAP to free resources
			return 5 * DateUtils.MS_PER_SECOND;
		}
		return 5 * DateUtils.MS_PER_MINUTE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executionUserParameters == null) ? 0 : executionUserParameters.hashCode());
		result = prime * result + ((javaExe == null) ? 0 : javaExe.hashCode());
		result = prime * result + ((sakerJar == null) ? 0 : sakerJar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteJavaCompilerCacheKey other = (RemoteJavaCompilerCacheKey) obj;
		if (executionUserParameters == null) {
			if (other.executionUserParameters != null)
				return false;
		} else if (!executionUserParameters.equals(other.executionUserParameters))
			return false;
		if (javaExe == null) {
			if (other.javaExe != null)
				return false;
		} else if (!javaExe.equals(other.javaExe))
			return false;
		if (sakerJar == null) {
			if (other.sakerJar != null)
				return false;
		} else if (!sakerJar.equals(other.sakerJar))
			return false;
		return true;
	}

}