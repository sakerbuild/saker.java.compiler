package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ 
	ElementType.TYPE, 
})
public @interface MyAnnot {
	public int value() default 0;
	
	public int[] valarr() default { 
		0,
		1, 
		2, 
		3 };

	public SecondAnnot[] nested() default {};
}