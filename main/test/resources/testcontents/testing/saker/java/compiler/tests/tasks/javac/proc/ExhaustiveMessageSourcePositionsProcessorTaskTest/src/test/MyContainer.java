package test;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

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
public @interface MyContainer {
	public MyRepeatable[] value();
}