public abstract class KeyResolverSpi {
   public boolean engineCanResolve(Element element, String BaseURI,
                                                    StorageResolver storage) {
           throw new UnsupportedOperationException();
   }
   public PublicKey engineResolvePublicKey(
      Element element, String BaseURI, StorageResolver storage)
         throws KeyResolverException {
           throw new UnsupportedOperationException();
    };
    public PublicKey engineLookupAndResolvePublicKey(
      Element element, String BaseURI, StorageResolver storage)
         throws KeyResolverException {
        KeyResolverSpi tmp = cloneIfNeeded();
        if (!tmp.engineCanResolve(element, BaseURI, storage))
                return null;
            return tmp.engineResolvePublicKey(element, BaseURI, storage);
    }
    private KeyResolverSpi cloneIfNeeded() throws KeyResolverException {
        KeyResolverSpi tmp=this;
        if (globalResolver) {
                try {
                        tmp = (KeyResolverSpi) getClass().newInstance();
                } catch (InstantiationException e) {
                        throw new KeyResolverException("",e);
                } catch (IllegalAccessException e) {
                        throw new KeyResolverException("",e);
                }
        }
        return tmp;
    }
    public X509Certificate engineResolveX509Certificate(
       Element element, String BaseURI, StorageResolver storage)
          throws KeyResolverException{
                   throw new UnsupportedOperationException();
    };
    public X509Certificate engineLookupResolveX509Certificate(
      Element element, String BaseURI, StorageResolver storage)
         throws KeyResolverException {
        KeyResolverSpi tmp = cloneIfNeeded();
        if (!tmp.engineCanResolve(element, BaseURI, storage))
                return null;
        return tmp.engineResolveX509Certificate(element, BaseURI, storage);
    }
    public SecretKey engineResolveSecretKey(
       Element element, String BaseURI, StorageResolver storage)
          throws KeyResolverException{
                   throw new UnsupportedOperationException();
    };
   public SecretKey engineLookupAndResolveSecretKey(
      Element element, String BaseURI, StorageResolver storage)
         throws KeyResolverException {
           KeyResolverSpi tmp = cloneIfNeeded();
           if (!tmp.engineCanResolve(element, BaseURI, storage))
                   return null;
                return tmp.engineResolveSecretKey(element, BaseURI, storage);
   }
   protected java.util.Map _properties = null;
   protected boolean globalResolver=false;
   public void engineSetProperty(String key, String value) {
           if (_properties==null)
                   _properties=new HashMap();
      this._properties.put(key, value);
   }
   public String engineGetProperty(String key) {
           if (_properties==null)
                   return null;
      return (String) this._properties.get(key);
   }
   public boolean understandsProperty(String propertyToTest) {
           if (_properties==null)
                   return false;
      return  this._properties.get(propertyToTest)!=null;
   }
   public void setGlobalResolver(boolean globalResolver) {
        this.globalResolver = globalResolver;
   }
}
