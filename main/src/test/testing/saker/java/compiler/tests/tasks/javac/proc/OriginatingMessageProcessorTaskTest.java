package testing.saker.java.compiler.tests.tasks.javac.proc;

//TODO reintroduce this when having the incremental Java compiler API on the classpath is solved
//@SakerTest
//
//public class OriginatingMessageProcessorTaskTest extends JavacProcessorMetricBuildFileTaskTestCase {
//	@Override
//	protected AnnotationProcessorReference getProcessor() {
//		SimpleProcessorConfiguration procconfig = new SimpleProcessorConfiguration(new SupplierProcessorCreator(OriginatingMessageProcessor.class));
//		procconfig.setAggregating(false);
//		return new SimpleAnnotationProcessorReference(procconfig);
//	}
//
//	@Override
//	protected Object getProcessorInfo() {
//		return false;
//	}
//
//	@Override
//	protected void runJavacTestImpl() throws Throwable  {
//		assertTaskException(JavaCompilationFailedException.class, () -> runScriptTask("build"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createErrorMessage(setOf("test.Main")));
//
//		byte[] secondbytes = "package test; @Pair public class Second { }".getBytes();
//		byte[] secondnoannotbytes = "package test; public class Second { }".getBytes();
//
//		files.putFile(SRC_PATH_BASE.resolve("test/Second.java"), secondbytes);
//		runScriptTask("build");
//		assertSetEquals(getMetric().getCompiledFiles(), SRC_PATH_BASE.resolve("test/Pair.java"), SRC_PATH_BASE.resolve("test/Main.java"),
//				SRC_PATH_BASE.resolve("test/Second.java"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createWarningMessage(setOf("test.Main", "test.Second")));
//
//		files.putFile(SRC_PATH_BASE.resolve("test/Second.java"), "package test;".getBytes());
//		assertTaskException(JavaCompilationFailedException.class, () -> runScriptTask("build"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createErrorMessage(setOf("test.Main")));
//
//		files.putFile(SRC_PATH_BASE.resolve("test/Second.java"), secondbytes);
//		runScriptTask("build");
//		assertSetEquals(getMetric().getCompiledFiles(), SRC_PATH_BASE.resolve("test/Second.java"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createWarningMessage(setOf("test.Main", "test.Second")));
//
//		files.putFile(SRC_PATH_BASE.resolve("test/Second.java"), secondnoannotbytes);
//		assertTaskException(JavaCompilationFailedException.class, () -> runScriptTask("build"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createErrorMessage(setOf("test.Main")));
//
//		files.putFile(SRC_PATH_BASE.resolve("test/Second.java"), secondbytes);
//		runScriptTask("build");
//		assertSetEquals(getMetric().getCompiledFiles(), SRC_PATH_BASE.resolve("test/Second.java"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createWarningMessage(setOf("test.Main", "test.Second")));
//
//		files.delete(SRC_PATH_BASE.resolve("test/Second.java"));
//		assertTaskException(JavaCompilationFailedException.class, () -> runScriptTask("build"));
//		assertSetEquals(getMetric().getReportedDiagnostics(), OriginatingMessageProcessor.createErrorMessage(setOf("test.Main")));
//	}
//
//}

//final class SupplierProcessorCreator implements ProcessorCreator, Serializable {
//	private static final long serialVersionUID = 1L;
//
//	private final String className;
//	private final Supplier<? extends Processor> processor;
//
//	public <P extends Processor> SupplierProcessorCreator(Class<P> clazz, Supplier<P> processor) {
//		this.className = clazz.getName();
//		this.processor = processor;
//	}
//
//	public <P extends Processor> SupplierProcessorCreator(Class<P> clazz) {
//		this.className = clazz.getName();
//		this.processor = (Serializable & Supplier<Processor>) () -> {
//			try {
//				return ReflectUtil.newInstance(clazz);
//			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
//					| SecurityException e) {
//				throw new IllegalArgumentException("Failed to instantiate: " + clazz, e);
//			}
//		};
//	}
//
//	@Override
//	public String getClassName() {
//		return className;
//	}
//
//	@Override
//	public ContentDescriptor getContentDescriptor() {
//		return EmptyContentDescriptor.INSTANCE;
//	}
//
//	@Override
//	public Processor create(TaskContext taskcontext) {
//		return processor.get();
//	}
//
//	@Override
//	public SupplierProcessorCreator clone() {
//		try {
//			return (SupplierProcessorCreator) super.clone();
//		} catch (CloneNotSupportedException e) {
//			throw new AssertionError(e);
//		}
//	}
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((className == null) ? 0 : className.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		SupplierProcessorCreator other = (SupplierProcessorCreator) obj;
//		if (className == null) {
//			if (other.className != null)
//				return false;
//		} else if (!className.equals(other.className))
//			return false;
//		return true;
//	}
//
//	@Override
//	public String toString() {
//		return "SupplierProcessorCreator [" + (className != null ? "className=" + className : "") + "]";
//	}
//
//}