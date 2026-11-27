package ua.music.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ua.music.validation.ValidAlbumRelease;

import java.time.LocalDate;
import java.util.Comparator;

@ValidAlbumRelease
public record Album(
        @NotBlank(message = "Код альбому не може бути порожнім")
        @Size(max = 30, message = "Код альбому має містити не більше 30 символів")
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9-]*", message = "Код альбому має містити літери, цифри або дефіс")
        String upcCode,
        @NotBlank(message = "Назва альбому не може бути порожньою")
        @Size(max = 150, message = "Назва альбому має містити не більше 150 символів")
        String title,
        @NotNull(message = "Виконавець має бути вказаний") @Valid
        Artist artist,
        @NotNull(message = "Жанр має бути вказаний")
        Genre genre,
        @NotNull(message = "Дата релізу має бути вказана")
        @PastOrPresent(message = "Дата релізу не може бути в майбутньому")
        LocalDate releaseDate) implements Comparable<Album> {

    @Override
    public int compareTo(Album other) {
        return Comparator.comparing(Album::upcCode)
                .thenComparing(Album::title)
                .thenComparing(Album::artist)
                .thenComparing(Album::genre)
                .thenComparing(Album::releaseDate)
                .compare(this, other);
    }
}
