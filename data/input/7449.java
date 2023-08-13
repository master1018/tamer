public class XMLSignatureException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public XMLSignatureException() {
      super();
   }
   public XMLSignatureException(String _msgID) {
      super(_msgID);
   }
   public XMLSignatureException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public XMLSignatureException(String _msgID, Exception _originalException) {
      super(_msgID, _originalException);
   }
   public XMLSignatureException(String _msgID, Object exArgs[],
                                Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
