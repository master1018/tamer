public class ReferenceNotInitializedException extends XMLSignatureException {
        private static final long serialVersionUID = 1L;
   public ReferenceNotInitializedException() {
      super();
   }
   public ReferenceNotInitializedException(String _msgID) {
      super(_msgID);
   }
   public ReferenceNotInitializedException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public ReferenceNotInitializedException(String _msgID,
                                           Exception _originalException) {
      super(_msgID, _originalException);
   }
   public ReferenceNotInitializedException(String _msgID, Object exArgs[],
                                           Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
