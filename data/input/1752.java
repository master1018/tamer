public class InvalidKeyResolverException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public InvalidKeyResolverException() {
      super();
   }
   public InvalidKeyResolverException(String _msgID) {
      super(_msgID);
   }
   public InvalidKeyResolverException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public InvalidKeyResolverException(String _msgID,
                                      Exception _originalException) {
      super(_msgID, _originalException);
   }
   public InvalidKeyResolverException(String _msgID, Object exArgs[],
                                      Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
