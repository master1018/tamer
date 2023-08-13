public class InvalidSignatureValueException extends XMLSignatureException {
        private static final long serialVersionUID = 1L;
   public InvalidSignatureValueException() {
      super();
   }
   public InvalidSignatureValueException(String _msgID) {
      super(_msgID);
   }
   public InvalidSignatureValueException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public InvalidSignatureValueException(String _msgID,
                                         Exception _originalException) {
      super(_msgID, _originalException);
   }
   public InvalidSignatureValueException(String _msgID, Object exArgs[],
                                         Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
