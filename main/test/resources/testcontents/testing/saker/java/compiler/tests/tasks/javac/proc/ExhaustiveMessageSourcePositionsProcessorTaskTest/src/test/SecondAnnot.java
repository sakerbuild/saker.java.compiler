package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ 
	ElementType.TYPE, 
})
public @interface SecondAnnot {
	public int value() default 0;
	
	public int[] valarr() default {};

	public Target targ() default @Target(ElementType.FIELD);
}