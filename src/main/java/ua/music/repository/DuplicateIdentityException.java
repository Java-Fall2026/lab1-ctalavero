package ua.music.repository;

public class DuplicateIdentityException extends IllegalStateException {
    public DuplicateIdentityException(String identity, String repositoryName) {
        super("Identity '" + identity + "' already exists in repository '" + repositoryName + "'");
    }
}
