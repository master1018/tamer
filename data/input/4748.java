public class ResourceResolver {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(ResourceResolver.class.getName());
   static boolean _alreadyInitialized = false;
   static List _resolverVector = null;
   static boolean allThreadSafeInList=true;
   protected ResourceResolverSpi _resolverSpi = null;
   private ResourceResolver(String className)
           throws ClassNotFoundException, IllegalAccessException,
                  InstantiationException {
      this._resolverSpi =
         (ResourceResolverSpi) Class.forName(className).newInstance();
   }
   public ResourceResolver(ResourceResolverSpi resourceResolver) {
      this._resolverSpi = resourceResolver;
   }
   public static final ResourceResolver getInstance(Attr uri, String BaseURI)
           throws ResourceResolverException {
      int length=ResourceResolver._resolverVector.size();
      for (int i = 0; i < length; i++) {
                  ResourceResolver resolver =
            (ResourceResolver) ResourceResolver._resolverVector.get(i);
                  ResourceResolver resolverTmp=null;
                  try {
                        resolverTmp =  allThreadSafeInList || resolver._resolverSpi.engineIsThreadSafe() ? resolver :
                                        new ResourceResolver((ResourceResolverSpi)resolver._resolverSpi.getClass().newInstance());
                  } catch (InstantiationException e) {
                          throw new ResourceResolverException("",e,uri,BaseURI);
                  } catch (IllegalAccessException e) {
                          throw new ResourceResolverException("",e,uri,BaseURI);
                  }
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "check resolvability by class " + resolver._resolverSpi.getClass().getName());
         if ((resolver != null) && resolverTmp.canResolve(uri, BaseURI)) {
                 if (i!=0) {
                         List resolverVector=(List)((ArrayList)_resolverVector).clone();
                         resolverVector.remove(i);
                         resolverVector.add(0,resolver);
                         _resolverVector=resolverVector;
                 } else {
                 }
            return resolverTmp;
         }
      }
      Object exArgs[] = { ((uri != null)
                           ? uri.getNodeValue()
                           : "null"), BaseURI };
      throw new ResourceResolverException("utils.resolver.noClass", exArgs,
                                          uri, BaseURI);
   }
   public static final ResourceResolver getInstance(
           Attr uri, String BaseURI, List individualResolvers)
              throws ResourceResolverException {
      if (log.isLoggable(java.util.logging.Level.FINE)) {
        log.log(java.util.logging.Level.FINE, "I was asked to create a ResourceResolver and got " + (individualResolvers==null? 0 : individualResolvers.size()) );
        log.log(java.util.logging.Level.FINE, " extra resolvers to my existing " + ResourceResolver._resolverVector.size() + " system-wide resolvers");
      }
          int size=0;
      if ((individualResolvers != null) && ((size=individualResolvers.size()) > 0)) {
         for (int i = 0; i < size; i++) {
            ResourceResolver resolver =
               (ResourceResolver) individualResolvers.get(i);
            if (resolver != null) {
               String currentClass = resolver._resolverSpi.getClass().getName();
               if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "check resolvability by class " + currentClass);
               if (resolver.canResolve(uri, BaseURI)) {
                  return resolver;
               }
            }
         }
      }
          return getInstance(uri,BaseURI);
   }
   public static void init() {
      if (!ResourceResolver._alreadyInitialized) {
         ResourceResolver._resolverVector = new ArrayList(10);
         _alreadyInitialized = true;
      }
   }
    public static void register(String className) {
        register(className, false);
    }
    public static void registerAtStart(String className) {
        register(className, true);
    }
    private static void register(String className, boolean start) {
        try {
            ResourceResolver resolver = new ResourceResolver(className);
            if (start) {
                ResourceResolver._resolverVector.add(0, resolver);
                log.log(java.util.logging.Level.FINE, "registered resolver");
            } else {
                ResourceResolver._resolverVector.add(resolver);
            }
            if (!resolver._resolverSpi.engineIsThreadSafe()) {
                allThreadSafeInList=false;
        }
        } catch (Exception e) {
            log.log(java.util.logging.Level.WARNING, "Error loading resolver " + className +" disabling it");
        } catch (NoClassDefFoundError e) {
            log.log(java.util.logging.Level.WARNING, "Error loading resolver " + className +" disabling it");
        }
    }
   public static XMLSignatureInput resolveStatic(Attr uri, String BaseURI)
           throws ResourceResolverException {
      ResourceResolver myResolver = ResourceResolver.getInstance(uri, BaseURI);
      return myResolver.resolve(uri, BaseURI);
   }
   public XMLSignatureInput resolve(Attr uri, String BaseURI)
           throws ResourceResolverException {
      return this._resolverSpi.engineResolve(uri, BaseURI);
   }
   public void setProperty(String key, String value) {
      this._resolverSpi.engineSetProperty(key, value);
   }
   public String getProperty(String key) {
      return this._resolverSpi.engineGetProperty(key);
   }
   public void addProperties(Map properties) {
      this._resolverSpi.engineAddProperies(properties);
   }
   public String[] getPropertyKeys() {
      return this._resolverSpi.engineGetPropertyKeys();
   }
   public boolean understandsProperty(String propertyToTest) {
      return this._resolverSpi.understandsProperty(propertyToTest);
   }
   private boolean canResolve(Attr uri, String BaseURI) {
      return this._resolverSpi.engineCanResolve(uri, BaseURI);
   }
}
