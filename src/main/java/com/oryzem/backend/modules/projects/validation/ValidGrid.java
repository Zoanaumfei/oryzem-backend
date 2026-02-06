package com.oryzem.backend.modules.projects.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = GridValidator.class)
@Documented
public @interface ValidGrid {
    String message() default "Grid must include ALS descriptions for 1..8, only include ALS 1..8, gates ZP5/ELET/ZP7, phases VFF/PVS/SO/TPPA/SOP, and valid dates";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
