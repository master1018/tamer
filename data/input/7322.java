public class InvalidDigestValueException extends XMLSignatureException {
        private static final long serialVersionUID = 1L;
   public InvalidDigestValueException() {
      super();
   }
   public InvalidDigestValueException(String _msgID) {
      super(_msgID);
   }
   public InvalidDigestValueException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public InvalidDigestValueException(String _msgID,
                                      Exception _originalException) {
      super(_msgID, _originalException);
   }
   public InvalidDigestValueException(String _msgID, Object exArgs[],
                                      Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
