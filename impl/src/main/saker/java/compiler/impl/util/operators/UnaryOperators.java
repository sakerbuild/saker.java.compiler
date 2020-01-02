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
import java.util.TreeMap;
import java.util.function.Function;

import com.sun.source.tree.Tree;

public class UnaryOperators {
	private static final Map<String, Map<Class<?>, Function<?, Object>>> UNARY_OPERATORS = new TreeMap<>();
	static {
		Map<Class<?>, Function<?, Object>> UnaryBitwiseComplementMap = new HashMap<>();
		UnaryBitwiseComplementMap.put(Byte.class,
				(Function<Byte, Object>) UnaryOperators::operatorUnaryBitwiseComplement);
		UnaryBitwiseComplementMap.put(Short.class,
				(Function<Short, Object>) UnaryOperators::operatorUnaryBitwiseComplement);
		UnaryBitwiseComplementMap.put(Integer.class,
				(Function<Integer, Object>) UnaryOperators::operatorUnaryBitwiseComplement);
		UnaryBitwiseComplementMap.put(Long.class,
				(Function<Long, Object>) UnaryOperators::operatorUnaryBitwiseComplement);
		UnaryBitwiseComplementMap.put(Character.class,
				(Function<Character, Object>) UnaryOperators::operatorUnaryBitwiseComplement);
		UNARY_OPERATORS.put("BITWISE_COMPLEMENT", UnaryBitwiseComplementMap);

		Map<Class<?>, Function<?, Object>> UnaryLogicalComplementMap = new HashMap<>();
		UnaryLogicalComplementMap.put(Boolean.class,
				(Function<Boolean, Object>) UnaryOperators::operatorUnaryLogicalComplement);
		UNARY_OPERATORS.put("LOGICAL_COMPLEMENT", UnaryLogicalComplementMap);

		Map<Class<?>, Function<?, Object>> UnaryMinusMap = new HashMap<>();
		UnaryMinusMap.put(Byte.class, (Function<Byte, Object>) UnaryOperators::operatorUnaryMinus);
		UnaryMinusMap.put(Short.class, (Function<Short, Object>) UnaryOperators::operatorUnaryMinus);
		UnaryMinusMap.put(Integer.class, (Function<Integer, Object>) UnaryOperators::operatorUnaryMinus);
		UnaryMinusMap.put(Long.class, (Function<Long, Object>) UnaryOperators::operatorUnaryMinus);
		UnaryMinusMap.put(Float.class, (Function<Float, Object>) UnaryOperators::operatorUnaryMinus);
		UnaryMinusMap.put(Double.class, (Function<Double, Object>) UnaryOperators::operatorUnaryMinus);
		UnaryMinusMap.put(Character.class, (Function<Character, Object>) UnaryOperators::operatorUnaryMinus);
		UNARY_OPERATORS.put("UNARY_MINUS", UnaryMinusMap);

		Map<Class<?>, Function<?, Object>> UnaryPlusMap = new HashMap<>();
		UnaryPlusMap.put(Byte.class, (Function<Byte, Object>) UnaryOperators::operatorUnaryPlus);
		UnaryPlusMap.put(Short.class, (Function<Short, Object>) UnaryOperators::operatorUnaryPlus);
		UnaryPlusMap.put(Integer.class, (Function<Integer, Object>) UnaryOperators::operatorUnaryPlus);
		UnaryPlusMap.put(Long.class, (Function<Long, Object>) UnaryOperators::operatorUnaryPlus);
		UnaryPlusMap.put(Float.class, (Function<Float, Object>) UnaryOperators::operatorUnaryPlus);
		UnaryPlusMap.put(Double.class, (Function<Double, Object>) UnaryOperators::operatorUnaryPlus);
		UnaryPlusMap.put(Character.class, (Function<Character, Object>) UnaryOperators::operatorUnaryPlus);
		UNARY_OPERATORS.put("UNARY_PLUS", UnaryPlusMap);
	}

	@SuppressWarnings("unchecked")
	public static Function<Object, Object> getOperatorFunction(String op, Object val) {
		Map<Class<?>, Function<?, Object>> opmap = UNARY_OPERATORS.get(op);
		if (opmap == null) {
			return null;
		}

		Function<?, Object> opfunc = opmap.get(val.getClass());
		if (opfunc == null) {
			opfunc = opmap.get(Object.class);
		}
		return (Function<Object, Object>) opfunc;
	}

	@SuppressWarnings("unchecked")
	public static Function<Object, Object> getOperatorFunction(Tree.Kind op, Object val) {
		return getOperatorFunction(op.name(), val);
	}

	public static Object operatorUnaryBitwiseComplement(java.lang.Byte l) {
		return ~((byte) l);
	}

	public static Object operatorUnaryBitwiseComplement(java.lang.Short l) {
		return ~((short) l);
	}

	public static Object operatorUnaryBitwiseComplement(java.lang.Integer l) {
		return ~((int) l);
	}

	public static Object operatorUnaryBitwiseComplement(java.lang.Long l) {
		return ~((long) l);
	}

	public static Object operatorUnaryBitwiseComplement(java.lang.Character l) {
		return ~((char) l);
	}

	public static Object operatorUnaryLogicalComplement(java.lang.Boolean l) {
		return !((boolean) l);
	}

	public static Object operatorUnaryMinus(java.lang.Byte l) {
		return -((byte) l);
	}

	public static Object operatorUnaryMinus(java.lang.Short l) {
		return -((short) l);
	}

	public static Object operatorUnaryMinus(java.lang.Integer l) {
		return -((int) l);
	}

	public static Object operatorUnaryMinus(java.lang.Long l) {
		return -((long) l);
	}

	public static Object operatorUnaryMinus(java.lang.Float l) {
		return -((float) l);
	}

	public static Object operatorUnaryMinus(java.lang.Double l) {
		return -((double) l);
	}

	public static Object operatorUnaryMinus(java.lang.Character l) {
		return -((char) l);
	}

	public static Object operatorUnaryPlus(java.lang.Byte l) {
		return +((byte) l);
	}

	public static Object operatorUnaryPlus(java.lang.Short l) {
		return +((short) l);
	}

	public static Object operatorUnaryPlus(java.lang.Integer l) {
		return +((int) l);
	}

	public static Object operatorUnaryPlus(java.lang.Long l) {
		return +((long) l);
	}

	public static Object operatorUnaryPlus(java.lang.Float l) {
		return +((float) l);
	}

	public static Object operatorUnaryPlus(java.lang.Double l) {
		return +((double) l);
	}

	public static Object operatorUnaryPlus(java.lang.Character l) {
		return +((char) l);
	}
}
