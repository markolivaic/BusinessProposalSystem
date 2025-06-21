package session;

/**
 * The SessionManager class handles user session management.
 * It keeps track of the currently logged-in user and their role.
 */
public class SessionManager {
    private static Long loggedInUserId;
    private static boolean isAdmin;

    private SessionManager() {
    }

    /**
     * Logs in a user by setting their user ID and role.
     *
     * @param userId The ID of the logged-in user.
     * @param isAdminStatus True if the user is an admin, false otherwise.
     */
    public static void login(Long userId, boolean isAdminStatus) {
        loggedInUserId = userId;
        isAdmin = isAdminStatus;
    }

    /**
     * Retrieves the ID of the logged-in user.
     *
     * @return The logged-in user ID.
     */
    public static Long getLoggedInUserId() {
        return loggedInUserId;
    }

    /**
     * Checks if the logged-in user is an admin.
     *
     * @return True if the user is an admin, false otherwise.
     */
    public static boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Checks if there is a user currently logged in.
     *
     * @return True if a user is logged in, false otherwise.
     */
    public static boolean isUserLoggedIn() {
        return loggedInUserId != null;
    }

    /**
     * Logs out the current user by resetting session values.
     */
    public static void logout() {
        loggedInUserId = null;
        isAdmin = false;
    }
}
