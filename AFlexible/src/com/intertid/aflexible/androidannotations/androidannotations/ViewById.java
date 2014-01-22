package com.intertid.aflexible.androidannotations.androidannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ViewById {

	int value();

	String click() default "";

	String longClick() default "";

	String itemClick() default "";

	String itemLongClick() default "";
}
