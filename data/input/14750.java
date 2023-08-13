public abstract class ResourceResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                    ResourceResolverSpi.class.getName());
   protected java.util.Map _properties = null;
   public abstract XMLSignatureInput engineResolve(Attr uri, String BaseURI)
      throws ResourceResolverException;
   public void engineSetProperty(String key, String value) {
          if (_properties==null) {
                  _properties=new HashMap();
          }
      this._properties.put(key, value);
   }
   public String engineGetProperty(String key) {
          if (_properties==null) {
                        return null;
          }
      return (String) this._properties.get(key);
   }
   public void engineAddProperies(Map properties) {
          if (properties!=null) {
                  if (_properties==null) {
                          _properties=new HashMap();
                  }
                  this._properties.putAll(properties);
          }
   }
   public boolean engineIsThreadSafe() {
           return false;
   }
   public abstract boolean engineCanResolve(Attr uri, String BaseURI);
   public String[] engineGetPropertyKeys() {
      return new String[0];
   }
   public boolean understandsProperty(String propertyToTest) {
      String[] understood = this.engineGetPropertyKeys();
      if (understood != null) {
         for (int i = 0; i < understood.length; i++) {
            if (understood[i].equals(propertyToTest)) {
               return true;
            }
         }
      }
      return false;
   }
   public static String fixURI(String str) {
      str = str.replace(java.io.File.separatorChar, '/');
      if (str.length() >= 4) {
         char ch0 = Character.toUpperCase(str.charAt(0));
         char ch1 = str.charAt(1);
         char ch2 = str.charAt(2);
         char ch3 = str.charAt(3);
         boolean isDosFilename = ((('A' <= ch0) && (ch0 <= 'Z'))
                                  && (ch1 == ':') && (ch2 == '/')
                                  && (ch3 != '/'));
         if (isDosFilename) {
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Found DOS filename: " + str);
         }
      }
      if (str.length() >= 2) {
         char ch1 = str.charAt(1);
         if (ch1 == ':') {
            char ch0 = Character.toUpperCase(str.charAt(0));
            if (('A' <= ch0) && (ch0 <= 'Z')) {
               str = "/" + str;
            }
         }
      }
      return str;
   }
}
