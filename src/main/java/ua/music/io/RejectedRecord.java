package ua.music.io;

import java.util.List;

public record RejectedRecord(int recordNumber, List<String> errors) {
    public RejectedRecord {
        errors = List.copyOf(errors);
    }
}
