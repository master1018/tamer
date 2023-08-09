public class JCEMapper {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(JCEMapper.class.getName());
   private static Map uriToJCEName;
   private static Map algorithmsMap;
   private static String providerName = null;
   public static void init(Element mappingElement) throws Exception {
      loadAlgorithms((Element)mappingElement.getElementsByTagName("Algorithms").item(0));
   }
   static void loadAlgorithms( Element algorithmsEl) {
       Element[] algorithms = XMLUtils.selectNodes(algorithmsEl.getFirstChild(),Init.CONF_NS,"Algorithm");
       uriToJCEName = new HashMap( algorithms.length * 2);
       algorithmsMap = new HashMap( algorithms.length * 2);
       for (int i = 0 ;i < algorithms.length ;i ++) {
           Element el = algorithms[i];
           String id = el.getAttribute("URI");
           String jceName = el.getAttribute("JCEName");
           uriToJCEName.put(id, jceName);
           algorithmsMap.put(id, new Algorithm(el));
       }
   }
   static Algorithm getAlgorithmMapping(String algoURI) {
           return ((Algorithm)algorithmsMap.get(algoURI));
   }
   public static String translateURItoJCEID(String AlgorithmURI) {
      if (log.isLoggable(java.util.logging.Level.FINE))
          log.log(java.util.logging.Level.FINE, "Request for URI " + AlgorithmURI);
      String jceName = (String) uriToJCEName.get(AlgorithmURI);
      return jceName;
   }
   public static String getAlgorithmClassFromURI(String AlgorithmURI) {
       if (log.isLoggable(java.util.logging.Level.FINE))
           log.log(java.util.logging.Level.FINE, "Request for URI " + AlgorithmURI);
       return ((Algorithm) algorithmsMap.get(AlgorithmURI)).algorithmClass;
   }
   public static int getKeyLengthFromURI(String AlgorithmURI) {
       return Integer.parseInt(((Algorithm) algorithmsMap.get(AlgorithmURI)).keyLength);
   }
   public static String getJCEKeyAlgorithmFromURI(String AlgorithmURI) {
        return  ((Algorithm) algorithmsMap.get(AlgorithmURI)).requiredKey;
   }
   public static String getProviderId() {
                return providerName;
   }
   public static void setProviderId(String provider) {
                providerName=provider;
   }
   public static class Algorithm {
            String algorithmClass;
            String keyLength;
            String requiredKey;
        public Algorithm(Element el) {
                algorithmClass=el.getAttribute("AlgorithmClass");
            keyLength=el.getAttribute("KeyLength");
            requiredKey=el.getAttribute("RequiredKey");
        }
   }
}
