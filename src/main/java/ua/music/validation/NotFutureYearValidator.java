package ua.music.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class NotFutureYearValidator implements ConstraintValidator<NotFutureYear, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || value <= Year.now(context.getClockProvider().getClock()).getValue();
    }
}
