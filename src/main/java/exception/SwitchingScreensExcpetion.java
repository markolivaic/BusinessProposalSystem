package exception;

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
  }
}
