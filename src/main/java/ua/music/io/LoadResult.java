package ua.music.io;

import java.util.List;

public record LoadResult(int read, int added, List<RejectedRecord> rejected) {
    public LoadResult {
        rejected = List.copyOf(rejected);
    }
}
