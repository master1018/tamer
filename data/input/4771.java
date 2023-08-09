public class InvalidCanonicalizerException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public InvalidCanonicalizerException() {
      super();
   }
   public InvalidCanonicalizerException(String _msgID) {
      super(_msgID);
   }
   public InvalidCanonicalizerException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public InvalidCanonicalizerException(String _msgID,
                                        Exception _originalException) {
      super(_msgID, _originalException);
   }
   public InvalidCanonicalizerException(String _msgID, Object exArgs[],
                                        Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
