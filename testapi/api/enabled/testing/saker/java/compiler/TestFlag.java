package testing.saker.java.compiler;

public class TestFlag {
	private static final JavaCompilerTestMetric NULL_METRIC_INSTANCE = new JavaCompilerTestMetric() {
	};
	public static final boolean ENABLED = true;

	public static JavaCompilerTestMetric metric() {
		Object res = testing.saker.build.flag.TestFlag.metric();
		if (res instanceof JavaCompilerTestMetric) {
			return (JavaCompilerTestMetric) res;
		}
		return NULL_METRIC_INSTANCE;
	}

}
