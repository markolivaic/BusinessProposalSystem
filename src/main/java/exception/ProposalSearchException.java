package exception;

/**
 * Neoznačena (unchecked) iznimka koja se baca specifično tijekom
 * procesa pretrage prijedloga, ako dođe do neočekivane greške.
 */
public class ProposalSearchException extends RuntimeException {
    public ProposalSearchException() {
        super();
    }

    public ProposalSearchException(String message) {
        super(message);
    }

    public ProposalSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProposalSearchException(Throwable cause) {
        super(cause);
    }

    public ProposalSearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}