package ua.music.model;

import ua.common.BaseEntity;
import ua.music.util.MusicUtils;

public class Artist extends BaseEntity {
    private final String artistCode;
    private final String stageName;
    private final String country;
    private final int debutYear;

    public Artist(String artistCode, String stageName, String country, int debutYear) {
        super();
        this.artistCode = MusicUtils.requireNotBlank(artistCode, "Artist code");
        this.stageName = MusicUtils.requireNotBlank(stageName, "Stage name");
        this.country = MusicUtils.requireNotBlank(country, "Country");
        this.debutYear = MusicUtils.validateDebutYear(debutYear);
    }

    public String getArtistCode() {
        return artistCode;
    }

    public String getStageName() {
        return stageName;
    }

    public String getCountry() {
        return country;
    }

    public int getDebutYear() {
        return debutYear;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Artist that = (Artist) other;
        return artistCode.equals(that.artistCode);
    }

    @Override
    public int hashCode() {
        return artistCode.hashCode();
    }

    @Override
    public String toString() {
        return "Artist{"
                + "artistCode=" + artistCode
                + ", stageName=" + stageName
                + ", country=" + country
                + ", debutYear=" + debutYear
                + ", createdAt=" + createdAt
                + '}';
    }
}
