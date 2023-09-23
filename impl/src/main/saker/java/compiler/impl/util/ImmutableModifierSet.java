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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;

import saker.java.compiler.api.processing.exc.ModifierNotFoundException;
import testing.saker.java.compiler.TestFlag;

public final class ImmutableModifierSet extends AbstractSet<Modifier> {
	//define our own modifier values array, as newer JDKs can add new values, which can distrupt order or ordinal values
	//if new values are introduced, export this array to a JDK version dependent bundle. only appending to it
	//      THE ORDER IN THIS ARRAY IS NOT TO BE MODIFIED FOR SERIALIZATION COMPATIBILITY
	private static final String[] ENUM_NAMES = { "PUBLIC", // 0 
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
	private static final Modifier[] ENUM_VALUES = new Modifier[ENUM_NAMES.length];

	private static final IllegalArgumentException[] ENUM_NOTFOUND_EXCEPTIONS = new IllegalArgumentException[ENUM_NAMES.length];

	//this array is a dynamic lookup table that converts the ordinals of Modifier enums to the index in MODIFIERS
	private static final int[] ENUM_ORDINAL_INDEX_LOOKUP;

	private static final int INDEX_SEALED = 12;
	private static final int INDEX_NON_SEALED = 13;

	public static final Modifier MODIFIER_SEALED;
	public static final Modifier MODIFIER_NON_SEALED;

	private static final short ALL_RECOGNIZED_FLAGS_MASK;
	static {
		if (ENUM_VALUES.length > 16) {
			throw new AssertionError("Too many modifiers. Need higher precision representation: " + ENUM_VALUES.length);
		}
		for (int i = 0; i < ENUM_NAMES.length; i++) {
			try {
				ENUM_VALUES[i] = Modifier.valueOf(ENUM_NAMES[i]);
			} catch (IllegalArgumentException e) {
				//an enum was not found in the current JVM
				//running on older version, not supported 
				ENUM_NOTFOUND_EXCEPTIONS[i] = e;
			}
		}

		ENUM_ORDINAL_INDEX_LOOKUP = new int[Math.max(ENUM_VALUES.length, Modifier.values().length)];
		Arrays.fill(ENUM_ORDINAL_INDEX_LOOKUP, -1);
		short allrecognized = 0;
		for (int i = 0; i < ENUM_VALUES.length; i++) {
			Modifier m = ENUM_VALUES[i];
			short f = (short) (1 << i);
			if (m != null) {
				//a modifier may not be supported in the current VM
				ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()] = i;
			}
			allrecognized |= f;
		}
		ALL_RECOGNIZED_FLAGS_MASK = allrecognized;
		MODIFIER_SEALED = ENUM_VALUES[INDEX_SEALED];
		MODIFIER_NON_SEALED = ENUM_VALUES[INDEX_NON_SEALED];
	}

	public static final short FLAG_NONE = 0;
	public static final short FLAG_PUBLIC = 1 << 0;
	public static final short FLAG_PROTECTED = 1 << 1;
	public static final short FLAG_PRIVATE = 1 << 2;
	public static final short FLAG_ABSTRACT = 1 << 3;
	public static final short FLAG_DEFAULT = 1 << 4;
	public static final short FLAG_STATIC = 1 << 5;
	public static final short FLAG_FINAL = 1 << 6;
	public static final short FLAG_TRANSIENT = 1 << 7;
	public static final short FLAG_VOLATILE = 1 << 8;
	public static final short FLAG_SYNCHRONIZED = 1 << 9;
	public static final short FLAG_NATIVE = 1 << 10;
	public static final short FLAG_STRICTFP = 1 << 11;
	public static final short FLAG_SEALED = 1 << 12;
	public static final short FLAG_NON_SEALED = 1 << 13;

