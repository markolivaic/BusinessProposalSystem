package model;


public non-sealed class User extends Entitiy {
    private String username;
    private String hashedPassword;
    private boolean isAdmin;

    /**
     * Represents a user in the system.
     */
    private User(Long id, String username, String hashedPassword, boolean isAdmin) {
        super(id);
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.isAdmin = isAdmin;
    }

    /**
     * Gets the username.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }
    /**
     * Gets the password.
     * @return The password.
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Checks if the user is an admin.
     * @return True if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Builder pattern for creating User objects.
     */
    public static class Builder{
        private Long id;
        private String username;
        private String hashedPassword;
        private boolean isAdmin;

        public Builder() {
        }

        public Builder(Long id) {
            this.id = id;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withHashedPassword(String hashedPassword) {
            this.hashedPassword = hashedPassword;
            return this;
        }

        public Builder withIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        /**
         * Builds and returns a new User object.
         * @return The created User instance.
         */
        public User build(){
            return new User(id, username, hashedPassword, isAdmin);
        }
    }
}
