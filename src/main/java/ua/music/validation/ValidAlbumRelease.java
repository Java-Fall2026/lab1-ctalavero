package ua.music.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AlbumReleaseValidator.class)
public @interface ValidAlbumRelease {
    String message() default "Реліз альбому не може передувати дебюту виконавця";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
