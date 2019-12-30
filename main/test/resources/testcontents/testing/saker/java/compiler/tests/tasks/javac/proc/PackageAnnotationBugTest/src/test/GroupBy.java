package test;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target({ ElementType.TYPE, ElementType.PACKAGE })
public @interface GroupBy {
	public int value() default 0;
}
