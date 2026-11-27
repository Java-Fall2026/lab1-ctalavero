package ua.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.music.io.CatalogLoader;
import ua.music.model.Album;
import ua.music.model.Artist;
import ua.music.model.Genre;
import ua.music.repository.AlbumRepository;
import ua.music.repository.ArtistRepository;
import ua.music.repository.SortOrder;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ArtistRepository artists = new ArtistRepository();
        new CatalogLoader().loadArtists(Path.of("data/artists.csv")).forEach(artists::addIfAbsent);
        Artist midnight = artists.findByIdentity("ART-01").orElseThrow();
        Artist daftPunk = artists.findByIdentity("ART-02").orElseThrow();

        AlbumRepository albums = new AlbumRepository();
        Album summer = new Album("ALB-01", "Endless Summer", midnight, Genre.ELECTRONIC,
                LocalDate.of(2016, 8, 5));
        Album memories = new Album("ALB-02", "Random Access Memories", daftPunk, Genre.ELECTRONIC,
                LocalDate.of(2013, 5, 17));
        Album jazz = new Album("ALB-03", "Late Night", midnight, Genre.JAZZ,
                LocalDate.of(2021, 1, 1));
        albums.addAll(List.of(jazz, memories, summer));

        System.out.println("Виконавці за природним порядком: "
                + artists.findAllSorted(Comparator.naturalOrder(), "natural artist order"));
        System.out.println("Альбоми за природним порядком: "
                + albums.findAllSorted(Comparator.naturalOrder(), "natural album order"));
        System.out.println("Виконавці за ім’ям: " + artists.sortByStageName());
        System.out.println("Виконавці за роком дебюту, від новіших: " + artists.sortByDebutYearDescending());
        System.out.println("Альбоми за назвою: " + albums.sortByTitle());
        System.out.println("Альбоми за датою релізу, від новіших: " + albums.sortByReleaseDateDescending());
        System.out.println("Ключі виконавців ASC: " + artists.findAllSortedByIdentity(SortOrder.ASC));
        System.out.println("Ключі виконавців DESC: " + artists.findAllSortedByIdentity(SortOrder.DESC));
        System.out.println("Ключі альбомів ASC: " + albums.findAllSortedByIdentity(SortOrder.ASC));
        System.out.println("Ключі альбомів DESC: " + albums.findAllSortedByIdentity(SortOrder.DESC));

        TreeSet<Album> genreSet = new TreeSet<>(Comparator.comparing(Album::genre));
        genreSet.add(summer);
        genreSet.add(memories);
        System.out.println("Два різні альбоми рівні за equals: " + summer.equals(memories));
        System.out.println("Кількість у TreeSet, що порівнює лише жанр: " + genreSet.size());

        System.out.println("Ім’я містить «mid»: " + artists.findByStageName("mid"));
        System.out.println("Дебют із 1990 до 2000: " + artists.findByDebutYearRange(1990, 2000));
        System.out.println("Виконавці зі США з дебютом від 2000: "
                + artists.findByCountryAndDebutYear("USA", 2000));
        System.out.println("Перший виконавець за точним ім’ям: "
                + artists.findFirstByStageName("The Midnight").map(Artist::artistCode).orElse("Не знайдено"));
        System.out.println("До двох різних країн: " + artists.findCountries(2));

        System.out.println("Назва альбому містить «night»: " + albums.findByTitle("night"));
        System.out.println("Релізи з 2015 до 2020: " + albums.findByReleaseDateRange(
                LocalDate.of(2015, 1, 1), LocalDate.of(2020, 12, 31)));
        System.out.println("Електронні альбоми ART-01: "
                + albums.findByGenreAndArtist(Genre.ELECTRONIC, "ART-01"));
        System.out.println("Пошук відсутнього альбому: "
                + albums.findFirstByTitle("Missing album").map(Album::title).orElse("Не знайдено"));
        System.out.println("Виконавці за країнами: " + artists.groupByCountry());
        System.out.println("Кількість альбомів за жанрами: " + albums.countByGenre());
        System.out.println("Сума повних років від релізу всіх альбомів: " + albums.totalAgeYears());

        Stream<Album> stream = albums.stream();
        System.out.println("Перший прохід стріму, кількість альбомів: " + stream.count());
        try {
            stream.count();
        } catch (IllegalStateException e) {
            log.error("Cannot reuse a consumed stream: {}", e.getMessage());
            System.out.println("Повторний прохід стріму відхилено: " + e.getMessage());
        }
        System.out.println("Демонстрацію завершено.");
    }
}