	public static final ImmutableModifierSet INSTANCE_NONE = new ImmutableModifierSet(FLAG_NONE);
	public static final ImmutableModifierSet INSTANCE_PUBLIC = new ImmutableModifierSet(FLAG_PUBLIC);
	public static final ImmutableModifierSet INSTANCE_PROTECTED = new ImmutableModifierSet(FLAG_PROTECTED);
	public static final ImmutableModifierSet INSTANCE_PRIVATE = new ImmutableModifierSet(FLAG_PRIVATE);
	public static final ImmutableModifierSet INSTANCE_ABSTRACT = new ImmutableModifierSet(FLAG_ABSTRACT);
	public static final ImmutableModifierSet INSTANCE_DEFAULT = new ImmutableModifierSet(FLAG_DEFAULT);
	public static final ImmutableModifierSet INSTANCE_STATIC = new ImmutableModifierSet(FLAG_STATIC);
	public static final ImmutableModifierSet INSTANCE_FINAL = new ImmutableModifierSet(FLAG_FINAL);
	public static final ImmutableModifierSet INSTANCE_TRANSIENT = new ImmutableModifierSet(FLAG_TRANSIENT);
	public static final ImmutableModifierSet INSTANCE_VOLATILE = new ImmutableModifierSet(FLAG_VOLATILE);
	public static final ImmutableModifierSet INSTANCE_SYNCHRONIZED = new ImmutableModifierSet(FLAG_SYNCHRONIZED);
	public static final ImmutableModifierSet INSTANCE_NATIVE = new ImmutableModifierSet(FLAG_NATIVE);
	public static final ImmutableModifierSet INSTANCE_STRICTFP = new ImmutableModifierSet(FLAG_STRICTFP);
	public static final ImmutableModifierSet INSTANCE_SEALED = new ImmutableModifierSet(FLAG_SEALED);
	public static final ImmutableModifierSet INSTANCE_NON_SEALED = new ImmutableModifierSet(FLAG_NON_SEALED);

