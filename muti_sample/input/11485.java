public class XmlResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
           String schemaName = systemId.substring(systemId.lastIndexOf("/"));
           if(systemId.startsWith("http:
               return new InputSource(this.getClass().getResourceAsStream(schemaName));
           } else {
              return null;
           }
       }
}
