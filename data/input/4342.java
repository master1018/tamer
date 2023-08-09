public class ConstructionException extends RuntimeException {
  public ConstructionException() {
    super();
  }
  public ConstructionException(String message) {
    super(message);
  }
  public ConstructionException(Throwable e) {
    super(e);
  }
  public ConstructionException(String message, Throwable e) {
    super(message, e);
  }
}
