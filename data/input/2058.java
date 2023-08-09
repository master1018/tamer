public class FontScalerException extends Exception {
    public FontScalerException() {
      super("Font scaler encountered runtime problem.");
    }
    public FontScalerException(String reason) {
      super (reason);
    }
}
