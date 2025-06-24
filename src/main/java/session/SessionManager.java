package session;

/**
 * Upravitelj sesije (SessionManager) je statička klasa koja prati stanje prijave korisnika.
 * Pohranjuje ID prijavljenog korisnika i njegovu rolu (admin ili ne).
 * Privatni konstruktor sprječava instanciranje ove pomoćne klase.
 */
public class SessionManager {
    private static Long loggedInUserId;
    private static boolean isAdmin;

    /**
     * Privatni konstruktor kako bi se spriječilo stvaranje instanci.
     */
    private SessionManager() {
    }

    /**
     * Prijavljuje korisnika postavljanjem njegovog ID-ja i statusa administratora.
     *
     * @param userId ID korisnika koji se prijavljuje.
     * @param isAdminStatus {@code true} ako je korisnik administrator, inače {@code false}.
     */
    public static void login(Long userId, boolean isAdminStatus) {
        loggedInUserId = userId;
        isAdmin = isAdminStatus;
    }

    /**
     * Dohvaća ID trenutno prijavljenog korisnika.
     *
     * @return ID prijavljenog korisnika, ili {@code null} ako nitko nije prijavljen.
     */
    public static Long getLoggedInUserId() {
        return loggedInUserId;
    }

    /**
     * Provjerava ima li trenutno prijavljeni korisnik administratorska prava.
     *
     * @return {@code true} ako je korisnik administrator, inače {@code false}.
     */
    public static boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Provjerava je li korisnik trenutno prijavljen.
     *
     * @return {@code true} ako je korisnik prijavljen, inače {@code false}.
     */
    public static boolean isUserLoggedIn() {
        return loggedInUserId != null;
    }

    /**
     * Odjavljuje trenutnog korisnika poništavanjem podataka o sesiji.
     */
    public static void logout() {
        loggedInUserId = null;
        isAdmin = false;
    }
}