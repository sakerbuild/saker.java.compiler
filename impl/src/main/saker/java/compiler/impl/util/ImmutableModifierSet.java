package saker.java.compiler.impl.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;

public final class ImmutableModifierSet extends AbstractSet<Modifier> implements Set<Modifier> {
	private static final ImmutableModifierSet EMPTY_INSTANCE = new ImmutableModifierSet((short) 0);

	//define our own modifier values array, as newer JDKs can add new values, which can distrupt order or ordinal values
	//XXX if new values are introduced, export this array to a JDK version dependent bundle. only appending to it
	//      THE ORDER IN THIS ARRAY IS NOT TO BE MODIFIED FOR SERIALIZATION COMPATIBILITY
	private static final Modifier[] MODIFIERS = { Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE,
			Modifier.ABSTRACT, Modifier.DEFAULT, Modifier.STATIC, Modifier.FINAL, Modifier.TRANSIENT, Modifier.VOLATILE,
			Modifier.SYNCHRONIZED, Modifier.NATIVE, Modifier.STRICTFP };

	//this array is a dynamic lookup table that converts the ordinals of Modifier enums to the index in MODIFIERS
	private static final int[] MODIFIER_ORDINAL_INDEX_LOOKUP;

	private static final short ALL_FLAGS_MASK;
	static {
		if (MODIFIERS.length > 16) {
			throw new AssertionError("Too many modifiers. Need higher precision representation: " + MODIFIERS.length);
		}
		MODIFIER_ORDINAL_INDEX_LOOKUP = new int[MODIFIERS.length];
		short allmask = 0;
		for (int i = 0; i < MODIFIERS.length; i++) {
			short f = (short) (1 << i);
			MODIFIER_ORDINAL_INDEX_LOOKUP[MODIFIERS[i].ordinal()] = i;
			allmask |= f;
		}
		ALL_FLAGS_MASK = allmask;
	}

	private short flags;

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
			f |= 1 << MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()];
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
			f |= 1 << MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()];
		}
		return f;
	}

	public static ImmutableModifierSet forFlags(short flags) {
		if ((flags & ~ALL_FLAGS_MASK) != 0) {
			throw new IllegalArgumentException("Modifier flags contain unrecognized modifiers: 0x"
					+ Integer.toHexString(flags) + " with mask: 0x" + Integer.toHexString(ALL_FLAGS_MASK));
		}
		return new ImmutableModifierSet(flags);
	}

	public static void writeExternalFlag(DataOutput out, short flags) throws IOException {
		out.writeShort(flags);
	}

	public static short readExternalFlag(DataInput in) throws IOException {
		short flags = in.readShort();
		if ((flags & ~ALL_FLAGS_MASK) != 0) {
			throw new IllegalArgumentException("Modifier flags contain unrecognized modifiers: 0x"
					+ Integer.toHexString(flags) + " with mask: 0x" + Integer.toHexString(ALL_FLAGS_MASK));
		}
		return flags;
	}

	public static ImmutableModifierSet readExternalFlagSet(DataInput in) throws IOException {
		short flags = in.readShort();
		if ((flags & ~ALL_FLAGS_MASK) != 0) {
			throw new IllegalArgumentException("Modifier flags contain unrecognized modifiers: 0x"
					+ Integer.toHexString(flags) + " with mask: 0x" + Integer.toHexString(ALL_FLAGS_MASK));
		}
		return new ImmutableModifierSet(flags);
	}

	public short getFlags() {
		return flags;
	}

	public ImmutableModifierSet added(Modifier modifier) {
		short f = (short) (1 << MODIFIER_ORDINAL_INDEX_LOOKUP[modifier.ordinal()]);
		short nf = (short) (this.flags | f);
		if (nf == this.flags) {
			return this;
		}
		return new ImmutableModifierSet(nf);
	}

	public ImmutableModifierSet added(Modifier... modifiers) {
		short nf = this.flags;
		for (Modifier m : modifiers) {
			short f = (short) (1 << MODIFIER_ORDINAL_INDEX_LOOKUP[m.ordinal()]);
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
				Modifier result = MODIFIERS[idx];
				idx++;
				itFlags >>= 1;
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
		short f = (short) (1 << MODIFIER_ORDINAL_INDEX_LOOKUP[((Modifier) o).ordinal()]);
		return ((flags & f) == f);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
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
			action.accept(MODIFIERS[idx]);
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
}
