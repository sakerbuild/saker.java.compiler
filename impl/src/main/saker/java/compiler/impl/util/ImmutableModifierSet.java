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
package saker.java.compiler.impl.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;

import saker.java.compiler.api.processing.exc.ModifierNotFoundException;

public final class ImmutableModifierSet extends AbstractSet<Modifier> implements Set<Modifier> {
	private static final ImmutableModifierSet EMPTY_INSTANCE = new ImmutableModifierSet((short) 0);

	//define our own modifier values array, as newer JDKs can add new values, which can distrupt order or ordinal values
	//if new values are introduced, export this array to a JDK version dependent bundle. only appending to it
	//      THE ORDER IN THIS ARRAY IS NOT TO BE MODIFIED FOR SERIALIZATION COMPATIBILITY
	private static final String[] MODIFIERS_NAMES = { "PUBLIC", // 0 
			"PROTECTED", // 1
			"PRIVATE", // 2
			"ABSTRACT", // 3
			"DEFAULT", // 4
			"STATIC", // 5
			"FINAL", // 6
			"TRANSIENT", // 7
			"VOLATILE", // 8
			"SYNCHRONIZED", // 9
			"NATIVE", // 10
			"STRICTFP", // 11
			"SEALED", // 12
			"NON_SEALED" // 13
	};
	private static final Modifier[] MODIFIERS = new Modifier[MODIFIERS_NAMES.length];

	private static final IllegalArgumentException[] MODIFIER_NOTFOUND_EXCEPTIONS = new IllegalArgumentException[MODIFIERS_NAMES.length];

	//this array is a dynamic lookup table that converts the ordinals of Modifier enums to the index in MODIFIERS
	private static final int[] MODIFIER_ORDINAL_INDEX_LOOKUP;

	private static final int INDEX_SEALED = 12;

	public static final Modifier MODIFIER_SEALED;

