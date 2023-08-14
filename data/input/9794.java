public class KeyResolver {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(KeyResolver.class.getName());
   static boolean _alreadyInitialized = false;
   static List _resolverVector = null;
   protected KeyResolverSpi _resolverSpi = null;
   protected StorageResolver _storage = null;
   private KeyResolver(String className)
           throws ClassNotFoundException, IllegalAccessException,
                  InstantiationException {
      this._resolverSpi =
         (KeyResolverSpi) Class.forName(className).newInstance();
      this._resolverSpi.setGlobalResolver(true);
   }
   public static int length() {
      return KeyResolver._resolverVector.size();
   }
   public static void hit(Iterator hintI) {
           ResolverIterator hint = (ResolverIterator) hintI;
           int i = hint.i;
           if (i!=1 && hint.res ==_resolverVector) {
                   List resolverVector=(List)((ArrayList)_resolverVector).clone();
                Object ob=resolverVector.remove(i-1);
                resolverVector.add(0,ob);
                 _resolverVector=resolverVector;
         } else {
         }
   }
   public static final X509Certificate getX509Certificate(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
          List resolverVector = KeyResolver._resolverVector;
      for (int i = 0; i < resolverVector.size(); i++) {
                  KeyResolver resolver=
            (KeyResolver) resolverVector.get(i);
                  if (resolver==null) {
            Object exArgs[] = {
               (((element != null)
                 && (element.getNodeType() == Node.ELEMENT_NODE))
                ? element.getTagName()
                : "null") };
            throw new KeyResolverException("utils.resolver.noClass", exArgs);
         }
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "check resolvability by class " + resolver.getClass());
         X509Certificate cert=resolver.resolveX509Certificate(element, BaseURI, storage);
         if (cert!=null) {
            return cert;
         }
      }
      Object exArgs[] = {
         (((element != null) && (element.getNodeType() == Node.ELEMENT_NODE))
          ? element.getTagName()
          : "null") };
      throw new KeyResolverException("utils.resolver.noClass", exArgs);
   }
   public static final PublicKey getPublicKey(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
          List resolverVector = KeyResolver._resolverVector;
      for (int i = 0; i < resolverVector.size(); i++) {
                  KeyResolver resolver=
            (KeyResolver) resolverVector.get(i);
                  if (resolver==null) {
            Object exArgs[] = {
               (((element != null)
                 && (element.getNodeType() == Node.ELEMENT_NODE))
                ? element.getTagName()
                : "null") };
            throw new KeyResolverException("utils.resolver.noClass", exArgs);
         }
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "check resolvability by class " + resolver.getClass());
         PublicKey cert=resolver.resolvePublicKey(element, BaseURI, storage);
         if (cert!=null) {
                 if (i!=0 && resolverVector==_resolverVector) {
                         resolverVector=(List)((ArrayList)_resolverVector).clone();
                                 Object ob=resolverVector.remove(i);
                                 resolverVector.add(0,ob);
                                 _resolverVector=resolverVector;
                 }
                 return cert;
         }
      }
      Object exArgs[] = {
         (((element != null) && (element.getNodeType() == Node.ELEMENT_NODE))
          ? element.getTagName()
          : "null") };
      throw new KeyResolverException("utils.resolver.noClass", exArgs);
   }
   public static void init() {
      if (!KeyResolver._alreadyInitialized) {
         KeyResolver._resolverVector = new ArrayList(10);
         _alreadyInitialized = true;
      }
   }
   public static void register(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
      KeyResolver._resolverVector.add(new KeyResolver(className));
   }
   public static void registerAtStart(String className) {
      KeyResolver._resolverVector.add(0, className);
   }
   public PublicKey resolvePublicKey(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
      return this._resolverSpi.engineLookupAndResolvePublicKey(element, BaseURI, storage);
   }
   public X509Certificate resolveX509Certificate(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
      return this._resolverSpi.engineLookupResolveX509Certificate(element, BaseURI,
              storage);
   }
   public SecretKey resolveSecretKey(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
      return this._resolverSpi.engineLookupAndResolveSecretKey(element, BaseURI,
              storage);
   }
   public void setProperty(String key, String value) {
      this._resolverSpi.engineSetProperty(key, value);
   }
   public String getProperty(String key) {
      return this._resolverSpi.engineGetProperty(key);
   }
   public boolean understandsProperty(String propertyToTest) {
      return this._resolverSpi.understandsProperty(propertyToTest);
   }
   public String resolverClassName() {
      return this._resolverSpi.getClass().getName();
   }
   static class ResolverIterator implements Iterator {
           List res;
                Iterator it;
                int i;
           public ResolverIterator(List list) {
                res = list;
                it = res.iterator();
        }
                public boolean hasNext() {
                        return it.hasNext();
                }
                public Object next() {
                        i++;
                        KeyResolver resolver = (KeyResolver) it.next();
                      if (resolver==null) {
                         throw new RuntimeException("utils.resolver.noClass");
                      }
                      return resolver._resolverSpi;
                }
                public void remove() {
                }
        };
        public static Iterator iterator() {
                return new ResolverIterator(_resolverVector);
   }
}
