package ua.music.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ua.music.validation.NotFutureYear;

import java.util.Comparator;

public record Artist(
        @NotBlank(message = "Код виконавця не може бути порожнім")
        @Size(max = 30, message = "Код виконавця має містити не більше 30 символів")
        String artistCode,
        @NotBlank(message = "Ім’я виконавця не може бути порожнім")
        @Size(max = 120, message = "Ім’я виконавця має містити не більше 120 символів")
        String stageName,
        @NotBlank(message = "Країна не може бути порожньою")
        @Pattern(regexp = "[\\p{L}][\\p{L} .'-]*", message = "Назва країни має містити літери")
        String country,
        @NotNull(message = "Рік дебюту має бути вказаний")
        @Min(value = 1900, message = "Рік дебюту має бути не раніше 1900")
        @NotFutureYear
        Integer debutYear) implements Comparable<Artist> {

    @Override
    public int compareTo(Artist other) {
        return Comparator.comparing(Artist::artistCode)
                .thenComparing(Artist::stageName)
                .thenComparing(Artist::country)
                .thenComparingInt(Artist::debutYear)
                .compare(this, other);
    }
}
