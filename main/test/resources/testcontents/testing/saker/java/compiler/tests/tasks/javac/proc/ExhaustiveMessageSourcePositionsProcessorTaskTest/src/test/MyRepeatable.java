package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ 
	ElementType.TYPE, 
	ElementType.FIELD, 
	ElementType.METHOD, 
	ElementType.PARAMETER, 
	ElementType.CONSTRUCTOR,
	ElementType.LOCAL_VARIABLE, 
	ElementType.ANNOTATION_TYPE, 
	ElementType.PACKAGE, 
	ElementType.TYPE_PARAMETER,
	ElementType.TYPE_USE, 
})
@Repeatable(MyContainer.class)
public @interface MyRepeatable {
	public int value() default 0;
	
}