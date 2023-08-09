public class XMLEncryptionException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public XMLEncryptionException() {
      super();
   }
   public XMLEncryptionException(String _msgID) {
      super(_msgID);
   }
   public XMLEncryptionException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public XMLEncryptionException(String _msgID,
                                              Exception _originalException) {
      super(_msgID, _originalException);
   }
   public XMLEncryptionException(String _msgID, Object exArgs[],
                                              Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
