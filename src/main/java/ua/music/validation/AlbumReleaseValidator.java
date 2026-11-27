package ua.music.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.music.model.Album;

public class AlbumReleaseValidator implements ConstraintValidator<ValidAlbumRelease, Album> {
    @Override
    public boolean isValid(Album album, ConstraintValidatorContext context) {
        if (album == null || album.artist() == null || album.artist().debutYear() == null
                || album.releaseDate() == null) {
            return true;
        }
        if (album.releaseDate().getYear() >= album.artist().debutYear()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("releaseDate").addConstraintViolation();
        return false;
    }
}