	private static final short ALL_RECOGNIZED_FLAGS_MASK;
	static {
		if (MODIFIERS.length > 16) {
			throw new AssertionError("Too many modifiers. Need higher precision representation: " + MODIFIERS.length);
		}
		for (int i = 0; i < MODIFIERS_NAMES.length; i++) {
			try {
				MODIFIERS[i] = Modifier.valueOf(MODIFIERS_NAMES[i]);
			} catch (IllegalArgumentException e) {
				//an enum was not found in the current JVM
				//running on older version, not supported 
				MODIFIER_NOTFOUND_EXCEPTIONS[i] = e;
			}
		}

		MODIFIER_ORDINAL_INDEX_LOOKUP = new int[Math.max(MODIFIERS.length, Modifier.values().length)];
		Arrays.fill(MODIFIER_ORDINAL_INDEX_LOOKUP, -1);
		short allrecognized = 0;
		for (int i = 0; i < MODIFIERS.length; i++) {
			Modifier m = MODIFIERS[i];
			short f = (short) (1 << i);
			if (m != null) {
				//a modifier may not be supported in the current VM
				MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()] = i;
			}
			allrecognized |= f;
		}
		ALL_RECOGNIZED_FLAGS_MASK = allrecognized;
		MODIFIER_SEALED = MODIFIERS[INDEX_SEALED];
	}

	private final short flags;

	private ImmutableModifierSet(short flags) {
		this.flags = flags;
	}

	public static ImmutableModifierSet empty() {
		return EMPTY_INSTANCE;
	}

	public static ImmutableModifierSet get(Set<Modifier> modifiers) {
		if (modifiers == null) {
			return null;
		}
		if (modifiers instanceof ImmutableModifierSet) {
			return (ImmutableModifierSet) modifiers;
		}
		short f = getFlag(modifiers);
		if (f == 0) {
			return EMPTY_INSTANCE;
		}
		return new ImmutableModifierSet(f);
	}

	public static ImmutableModifierSet of(Modifier... modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		short f = 0;
		for (Modifier m : modifiers) {
			Objects.requireNonNull(m, "modifier");
			int ordinal = MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("Modifier not found in ordinal lookup: " + m);
			}
			f |= 1 << ordinal;
		}
		return new ImmutableModifierSet(f);
	}

	public static short getFlag(Set<Modifier> modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		if (modifiers instanceof ImmutableModifierSet) {
			return ((ImmutableModifierSet) modifiers).flags;
		}
		short f = 0;
		for (Modifier m : modifiers) {
			Objects.requireNonNull(m, "modifier");
			int ordinal = MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("Modifier not found in ordinal lookup: " + m);
			}
			f |= 1 << ordinal;
		}
		return f;
	}

	public static ImmutableModifierSet forFlags(short flags) {
		checkFlagsSupport(flags);
		return new ImmutableModifierSet(flags);
	}

	public static void writeExternalFlag(DataOutput out, short flags) throws IOException {
		out.writeShort(flags);
	}

	public static short readExternalFlag(DataInput in) throws IOException {
		short flags = in.readShort();
		checkFlagsSupport(flags);
		return flags;
	}

	public static ImmutableModifierSet readExternalFlagSet(DataInput in) throws IOException {
		short flags = in.readShort();
		checkFlagsSupport(flags);
		return new ImmutableModifierSet(flags);
	}

	private static void checkFlagsSupport(short flags) {
		int unsupportedflags = flags & ~ALL_RECOGNIZED_FLAGS_MASK;
		if (unsupportedflags == 0) {
			return;
		}
		throw new IllegalArgumentException("Modifier flags contain unrecognized modifiers: 0x"
				+ Integer.toHexString(flags) + " with mask: 0x" + Integer.toHexString(ALL_RECOGNIZED_FLAGS_MASK));
	}

	public short getFlags() {
		return flags;
	}

	public ImmutableModifierSet added(Modifier modifier) {
		Objects.requireNonNull(modifier, "modifier");
		int ordinal = MODIFIER_ORDINAL_INDEX_LOOKUP[modifier.ordinal()];
		if (ordinal < 0) {
			throw new AssertionError("Modifier not found in ordinal lookup: " + modifier);
		}
		short f = (short) (1 << ordinal);
		short nf = (short) (this.flags | f);
		if (nf == this.flags) {
			return this;
		}
		return new ImmutableModifierSet(nf);
	}

	public ImmutableModifierSet added(Set<Modifier> modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		short nf = getFlag(modifiers);
		nf |= this.flags;
		if (nf == this.flags) {
			return this;
		}
		return new ImmutableModifierSet(nf);
	}

	public ImmutableModifierSet added(Modifier... modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		short nf = this.flags;
		for (Modifier m : modifiers) {
			Objects.requireNonNull(m, "modifier");
			int ordinal = MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("Modifier not found in ordinal lookup: " + m);
			}
			short f = (short) (1 << ordinal);
			nf |= f;
		}
		if (nf == this.flags) {
			return this;
		}
		return new ImmutableModifierSet(nf);
	}

	@Override
	public Iterator<Modifier> iterator() {
		return new Iterator<Modifier>() {
			private short itFlags = flags;
			private int idx = 0;

			@Override
			public Modifier next() {
				if (itFlags == 0) {
					throw new NoSuchElementException();
				}
				while ((itFlags & 1) != 1) {
					++idx;
					itFlags >>= 1;
				}
				int modidx = idx;
				Modifier result = MODIFIERS[modidx];
				idx++;
				itFlags >>= 1;
				if (result == null) {
					throw new ModifierNotFoundException(MODIFIERS_NAMES[modidx], MODIFIER_NOTFOUND_EXCEPTIONS[modidx]);
				}
				return result;
			}

			@Override
			public boolean hasNext() {
				return itFlags != 0;
			}
		};
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Modifier)) {
			return false;
		}
		int ordinal = MODIFIER_ORDINAL_INDEX_LOOKUP[((Modifier) o).ordinal()];
		if (ordinal < 0) {
			throw new AssertionError("Modifier not found in ordinal lookup: " + o);
		}
		short f = (short) (1 << ordinal);
		return ((flags & f) == f);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof ImmutableModifierSet) {
			short cflags = ((ImmutableModifierSet) c).flags;
			return (flags & cflags) == cflags;
		}
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void forEach(Consumer<? super Modifier> action) {
		Objects.requireNonNull(action, "action");

		short f = this.flags;
		int idx = 0;
		while (f != 0) {
			while ((f & 1) != 1) {
				++idx;
				f >>= 1;
			}
			Modifier m = MODIFIERS[idx];
			if (m == null) {
				throw new ModifierNotFoundException(MODIFIERS_NAMES[idx], MODIFIER_NOTFOUND_EXCEPTIONS[idx]);
			}
			action.accept(m);
			idx++;
			f >>= 1;
		}
	}

	@Override
	public int size() {
		return Integer.bitCount(flags);
	}

	@Override
	public boolean isEmpty() {
		return flags == 0;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIf(Predicate<? super Modifier> filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	//hashCode from AbstractSet
	@Override
	public int hashCode() {
		if (flags == 0) {
			return 0;
		}
		int h = 0;
		for (int i = 0; i < MODIFIERS.length; i++) {
			short f = (short) (1 << i);
			if (((this.flags & f) == f)) {
				Modifier m = MODIFIERS[i];
				if (m == null) {
					//if the modifier not found, then add some hash
					//violates Set contract, but the Set is not even valid so...
					h += f;
				} else {
					h += m.hashCode();
				}
			}
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		//based on AbstractSet
		if (o == this) {
			return true;
		}
		if (!(o instanceof Set)) {
			return false;
		}
		if (o instanceof ImmutableModifierSet) {
			return this.flags == ((ImmutableModifierSet) o).flags;
		}
		Collection<?> c = (Collection<?>) o;
		if (c.size() != size()) {
			return false;
		}
		try {
			return containsAll(c);
		} catch (ClassCastException | NullPointerException unused) {
			return false;
		}
	}

	@Override
	public String toString() {
		if (flags == 0) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < MODIFIERS_NAMES.length; i++) {
			short f = (short) (1 << i);
			if (((this.flags & f) == f)) {
				if (sb.length() > 1) {
					sb.append(", ");
				}
				sb.append(MODIFIERS_NAMES[i]);
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
