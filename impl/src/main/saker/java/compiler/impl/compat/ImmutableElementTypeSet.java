package saker.java.compiler.impl.compat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.api.processing.exc.ElementTypeNotFoundException;

public class ImmutableElementTypeSet extends AbstractSet<ElementType> {
	private static final ImmutableElementTypeSet EMPTY_INSTANCE = new ImmutableElementTypeSet((short) 0);

	//define our own values array, as newer JDKs can add new values, which can distrupt order or ordinal values
	//if new values are introduced, export this array to a JDK version dependent bundle. only appending to it
	//      THE ORDER IN THIS ARRAY IS NOT TO BE MODIFIED FOR SERIALIZATION COMPATIBILITY
	private static final String[] ENUM_NAMES = { "TYPE", // 0 
			"FIELD", // 1
			"METHOD", // 2
			"PARAMETER", // 3
			"CONSTRUCTOR", // 4
			"LOCAL_VARIABLE", // 5
			"ANNOTATION_TYPE", // 6
			"PACKAGE", // 7
			"TYPE_PARAMETER", // 8
			"TYPE_USE", // 9
			"MODULE", // 10
			"RECORD_COMPONENT", // 11
	};
	private static final ElementType[] ENUM_VALUES = new ElementType[ENUM_NAMES.length];

	private static final IllegalArgumentException[] ENUM_NOTFOUND_EXCEPTIONS = new IllegalArgumentException[ENUM_NAMES.length];

	//this array is a dynamic lookup table that converts the ordinals of enums to the index in ENUM_VALUES
	private static final int[] ENUM_ORDINAL_INDEX_LOOKUP;

	private static final Map<String, Short> NAME_FLAGS_MAP = new TreeMap<>();

