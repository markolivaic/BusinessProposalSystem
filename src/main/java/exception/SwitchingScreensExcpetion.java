package exception;

/**
 * Označena (checked) iznimka koja se baca kada dođe do greške prilikom
 * prijelaza između različitih ekrana (prizora) u JavaFX aplikaciji,
 * najčešće zbog problema s učitavanjem FXML datoteke.
 */
public class SwitchingScreensExcpetion extends Exception {
    public SwitchingScreensExcpetion(String message) {
        super(message);
    }

    public SwitchingScreensExcpetion(String message, Throwable cause) {
        super(message, cause);
    }

    public SwitchingScreensExcpetion(Throwable cause) {
        super(cause);
    }

    public SwitchingScreensExcpetion(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SwitchingScreensExcpetion() {
        super();
    }
}