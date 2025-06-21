package exception;

public class ProposalSearchException extends RuntimeException{
    public ProposalSearchException() {
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
