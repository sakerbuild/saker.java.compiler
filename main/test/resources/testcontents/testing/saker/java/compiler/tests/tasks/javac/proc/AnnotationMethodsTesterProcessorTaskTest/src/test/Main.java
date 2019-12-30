package test;

import static test.CommonEnum.SECOND;
import static test.CommonEnum.FOURTH;

public class Main {
	@TestAnnotation
	public int def;
	@TestAnnotation(type = CommonClass.class)
	public int common;
	@TestAnnotation(type = CompileOnlyClass.class)
	public int compile;
	@TestAnnotation(type = ThisTypeIsNotPresent.class)
	public int typenotpresent;

	@TestAnnotation(typeArray = TestAnnotation.class)
	public int defA;
	@TestAnnotation(typeArray = CommonClass.class)
	public int commonA;
	@TestAnnotation(typeArray = CompileOnlyClass.class)
	public int compileA;

	@TestAnnotation(typeArray = { TestAnnotation.class, TestAnnotation.class })
	public int defAA;
	@TestAnnotation(typeArray = { CommonClass.class, CommonClass.class })
	public int commonAA;
	@TestAnnotation(typeArray = { CompileOnlyClass.class, CompileOnlyClass.class })
	public int compileAA;

	@TestAnnotation(cEnum = CommonEnum.FIRST)
	public int en1;
	@TestAnnotation(cEnum = SECOND)
	public int en2;
	@TestAnnotation(cEnum = CommonEnum.THIRD)
	public int enX3;
	@TestAnnotation(cEnum = FOURTH)
	public int enX4;
}