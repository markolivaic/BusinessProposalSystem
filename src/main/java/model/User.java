package model;

/**
 * Predstavlja korisnika sustava.
 * Nasljeđuje klasu {@link Entitiy} i sadrži korisničko ime, hashiranu lozinku i informaciju o administratorskim pravima.
 * Koristi Builder pattern za stvaranje instanci.
 */
public non-sealed class User extends Entitiy {
    private final String username;
    private final String hashedPassword;
    private final boolean isAdmin;

    /**
     * Privatni konstruktor koji se poziva putem Buildera.
     * @param id Jedinstveni identifikator korisnika.
     * @param username Korisničko ime.
     * @param hashedPassword Hashirana lozinka.
     * @param isAdmin Status administratorskih prava.
     */
    private User(Long id, String username, String hashedPassword, boolean isAdmin) {
        super(id);
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.isAdmin = isAdmin;
    }

    /**
     * Dohvaća korisničko ime.
     * @return Korisničko ime.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Dohvaća hashiranu lozinku korisnika.
     * @return Hashirana lozinka.
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Provjerava ima li korisnik administratorska prava.
     * @return {@code true} ako je korisnik administrator, inače {@code false}.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Builder pattern za stvaranje {@link User} objekata.
     * Omogućuje postepeno i čitljivo kreiranje kompleksnih objekata.
     */
    public static class Builder{
        private Long id;
        private String username;
        private String hashedPassword;
        private boolean isAdmin;

        /**
         * Prazan konstruktor za Builder.
         */
        public Builder() {
        }

        /**
         * Konstruktor za Builder koji odmah postavlja ID.
         * @param id Jedinstveni identifikator korisnika.
         */
        public Builder(Long id) {
            this.id = id;
        }

        /**
         * Postavlja korisničko ime.
         * @param username Korisničko ime.
         * @return Referenca na ovaj Builder za lančano pozivanje.
         */
        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Postavlja hashiranu lozinku.
         * @param hashedPassword Hashirana lozinka.
         * @return Referenca na ovaj Builder za lančano pozivanje.
         */
        public Builder withHashedPassword(String hashedPassword) {
            this.hashedPassword = hashedPassword;
            return this;
        }

        /**
         * Postavlja administratorski status.
         * @param isAdmin {@code true} ako je korisnik admin, inače {@code false}.
         * @return Referenca na ovaj Builder za lančano pozivanje.
         */
        public Builder withIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        /**
         * Gradi i vraća novi {@link User} objekt s postavljenim svojstvima.
         * @return Kreirana {@code User} instanca.
         */
        public User build(){
            return new User(id, username, hashedPassword, isAdmin);
        }
    }
}