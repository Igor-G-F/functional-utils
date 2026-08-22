package io.github.igorgf;

import org.junit.jupiter.api.DisplayNameGeneration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@DisplayNameGeneration(GivenWhenThenGenerator.class)
public @interface GivenWhenThen {
    String[] given() default {};
    String[] when()  default {};
    String[] then()  default {};
}
