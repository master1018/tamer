public class ResourceResolverException extends XMLSecurityException {
        private static final long serialVersionUID = 1L;
   public ResourceResolverException(String _msgID, Attr uri, String BaseURI) {
      super(_msgID);
      this._uri = uri;
      this._BaseURI = BaseURI;
   }
   public ResourceResolverException(String _msgID, Object exArgs[], Attr uri,
                                    String BaseURI) {
      super(_msgID, exArgs);
      this._uri = uri;
      this._BaseURI = BaseURI;
   }
   public ResourceResolverException(String _msgID, Exception _originalException,
                                    Attr uri, String BaseURI) {
      super(_msgID, _originalException);
      this._uri = uri;
      this._BaseURI = BaseURI;
   }
   public ResourceResolverException(String _msgID, Object exArgs[],
                                    Exception _originalException, Attr uri,
                                    String BaseURI) {
      super(_msgID, exArgs, _originalException);
      this._uri = uri;
      this._BaseURI = BaseURI;
   }
   Attr _uri = null;
   public void setURI(Attr uri) {
      this._uri = uri;
   }
   public Attr getURI() {
      return this._uri;
   }
   String _BaseURI;
   public void setBaseURI(String BaseURI) {
      this._BaseURI = BaseURI;
   }
   public String getBaseURI() {
      return this._BaseURI;
   }
}
