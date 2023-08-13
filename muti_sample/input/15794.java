public class MissingResourceFailureException extends XMLSignatureException {
        private static final long serialVersionUID = 1L;
   Reference uninitializedReference = null;
   public MissingResourceFailureException(String _msgID, Reference reference) {
      super(_msgID);
      this.uninitializedReference = reference;
   }
   public MissingResourceFailureException(String _msgID, Object exArgs[],
                                          Reference reference) {
      super(_msgID, exArgs);
      this.uninitializedReference = reference;
   }
   public MissingResourceFailureException(String _msgID,
                                          Exception _originalException,
                                          Reference reference) {
      super(_msgID, _originalException);
      this.uninitializedReference = reference;
   }
   public MissingResourceFailureException(String _msgID, Object exArgs[],
                                          Exception _originalException,
                                          Reference reference) {
      super(_msgID, exArgs, _originalException);
      this.uninitializedReference = reference;
   }
   public void setReference(Reference reference) {
      this.uninitializedReference = reference;
   }
   public Reference getReference() {
      return this.uninitializedReference;
   }
}
