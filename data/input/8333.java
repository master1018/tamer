public class CanonicalizationException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public CanonicalizationException() {
      super();
   }
   public CanonicalizationException(String _msgID) {
      super(_msgID);
   }
   public CanonicalizationException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public CanonicalizationException(String _msgID, Exception _originalException) {
      super(_msgID, _originalException);
   }
   public CanonicalizationException(String _msgID, Object exArgs[],
                                    Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
