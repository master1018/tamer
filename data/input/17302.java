public abstract class ElementCheckerImpl implements ElementChecker {
        public boolean isNamespaceElement(Node el, String type, String ns) {
                if ((el == null) ||
                   ns!=el.getNamespaceURI() || !el.getLocalName().equals(type)){
                   return false;
                }
                return true;
        }
        public static class InternedNsChecker extends ElementCheckerImpl{
                public void guaranteeThatElementInCorrectSpace(ElementProxy expected,
                                Element actual) throws XMLSecurityException {
                      String localnameSHOULDBE = expected.getBaseLocalName();
                      String namespaceSHOULDBE = expected.getBaseNamespace();
                      String localnameIS = actual.getLocalName();
                      String namespaceIS = actual.getNamespaceURI();
                      if ((namespaceSHOULDBE!=namespaceIS) ||
                       !localnameSHOULDBE.equals(localnameIS) ) {
                         Object exArgs[] = { namespaceIS +":"+ localnameIS,
                           namespaceSHOULDBE +":"+ localnameSHOULDBE};
                         throw new XMLSecurityException("xml.WrongElement", exArgs);
                      }
                }
        }
        public static class FullChecker extends ElementCheckerImpl {
                public void guaranteeThatElementInCorrectSpace(ElementProxy expected,
                                Element actual) throws XMLSecurityException {
                      String localnameSHOULDBE = expected.getBaseLocalName();
                      String namespaceSHOULDBE = expected.getBaseNamespace();
                      String localnameIS = actual.getLocalName();
                      String namespaceIS = actual.getNamespaceURI();
                      if ((!namespaceSHOULDBE.equals(namespaceIS)) ||
                       !localnameSHOULDBE.equals(localnameIS) ) {
                         Object exArgs[] = { namespaceIS +":"+ localnameIS,
                           namespaceSHOULDBE +":"+ localnameSHOULDBE};
                         throw new XMLSecurityException("xml.WrongElement", exArgs);
                      }
                }
        }
        public static class EmptyChecker extends ElementCheckerImpl {
                public void guaranteeThatElementInCorrectSpace(ElementProxy expected,
                                Element actual) throws XMLSecurityException {
                }
        }
}
