package io.github.voduku.validator;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;

/**
 * @author VuDo
 * @since 5/1/2021
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.FIELD})
@Null
@NotBlank
@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = {})
public @interface NullOrNotBlank {

  String message() default "{javax.validation.constraints.Null.message} or {javax.validation.constraints.NotBlank.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
