public class InvalidTransformException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public InvalidTransformException() {
      super();
   }
   public InvalidTransformException(String _msgId) {
      super(_msgId);
   }
   public InvalidTransformException(String _msgId, Object exArgs[]) {
      super(_msgId, exArgs);
   }
   public InvalidTransformException(String _msgId, Exception _originalException) {
      super(_msgId, _originalException);
   }
   public InvalidTransformException(String _msgId, Object exArgs[],
                                    Exception _originalException) {
      super(_msgId, exArgs, _originalException);
   }
}