	private static final short ALL_RECOGNIZED_FLAGS_MASK;
	static {
		if (ENUM_VALUES.length > 16) {
			throw new AssertionError(
					"Too many element types. Need higher precision representation: " + ENUM_VALUES.length);
		}
		for (int i = 0; i < ENUM_NAMES.length; i++) {
			try {
				ENUM_VALUES[i] = ElementType.valueOf(ENUM_NAMES[i]);
			} catch (IllegalArgumentException e) {
				//an enum was not found in the current JVM
				//running on older version, not supported 
				ENUM_NOTFOUND_EXCEPTIONS[i] = e;
			}
		}

		ENUM_ORDINAL_INDEX_LOOKUP = new int[Math.max(ENUM_VALUES.length, ElementType.values().length)];
		Arrays.fill(ENUM_ORDINAL_INDEX_LOOKUP, -1);
		short allrecognized = 0;
		for (int i = 0; i < ENUM_VALUES.length; i++) {
			ElementType m = ENUM_VALUES[i];
			short f = (short) (1 << i);
			if (m != null) {
				//a value may not be supported in the current VM
				ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()] = i;
			}
			allrecognized |= f;
			NAME_FLAGS_MAP.put(ENUM_NAMES[i], f);
		}
		ALL_RECOGNIZED_FLAGS_MASK = allrecognized;
	}
	private final short flags;

	private ImmutableElementTypeSet(short flags) {
		this.flags = flags;
	}

	public static ImmutableElementTypeSet empty() {
		return EMPTY_INSTANCE;
	}

	public static ImmutableElementTypeSet get(Set<ElementType> types) {
		if (types == null) {
			return null;
		}
		if (types instanceof ImmutableElementTypeSet) {
			return (ImmutableElementTypeSet) types;
		}
		short f = getFlag(types);
		if (f == 0) {
			return EMPTY_INSTANCE;
		}
		return new ImmutableElementTypeSet(f);
	}

	public static ImmutableElementTypeSet of(ElementType... types) {
		Objects.requireNonNull(types, "types");
		if (types.length == 0) {
			return EMPTY_INSTANCE;
		}
		short f = 0;
		for (ElementType m : types) {
			Objects.requireNonNull(m, "type");
			int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("ElementType not found in ordinal lookup: " + m);
			}
			f |= 1 << ordinal;
		}
		return new ImmutableElementTypeSet(f);
	}

	public static short getFlag(Set<ElementType> types) {
		Objects.requireNonNull(types, "types");
		if (types instanceof ImmutableElementTypeSet) {
			return ((ImmutableElementTypeSet) types).flags;
		}
		short f = 0;
		for (ElementType m : types) {
			Objects.requireNonNull(m, "type");
			int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[m.ordinal()];
			if (ordinal < 0) {
				throw new AssertionError("ElementType not found in ordinal lookup: " + m);
			}
			f |= 1 << ordinal;
		}
		return f;
	}

	public static ImmutableElementTypeSet forCommaSeparatedNames(String names) {
		Objects.requireNonNull(names, "names");
		if (names.isEmpty()) {
			return EMPTY_INSTANCE;
		}
		short f = 0;
		Iterator<? extends CharSequence> it = StringUtils.splitCharSequenceIterator(names, ',');
		while (it.hasNext()) {
			String name = it.next().toString();
			Short eflag = NAME_FLAGS_MAP.get(name);
			if (eflag == null) {
				throw new IllegalArgumentException("Unrecognized ElementType enumeration name: " + name);
			}
			f |= eflag.shortValue();
		}
		return new ImmutableElementTypeSet(f);
	}

	public static ImmutableElementTypeSet forFlags(short flags) {
		checkFlagsSupport(flags);
		return new ImmutableElementTypeSet(flags);
	}

	public static void writeExternalFlag(DataOutput out, short flags) throws IOException {
		out.writeShort(flags);
	}

	public static short readExternalFlag(DataInput in) throws IOException {
		short flags = in.readShort();
		checkFlagsSupport(flags);
		return flags;
	}

	public static ImmutableElementTypeSet readExternalFlagSet(DataInput in) throws IOException {
		short flags = in.readShort();
		checkFlagsSupport(flags);
		return new ImmutableElementTypeSet(flags);
	}

	private static void checkFlagsSupport(short flags) {
		int unsupportedflags = flags & ~ALL_RECOGNIZED_FLAGS_MASK;
		if (unsupportedflags == 0) {
			return;
		}
		throw new IllegalArgumentException("ElementType flags contain unrecognized element types: 0x"
				+ Integer.toHexString(flags) + " with mask: 0x" + Integer.toHexString(ALL_RECOGNIZED_FLAGS_MASK));
	}

	public short getFlags() {
		return flags;
	}

	@Override
	public Iterator<ElementType> iterator() {
		return new Iterator<ElementType>() {
			private short itFlags = flags;
			private int idx = 0;

			@Override
			public ElementType next() {
				if (itFlags == 0) {
					throw new NoSuchElementException();
				}
				while ((itFlags & 1) != 1) {
					++idx;
					itFlags >>= 1;
				}
				int modidx = idx;
				ElementType result = ENUM_VALUES[modidx];
				idx++;
				itFlags >>= 1;
				if (result == null) {
					throw new ElementTypeNotFoundException(ENUM_NAMES[modidx], ENUM_NOTFOUND_EXCEPTIONS[modidx]);
				}
				return result;
			}

			@Override
			public boolean hasNext() {
				return itFlags != 0;
			}
		};
	}

	public boolean containsAny(ImmutableElementTypeSet c) {
		Objects.requireNonNull(c, "element types");
		return (flags & c.flags) != 0;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof ElementType)) {
			return false;
		}
		int ordinal = ENUM_ORDINAL_INDEX_LOOKUP[((ElementType) o).ordinal()];
		if (ordinal < 0) {
			throw new AssertionError("ElementType not found in ordinal lookup: " + o);
		}
		short f = (short) (1 << ordinal);
		return ((flags & f) == f);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof ImmutableElementTypeSet) {
			short cflags = ((ImmutableElementTypeSet) c).flags;
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
	public void forEach(Consumer<? super ElementType> action) {
		Objects.requireNonNull(action, "action");

		short f = this.flags;
		int idx = 0;
		while (f != 0) {
			while ((f & 1) != 1) {
				++idx;
				f >>= 1;
			}
			ElementType m = ENUM_VALUES[idx];
			if (m == null) {
				throw new ElementTypeNotFoundException(ENUM_NAMES[idx], ENUM_NOTFOUND_EXCEPTIONS[idx]);
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
	public boolean removeIf(Predicate<? super ElementType> filter) {
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
				ElementType m = ENUM_VALUES[i];
				if (m == null) {
					//if the enum not found, then add some hash
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
		if (o instanceof ImmutableElementTypeSet) {
			return this.flags == ((ImmutableElementTypeSet) o).flags;
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
