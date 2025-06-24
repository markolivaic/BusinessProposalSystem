package exception;

/**
 * Označena (checked) iznimka koja se baca kada operacija nad repozitorijem
 * ne vrati očekivani rezultat (npr. pretraga po ID-ju ne pronađe entitet).
 */
public class EmptyRepositoryResultException extends Exception {
    public EmptyRepositoryResultException() {
        super();
    }

    public EmptyRepositoryResultException(String message) {
        super(message);
    }

    public EmptyRepositoryResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyRepositoryResultException(Throwable cause) {
        super(cause);
    }

    public EmptyRepositoryResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}