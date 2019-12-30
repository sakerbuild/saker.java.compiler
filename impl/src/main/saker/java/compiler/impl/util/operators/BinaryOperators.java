package saker.java.compiler.impl.util.operators;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

@SuppressWarnings("cast")
public class BinaryOperators {
	/**
	 * Maps name of Tree.Kind enum constants to maps of left and right operands resulting in a bifunction that accepts
	 * the appropriate typed objects as parameters.
	 */
	private static final Map<String, Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>>> BINARY_OPERATORS = new TreeMap<>();
	static {
		initAnd();
		initConditionalAnd();
		initConditionalOr();
		initDivide();
		initEqual();
		initGreater();
		initGreaterEqual();
		initLess();
		initLessEqual();
		initMinus();
		initMultiply();
		initNotEqual();
		initOr();
		initPlus();
		initRemainder();
		initShiftLeft();
		initShiftRight();
		initShiftUnsignedRight();
		initXor();
	}

	public static <L, R> BiFunction<L, R, Object> getOperatorFunction(String op, L l, R r) {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> opmap = BINARY_OPERATORS.get(op);
		if (opmap == null) {
			return null;
		}

		return getOperatorInMap(l, r, opmap);
	}

	@SuppressWarnings("unchecked")
	private static <L, R> BiFunction<L, R, Object> getOperatorInMap(L l, R r,
			Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> opmap) {
		Map<Class<?>, BiFunction<?, ?, Object>> leftmap = opmap.get(l.getClass());
		if (leftmap == null) {
			leftmap = opmap.get(Object.class);
			if (leftmap == null) {
				return null;
			}
		}

		BiFunction<?, ?, Object> opfunc = leftmap.get(r.getClass());
		if (opfunc == null) {
			opfunc = leftmap.get(Object.class);
		}
		return (BiFunction<L, R, Object>) opfunc;
	}

