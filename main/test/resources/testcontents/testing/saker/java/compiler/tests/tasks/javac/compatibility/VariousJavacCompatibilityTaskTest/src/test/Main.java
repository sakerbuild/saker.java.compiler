package test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@interface FieldAnnot {

}

public class Main {
	public static final byte CONST0 = 123;
	public static final byte CONST1 = (int) 123 + 3;
	public static final short CONST2 = 123;
	public static final int CONST3 = 123;
	public static final long CONST4 = 123;
	public static final float CONST5 = 123;
	public static final double CONST6 = 123;
	public static final boolean CONST7 = true;
	public static final char CONST8 = '\0' + 2;
	public static final String CONST9 = "123";
	public static final java.lang.String CONST10 = "123";
	public static final String CONST11 = "123" + 123;
	public static final java.lang.String CONST12 = "123" + 123;
	
	//test instance constants as well
	public final byte instance_CONST0 = 123;
	public final byte instance_CONST1 = (int) 123 + 3;
	public final short instance_CONST2 = 123;
	public final int instance_CONST3 = 123;
	public final long instance_CONST4 = 123;
	public final float instance_CONST5 = 123;
	public final double instance_CONST6 = 123;
	public final boolean instance_CONST7 = true;
	public final char instance_CONST8 = '\0' + 2;
	public final String instance_CONST9 = "123";
	public final java.lang.String instance_CONST10 = "123";
	public final String instance_CONST11 = "123" + 123;
	public final java.lang.String instance_CONST12 = "123" + 123;

	/**
	 * docdynamic
	 */
	public static final Object dynamic;
	static {
		dynamic = new Object();
	}
	{
		//initer
	}

	/**
	 * docmain
	 */
	public static void main(String... args) {
		System.out.println("Main.main()");
	}

	public static void finalParamed(final int i) {
	}

	public void rec(Main this, Main arg) {
	}

	//post-declaration array return type
	public int iReturnAnIntArrayReallyIDo()[] {
		return new int[0];
	}
}

/**
 * docenum
 */
enum MyEnum {
	/**
	 * docFirst
	 */
	FIRST,
	SECOND,
	/**
	 * docImpl
	 */
	IMPL {

	},
	@FieldAnnot
	ANNOTED,
	@FieldAnnot
	ANNOTEDANONYMOUS {
	};

}

/**
 * docclass
 */
class RecursiveTypeParams<T extends RecursiveTypeParams<T>> {
	RecursiveTypeParams<? extends T> wcext;
	RecursiveTypeParams<? super T> wcsup;
	RecursiveTypeParams<T> t;
	RecursiveTypeParams<?> wct;
}

/**
 * docitf
 */
interface MyItf {
	int C = 0;
	public int PC = 0;
	static int SC = 0;
	final int FC = 0;

	public static int PSC = 0;
	public final int PFC = 0;
	static final int SFC = 0;

	public static final int PSFC = 0;

	void f();

	public void pf();

	static void sf() {
	}

	public static void psf() {
	}

	default void df() {
	}

	public default void pdf() {
	}

	abstract void af();

	public abstract void paf();
}

/**
 * docannot
 */
@interface MyAnnot {
	int C = 0;
	public int PC = 0;
	static int SC = 0;
	final int FC = 0;

	public static int PSC = 0;
	public final int PFC = 0;
	static final int SFC = 0;

	public static final int PSFC = 0;

	/**
	 * doci
	 */
	int i();

	int di() default 1;

	public int pi();

	public int pdi() default 1;

	public abstract int pai();

	public abstract int padi() default 1;

	abstract int ai();

	abstract int adi() default 1;

	int[] ia() default { 1, 2, 3 };

	MyEnum e() default MyEnum.FIRST;

	MyEnum[] ea() default { MyEnum.FIRST, MyEnum.SECOND };

	Class<?> c() default List.class;

	Class<?>[] ca() default { List.class, Map.class };
}

class TypeResolutions {
	public Map.Entry<?, ?> f1;
	public Inner.In2 f2;
	public TypeResolutions.Inner.In2 f3;
	public Paramed<TypeResolutions.Inner.In2>.Inner<Paramed<Inner.In2>.Inner<TypeResolutions.Inner>> f4;

	public Paramed<TypeResolutions.Inner.In2>.Inner<Paramed<Inner.In2>.Inner<TypeResolutions.Inner>> f() {
		return null;
	}

	class Inner {
		class In2 {
		}

	}
}

class Paramed<T> {
	class Inner<U> {
		public Paramed<TypeResolutions.Inner.In2>.Inner<Paramed<TypeResolutions.Inner.In2>.Inner<TypeResolutions.Inner>> x2;
	}

	public Paramed<TypeResolutions.Inner.In2>.Inner<Paramed<TypeResolutions.Inner.In2>.Inner<TypeResolutions.Inner>> x1;
}
