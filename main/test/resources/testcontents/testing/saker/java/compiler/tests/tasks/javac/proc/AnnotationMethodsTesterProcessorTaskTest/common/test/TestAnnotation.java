package test;

public @interface TestAnnotation {
	public Class<?> type() default TestAnnotation.class;

	public Class<?>[] typeArray() default {};

	public CommonEnum cEnum() default CommonEnum.FIRST;
}