package com.oryzem.backend.modules.vehicles.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = VehicleProjectValidator.class)
@Documented
public @interface ValidVehicleProject {
    String message() default "Vehicle project request has invalid milestone dates";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
