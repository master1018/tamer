public class TransformationException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public TransformationException() {
      super();
   }
   public TransformationException(String _msgID) {
      super(_msgID);
   }
   public TransformationException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public TransformationException(String _msgID, Exception _originalException) {
      super(_msgID, _originalException);
   }
   public TransformationException(String _msgID, Object exArgs[],
                                  Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
