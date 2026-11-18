package ua.music.model;

import ua.music.util.MusicUtils;

public class MusicUser {
    private final String accountEmail;
    private final String username;
    private Subscription subscription;

    private MusicUser(String accountEmail, String username, Subscription subscription) {
        this.accountEmail = MusicUtils.normalizeEmail(accountEmail);
        this.username = MusicUtils.requireNotBlank(username, "Username");
        setSubscription(subscription);
    }

    public static MusicUser of(String accountEmail, String username, Subscription subscription) {
        return new MusicUser(accountEmail, username, subscription);
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public String getUsername() {
        return username;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public final void setSubscription(Subscription subscription) {
        this.subscription = MusicUtils.requireNotNull(subscription, "Subscription");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        MusicUser that = (MusicUser) other;
        return accountEmail.equals(that.accountEmail);
    }

    @Override
    public int hashCode() {
        return accountEmail.hashCode();
    }

    @Override
    public String toString() {
        return "MusicUser{"
                + "accountEmail=" + accountEmail
                + ", username=" + username
                + ", subscription=" + subscription
                + '}';
    }
}
