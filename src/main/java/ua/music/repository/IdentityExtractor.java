package ua.music.repository;

@FunctionalInterface
public interface IdentityExtractor<T> {
    String identityOf(T item);
}
