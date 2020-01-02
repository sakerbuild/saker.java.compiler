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
package saker.java.compiler.impl.util.operators;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("cast")
public class CastOperators {
	private static final Map<Class<?>, Map<Class<?>, Function<?, Object>>> CAST_OPERATORS = new HashMap<>();
	static {
		Map<Class<?>, Function<?, Object>> ObjectMap = new HashMap<>();
		ObjectMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Boolean.class, (Function<Boolean, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(String.class, (Function<String, Object>) CastOperators::operatorCastObject);
		ObjectMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastObject);
		CAST_OPERATORS.put(Object.class, ObjectMap);

		Map<Class<?>, Function<?, Object>> BooleanMap = new HashMap<>();
		BooleanMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastBoolean);
		BooleanMap.put(Boolean.class, (Function<Boolean, Object>) CastOperators::operatorCastBoolean);
		CAST_OPERATORS.put(Boolean.class, BooleanMap);

		Map<Class<?>, Function<?, Object>> ByteMap = new HashMap<>();
		ByteMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastByte);
		ByteMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastByte);
		CAST_OPERATORS.put(Byte.class, ByteMap);

		Map<Class<?>, Function<?, Object>> ShortMap = new HashMap<>();
		ShortMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastShort);
		ShortMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastShort);
		CAST_OPERATORS.put(Short.class, ShortMap);

		Map<Class<?>, Function<?, Object>> IntegerMap = new HashMap<>();
		IntegerMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastInteger);
		IntegerMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastInteger);
		CAST_OPERATORS.put(Integer.class, IntegerMap);

		Map<Class<?>, Function<?, Object>> LongMap = new HashMap<>();
		LongMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastLong);
		LongMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastLong);
		LongMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastLong);
		LongMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastLong);
		LongMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastLong);
		LongMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastLong);
		LongMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastLong);
		LongMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastLong);
		CAST_OPERATORS.put(Long.class, LongMap);

		Map<Class<?>, Function<?, Object>> FloatMap = new HashMap<>();
		FloatMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastFloat);
		FloatMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastFloat);
		CAST_OPERATORS.put(Float.class, FloatMap);

		Map<Class<?>, Function<?, Object>> DoubleMap = new HashMap<>();
		DoubleMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastDouble);
		DoubleMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastDouble);
		CAST_OPERATORS.put(Double.class, DoubleMap);

		Map<Class<?>, Function<?, Object>> StringMap = new HashMap<>();
		StringMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastString);
		StringMap.put(String.class, (Function<String, Object>) CastOperators::operatorCastString);
		CAST_OPERATORS.put(String.class, StringMap);

		Map<Class<?>, Function<?, Object>> CharacterMap = new HashMap<>();
		CharacterMap.put(Object.class, (Function<Object, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Byte.class, (Function<Byte, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Short.class, (Function<Short, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Integer.class, (Function<Integer, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Long.class, (Function<Long, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Float.class, (Function<Float, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Double.class, (Function<Double, Object>) CastOperators::operatorCastCharacter);
		CharacterMap.put(Character.class, (Function<Character, Object>) CastOperators::operatorCastCharacter);
		CAST_OPERATORS.put(Character.class, CharacterMap);
	}

	@SuppressWarnings("unchecked")
	public static <ARG, T> Function<ARG, T> getOperatorFunctionNoObject(Class<ARG> argumentclass, Class<T> casttarget) {
		Map<Class<?>, Function<?, Object>> opmap = CAST_OPERATORS.get(casttarget);
		if (opmap == null) {
			return null;
		}
		return (Function<ARG, T>) opmap.get(argumentclass);
	}

	@SuppressWarnings("unchecked")
	public static <ARG, T> Function<ARG, T> getOperatorFunction(ARG argument, Class<T> casttarget) {
		Map<Class<?>, Function<?, Object>> opmap = CAST_OPERATORS.get(casttarget);
		if (opmap == null) {
			return null;
		}
		Function<?, Object> opfunc = opmap.get(argument.getClass());
		if (opfunc == null) {
			opfunc = opmap.get(Object.class);
		}
		return (Function<ARG, T>) opfunc;
	}

	public static Object operatorCastObject(java.lang.Object l) {
		return l;
	}

	public static Boolean operatorCastBoolean(java.lang.Object l) {
		return (boolean) l;
	}

	public static Byte operatorCastByte(java.lang.Object l) {
		return (byte) l;
	}

	public static Short operatorCastShort(java.lang.Object l) {
		return (short) l;
	}

	public static Integer operatorCastInteger(java.lang.Object l) {
		return (int) l;
	}

	public static Long operatorCastLong(java.lang.Object l) {
		return (long) l;
	}

	public static Float operatorCastFloat(java.lang.Object l) {
		return (float) l;
	}

	public static Double operatorCastDouble(java.lang.Object l) {
		return (double) l;
	}

	public static String operatorCastString(java.lang.Object l) {
		return (String) l;
	}

	public static Character operatorCastCharacter(java.lang.Object l) {
		return (char) l;
	}

	public static Object operatorCastObject(java.lang.Boolean l) {
		return (Object) ((boolean) l);
	}

	public static Boolean operatorCastBoolean(java.lang.Boolean l) {
		return l;
	}

	public static Object operatorCastObject(java.lang.Byte l) {
		return (Object) ((byte) l);
	}

	public static Byte operatorCastByte(java.lang.Byte l) {
		return l;
	}

	public static Short operatorCastShort(java.lang.Byte l) {
		return (short) ((byte) l);
	}

	public static Integer operatorCastInteger(java.lang.Byte l) {
		return (int) ((byte) l);
	}

	public static Long operatorCastLong(java.lang.Byte l) {
		return (long) ((byte) l);
	}

	public static Float operatorCastFloat(java.lang.Byte l) {
		return (float) ((byte) l);
	}

	public static Double operatorCastDouble(java.lang.Byte l) {
		return (double) ((byte) l);
	}

	public static Character operatorCastCharacter(java.lang.Byte l) {
		return (char) ((byte) l);
	}

	public static Object operatorCastObject(java.lang.Short l) {
		return (Object) ((short) l);
	}

	public static Byte operatorCastByte(java.lang.Short l) {
		return (byte) ((short) l);
	}

	public static Short operatorCastShort(java.lang.Short l) {
		return l;
	}

	public static Integer operatorCastInteger(java.lang.Short l) {
		return (int) ((short) l);
	}

	public static Long operatorCastLong(java.lang.Short l) {
		return (long) ((short) l);
	}

	public static Float operatorCastFloat(java.lang.Short l) {
		return (float) ((short) l);
	}

	public static Double operatorCastDouble(java.lang.Short l) {
		return (double) ((short) l);
	}

	public static Character operatorCastCharacter(java.lang.Short l) {
		return (char) ((short) l);
	}

	public static Object operatorCastObject(java.lang.Integer l) {
		return (Object) ((int) l);
	}

	public static Byte operatorCastByte(java.lang.Integer l) {
		return (byte) ((int) l);
	}

	public static Short operatorCastShort(java.lang.Integer l) {
		return (short) ((int) l);
	}

	public static Integer operatorCastInteger(java.lang.Integer l) {
		return l;
	}

	public static Long operatorCastLong(java.lang.Integer l) {
		return (long) ((int) l);
	}

	public static Float operatorCastFloat(java.lang.Integer l) {
		return (float) ((int) l);
	}

	public static Double operatorCastDouble(java.lang.Integer l) {
		return (double) ((int) l);
	}

	public static Character operatorCastCharacter(java.lang.Integer l) {
		return (char) ((int) l);
	}

	public static Object operatorCastObject(java.lang.Long l) {
		return (Object) ((long) l);
	}

	public static Byte operatorCastByte(java.lang.Long l) {
		return (byte) ((long) l);
	}

	public static Short operatorCastShort(java.lang.Long l) {
		return (short) ((long) l);
	}

	public static Integer operatorCastInteger(java.lang.Long l) {
		return (int) ((long) l);
	}

	public static Long operatorCastLong(java.lang.Long l) {
		return l;
	}

	public static Float operatorCastFloat(java.lang.Long l) {
		return (float) ((long) l);
	}

	public static Double operatorCastDouble(java.lang.Long l) {
		return (double) ((long) l);
	}

	public static Character operatorCastCharacter(java.lang.Long l) {
		return (char) ((long) l);
	}

	public static Object operatorCastObject(java.lang.Float l) {
		return (Object) ((float) l);
	}

	public static Byte operatorCastByte(java.lang.Float l) {
		return (byte) ((float) l);
	}

	public static Short operatorCastShort(java.lang.Float l) {
		return (short) ((float) l);
	}

	public static Integer operatorCastInteger(java.lang.Float l) {
		return (int) ((float) l);
	}

	public static Long operatorCastLong(java.lang.Float l) {
		return (long) ((float) l);
	}

	public static Float operatorCastFloat(java.lang.Float l) {
		return l;
	}

	public static Double operatorCastDouble(java.lang.Float l) {
		return (double) ((float) l);
	}

	public static Character operatorCastCharacter(java.lang.Float l) {
		return (char) ((float) l);
	}

	public static Object operatorCastObject(java.lang.Double l) {
		return (Object) ((double) l);
	}

	public static Byte operatorCastByte(java.lang.Double l) {
		return (byte) ((double) l);
	}

	public static Short operatorCastShort(java.lang.Double l) {
		return (short) ((double) l);
	}

	public static Integer operatorCastInteger(java.lang.Double l) {
		return (int) ((double) l);
	}

	public static Long operatorCastLong(java.lang.Double l) {
		return (long) ((double) l);
	}

	public static Float operatorCastFloat(java.lang.Double l) {
		return (float) ((double) l);
	}

	public static Double operatorCastDouble(java.lang.Double l) {
		return l;
	}

	public static Character operatorCastCharacter(java.lang.Double l) {
		return (char) ((double) l);
	}

	public static Object operatorCastObject(java.lang.String l) {
		return (Object) l;
	}

	public static String operatorCastString(java.lang.String l) {
		return l;
	}

	public static Object operatorCastObject(java.lang.Character l) {
		return (Object) ((char) l);
	}

	public static Byte operatorCastByte(java.lang.Character l) {
		return (byte) ((char) l);
	}

	public static Short operatorCastShort(java.lang.Character l) {
		return (short) ((char) l);
	}

	public static Integer operatorCastInteger(java.lang.Character l) {
		return (int) ((char) l);
	}

	public static Long operatorCastLong(java.lang.Character l) {
		return (long) ((char) l);
	}

	public static Float operatorCastFloat(java.lang.Character l) {
		return (float) ((char) l);
	}

	public static Double operatorCastDouble(java.lang.Character l) {
		return (double) ((char) l);
	}

	public static Character operatorCastCharacter(java.lang.Character l) {
		return l;
	}
}
