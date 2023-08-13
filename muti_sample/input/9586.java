public class AlgorithmAlreadyRegisteredException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public AlgorithmAlreadyRegisteredException() {
      super();
   }
   public AlgorithmAlreadyRegisteredException(String _msgID) {
      super(_msgID);
   }
   public AlgorithmAlreadyRegisteredException(String _msgID, Object exArgs[]) {
      super(_msgID, exArgs);
   }
   public AlgorithmAlreadyRegisteredException(String _msgID,
                                              Exception _originalException) {
      super(_msgID, _originalException);
   }
   public AlgorithmAlreadyRegisteredException(String _msgID, Object exArgs[],
                                              Exception _originalException) {
      super(_msgID, exArgs, _originalException);
   }
}