	public static final ImmutableModifierSet INSTANCE_PUBLIC_STATIC = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_STATIC));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_STATIC = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_STATIC));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_STATIC = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_STATIC));

	public static final ImmutableModifierSet INSTANCE_STATIC_FINAL = new ImmutableModifierSet(
			(short) (FLAG_STATIC | FLAG_FINAL));
	public static final ImmutableModifierSet INSTANCE_PUBLIC_STATIC_FINAL = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_STATIC | FLAG_FINAL));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_STATIC_FINAL = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_STATIC | FLAG_FINAL));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_STATIC_FINAL = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_STATIC | FLAG_FINAL));

	public static final ImmutableModifierSet INSTANCE_PUBLIC_ABSTRACT = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_ABSTRACT));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_ABSTRACT = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_ABSTRACT));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_ABSTRACT = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_ABSTRACT));

	public static final ImmutableModifierSet INSTANCE_PUBLIC_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_VOLATILE));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_VOLATILE));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_VOLATILE));

	public static final ImmutableModifierSet INSTANCE_PUBLIC_SYNCHRONIZED = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_SYNCHRONIZED));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_SYNCHRONIZED = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_SYNCHRONIZED));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_SYNCHRONIZED = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_SYNCHRONIZED));

	public static final ImmutableModifierSet INSTANCE_SYNCHRONIZED_FINAL = new ImmutableModifierSet(
			(short) (FLAG_SYNCHRONIZED | FLAG_FINAL));
	public static final ImmutableModifierSet INSTANCE_PUBLIC_SYNCHRONIZED_FINAL = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_SYNCHRONIZED | FLAG_FINAL));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_SYNCHRONIZED_FINAL = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_SYNCHRONIZED | FLAG_FINAL));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_SYNCHRONIZED_FINAL = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_SYNCHRONIZED | FLAG_FINAL));

	public static final ImmutableModifierSet INSTANCE_PUBLIC_NATIVE = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_NATIVE));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_NATIVE = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_NATIVE));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_NATIVE = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_NATIVE));

	public static final ImmutableModifierSet INSTANCE_TRANSIENT_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_TRANSIENT | FLAG_VOLATILE));
	public static final ImmutableModifierSet INSTANCE_PUBLIC_TRANSIENT_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_PUBLIC | FLAG_TRANSIENT | FLAG_VOLATILE));
	public static final ImmutableModifierSet INSTANCE_PROTECTED_TRANSIENT_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_PROTECTED | FLAG_TRANSIENT | FLAG_VOLATILE));
	public static final ImmutableModifierSet INSTANCE_PRIVATE_TRANSIENT_VOLATILE = new ImmutableModifierSet(
			(short) (FLAG_PRIVATE | FLAG_TRANSIENT | FLAG_VOLATILE));

	static {
		if (TestFlag.ENABLED) {
			if (FLAG_PUBLIC != getFlag(EnumSet.of(Modifier.PUBLIC))
					|| FLAG_PROTECTED != getFlag(EnumSet.of(Modifier.PROTECTED))
					|| FLAG_PRIVATE != getFlag(EnumSet.of(Modifier.PRIVATE))
					|| FLAG_ABSTRACT != getFlag(EnumSet.of(Modifier.ABSTRACT))
					|| FLAG_DEFAULT != getFlag(EnumSet.of(Modifier.DEFAULT))
					|| FLAG_STATIC != getFlag(EnumSet.of(Modifier.STATIC))
					|| FLAG_FINAL != getFlag(EnumSet.of(Modifier.FINAL))
					|| FLAG_TRANSIENT != getFlag(EnumSet.of(Modifier.TRANSIENT))
					|| FLAG_VOLATILE != getFlag(EnumSet.of(Modifier.VOLATILE))
					|| FLAG_SYNCHRONIZED != getFlag(EnumSet.of(Modifier.SYNCHRONIZED))
					|| FLAG_NATIVE != getFlag(EnumSet.of(Modifier.NATIVE))
					|| FLAG_STRICTFP != getFlag(EnumSet.of(Modifier.STRICTFP))
					|| (MODIFIER_SEALED != null && FLAG_SEALED != getFlag(EnumSet.of(MODIFIER_SEALED)))
					|| (MODIFIER_NON_SEALED != null && FLAG_NON_SEALED != getFlag(EnumSet.of(MODIFIER_NON_SEALED)))) {
				//sanity checks that the compile time constants equal the converted ones
				throw new AssertionError("Flag value mismatch.");
			}
		}
	}

	private final short flags;

	private ImmutableModifierSet(short flags) {
		this.flags = flags;
	}

	public static ImmutableModifierSet empty() {
		return INSTANCE_NONE;
	}

	public static ImmutableModifierSet get(Set<Modifier> modifiers) {
		if (modifiers == null) {
			return null;
		}
		if (modifiers instanceof ImmutableModifierSet) {
			return (ImmutableModifierSet) modifiers;
		}
		short f = getFlag(modifiers);
		return getSetOfFlags(f);
	}

	public static ImmutableModifierSet of(Modifier... modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		if (modifiers.length == 0) {
			return INSTANCE_NONE;
		}
		short f = 0;
		for (Modifier m : modifiers) {
			Objects.requireNonNull(m, "modifier");
			int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("Modifier not found in ordinal lookup: " + m);
			}
			f |= 1 << ordinal;
		}
		return getSetOfFlags(f);
	}

	public static short getFlag(Set<Modifier> modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		if (modifiers instanceof ImmutableModifierSet) {
			return ((ImmutableModifierSet) modifiers).flags;
		}
		short f = 0;
		for (Modifier m : modifiers) {
			Objects.requireNonNull(m, "modifier");
			int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("Modifier not found in ordinal lookup: " + m);
			}
			f |= 1 << ordinal;
		}
		return f;
	}

	public static ImmutableModifierSet forFlags(short flags) {
		checkFlagsSupport(flags);
		return getSetOfFlags(flags);
	}

	private static ImmutableModifierSet getSetOfFlags(short flags) {
		//cache commonly occurring modifier instances so their retrieval doesn't create new set objects
		switch (flags) {
			case FLAG_NONE:
				return INSTANCE_NONE;
			case FLAG_PUBLIC:
				return INSTANCE_PUBLIC;
			case FLAG_PROTECTED:
				return INSTANCE_PROTECTED;
			case FLAG_PRIVATE:
				return INSTANCE_PRIVATE;
			case FLAG_ABSTRACT:
				return INSTANCE_ABSTRACT;
			case FLAG_DEFAULT:
				return INSTANCE_DEFAULT;
			case FLAG_STATIC:
				return INSTANCE_STATIC;
			case FLAG_FINAL:
				return INSTANCE_FINAL;
			case FLAG_TRANSIENT:
				return INSTANCE_TRANSIENT;
			case FLAG_VOLATILE:
				return INSTANCE_VOLATILE;
			case FLAG_SYNCHRONIZED:
				return INSTANCE_SYNCHRONIZED;
			case FLAG_NATIVE:
				return INSTANCE_NATIVE;
			case FLAG_STRICTFP:
				return INSTANCE_STRICTFP;
			case FLAG_SEALED:
				return INSTANCE_SEALED;
			case FLAG_NON_SEALED:
				return INSTANCE_NON_SEALED;

			case FLAG_PUBLIC | FLAG_STATIC:
				return INSTANCE_PUBLIC_STATIC;
			case FLAG_PROTECTED | FLAG_STATIC:
				return INSTANCE_PROTECTED_STATIC;
			case FLAG_PRIVATE | FLAG_STATIC:
				return INSTANCE_PRIVATE_STATIC;

			case FLAG_STATIC | FLAG_FINAL:
				return INSTANCE_STATIC_FINAL;
			case FLAG_PUBLIC | FLAG_STATIC | FLAG_FINAL:
				return INSTANCE_PUBLIC_STATIC_FINAL;
			case FLAG_PROTECTED | FLAG_STATIC | FLAG_FINAL:
				return INSTANCE_PROTECTED_STATIC_FINAL;
			case FLAG_PRIVATE | FLAG_STATIC | FLAG_FINAL:
				return INSTANCE_PRIVATE_STATIC_FINAL;

			case FLAG_PUBLIC | FLAG_ABSTRACT:
				return INSTANCE_PUBLIC_ABSTRACT;
			case FLAG_PROTECTED | FLAG_ABSTRACT:
				return INSTANCE_PROTECTED_ABSTRACT;
			case FLAG_PRIVATE | FLAG_ABSTRACT:
				return INSTANCE_PRIVATE_ABSTRACT;

			case FLAG_PUBLIC | FLAG_VOLATILE:
				return INSTANCE_PUBLIC_VOLATILE;
			case FLAG_PROTECTED | FLAG_VOLATILE:
				return INSTANCE_PROTECTED_VOLATILE;
			case FLAG_PRIVATE | FLAG_VOLATILE:
				return INSTANCE_PRIVATE_VOLATILE;

			case FLAG_PUBLIC | FLAG_SYNCHRONIZED:
				return INSTANCE_PUBLIC_SYNCHRONIZED;
			case FLAG_PROTECTED | FLAG_SYNCHRONIZED:
				return INSTANCE_PROTECTED_SYNCHRONIZED;
			case FLAG_PRIVATE | FLAG_SYNCHRONIZED:
				return INSTANCE_PRIVATE_SYNCHRONIZED;

			case FLAG_SYNCHRONIZED | FLAG_FINAL:
				return INSTANCE_SYNCHRONIZED_FINAL;
			case FLAG_PUBLIC | FLAG_SYNCHRONIZED | FLAG_FINAL:
				return INSTANCE_PUBLIC_SYNCHRONIZED_FINAL;
			case FLAG_PROTECTED | FLAG_SYNCHRONIZED | FLAG_FINAL:
				return INSTANCE_PROTECTED_SYNCHRONIZED_FINAL;
			case FLAG_PRIVATE | FLAG_SYNCHRONIZED | FLAG_FINAL:
				return INSTANCE_PRIVATE_SYNCHRONIZED_FINAL;

			case FLAG_PUBLIC | FLAG_NATIVE:
				return INSTANCE_PUBLIC_NATIVE;
			case FLAG_PROTECTED | FLAG_NATIVE:
				return INSTANCE_PROTECTED_NATIVE;
			case FLAG_PRIVATE | FLAG_NATIVE:
				return INSTANCE_PRIVATE_NATIVE;

			case FLAG_TRANSIENT | FLAG_VOLATILE:
				return INSTANCE_TRANSIENT_VOLATILE;
			case FLAG_PUBLIC | FLAG_TRANSIENT | FLAG_VOLATILE:
				return INSTANCE_PUBLIC_TRANSIENT_VOLATILE;
			case FLAG_PROTECTED | FLAG_TRANSIENT | FLAG_VOLATILE:
				return INSTANCE_PROTECTED_TRANSIENT_VOLATILE;
			case FLAG_PRIVATE | FLAG_TRANSIENT | FLAG_VOLATILE:
				return INSTANCE_PRIVATE_TRANSIENT_VOLATILE;
			default:
				return new ImmutableModifierSet(flags);
		}
	}

	public static void writeExternalFlag(DataOutput out, short flags) throws IOException {
		out.writeShort(flags);
	}

	public static void writeExternalObjectFlag(ObjectOutput out, short flags) throws IOException {
		out.writeObject(flags);
	}

	public static short readExternalFlag(DataInput in) throws IOException {
		short flags = in.readShort();
		checkFlagsSupport(flags);
		return flags;
	}

	public static short readExternalObjectFlag(ObjectInput in) throws IOException, ClassNotFoundException {
		return fromExternalObjectFlag(in.readObject());
	}

	public static ImmutableModifierSet readSetExternalObjectFlag(ObjectInput in)
			throws IOException, ClassNotFoundException {
		return forFlags(readExternalObjectFlag(in));
	}

	public static short fromExternalObjectFlag(Object o) {
		short flags = (short) o;
		checkFlagsSupport(flags);
		return flags;
	}

	public static ImmutableModifierSet setFromExternalObjectFlag(Object o) {
		return forFlags(fromExternalObjectFlag(o));
	}

	public static boolean isExternalObjectFlag(Object o) {
		return o instanceof Short;
	}

	public static ImmutableModifierSet readExternalFlagSet(DataInput in) throws IOException {
		short flags = in.readShort();
		checkFlagsSupport(flags);
		return getSetOfFlags(flags);
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
		int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[modifier.ordinal()];
		if (ordinal < 0) {
			throw new AssertionError("Modifier not found in ordinal lookup: " + modifier);
		}
		short f = (short) (1 << ordinal);
		short nf = (short) (this.flags | f);
		if (nf == this.flags) {
			return this;
		}
		return getSetOfFlags(nf);
	}

	public ImmutableModifierSet added(Set<Modifier> modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		short nf = (short) (this.flags | getFlag(modifiers));
		if (nf == this.flags) {
			return this;
		}
		return getSetOfFlags(nf);
	}

	public ImmutableModifierSet added(ImmutableModifierSet modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		short nf = (short) (this.flags | modifiers.flags);
		if (nf == this.flags) {
			return this;
		}
		return getSetOfFlags(nf);
	}

	public ImmutableModifierSet added(Modifier... modifiers) {
		Objects.requireNonNull(modifiers, "modifiers");
		short nf = this.flags;
		for (Modifier m : modifiers) {
			Objects.requireNonNull(m, "modifier");
			int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("Modifier not found in ordinal lookup: " + m);
			}
			short f = (short) (1 << ordinal);
			nf |= f;
		}
		if (nf == this.flags) {
			return this;
		}
		return getSetOfFlags(nf);
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
				Modifier result = ENUM_VALUES[modidx];
				idx++;
				itFlags >>= 1;
				if (result == null) {
					throw new ModifierNotFoundException(ENUM_NAMES[modidx], ENUM_NOTFOUND_EXCEPTIONS[modidx]);
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
		int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[((Modifier) o).ordinal()];
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
			Modifier m = ENUM_VALUES[idx];
			if (m == null) {
				throw new ModifierNotFoundException(ENUM_NAMES[idx], ENUM_NOTFOUND_EXCEPTIONS[idx]);
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
		for (int i = 0; i < ENUM_VALUES.length; i++) {
			short f = (short) (1 << i);
			if (((this.flags & f) == f)) {
				Modifier m = ENUM_VALUES[i];
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
		for (int i = 0; i < ENUM_NAMES.length; i++) {
			short f = (short) (1 << i);
			if (((this.flags & f) == f)) {
				if (sb.length() > 1) {
					sb.append(", ");
				}
				sb.append(ENUM_NAMES[i]);
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
