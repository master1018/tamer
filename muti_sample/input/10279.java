public class C14nHelper {
   private C14nHelper() {
   }
   public static boolean namespaceIsRelative(Attr namespace) {
      return !namespaceIsAbsolute(namespace);
   }
   public static boolean namespaceIsRelative(String namespaceValue) {
      return !namespaceIsAbsolute(namespaceValue);
   }
   public static boolean namespaceIsAbsolute(Attr namespace) {
      return namespaceIsAbsolute(namespace.getValue());
   }
   public static boolean namespaceIsAbsolute(String namespaceValue) {
      if (namespaceValue.length() == 0) {
         return true;
      }
      return namespaceValue.indexOf(':')>0;
   }
   public static void assertNotRelativeNS(Attr attr)
           throws CanonicalizationException {
      if (attr == null) {
         return;
      }
      String nodeAttrName = attr.getNodeName();
      boolean definesDefaultNS = nodeAttrName.equals("xmlns");
      boolean definesNonDefaultNS = nodeAttrName.startsWith("xmlns:");
      if (definesDefaultNS || definesNonDefaultNS) {
         if (namespaceIsRelative(attr)) {
            String parentName = attr.getOwnerElement().getTagName();
            String attrValue = attr.getValue();
            Object exArgs[] = { parentName, nodeAttrName, attrValue };
            throw new CanonicalizationException(
               "c14n.Canonicalizer.RelativeNamespace", exArgs);
         }
      }
   }
   public static void checkTraversability(Document document)
           throws CanonicalizationException {
      if (!document.isSupported("Traversal", "2.0")) {
         Object exArgs[] = {
            document.getImplementation().getClass().getName() };
         throw new CanonicalizationException(
            "c14n.Canonicalizer.TraversalNotSupported", exArgs);
      }
   }
   public static void checkForRelativeNamespace(Element ctxNode)
           throws CanonicalizationException {
      if (ctxNode != null) {
         NamedNodeMap attributes = ctxNode.getAttributes();
         for (int i = 0; i < attributes.getLength(); i++) {
            C14nHelper.assertNotRelativeNS((Attr) attributes.item(i));
         }
      } else {
         throw new CanonicalizationException(
            "Called checkForRelativeNamespace() on null");
      }
   }
}