	private static void initAnd() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> AndMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanAnd = new HashMap<>();
		BooleanAnd.put(Boolean.class, (BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorAnd);
		AndMap.put(Boolean.class, BooleanAnd);
		Map<Class<?>, BiFunction<?, ?, Object>> ByteAnd = new HashMap<>();
		ByteAnd.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorAnd);
		ByteAnd.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorAnd);
		ByteAnd.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorAnd);
		ByteAnd.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorAnd);
		ByteAnd.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorAnd);
		AndMap.put(Byte.class, ByteAnd);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortAnd = new HashMap<>();
		ShortAnd.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorAnd);
		ShortAnd.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorAnd);
		ShortAnd.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorAnd);
		ShortAnd.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorAnd);
		ShortAnd.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorAnd);
		AndMap.put(Short.class, ShortAnd);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerAnd = new HashMap<>();
		IntegerAnd.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorAnd);
		IntegerAnd.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorAnd);
		IntegerAnd.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorAnd);
		IntegerAnd.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorAnd);
		IntegerAnd.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorAnd);
		AndMap.put(Integer.class, IntegerAnd);
		Map<Class<?>, BiFunction<?, ?, Object>> LongAnd = new HashMap<>();
		LongAnd.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorAnd);
		LongAnd.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorAnd);
		LongAnd.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorAnd);
		LongAnd.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorAnd);
		LongAnd.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorAnd);
		AndMap.put(Long.class, LongAnd);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterAnd = new HashMap<>();
		CharacterAnd.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorAnd);
		CharacterAnd.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorAnd);
		CharacterAnd.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorAnd);
		CharacterAnd.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorAnd);
		CharacterAnd.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorAnd);
		AndMap.put(Character.class, CharacterAnd);
		BINARY_OPERATORS.put("AND", AndMap);
	}

	private static void initConditionalAnd() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> ConditionalAndMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanConditionalAnd = new HashMap<>();
		BooleanConditionalAnd.put(Boolean.class,
				(BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorConditionalAnd);
		ConditionalAndMap.put(Boolean.class, BooleanConditionalAnd);
		BINARY_OPERATORS.put("CONDITIONAL_AND", ConditionalAndMap);
	}

	private static void initConditionalOr() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> ConditionalOrMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanConditionalOr = new HashMap<>();
		BooleanConditionalOr.put(Boolean.class,
				(BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorConditionalOr);
		ConditionalOrMap.put(Boolean.class, BooleanConditionalOr);
		BINARY_OPERATORS.put("CONDITIONAL_OR", ConditionalOrMap);
	}

	private static void initDivide() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> DivideMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteDivide = new HashMap<>();
		ByteDivide.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorDivide);
		ByteDivide.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorDivide);
		ByteDivide.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorDivide);
		ByteDivide.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorDivide);
		ByteDivide.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorDivide);
		ByteDivide.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorDivide);
		ByteDivide.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Byte.class, ByteDivide);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortDivide = new HashMap<>();
		ShortDivide.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorDivide);
		ShortDivide.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorDivide);
		ShortDivide.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorDivide);
		ShortDivide.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorDivide);
		ShortDivide.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorDivide);
		ShortDivide.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorDivide);
		ShortDivide.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Short.class, ShortDivide);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerDivide = new HashMap<>();
		IntegerDivide.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorDivide);
		IntegerDivide.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorDivide);
		IntegerDivide.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorDivide);
		IntegerDivide.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorDivide);
		IntegerDivide.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorDivide);
		IntegerDivide.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorDivide);
		IntegerDivide.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Integer.class, IntegerDivide);
		Map<Class<?>, BiFunction<?, ?, Object>> LongDivide = new HashMap<>();
		LongDivide.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorDivide);
		LongDivide.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorDivide);
		LongDivide.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorDivide);
		LongDivide.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorDivide);
		LongDivide.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorDivide);
		LongDivide.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorDivide);
		LongDivide.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Long.class, LongDivide);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatDivide = new HashMap<>();
		FloatDivide.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorDivide);
		FloatDivide.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorDivide);
		FloatDivide.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorDivide);
		FloatDivide.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorDivide);
		FloatDivide.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorDivide);
		FloatDivide.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorDivide);
		FloatDivide.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Float.class, FloatDivide);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleDivide = new HashMap<>();
		DoubleDivide.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorDivide);
		DoubleDivide.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorDivide);
		DoubleDivide.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorDivide);
		DoubleDivide.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorDivide);
		DoubleDivide.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorDivide);
		DoubleDivide.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorDivide);
		DoubleDivide.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Double.class, DoubleDivide);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterDivide = new HashMap<>();
		CharacterDivide.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorDivide);
		CharacterDivide.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorDivide);
		CharacterDivide.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorDivide);
		CharacterDivide.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorDivide);
		CharacterDivide.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorDivide);
		CharacterDivide.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorDivide);
		CharacterDivide.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorDivide);
		DivideMap.put(Character.class, CharacterDivide);
		BINARY_OPERATORS.put("DIVIDE", DivideMap);
	}

	private static void initEqual() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> EqualMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ObjectEqual = new HashMap<>();
		ObjectEqual.put(String.class, (BiFunction<Object, String, Object>) BinaryOperators::operatorEqual);
		ObjectEqual.put(Object.class, (BiFunction<Object, Object, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Object.class, ObjectEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanEqual = new HashMap<>();
		BooleanEqual.put(Boolean.class, (BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Boolean.class, BooleanEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> ByteEqual = new HashMap<>();
		ByteEqual.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorEqual);
		ByteEqual.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorEqual);
		ByteEqual.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorEqual);
		ByteEqual.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorEqual);
		ByteEqual.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorEqual);
		ByteEqual.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorEqual);
		ByteEqual.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Byte.class, ByteEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortEqual = new HashMap<>();
		ShortEqual.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorEqual);
		ShortEqual.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorEqual);
		ShortEqual.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorEqual);
		ShortEqual.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorEqual);
		ShortEqual.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorEqual);
		ShortEqual.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorEqual);
		ShortEqual.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Short.class, ShortEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerEqual = new HashMap<>();
		IntegerEqual.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorEqual);
		IntegerEqual.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorEqual);
		IntegerEqual.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorEqual);
		IntegerEqual.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorEqual);
		IntegerEqual.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorEqual);
		IntegerEqual.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorEqual);
		IntegerEqual.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Integer.class, IntegerEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> LongEqual = new HashMap<>();
		LongEqual.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorEqual);
		LongEqual.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorEqual);
		LongEqual.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorEqual);
		LongEqual.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorEqual);
		LongEqual.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorEqual);
		LongEqual.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorEqual);
		LongEqual.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Long.class, LongEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatEqual = new HashMap<>();
		FloatEqual.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorEqual);
		FloatEqual.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorEqual);
		FloatEqual.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorEqual);
		FloatEqual.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorEqual);
		FloatEqual.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorEqual);
		FloatEqual.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorEqual);
		FloatEqual.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Float.class, FloatEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleEqual = new HashMap<>();
		DoubleEqual.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorEqual);
		DoubleEqual.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorEqual);
		DoubleEqual.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorEqual);
		DoubleEqual.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorEqual);
		DoubleEqual.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorEqual);
		DoubleEqual.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorEqual);
		DoubleEqual.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Double.class, DoubleEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> StringEqual = new HashMap<>();
		StringEqual.put(Object.class, (BiFunction<String, Object, Object>) BinaryOperators::operatorEqual);
		StringEqual.put(String.class, (BiFunction<String, String, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(String.class, StringEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterEqual = new HashMap<>();
		CharacterEqual.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorEqual);
		CharacterEqual.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorEqual);
		CharacterEqual.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorEqual);
		CharacterEqual.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorEqual);
		CharacterEqual.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorEqual);
		CharacterEqual.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorEqual);
		CharacterEqual.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorEqual);
		EqualMap.put(Character.class, CharacterEqual);
		BINARY_OPERATORS.put("EQUAL_TO", EqualMap);
	}

	private static void initGreater() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> GreaterMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteGreater = new HashMap<>();
		ByteGreater.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorGreater);
		ByteGreater.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorGreater);
		ByteGreater.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorGreater);
		ByteGreater.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorGreater);
		ByteGreater.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorGreater);
		ByteGreater.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorGreater);
		ByteGreater.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Byte.class, ByteGreater);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortGreater = new HashMap<>();
		ShortGreater.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorGreater);
		ShortGreater.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorGreater);
		ShortGreater.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorGreater);
		ShortGreater.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorGreater);
		ShortGreater.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorGreater);
		ShortGreater.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorGreater);
		ShortGreater.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Short.class, ShortGreater);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerGreater = new HashMap<>();
		IntegerGreater.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorGreater);
		IntegerGreater.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorGreater);
		IntegerGreater.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorGreater);
		IntegerGreater.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorGreater);
		IntegerGreater.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorGreater);
		IntegerGreater.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorGreater);
		IntegerGreater.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Integer.class, IntegerGreater);
		Map<Class<?>, BiFunction<?, ?, Object>> LongGreater = new HashMap<>();
		LongGreater.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorGreater);
		LongGreater.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorGreater);
		LongGreater.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorGreater);
		LongGreater.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorGreater);
		LongGreater.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorGreater);
		LongGreater.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorGreater);
		LongGreater.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Long.class, LongGreater);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatGreater = new HashMap<>();
		FloatGreater.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorGreater);
		FloatGreater.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorGreater);
		FloatGreater.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorGreater);
		FloatGreater.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorGreater);
		FloatGreater.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorGreater);
		FloatGreater.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorGreater);
		FloatGreater.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Float.class, FloatGreater);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleGreater = new HashMap<>();
		DoubleGreater.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorGreater);
		DoubleGreater.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorGreater);
		DoubleGreater.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorGreater);
		DoubleGreater.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorGreater);
		DoubleGreater.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorGreater);
		DoubleGreater.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorGreater);
		DoubleGreater.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Double.class, DoubleGreater);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterGreater = new HashMap<>();
		CharacterGreater.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorGreater);
		CharacterGreater.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorGreater);
		CharacterGreater.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorGreater);
		CharacterGreater.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorGreater);
		CharacterGreater.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorGreater);
		CharacterGreater.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorGreater);
		CharacterGreater.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorGreater);
		GreaterMap.put(Character.class, CharacterGreater);
		BINARY_OPERATORS.put("GREATER_THAN", GreaterMap);
	}

	private static void initGreaterEqual() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> GreaterEqualMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteGreaterEqual = new HashMap<>();
		ByteGreaterEqual.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorGreaterEqual);
		ByteGreaterEqual.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorGreaterEqual);
		ByteGreaterEqual.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorGreaterEqual);
		ByteGreaterEqual.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		ByteGreaterEqual.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorGreaterEqual);
		ByteGreaterEqual.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		ByteGreaterEqual.put(Character.class,
				(BiFunction<Byte, Character, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Byte.class, ByteGreaterEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortGreaterEqual = new HashMap<>();
		ShortGreaterEqual.put(Character.class,
				(BiFunction<Short, Character, Object>) BinaryOperators::operatorGreaterEqual);
		ShortGreaterEqual.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorGreaterEqual);
		ShortGreaterEqual.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorGreaterEqual);
		ShortGreaterEqual.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorGreaterEqual);
		ShortGreaterEqual.put(Integer.class,
				(BiFunction<Short, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		ShortGreaterEqual.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorGreaterEqual);
		ShortGreaterEqual.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Short.class, ShortGreaterEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerGreaterEqual = new HashMap<>();
		IntegerGreaterEqual.put(Short.class,
				(BiFunction<Integer, Short, Object>) BinaryOperators::operatorGreaterEqual);
		IntegerGreaterEqual.put(Integer.class,
				(BiFunction<Integer, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		IntegerGreaterEqual.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorGreaterEqual);
		IntegerGreaterEqual.put(Float.class,
				(BiFunction<Integer, Float, Object>) BinaryOperators::operatorGreaterEqual);
		IntegerGreaterEqual.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		IntegerGreaterEqual.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorGreaterEqual);
		IntegerGreaterEqual.put(Double.class,
				(BiFunction<Integer, Double, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Integer.class, IntegerGreaterEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> LongGreaterEqual = new HashMap<>();
		LongGreaterEqual.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorGreaterEqual);
		LongGreaterEqual.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		LongGreaterEqual.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorGreaterEqual);
		LongGreaterEqual.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		LongGreaterEqual.put(Character.class,
				(BiFunction<Long, Character, Object>) BinaryOperators::operatorGreaterEqual);
		LongGreaterEqual.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorGreaterEqual);
		LongGreaterEqual.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Long.class, LongGreaterEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatGreaterEqual = new HashMap<>();
		FloatGreaterEqual.put(Integer.class,
				(BiFunction<Float, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		FloatGreaterEqual.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorGreaterEqual);
		FloatGreaterEqual.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		FloatGreaterEqual.put(Character.class,
				(BiFunction<Float, Character, Object>) BinaryOperators::operatorGreaterEqual);
		FloatGreaterEqual.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorGreaterEqual);
		FloatGreaterEqual.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorGreaterEqual);
		FloatGreaterEqual.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Float.class, FloatGreaterEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleGreaterEqual = new HashMap<>();
		DoubleGreaterEqual.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorGreaterEqual);
		DoubleGreaterEqual.put(Double.class,
				(BiFunction<Double, Double, Object>) BinaryOperators::operatorGreaterEqual);
		DoubleGreaterEqual.put(Character.class,
				(BiFunction<Double, Character, Object>) BinaryOperators::operatorGreaterEqual);
		DoubleGreaterEqual.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorGreaterEqual);
		DoubleGreaterEqual.put(Integer.class,
				(BiFunction<Double, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		DoubleGreaterEqual.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorGreaterEqual);
		DoubleGreaterEqual.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Double.class, DoubleGreaterEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterGreaterEqual = new HashMap<>();
		CharacterGreaterEqual.put(Byte.class,
				(BiFunction<Character, Byte, Object>) BinaryOperators::operatorGreaterEqual);
		CharacterGreaterEqual.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorGreaterEqual);
		CharacterGreaterEqual.put(Double.class,
				(BiFunction<Character, Double, Object>) BinaryOperators::operatorGreaterEqual);
		CharacterGreaterEqual.put(Float.class,
				(BiFunction<Character, Float, Object>) BinaryOperators::operatorGreaterEqual);
		CharacterGreaterEqual.put(Long.class,
				(BiFunction<Character, Long, Object>) BinaryOperators::operatorGreaterEqual);
		CharacterGreaterEqual.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorGreaterEqual);
		CharacterGreaterEqual.put(Short.class,
				(BiFunction<Character, Short, Object>) BinaryOperators::operatorGreaterEqual);
		GreaterEqualMap.put(Character.class, CharacterGreaterEqual);
		BINARY_OPERATORS.put("GREATER_THAN_EQUAL", GreaterEqualMap);
	}

	private static void initLess() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> LessMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteLess = new HashMap<>();
		ByteLess.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorLess);
		ByteLess.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorLess);
		ByteLess.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorLess);
		ByteLess.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorLess);
		ByteLess.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorLess);
		ByteLess.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorLess);
		ByteLess.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorLess);
		LessMap.put(Byte.class, ByteLess);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortLess = new HashMap<>();
		ShortLess.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorLess);
		ShortLess.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorLess);
		ShortLess.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorLess);
		ShortLess.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorLess);
		ShortLess.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorLess);
		ShortLess.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorLess);
		ShortLess.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorLess);
		LessMap.put(Short.class, ShortLess);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerLess = new HashMap<>();
		IntegerLess.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorLess);
		IntegerLess.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorLess);
		IntegerLess.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorLess);
		IntegerLess.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorLess);
		IntegerLess.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorLess);
		IntegerLess.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorLess);
		IntegerLess.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorLess);
		LessMap.put(Integer.class, IntegerLess);
		Map<Class<?>, BiFunction<?, ?, Object>> LongLess = new HashMap<>();
		LongLess.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorLess);
		LongLess.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorLess);
		LongLess.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorLess);
		LongLess.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorLess);
		LongLess.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorLess);
		LongLess.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorLess);
		LongLess.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorLess);
		LessMap.put(Long.class, LongLess);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatLess = new HashMap<>();
		FloatLess.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorLess);
		FloatLess.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorLess);
		FloatLess.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorLess);
		FloatLess.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorLess);
		FloatLess.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorLess);
		FloatLess.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorLess);
		FloatLess.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorLess);
		LessMap.put(Float.class, FloatLess);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleLess = new HashMap<>();
		DoubleLess.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorLess);
		DoubleLess.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorLess);
		DoubleLess.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorLess);
		DoubleLess.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorLess);
		DoubleLess.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorLess);
		DoubleLess.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorLess);
		DoubleLess.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorLess);
		LessMap.put(Double.class, DoubleLess);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterLess = new HashMap<>();
		CharacterLess.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorLess);
		CharacterLess.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorLess);
		CharacterLess.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorLess);
		CharacterLess.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorLess);
		CharacterLess.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorLess);
		CharacterLess.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorLess);
		CharacterLess.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorLess);
		LessMap.put(Character.class, CharacterLess);
		BINARY_OPERATORS.put("LESS_THAN", LessMap);
	}

	private static void initLessEqual() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> LessEqualMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteLessEqual = new HashMap<>();
		ByteLessEqual.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorLessEqual);
		ByteLessEqual.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorLessEqual);
		ByteLessEqual.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorLessEqual);
		ByteLessEqual.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorLessEqual);
		ByteLessEqual.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorLessEqual);
		ByteLessEqual.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorLessEqual);
		ByteLessEqual.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Byte.class, ByteLessEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortLessEqual = new HashMap<>();
		ShortLessEqual.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorLessEqual);
		ShortLessEqual.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorLessEqual);
		ShortLessEqual.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorLessEqual);
		ShortLessEqual.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorLessEqual);
		ShortLessEqual.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorLessEqual);
		ShortLessEqual.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorLessEqual);
		ShortLessEqual.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Short.class, ShortLessEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerLessEqual = new HashMap<>();
		IntegerLessEqual.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorLessEqual);
		IntegerLessEqual.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorLessEqual);
		IntegerLessEqual.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorLessEqual);
		IntegerLessEqual.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorLessEqual);
		IntegerLessEqual.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorLessEqual);
		IntegerLessEqual.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorLessEqual);
		IntegerLessEqual.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Integer.class, IntegerLessEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> LongLessEqual = new HashMap<>();
		LongLessEqual.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorLessEqual);
		LongLessEqual.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorLessEqual);
		LongLessEqual.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorLessEqual);
		LongLessEqual.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorLessEqual);
		LongLessEqual.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorLessEqual);
		LongLessEqual.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorLessEqual);
		LongLessEqual.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Long.class, LongLessEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatLessEqual = new HashMap<>();
		FloatLessEqual.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorLessEqual);
		FloatLessEqual.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorLessEqual);
		FloatLessEqual.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorLessEqual);
		FloatLessEqual.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorLessEqual);
		FloatLessEqual.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorLessEqual);
		FloatLessEqual.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorLessEqual);
		FloatLessEqual.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Float.class, FloatLessEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleLessEqual = new HashMap<>();
		DoubleLessEqual.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorLessEqual);
		DoubleLessEqual.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorLessEqual);
		DoubleLessEqual.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorLessEqual);
		DoubleLessEqual.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorLessEqual);
		DoubleLessEqual.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorLessEqual);
		DoubleLessEqual.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorLessEqual);
		DoubleLessEqual.put(Character.class,
				(BiFunction<Double, Character, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Double.class, DoubleLessEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterLessEqual = new HashMap<>();
		CharacterLessEqual.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorLessEqual);
		CharacterLessEqual.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorLessEqual);
		CharacterLessEqual.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorLessEqual);
		CharacterLessEqual.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorLessEqual);
		CharacterLessEqual.put(Double.class,
				(BiFunction<Character, Double, Object>) BinaryOperators::operatorLessEqual);
		CharacterLessEqual.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorLessEqual);
		CharacterLessEqual.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorLessEqual);
		LessEqualMap.put(Character.class, CharacterLessEqual);
		BINARY_OPERATORS.put("LESS_THAN_EQUAL", LessEqualMap);
	}

	private static void initMinus() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> MinusMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteMinus = new HashMap<>();
		ByteMinus.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorMinus);
		ByteMinus.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorMinus);
		ByteMinus.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorMinus);
		ByteMinus.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorMinus);
		ByteMinus.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorMinus);
		ByteMinus.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorMinus);
		ByteMinus.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Byte.class, ByteMinus);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortMinus = new HashMap<>();
		ShortMinus.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorMinus);
		ShortMinus.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorMinus);
		ShortMinus.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorMinus);
		ShortMinus.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorMinus);
		ShortMinus.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorMinus);
		ShortMinus.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorMinus);
		ShortMinus.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Short.class, ShortMinus);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerMinus = new HashMap<>();
		IntegerMinus.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorMinus);
		IntegerMinus.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorMinus);
		IntegerMinus.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorMinus);
		IntegerMinus.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorMinus);
		IntegerMinus.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorMinus);
		IntegerMinus.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorMinus);
		IntegerMinus.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Integer.class, IntegerMinus);
		Map<Class<?>, BiFunction<?, ?, Object>> LongMinus = new HashMap<>();
		LongMinus.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorMinus);
		LongMinus.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorMinus);
		LongMinus.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorMinus);
		LongMinus.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorMinus);
		LongMinus.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorMinus);
		LongMinus.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorMinus);
		LongMinus.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Long.class, LongMinus);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatMinus = new HashMap<>();
		FloatMinus.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorMinus);
		FloatMinus.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorMinus);
		FloatMinus.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorMinus);
		FloatMinus.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorMinus);
		FloatMinus.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorMinus);
		FloatMinus.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorMinus);
		FloatMinus.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Float.class, FloatMinus);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleMinus = new HashMap<>();
		DoubleMinus.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorMinus);
		DoubleMinus.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorMinus);
		DoubleMinus.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorMinus);
		DoubleMinus.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorMinus);
		DoubleMinus.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorMinus);
		DoubleMinus.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorMinus);
		DoubleMinus.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Double.class, DoubleMinus);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterMinus = new HashMap<>();
		CharacterMinus.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorMinus);
		CharacterMinus.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorMinus);
		CharacterMinus.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorMinus);
		CharacterMinus.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorMinus);
		CharacterMinus.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorMinus);
		CharacterMinus.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorMinus);
		CharacterMinus.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorMinus);
		MinusMap.put(Character.class, CharacterMinus);
		BINARY_OPERATORS.put("MINUS", MinusMap);
	}

	private static void initMultiply() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> MultiplyMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteMultiply = new HashMap<>();
		ByteMultiply.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorMultiply);
		ByteMultiply.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorMultiply);
		ByteMultiply.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorMultiply);
		ByteMultiply.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorMultiply);
		ByteMultiply.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorMultiply);
		ByteMultiply.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorMultiply);
		ByteMultiply.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Byte.class, ByteMultiply);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortMultiply = new HashMap<>();
		ShortMultiply.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorMultiply);
		ShortMultiply.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorMultiply);
		ShortMultiply.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorMultiply);
		ShortMultiply.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorMultiply);
		ShortMultiply.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorMultiply);
		ShortMultiply.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorMultiply);
		ShortMultiply.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Short.class, ShortMultiply);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerMultiply = new HashMap<>();
		IntegerMultiply.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorMultiply);
		IntegerMultiply.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorMultiply);
		IntegerMultiply.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorMultiply);
		IntegerMultiply.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorMultiply);
		IntegerMultiply.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorMultiply);
		IntegerMultiply.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorMultiply);
		IntegerMultiply.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Integer.class, IntegerMultiply);
		Map<Class<?>, BiFunction<?, ?, Object>> LongMultiply = new HashMap<>();
		LongMultiply.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorMultiply);
		LongMultiply.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorMultiply);
		LongMultiply.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorMultiply);
		LongMultiply.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorMultiply);
		LongMultiply.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorMultiply);
		LongMultiply.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorMultiply);
		LongMultiply.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Long.class, LongMultiply);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatMultiply = new HashMap<>();
		FloatMultiply.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorMultiply);
		FloatMultiply.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorMultiply);
		FloatMultiply.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorMultiply);
		FloatMultiply.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorMultiply);
		FloatMultiply.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorMultiply);
		FloatMultiply.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorMultiply);
		FloatMultiply.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Float.class, FloatMultiply);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleMultiply = new HashMap<>();
		DoubleMultiply.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorMultiply);
		DoubleMultiply.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorMultiply);
		DoubleMultiply.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorMultiply);
		DoubleMultiply.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorMultiply);
		DoubleMultiply.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorMultiply);
		DoubleMultiply.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorMultiply);
		DoubleMultiply.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Double.class, DoubleMultiply);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterMultiply = new HashMap<>();
		CharacterMultiply.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorMultiply);
		CharacterMultiply.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorMultiply);
		CharacterMultiply.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorMultiply);
		CharacterMultiply.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorMultiply);
		CharacterMultiply.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorMultiply);
		CharacterMultiply.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorMultiply);
		CharacterMultiply.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorMultiply);
		MultiplyMap.put(Character.class, CharacterMultiply);
		BINARY_OPERATORS.put("MULTIPLY", MultiplyMap);
	}

	private static void initNotEqual() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> NotEqualMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ObjectNotEqual = new HashMap<>();
		ObjectNotEqual.put(String.class, (BiFunction<Object, String, Object>) BinaryOperators::operatorNotEqual);
		ObjectNotEqual.put(Object.class, (BiFunction<Object, Object, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Object.class, ObjectNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanNotEqual = new HashMap<>();
		BooleanNotEqual.put(Boolean.class, (BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Boolean.class, BooleanNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> ByteNotEqual = new HashMap<>();
		ByteNotEqual.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorNotEqual);
		ByteNotEqual.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorNotEqual);
		ByteNotEqual.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorNotEqual);
		ByteNotEqual.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorNotEqual);
		ByteNotEqual.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorNotEqual);
		ByteNotEqual.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorNotEqual);
		ByteNotEqual.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Byte.class, ByteNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortNotEqual = new HashMap<>();
		ShortNotEqual.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorNotEqual);
		ShortNotEqual.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorNotEqual);
		ShortNotEqual.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorNotEqual);
		ShortNotEqual.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorNotEqual);
		ShortNotEqual.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorNotEqual);
		ShortNotEqual.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorNotEqual);
		ShortNotEqual.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Short.class, ShortNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerNotEqual = new HashMap<>();
		IntegerNotEqual.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorNotEqual);
		IntegerNotEqual.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorNotEqual);
		IntegerNotEqual.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorNotEqual);
		IntegerNotEqual.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorNotEqual);
		IntegerNotEqual.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorNotEqual);
		IntegerNotEqual.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorNotEqual);
		IntegerNotEqual.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Integer.class, IntegerNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> LongNotEqual = new HashMap<>();
		LongNotEqual.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorNotEqual);
		LongNotEqual.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorNotEqual);
		LongNotEqual.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorNotEqual);
		LongNotEqual.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorNotEqual);
		LongNotEqual.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorNotEqual);
		LongNotEqual.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorNotEqual);
		LongNotEqual.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Long.class, LongNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatNotEqual = new HashMap<>();
		FloatNotEqual.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorNotEqual);
		FloatNotEqual.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorNotEqual);
		FloatNotEqual.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorNotEqual);
		FloatNotEqual.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorNotEqual);
		FloatNotEqual.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorNotEqual);
		FloatNotEqual.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorNotEqual);
		FloatNotEqual.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Float.class, FloatNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleNotEqual = new HashMap<>();
		DoubleNotEqual.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorNotEqual);
		DoubleNotEqual.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorNotEqual);
		DoubleNotEqual.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorNotEqual);
		DoubleNotEqual.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorNotEqual);
		DoubleNotEqual.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorNotEqual);
		DoubleNotEqual.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorNotEqual);
		DoubleNotEqual.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Double.class, DoubleNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> StringNotEqual = new HashMap<>();
		StringNotEqual.put(Object.class, (BiFunction<String, Object, Object>) BinaryOperators::operatorNotEqual);
		StringNotEqual.put(String.class, (BiFunction<String, String, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(String.class, StringNotEqual);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterNotEqual = new HashMap<>();
		CharacterNotEqual.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorNotEqual);
		CharacterNotEqual.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorNotEqual);
		CharacterNotEqual.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorNotEqual);
		CharacterNotEqual.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorNotEqual);
		CharacterNotEqual.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorNotEqual);
		CharacterNotEqual.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorNotEqual);
		CharacterNotEqual.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorNotEqual);
		NotEqualMap.put(Character.class, CharacterNotEqual);
		BINARY_OPERATORS.put("NOT_EQUAL_TO", NotEqualMap);
	}

	private static void initOr() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> OrMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanOr = new HashMap<>();
		BooleanOr.put(Boolean.class, (BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorOr);
		OrMap.put(Boolean.class, BooleanOr);
		Map<Class<?>, BiFunction<?, ?, Object>> ByteOr = new HashMap<>();
		ByteOr.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorOr);
		ByteOr.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorOr);
		ByteOr.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorOr);
		ByteOr.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorOr);
		ByteOr.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorOr);
		OrMap.put(Byte.class, ByteOr);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortOr = new HashMap<>();
		ShortOr.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorOr);
		ShortOr.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorOr);
		ShortOr.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorOr);
		ShortOr.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorOr);
		ShortOr.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorOr);
		OrMap.put(Short.class, ShortOr);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerOr = new HashMap<>();
		IntegerOr.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorOr);
		IntegerOr.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorOr);
		IntegerOr.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorOr);
		IntegerOr.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorOr);
		IntegerOr.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorOr);
		OrMap.put(Integer.class, IntegerOr);
		Map<Class<?>, BiFunction<?, ?, Object>> LongOr = new HashMap<>();
		LongOr.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorOr);
		LongOr.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorOr);
		LongOr.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorOr);
		LongOr.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorOr);
		LongOr.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorOr);
		OrMap.put(Long.class, LongOr);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterOr = new HashMap<>();
		CharacterOr.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorOr);
		CharacterOr.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorOr);
		CharacterOr.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorOr);
		CharacterOr.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorOr);
		CharacterOr.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorOr);
		OrMap.put(Character.class, CharacterOr);
		BINARY_OPERATORS.put("OR", OrMap);
	}

	private static void initPlus() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> PlusMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ObjectPlus = new HashMap<>();
		ObjectPlus.put(String.class, (BiFunction<Object, String, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Object.class, ObjectPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanPlus = new HashMap<>();
		BooleanPlus.put(String.class, (BiFunction<Boolean, String, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Boolean.class, BooleanPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> BytePlus = new HashMap<>();
		BytePlus.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(String.class, (BiFunction<Byte, String, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorPlus);
		BytePlus.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Byte.class, BytePlus);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortPlus = new HashMap<>();
		ShortPlus.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(String.class, (BiFunction<Short, String, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorPlus);
		ShortPlus.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Short.class, ShortPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerPlus = new HashMap<>();
		IntegerPlus.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(String.class, (BiFunction<Integer, String, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorPlus);
		IntegerPlus.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Integer.class, IntegerPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> LongPlus = new HashMap<>();
		LongPlus.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(String.class, (BiFunction<Long, String, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorPlus);
		LongPlus.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Long.class, LongPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatPlus = new HashMap<>();
		FloatPlus.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(String.class, (BiFunction<Float, String, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorPlus);
		FloatPlus.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Float.class, FloatPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> DoublePlus = new HashMap<>();
		DoublePlus.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(String.class, (BiFunction<Double, String, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(Character.class, (BiFunction<Double, Character, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorPlus);
		DoublePlus.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Double.class, DoublePlus);
		Map<Class<?>, BiFunction<?, ?, Object>> StringPlus = new HashMap<>();
		StringPlus.put(Float.class, (BiFunction<String, Float, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Double.class, (BiFunction<String, Double, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(String.class, (BiFunction<String, String, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Character.class, (BiFunction<String, Character, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Boolean.class, (BiFunction<String, Boolean, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Byte.class, (BiFunction<String, Byte, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Short.class, (BiFunction<String, Short, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Integer.class, (BiFunction<String, Integer, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Object.class, (BiFunction<String, Object, Object>) BinaryOperators::operatorPlus);
		StringPlus.put(Long.class, (BiFunction<String, Long, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(String.class, StringPlus);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterPlus = new HashMap<>();
		CharacterPlus.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(Double.class, (BiFunction<Character, Double, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(String.class, (BiFunction<Character, String, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorPlus);
		CharacterPlus.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorPlus);
		PlusMap.put(Character.class, CharacterPlus);
		BINARY_OPERATORS.put("PLUS", PlusMap);
	}

	private static void initRemainder() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> RemainderMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteRemainder = new HashMap<>();
		ByteRemainder.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorRemainder);
		ByteRemainder.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorRemainder);
		ByteRemainder.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorRemainder);
		ByteRemainder.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorRemainder);
		ByteRemainder.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorRemainder);
		ByteRemainder.put(Float.class, (BiFunction<Byte, Float, Object>) BinaryOperators::operatorRemainder);
		ByteRemainder.put(Double.class, (BiFunction<Byte, Double, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Byte.class, ByteRemainder);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortRemainder = new HashMap<>();
		ShortRemainder.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorRemainder);
		ShortRemainder.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorRemainder);
		ShortRemainder.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorRemainder);
		ShortRemainder.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorRemainder);
		ShortRemainder.put(Float.class, (BiFunction<Short, Float, Object>) BinaryOperators::operatorRemainder);
		ShortRemainder.put(Double.class, (BiFunction<Short, Double, Object>) BinaryOperators::operatorRemainder);
		ShortRemainder.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Short.class, ShortRemainder);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerRemainder = new HashMap<>();
		IntegerRemainder.put(Double.class, (BiFunction<Integer, Double, Object>) BinaryOperators::operatorRemainder);
		IntegerRemainder.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorRemainder);
		IntegerRemainder.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorRemainder);
		IntegerRemainder.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorRemainder);
		IntegerRemainder.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorRemainder);
		IntegerRemainder.put(Float.class, (BiFunction<Integer, Float, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Integer.class, IntegerRemainder);
		Map<Class<?>, BiFunction<?, ?, Object>> LongRemainder = new HashMap<>();
		LongRemainder.put(Float.class, (BiFunction<Long, Float, Object>) BinaryOperators::operatorRemainder);
		LongRemainder.put(Double.class, (BiFunction<Long, Double, Object>) BinaryOperators::operatorRemainder);
		LongRemainder.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorRemainder);
		LongRemainder.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorRemainder);
		LongRemainder.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorRemainder);
		LongRemainder.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorRemainder);
		LongRemainder.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Long.class, LongRemainder);
		Map<Class<?>, BiFunction<?, ?, Object>> FloatRemainder = new HashMap<>();
		FloatRemainder.put(Double.class, (BiFunction<Float, Double, Object>) BinaryOperators::operatorRemainder);
		FloatRemainder.put(Float.class, (BiFunction<Float, Float, Object>) BinaryOperators::operatorRemainder);
		FloatRemainder.put(Long.class, (BiFunction<Float, Long, Object>) BinaryOperators::operatorRemainder);
		FloatRemainder.put(Character.class, (BiFunction<Float, Character, Object>) BinaryOperators::operatorRemainder);
		FloatRemainder.put(Byte.class, (BiFunction<Float, Byte, Object>) BinaryOperators::operatorRemainder);
		FloatRemainder.put(Short.class, (BiFunction<Float, Short, Object>) BinaryOperators::operatorRemainder);
		FloatRemainder.put(Integer.class, (BiFunction<Float, Integer, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Float.class, FloatRemainder);
		Map<Class<?>, BiFunction<?, ?, Object>> DoubleRemainder = new HashMap<>();
		DoubleRemainder.put(Byte.class, (BiFunction<Double, Byte, Object>) BinaryOperators::operatorRemainder);
		DoubleRemainder.put(Short.class, (BiFunction<Double, Short, Object>) BinaryOperators::operatorRemainder);
		DoubleRemainder.put(Integer.class, (BiFunction<Double, Integer, Object>) BinaryOperators::operatorRemainder);
		DoubleRemainder.put(Long.class, (BiFunction<Double, Long, Object>) BinaryOperators::operatorRemainder);
		DoubleRemainder.put(Float.class, (BiFunction<Double, Float, Object>) BinaryOperators::operatorRemainder);
		DoubleRemainder.put(Double.class, (BiFunction<Double, Double, Object>) BinaryOperators::operatorRemainder);
		DoubleRemainder.put(Character.class,
				(BiFunction<Double, Character, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Double.class, DoubleRemainder);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterRemainder = new HashMap<>();
		CharacterRemainder.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorRemainder);
		CharacterRemainder.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorRemainder);
		CharacterRemainder.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorRemainder);
		CharacterRemainder.put(Float.class, (BiFunction<Character, Float, Object>) BinaryOperators::operatorRemainder);
		CharacterRemainder.put(Double.class,
				(BiFunction<Character, Double, Object>) BinaryOperators::operatorRemainder);
		CharacterRemainder.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorRemainder);
		CharacterRemainder.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorRemainder);
		RemainderMap.put(Character.class, CharacterRemainder);
		BINARY_OPERATORS.put("REMAINDER", RemainderMap);
	}

	private static void initShiftLeft() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> ShiftLeftMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteShiftLeft = new HashMap<>();
		ByteShiftLeft.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorShiftLeft);
		ByteShiftLeft.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorShiftLeft);
		ByteShiftLeft.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorShiftLeft);
		ByteShiftLeft.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorShiftLeft);
		ByteShiftLeft.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorShiftLeft);
		ShiftLeftMap.put(Byte.class, ByteShiftLeft);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortShiftLeft = new HashMap<>();
		ShortShiftLeft.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorShiftLeft);
		ShortShiftLeft.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorShiftLeft);
		ShortShiftLeft.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorShiftLeft);
		ShortShiftLeft.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorShiftLeft);
		ShortShiftLeft.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorShiftLeft);
		ShiftLeftMap.put(Short.class, ShortShiftLeft);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerShiftLeft = new HashMap<>();
		IntegerShiftLeft.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorShiftLeft);
		IntegerShiftLeft.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorShiftLeft);
		IntegerShiftLeft.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorShiftLeft);
		IntegerShiftLeft.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorShiftLeft);
		IntegerShiftLeft.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorShiftLeft);
		ShiftLeftMap.put(Integer.class, IntegerShiftLeft);
		Map<Class<?>, BiFunction<?, ?, Object>> LongShiftLeft = new HashMap<>();
		LongShiftLeft.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorShiftLeft);
		LongShiftLeft.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorShiftLeft);
		LongShiftLeft.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorShiftLeft);
		LongShiftLeft.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorShiftLeft);
		LongShiftLeft.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorShiftLeft);
		ShiftLeftMap.put(Long.class, LongShiftLeft);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterShiftLeft = new HashMap<>();
		CharacterShiftLeft.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorShiftLeft);
		CharacterShiftLeft.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorShiftLeft);
		CharacterShiftLeft.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorShiftLeft);
		CharacterShiftLeft.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorShiftLeft);
		CharacterShiftLeft.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorShiftLeft);
		ShiftLeftMap.put(Character.class, CharacterShiftLeft);
		BINARY_OPERATORS.put("LEFT_SHIFT", ShiftLeftMap);
	}

	private static void initShiftRight() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> ShiftRightMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteShiftRight = new HashMap<>();
		ByteShiftRight.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorShiftRight);
		ByteShiftRight.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorShiftRight);
		ByteShiftRight.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorShiftRight);
		ByteShiftRight.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorShiftRight);
		ByteShiftRight.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorShiftRight);
		ShiftRightMap.put(Byte.class, ByteShiftRight);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortShiftRight = new HashMap<>();
		ShortShiftRight.put(Character.class,
				(BiFunction<Short, Character, Object>) BinaryOperators::operatorShiftRight);
		ShortShiftRight.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorShiftRight);
		ShortShiftRight.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorShiftRight);
		ShortShiftRight.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorShiftRight);
		ShortShiftRight.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorShiftRight);
		ShiftRightMap.put(Short.class, ShortShiftRight);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerShiftRight = new HashMap<>();
		IntegerShiftRight.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorShiftRight);
		IntegerShiftRight.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorShiftRight);
		IntegerShiftRight.put(Integer.class,
				(BiFunction<Integer, Integer, Object>) BinaryOperators::operatorShiftRight);
		IntegerShiftRight.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorShiftRight);
		IntegerShiftRight.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorShiftRight);
		ShiftRightMap.put(Integer.class, IntegerShiftRight);
		Map<Class<?>, BiFunction<?, ?, Object>> LongShiftRight = new HashMap<>();
		LongShiftRight.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorShiftRight);
		LongShiftRight.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorShiftRight);
		LongShiftRight.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorShiftRight);
		LongShiftRight.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorShiftRight);
		LongShiftRight.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorShiftRight);
		ShiftRightMap.put(Long.class, LongShiftRight);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterShiftRight = new HashMap<>();
		CharacterShiftRight.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorShiftRight);
		CharacterShiftRight.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorShiftRight);
		CharacterShiftRight.put(Short.class,
				(BiFunction<Character, Short, Object>) BinaryOperators::operatorShiftRight);
		CharacterShiftRight.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorShiftRight);
		CharacterShiftRight.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorShiftRight);
		ShiftRightMap.put(Character.class, CharacterShiftRight);
		BINARY_OPERATORS.put("RIGHT_SHIFT", ShiftRightMap);
	}

	private static void initShiftUnsignedRight() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> ShiftUnsignedRightMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> ByteShiftUnsignedRight = new HashMap<>();
		ByteShiftUnsignedRight.put(Long.class,
				(BiFunction<Byte, Long, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ByteShiftUnsignedRight.put(Character.class,
				(BiFunction<Byte, Character, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ByteShiftUnsignedRight.put(Integer.class,
				(BiFunction<Byte, Integer, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ByteShiftUnsignedRight.put(Short.class,
				(BiFunction<Byte, Short, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ByteShiftUnsignedRight.put(Byte.class,
				(BiFunction<Byte, Byte, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShiftUnsignedRightMap.put(Byte.class, ByteShiftUnsignedRight);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortShiftUnsignedRight = new HashMap<>();
		ShortShiftUnsignedRight.put(Character.class,
				(BiFunction<Short, Character, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShortShiftUnsignedRight.put(Long.class,
				(BiFunction<Short, Long, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShortShiftUnsignedRight.put(Byte.class,
				(BiFunction<Short, Byte, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShortShiftUnsignedRight.put(Short.class,
				(BiFunction<Short, Short, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShortShiftUnsignedRight.put(Integer.class,
				(BiFunction<Short, Integer, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShiftUnsignedRightMap.put(Short.class, ShortShiftUnsignedRight);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerShiftUnsignedRight = new HashMap<>();
		IntegerShiftUnsignedRight.put(Byte.class,
				(BiFunction<Integer, Byte, Object>) BinaryOperators::operatorShiftUnsignedRight);
		IntegerShiftUnsignedRight.put(Short.class,
				(BiFunction<Integer, Short, Object>) BinaryOperators::operatorShiftUnsignedRight);
		IntegerShiftUnsignedRight.put(Integer.class,
				(BiFunction<Integer, Integer, Object>) BinaryOperators::operatorShiftUnsignedRight);
		IntegerShiftUnsignedRight.put(Long.class,
				(BiFunction<Integer, Long, Object>) BinaryOperators::operatorShiftUnsignedRight);
		IntegerShiftUnsignedRight.put(Character.class,
				(BiFunction<Integer, Character, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShiftUnsignedRightMap.put(Integer.class, IntegerShiftUnsignedRight);
		Map<Class<?>, BiFunction<?, ?, Object>> LongShiftUnsignedRight = new HashMap<>();
		LongShiftUnsignedRight.put(Character.class,
				(BiFunction<Long, Character, Object>) BinaryOperators::operatorShiftUnsignedRight);
		LongShiftUnsignedRight.put(Byte.class,
				(BiFunction<Long, Byte, Object>) BinaryOperators::operatorShiftUnsignedRight);
		LongShiftUnsignedRight.put(Short.class,
				(BiFunction<Long, Short, Object>) BinaryOperators::operatorShiftUnsignedRight);
		LongShiftUnsignedRight.put(Integer.class,
				(BiFunction<Long, Integer, Object>) BinaryOperators::operatorShiftUnsignedRight);
		LongShiftUnsignedRight.put(Long.class,
				(BiFunction<Long, Long, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShiftUnsignedRightMap.put(Long.class, LongShiftUnsignedRight);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterShiftUnsignedRight = new HashMap<>();
		CharacterShiftUnsignedRight.put(Byte.class,
				(BiFunction<Character, Byte, Object>) BinaryOperators::operatorShiftUnsignedRight);
		CharacterShiftUnsignedRight.put(Short.class,
				(BiFunction<Character, Short, Object>) BinaryOperators::operatorShiftUnsignedRight);
		CharacterShiftUnsignedRight.put(Integer.class,
				(BiFunction<Character, Integer, Object>) BinaryOperators::operatorShiftUnsignedRight);
		CharacterShiftUnsignedRight.put(Long.class,
				(BiFunction<Character, Long, Object>) BinaryOperators::operatorShiftUnsignedRight);
		CharacterShiftUnsignedRight.put(Character.class,
				(BiFunction<Character, Character, Object>) BinaryOperators::operatorShiftUnsignedRight);
		ShiftUnsignedRightMap.put(Character.class, CharacterShiftUnsignedRight);
		BINARY_OPERATORS.put("UNSIGNED_RIGHT_SHIFT", ShiftUnsignedRightMap);
	}

	private static void initXor() {
		Map<Class<?>, Map<Class<?>, BiFunction<?, ?, Object>>> XorMap = new HashMap<>();
		Map<Class<?>, BiFunction<?, ?, Object>> BooleanXor = new HashMap<>();
		BooleanXor.put(Boolean.class, (BiFunction<Boolean, Boolean, Object>) BinaryOperators::operatorXor);
		XorMap.put(Boolean.class, BooleanXor);
		Map<Class<?>, BiFunction<?, ?, Object>> ByteXor = new HashMap<>();
		ByteXor.put(Short.class, (BiFunction<Byte, Short, Object>) BinaryOperators::operatorXor);
		ByteXor.put(Integer.class, (BiFunction<Byte, Integer, Object>) BinaryOperators::operatorXor);
		ByteXor.put(Long.class, (BiFunction<Byte, Long, Object>) BinaryOperators::operatorXor);
		ByteXor.put(Character.class, (BiFunction<Byte, Character, Object>) BinaryOperators::operatorXor);
		ByteXor.put(Byte.class, (BiFunction<Byte, Byte, Object>) BinaryOperators::operatorXor);
		XorMap.put(Byte.class, ByteXor);
		Map<Class<?>, BiFunction<?, ?, Object>> ShortXor = new HashMap<>();
		ShortXor.put(Character.class, (BiFunction<Short, Character, Object>) BinaryOperators::operatorXor);
		ShortXor.put(Long.class, (BiFunction<Short, Long, Object>) BinaryOperators::operatorXor);
		ShortXor.put(Integer.class, (BiFunction<Short, Integer, Object>) BinaryOperators::operatorXor);
		ShortXor.put(Short.class, (BiFunction<Short, Short, Object>) BinaryOperators::operatorXor);
		ShortXor.put(Byte.class, (BiFunction<Short, Byte, Object>) BinaryOperators::operatorXor);
		XorMap.put(Short.class, ShortXor);
		Map<Class<?>, BiFunction<?, ?, Object>> IntegerXor = new HashMap<>();
		IntegerXor.put(Character.class, (BiFunction<Integer, Character, Object>) BinaryOperators::operatorXor);
		IntegerXor.put(Long.class, (BiFunction<Integer, Long, Object>) BinaryOperators::operatorXor);
		IntegerXor.put(Integer.class, (BiFunction<Integer, Integer, Object>) BinaryOperators::operatorXor);
		IntegerXor.put(Short.class, (BiFunction<Integer, Short, Object>) BinaryOperators::operatorXor);
		IntegerXor.put(Byte.class, (BiFunction<Integer, Byte, Object>) BinaryOperators::operatorXor);
		XorMap.put(Integer.class, IntegerXor);
		Map<Class<?>, BiFunction<?, ?, Object>> LongXor = new HashMap<>();
		LongXor.put(Byte.class, (BiFunction<Long, Byte, Object>) BinaryOperators::operatorXor);
		LongXor.put(Integer.class, (BiFunction<Long, Integer, Object>) BinaryOperators::operatorXor);
		LongXor.put(Short.class, (BiFunction<Long, Short, Object>) BinaryOperators::operatorXor);
		LongXor.put(Character.class, (BiFunction<Long, Character, Object>) BinaryOperators::operatorXor);
		LongXor.put(Long.class, (BiFunction<Long, Long, Object>) BinaryOperators::operatorXor);
		XorMap.put(Long.class, LongXor);
		Map<Class<?>, BiFunction<?, ?, Object>> CharacterXor = new HashMap<>();
		CharacterXor.put(Integer.class, (BiFunction<Character, Integer, Object>) BinaryOperators::operatorXor);
		CharacterXor.put(Long.class, (BiFunction<Character, Long, Object>) BinaryOperators::operatorXor);
		CharacterXor.put(Short.class, (BiFunction<Character, Short, Object>) BinaryOperators::operatorXor);
		CharacterXor.put(Byte.class, (BiFunction<Character, Byte, Object>) BinaryOperators::operatorXor);
		CharacterXor.put(Character.class, (BiFunction<Character, Character, Object>) BinaryOperators::operatorXor);
		XorMap.put(Character.class, CharacterXor);
		BINARY_OPERATORS.put("XOR", XorMap);
	}

	public static Object operatorAnd(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) & ((boolean) r);
	}

	public static Object operatorAnd(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) & ((byte) r);
	}

	public static Object operatorAnd(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) & ((short) r);
	}

	public static Object operatorAnd(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) & ((int) r);
	}

	public static Object operatorAnd(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) & ((long) r);
	}

	public static Object operatorAnd(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) & ((char) r);
	}

	public static Object operatorAnd(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) & ((byte) r);
	}

	public static Object operatorAnd(java.lang.Short l, java.lang.Short r) {
		return ((short) l) & ((short) r);
	}

	public static Object operatorAnd(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) & ((int) r);
	}

	public static Object operatorAnd(java.lang.Short l, java.lang.Long r) {
		return ((short) l) & ((long) r);
	}

	public static Object operatorAnd(java.lang.Short l, java.lang.Character r) {
		return ((short) l) & ((char) r);
	}

	public static Object operatorAnd(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) & ((byte) r);
	}

	public static Object operatorAnd(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) & ((short) r);
	}

	public static Object operatorAnd(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) & ((int) r);
	}

	public static Object operatorAnd(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) & ((long) r);
	}

	public static Object operatorAnd(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) & ((char) r);
	}

	public static Object operatorAnd(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) & ((byte) r);
	}

	public static Object operatorAnd(java.lang.Long l, java.lang.Short r) {
		return ((long) l) & ((short) r);
	}

	public static Object operatorAnd(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) & ((int) r);
	}

	public static Object operatorAnd(java.lang.Long l, java.lang.Long r) {
		return ((long) l) & ((long) r);
	}

	public static Object operatorAnd(java.lang.Long l, java.lang.Character r) {
		return ((long) l) & ((char) r);
	}

	public static Object operatorAnd(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) & ((byte) r);
	}

	public static Object operatorAnd(java.lang.Character l, java.lang.Short r) {
		return ((char) l) & ((short) r);
	}

	public static Object operatorAnd(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) & ((int) r);
	}

	public static Object operatorAnd(java.lang.Character l, java.lang.Long r) {
		return ((char) l) & ((long) r);
	}

	public static Object operatorAnd(java.lang.Character l, java.lang.Character r) {
		return ((char) l) & ((char) r);
	}

	public static Object operatorConditionalAnd(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) && ((boolean) r);
	}

	public static Object operatorConditionalOr(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) || ((boolean) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) / ((char) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Short r) {
		return ((short) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Long r) {
		return ((short) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Float r) {
		return ((short) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Double r) {
		return ((short) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Short l, java.lang.Character r) {
		return ((short) l) / ((char) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) / ((char) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Short r) {
		return ((long) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Long r) {
		return ((long) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Float r) {
		return ((long) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Double r) {
		return ((long) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Long l, java.lang.Character r) {
		return ((long) l) / ((char) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Short r) {
		return ((float) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Long r) {
		return ((float) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Float r) {
		return ((float) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Double r) {
		return ((float) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Float l, java.lang.Character r) {
		return ((float) l) / ((char) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Short r) {
		return ((double) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Long r) {
		return ((double) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Float r) {
		return ((double) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Double r) {
		return ((double) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Double l, java.lang.Character r) {
		return ((double) l) / ((char) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) / ((byte) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Short r) {
		return ((char) l) / ((short) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) / ((int) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Long r) {
		return ((char) l) / ((long) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Float r) {
		return ((char) l) / ((float) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Double r) {
		return ((char) l) / ((double) r);
	}

	public static Object operatorDivide(java.lang.Character l, java.lang.Character r) {
		return ((char) l) / ((char) r);
	}

	public static Object operatorEqual(java.lang.Object l, java.lang.Object r) {
		return l == r;
	}

	public static Object operatorEqual(java.lang.Object l, java.lang.String r) {
		return l == r;
	}

	public static Object operatorEqual(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) == ((boolean) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) == ((char) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Short r) {
		return ((short) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Long r) {
		return ((short) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Float r) {
		return ((short) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Double r) {
		return ((short) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Short l, java.lang.Character r) {
		return ((short) l) == ((char) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) == ((char) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Short r) {
		return ((long) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Long r) {
		return ((long) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Float r) {
		return ((long) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Double r) {
		return ((long) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Long l, java.lang.Character r) {
		return ((long) l) == ((char) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Short r) {
		return ((float) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Long r) {
		return ((float) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Float r) {
		return ((float) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Double r) {
		return ((float) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Float l, java.lang.Character r) {
		return ((float) l) == ((char) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Short r) {
		return ((double) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Long r) {
		return ((double) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Float r) {
		return ((double) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Double r) {
		return ((double) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Double l, java.lang.Character r) {
		return ((double) l) == ((char) r);
	}

	public static Object operatorEqual(java.lang.String l, java.lang.Object r) {
		return l == r;
	}

	public static Object operatorEqual(java.lang.String l, java.lang.String r) {
		return l == r;
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) == ((byte) r);
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Short r) {
		return ((char) l) == ((short) r);
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) == ((int) r);
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Long r) {
		return ((char) l) == ((long) r);
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Float r) {
		return ((char) l) == ((float) r);
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Double r) {
		return ((char) l) == ((double) r);
	}

	public static Object operatorEqual(java.lang.Character l, java.lang.Character r) {
		return ((char) l) == ((char) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) > ((char) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Short r) {
		return ((short) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Long r) {
		return ((short) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Float r) {
		return ((short) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Double r) {
		return ((short) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Short l, java.lang.Character r) {
		return ((short) l) > ((char) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) > ((char) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Short r) {
		return ((long) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Long r) {
		return ((long) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Float r) {
		return ((long) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Double r) {
		return ((long) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Long l, java.lang.Character r) {
		return ((long) l) > ((char) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Short r) {
		return ((float) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Long r) {
		return ((float) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Float r) {
		return ((float) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Double r) {
		return ((float) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Float l, java.lang.Character r) {
		return ((float) l) > ((char) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Short r) {
		return ((double) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Long r) {
		return ((double) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Float r) {
		return ((double) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Double r) {
		return ((double) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Double l, java.lang.Character r) {
		return ((double) l) > ((char) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) > ((byte) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Short r) {
		return ((char) l) > ((short) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) > ((int) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Long r) {
		return ((char) l) > ((long) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Float r) {
		return ((char) l) > ((float) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Double r) {
		return ((char) l) > ((double) r);
	}

	public static Object operatorGreater(java.lang.Character l, java.lang.Character r) {
		return ((char) l) > ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) >= ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Short r) {
		return ((short) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Long r) {
		return ((short) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Float r) {
		return ((short) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Double r) {
		return ((short) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Short l, java.lang.Character r) {
		return ((short) l) >= ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) >= ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Short r) {
		return ((long) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Long r) {
		return ((long) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Float r) {
		return ((long) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Double r) {
		return ((long) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Long l, java.lang.Character r) {
		return ((long) l) >= ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Short r) {
		return ((float) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Long r) {
		return ((float) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Float r) {
		return ((float) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Double r) {
		return ((float) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Float l, java.lang.Character r) {
		return ((float) l) >= ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Short r) {
		return ((double) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Long r) {
		return ((double) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Float r) {
		return ((double) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Double r) {
		return ((double) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Double l, java.lang.Character r) {
		return ((double) l) >= ((char) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) >= ((byte) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Short r) {
		return ((char) l) >= ((short) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) >= ((int) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Long r) {
		return ((char) l) >= ((long) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Float r) {
		return ((char) l) >= ((float) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Double r) {
		return ((char) l) >= ((double) r);
	}

	public static Object operatorGreaterEqual(java.lang.Character l, java.lang.Character r) {
		return ((char) l) >= ((char) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) < ((char) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Short r) {
		return ((short) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Long r) {
		return ((short) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Float r) {
		return ((short) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Double r) {
		return ((short) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Short l, java.lang.Character r) {
		return ((short) l) < ((char) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) < ((char) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Short r) {
		return ((long) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Long r) {
		return ((long) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Float r) {
		return ((long) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Double r) {
		return ((long) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Long l, java.lang.Character r) {
		return ((long) l) < ((char) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Short r) {
		return ((float) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Long r) {
		return ((float) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Float r) {
		return ((float) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Double r) {
		return ((float) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Float l, java.lang.Character r) {
		return ((float) l) < ((char) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Short r) {
		return ((double) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Long r) {
		return ((double) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Float r) {
		return ((double) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Double r) {
		return ((double) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Double l, java.lang.Character r) {
		return ((double) l) < ((char) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) < ((byte) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Short r) {
		return ((char) l) < ((short) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) < ((int) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Long r) {
		return ((char) l) < ((long) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Float r) {
		return ((char) l) < ((float) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Double r) {
		return ((char) l) < ((double) r);
	}

	public static Object operatorLess(java.lang.Character l, java.lang.Character r) {
		return ((char) l) < ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) <= ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Short r) {
		return ((short) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Long r) {
		return ((short) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Float r) {
		return ((short) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Double r) {
		return ((short) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Short l, java.lang.Character r) {
		return ((short) l) <= ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) <= ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Short r) {
		return ((long) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Long r) {
		return ((long) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Float r) {
		return ((long) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Double r) {
		return ((long) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Long l, java.lang.Character r) {
		return ((long) l) <= ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Short r) {
		return ((float) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Long r) {
		return ((float) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Float r) {
		return ((float) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Double r) {
		return ((float) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Float l, java.lang.Character r) {
		return ((float) l) <= ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Short r) {
		return ((double) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Long r) {
		return ((double) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Float r) {
		return ((double) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Double r) {
		return ((double) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Double l, java.lang.Character r) {
		return ((double) l) <= ((char) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) <= ((byte) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Short r) {
		return ((char) l) <= ((short) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) <= ((int) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Long r) {
		return ((char) l) <= ((long) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Float r) {
		return ((char) l) <= ((float) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Double r) {
		return ((char) l) <= ((double) r);
	}

	public static Object operatorLessEqual(java.lang.Character l, java.lang.Character r) {
		return ((char) l) <= ((char) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) - ((char) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Short r) {
		return ((short) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Long r) {
		return ((short) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Float r) {
		return ((short) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Double r) {
		return ((short) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Short l, java.lang.Character r) {
		return ((short) l) - ((char) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) - ((char) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Short r) {
		return ((long) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Long r) {
		return ((long) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Float r) {
		return ((long) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Double r) {
		return ((long) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Long l, java.lang.Character r) {
		return ((long) l) - ((char) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Short r) {
		return ((float) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Long r) {
		return ((float) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Float r) {
		return ((float) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Double r) {
		return ((float) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Float l, java.lang.Character r) {
		return ((float) l) - ((char) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Short r) {
		return ((double) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Long r) {
		return ((double) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Float r) {
		return ((double) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Double r) {
		return ((double) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Double l, java.lang.Character r) {
		return ((double) l) - ((char) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) - ((byte) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Short r) {
		return ((char) l) - ((short) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) - ((int) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Long r) {
		return ((char) l) - ((long) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Float r) {
		return ((char) l) - ((float) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Double r) {
		return ((char) l) - ((double) r);
	}

	public static Object operatorMinus(java.lang.Character l, java.lang.Character r) {
		return ((char) l) - ((char) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) * ((char) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Short r) {
		return ((short) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Long r) {
		return ((short) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Float r) {
		return ((short) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Double r) {
		return ((short) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Short l, java.lang.Character r) {
		return ((short) l) * ((char) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) * ((char) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Short r) {
		return ((long) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Long r) {
		return ((long) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Float r) {
		return ((long) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Double r) {
		return ((long) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Long l, java.lang.Character r) {
		return ((long) l) * ((char) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Short r) {
		return ((float) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Long r) {
		return ((float) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Float r) {
		return ((float) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Double r) {
		return ((float) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Float l, java.lang.Character r) {
		return ((float) l) * ((char) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Short r) {
		return ((double) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Long r) {
		return ((double) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Float r) {
		return ((double) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Double r) {
		return ((double) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Double l, java.lang.Character r) {
		return ((double) l) * ((char) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) * ((byte) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Short r) {
		return ((char) l) * ((short) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) * ((int) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Long r) {
		return ((char) l) * ((long) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Float r) {
		return ((char) l) * ((float) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Double r) {
		return ((char) l) * ((double) r);
	}

	public static Object operatorMultiply(java.lang.Character l, java.lang.Character r) {
		return ((char) l) * ((char) r);
	}

	public static Object operatorNotEqual(java.lang.Object l, java.lang.Object r) {
		return l != r;
	}

	public static Object operatorNotEqual(java.lang.Object l, java.lang.String r) {
		return l != r;
	}

	public static Object operatorNotEqual(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) != ((boolean) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) != ((char) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Short r) {
		return ((short) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Long r) {
		return ((short) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Float r) {
		return ((short) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Double r) {
		return ((short) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Short l, java.lang.Character r) {
		return ((short) l) != ((char) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) != ((char) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Short r) {
		return ((long) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Long r) {
		return ((long) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Float r) {
		return ((long) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Double r) {
		return ((long) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Long l, java.lang.Character r) {
		return ((long) l) != ((char) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Short r) {
		return ((float) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Long r) {
		return ((float) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Float r) {
		return ((float) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Double r) {
		return ((float) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Float l, java.lang.Character r) {
		return ((float) l) != ((char) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Short r) {
		return ((double) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Long r) {
		return ((double) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Float r) {
		return ((double) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Double r) {
		return ((double) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Double l, java.lang.Character r) {
		return ((double) l) != ((char) r);
	}

	public static Object operatorNotEqual(java.lang.String l, java.lang.Object r) {
		return l != r;
	}

	public static Object operatorNotEqual(java.lang.String l, java.lang.String r) {
		return l != r;
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) != ((byte) r);
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Short r) {
		return ((char) l) != ((short) r);
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) != ((int) r);
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Long r) {
		return ((char) l) != ((long) r);
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Float r) {
		return ((char) l) != ((float) r);
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Double r) {
		return ((char) l) != ((double) r);
	}

	public static Object operatorNotEqual(java.lang.Character l, java.lang.Character r) {
		return ((char) l) != ((char) r);
	}

	public static Object operatorOr(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) | ((boolean) r);
	}

	public static Object operatorOr(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) | ((byte) r);
	}

	public static Object operatorOr(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) | ((short) r);
	}

	public static Object operatorOr(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) | ((int) r);
	}

	public static Object operatorOr(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) | ((long) r);
	}

	public static Object operatorOr(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) | ((char) r);
	}

	public static Object operatorOr(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) | ((byte) r);
	}

	public static Object operatorOr(java.lang.Short l, java.lang.Short r) {
		return ((short) l) | ((short) r);
	}

	public static Object operatorOr(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) | ((int) r);
	}

	public static Object operatorOr(java.lang.Short l, java.lang.Long r) {
		return ((short) l) | ((long) r);
	}

	public static Object operatorOr(java.lang.Short l, java.lang.Character r) {
		return ((short) l) | ((char) r);
	}

	public static Object operatorOr(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) | ((byte) r);
	}

	public static Object operatorOr(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) | ((short) r);
	}

	public static Object operatorOr(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) | ((int) r);
	}

	public static Object operatorOr(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) | ((long) r);
	}

	public static Object operatorOr(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) | ((char) r);
	}

	public static Object operatorOr(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) | ((byte) r);
	}

	public static Object operatorOr(java.lang.Long l, java.lang.Short r) {
		return ((long) l) | ((short) r);
	}

	public static Object operatorOr(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) | ((int) r);
	}

	public static Object operatorOr(java.lang.Long l, java.lang.Long r) {
		return ((long) l) | ((long) r);
	}

	public static Object operatorOr(java.lang.Long l, java.lang.Character r) {
		return ((long) l) | ((char) r);
	}

	public static Object operatorOr(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) | ((byte) r);
	}

	public static Object operatorOr(java.lang.Character l, java.lang.Short r) {
		return ((char) l) | ((short) r);
	}

	public static Object operatorOr(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) | ((int) r);
	}

	public static Object operatorOr(java.lang.Character l, java.lang.Long r) {
		return ((char) l) | ((long) r);
	}

	public static Object operatorOr(java.lang.Character l, java.lang.Character r) {
		return ((char) l) | ((char) r);
	}

	public static Object operatorPlus(java.lang.Object l, java.lang.String r) {
		return l + r;
	}

	public static Object operatorPlus(java.lang.Boolean l, java.lang.String r) {
		return ((boolean) l) + r;
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.String r) {
		return ((byte) l) + r;
	}

	public static Object operatorPlus(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) + ((char) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Short r) {
		return ((short) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Long r) {
		return ((short) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Float r) {
		return ((short) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Double r) {
		return ((short) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.String r) {
		return ((short) l) + r;
	}

	public static Object operatorPlus(java.lang.Short l, java.lang.Character r) {
		return ((short) l) + ((char) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.String r) {
		return ((int) l) + r;
	}

	public static Object operatorPlus(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) + ((char) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Short r) {
		return ((long) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Long r) {
		return ((long) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Float r) {
		return ((long) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Double r) {
		return ((long) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.String r) {
		return ((long) l) + r;
	}

	public static Object operatorPlus(java.lang.Long l, java.lang.Character r) {
		return ((long) l) + ((char) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Short r) {
		return ((float) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Long r) {
		return ((float) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Float r) {
		return ((float) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Double r) {
		return ((float) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.String r) {
		return ((float) l) + r;
	}

	public static Object operatorPlus(java.lang.Float l, java.lang.Character r) {
		return ((float) l) + ((char) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Short r) {
		return ((double) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Long r) {
		return ((double) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Float r) {
		return ((double) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Double r) {
		return ((double) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.String r) {
		return ((double) l) + r;
	}

	public static Object operatorPlus(java.lang.Double l, java.lang.Character r) {
		return ((double) l) + ((char) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Object r) {
		return l + r;
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Boolean r) {
		return l + ((boolean) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Byte r) {
		return l + ((byte) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Short r) {
		return l + ((short) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Integer r) {
		return l + ((int) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Long r) {
		return l + ((long) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Float r) {
		return l + ((float) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Double r) {
		return l + ((double) r);
	}

	public static Object operatorPlus(java.lang.String l, java.lang.String r) {
		return l + r;
	}

	public static Object operatorPlus(java.lang.String l, java.lang.Character r) {
		return l + ((char) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) + ((byte) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Short r) {
		return ((char) l) + ((short) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) + ((int) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Long r) {
		return ((char) l) + ((long) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Float r) {
		return ((char) l) + ((float) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Double r) {
		return ((char) l) + ((double) r);
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.String r) {
		return ((char) l) + r;
	}

	public static Object operatorPlus(java.lang.Character l, java.lang.Character r) {
		return ((char) l) + ((char) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) % ((byte) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Float r) {
		return ((byte) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Double r) {
		return ((byte) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) % ((char) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) % ((byte) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Short r) {
		return ((short) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Long r) {
		return ((short) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Float r) {
		return ((short) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Double r) {
		return ((short) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Short l, java.lang.Character r) {
		return ((short) l) % ((char) r);
	}

	public static Object operatorRemainder(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Integer l, java.lang.Float r) {
		return ((int) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Integer l, java.lang.Double r) {
		return ((int) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) % ((char) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) % ((byte) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Short r) {
		return ((long) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Long r) {
		return ((long) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Float r) {
		return ((long) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Double r) {
		return ((long) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Long l, java.lang.Character r) {
		return ((long) l) % ((char) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Byte r) {
		return ((float) l) % ((byte) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Short r) {
		return ((float) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Integer r) {
		return ((float) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Long r) {
		return ((float) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Float r) {
		return ((float) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Double r) {
		return ((float) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Float l, java.lang.Character r) {
		return ((float) l) % ((char) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Byte r) {
		return ((double) l) % ((byte) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Short r) {
		return ((double) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Integer r) {
		return ((double) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Long r) {
		return ((double) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Float r) {
		return ((double) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Double r) {
		return ((double) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Double l, java.lang.Character r) {
		return ((double) l) % ((char) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) % ((byte) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Short r) {
		return ((char) l) % ((short) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) % ((int) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Long r) {
		return ((char) l) % ((long) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Float r) {
		return ((char) l) % ((float) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Double r) {
		return ((char) l) % ((double) r);
	}

	public static Object operatorRemainder(java.lang.Character l, java.lang.Character r) {
		return ((char) l) % ((char) r);
	}

	public static Object operatorShiftLeft(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) << ((byte) r);
	}

	public static Object operatorShiftLeft(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) << ((short) r);
	}

	public static Object operatorShiftLeft(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) << ((int) r);
	}

	public static Object operatorShiftLeft(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) << ((long) r);
	}

	public static Object operatorShiftLeft(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) << ((char) r);
	}

	public static Object operatorShiftLeft(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) << ((byte) r);
	}

	public static Object operatorShiftLeft(java.lang.Short l, java.lang.Short r) {
		return ((short) l) << ((short) r);
	}

	public static Object operatorShiftLeft(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) << ((int) r);
	}

	public static Object operatorShiftLeft(java.lang.Short l, java.lang.Long r) {
		return ((short) l) << ((long) r);
	}

	public static Object operatorShiftLeft(java.lang.Short l, java.lang.Character r) {
		return ((short) l) << ((char) r);
	}

	public static Object operatorShiftLeft(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) << ((byte) r);
	}

	public static Object operatorShiftLeft(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) << ((short) r);
	}

	public static Object operatorShiftLeft(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) << ((int) r);
	}

	public static Object operatorShiftLeft(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) << ((long) r);
	}

	public static Object operatorShiftLeft(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) << ((char) r);
	}

	public static Object operatorShiftLeft(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) << ((byte) r);
	}

	public static Object operatorShiftLeft(java.lang.Long l, java.lang.Short r) {
		return ((long) l) << ((short) r);
	}

	public static Object operatorShiftLeft(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) << ((int) r);
	}

	public static Object operatorShiftLeft(java.lang.Long l, java.lang.Long r) {
		return ((long) l) << ((long) r);
	}

	public static Object operatorShiftLeft(java.lang.Long l, java.lang.Character r) {
		return ((long) l) << ((char) r);
	}

	public static Object operatorShiftLeft(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) << ((byte) r);
	}

	public static Object operatorShiftLeft(java.lang.Character l, java.lang.Short r) {
		return ((char) l) << ((short) r);
	}

	public static Object operatorShiftLeft(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) << ((int) r);
	}

	public static Object operatorShiftLeft(java.lang.Character l, java.lang.Long r) {
		return ((char) l) << ((long) r);
	}

	public static Object operatorShiftLeft(java.lang.Character l, java.lang.Character r) {
		return ((char) l) << ((char) r);
	}

	public static Object operatorShiftRight(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) >> ((byte) r);
	}

	public static Object operatorShiftRight(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) >> ((short) r);
	}

	public static Object operatorShiftRight(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) >> ((int) r);
	}

	public static Object operatorShiftRight(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) >> ((long) r);
	}

	public static Object operatorShiftRight(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) >> ((char) r);
	}

	public static Object operatorShiftRight(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) >> ((byte) r);
	}

	public static Object operatorShiftRight(java.lang.Short l, java.lang.Short r) {
		return ((short) l) >> ((short) r);
	}

	public static Object operatorShiftRight(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) >> ((int) r);
	}

	public static Object operatorShiftRight(java.lang.Short l, java.lang.Long r) {
		return ((short) l) >> ((long) r);
	}

	public static Object operatorShiftRight(java.lang.Short l, java.lang.Character r) {
		return ((short) l) >> ((char) r);
	}

	public static Object operatorShiftRight(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) >> ((byte) r);
	}

	public static Object operatorShiftRight(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) >> ((short) r);
	}

	public static Object operatorShiftRight(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) >> ((int) r);
	}

	public static Object operatorShiftRight(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) >> ((long) r);
	}

	public static Object operatorShiftRight(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) >> ((char) r);
	}

	public static Object operatorShiftRight(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) >> ((byte) r);
	}

	public static Object operatorShiftRight(java.lang.Long l, java.lang.Short r) {
		return ((long) l) >> ((short) r);
	}

	public static Object operatorShiftRight(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) >> ((int) r);
	}

	public static Object operatorShiftRight(java.lang.Long l, java.lang.Long r) {
		return ((long) l) >> ((long) r);
	}

	public static Object operatorShiftRight(java.lang.Long l, java.lang.Character r) {
		return ((long) l) >> ((char) r);
	}

	public static Object operatorShiftRight(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) >> ((byte) r);
	}

	public static Object operatorShiftRight(java.lang.Character l, java.lang.Short r) {
		return ((char) l) >> ((short) r);
	}

	public static Object operatorShiftRight(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) >> ((int) r);
	}

	public static Object operatorShiftRight(java.lang.Character l, java.lang.Long r) {
		return ((char) l) >> ((long) r);
	}

	public static Object operatorShiftRight(java.lang.Character l, java.lang.Character r) {
		return ((char) l) >> ((char) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) >>> ((byte) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) >>> ((short) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) >>> ((int) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) >>> ((long) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) >>> ((char) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) >>> ((byte) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Short l, java.lang.Short r) {
		return ((short) l) >>> ((short) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) >>> ((int) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Short l, java.lang.Long r) {
		return ((short) l) >>> ((long) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Short l, java.lang.Character r) {
		return ((short) l) >>> ((char) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) >>> ((byte) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) >>> ((short) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) >>> ((int) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) >>> ((long) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) >>> ((char) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) >>> ((byte) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Long l, java.lang.Short r) {
		return ((long) l) >>> ((short) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) >>> ((int) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Long l, java.lang.Long r) {
		return ((long) l) >>> ((long) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Long l, java.lang.Character r) {
		return ((long) l) >>> ((char) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) >>> ((byte) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Character l, java.lang.Short r) {
		return ((char) l) >>> ((short) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) >>> ((int) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Character l, java.lang.Long r) {
		return ((char) l) >>> ((long) r);
	}

	public static Object operatorShiftUnsignedRight(java.lang.Character l, java.lang.Character r) {
		return ((char) l) >>> ((char) r);
	}

	public static Object operatorXor(java.lang.Boolean l, java.lang.Boolean r) {
		return ((boolean) l) ^ ((boolean) r);
	}

	public static Object operatorXor(java.lang.Byte l, java.lang.Byte r) {
		return ((byte) l) ^ ((byte) r);
	}

	public static Object operatorXor(java.lang.Byte l, java.lang.Short r) {
		return ((byte) l) ^ ((short) r);
	}

	public static Object operatorXor(java.lang.Byte l, java.lang.Integer r) {
		return ((byte) l) ^ ((int) r);
	}

	public static Object operatorXor(java.lang.Byte l, java.lang.Long r) {
		return ((byte) l) ^ ((long) r);
	}

	public static Object operatorXor(java.lang.Byte l, java.lang.Character r) {
		return ((byte) l) ^ ((char) r);
	}

	public static Object operatorXor(java.lang.Short l, java.lang.Byte r) {
		return ((short) l) ^ ((byte) r);
	}

	public static Object operatorXor(java.lang.Short l, java.lang.Short r) {
		return ((short) l) ^ ((short) r);
	}

	public static Object operatorXor(java.lang.Short l, java.lang.Integer r) {
		return ((short) l) ^ ((int) r);
	}

	public static Object operatorXor(java.lang.Short l, java.lang.Long r) {
		return ((short) l) ^ ((long) r);
	}

	public static Object operatorXor(java.lang.Short l, java.lang.Character r) {
		return ((short) l) ^ ((char) r);
	}

	public static Object operatorXor(java.lang.Integer l, java.lang.Byte r) {
		return ((int) l) ^ ((byte) r);
	}

	public static Object operatorXor(java.lang.Integer l, java.lang.Short r) {
		return ((int) l) ^ ((short) r);
	}

	public static Object operatorXor(java.lang.Integer l, java.lang.Integer r) {
		return ((int) l) ^ ((int) r);
	}

	public static Object operatorXor(java.lang.Integer l, java.lang.Long r) {
		return ((int) l) ^ ((long) r);
	}

	public static Object operatorXor(java.lang.Integer l, java.lang.Character r) {
		return ((int) l) ^ ((char) r);
	}

	public static Object operatorXor(java.lang.Long l, java.lang.Byte r) {
		return ((long) l) ^ ((byte) r);
	}

	public static Object operatorXor(java.lang.Long l, java.lang.Short r) {
		return ((long) l) ^ ((short) r);
	}

	public static Object operatorXor(java.lang.Long l, java.lang.Integer r) {
		return ((long) l) ^ ((int) r);
	}

	public static Object operatorXor(java.lang.Long l, java.lang.Long r) {
		return ((long) l) ^ ((long) r);
	}

	public static Object operatorXor(java.lang.Long l, java.lang.Character r) {
		return ((long) l) ^ ((char) r);
	}

	public static Object operatorXor(java.lang.Character l, java.lang.Byte r) {
		return ((char) l) ^ ((byte) r);
	}

	public static Object operatorXor(java.lang.Character l, java.lang.Short r) {
		return ((char) l) ^ ((short) r);
	}

	public static Object operatorXor(java.lang.Character l, java.lang.Integer r) {
		return ((char) l) ^ ((int) r);
	}

	public static Object operatorXor(java.lang.Character l, java.lang.Long r) {
		return ((char) l) ^ ((long) r);
	}

	public static Object operatorXor(java.lang.Character l, java.lang.Character r) {
		return ((char) l) ^ ((char) r);
	}
}
