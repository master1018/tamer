public class TransformEnvelopedSignature extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_ENVELOPED_SIGNATURE;
   protected String engineGetURI() {
      return implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, Transform _transformObject)
           throws TransformationException {
         Node signatureElement = _transformObject.getElement();
         signatureElement = searchSignatureElement(signatureElement);
                input.setExcludeNode(signatureElement);
                input.addNodeFilter(new EnvelopedNodeFilter(signatureElement));
                return input;
   }
    private static Node searchSignatureElement(Node signatureElement) throws TransformationException {
            boolean found=false;
            while (true) {
                if ((signatureElement == null)
                    || (signatureElement.getNodeType() == Node.DOCUMENT_NODE)) {
                        break;
                }
                Element el=(Element)signatureElement;
                if (el.getNamespaceURI().equals(Constants.SignatureSpecNS)
                    &&
                       el.getLocalName().equals(Constants._TAG_SIGNATURE)) {
                        found = true;
                        break;
                }
                signatureElement = signatureElement.getParentNode();
            }
            if (!found) {
              throw new TransformationException(
               "envelopedSignatureTransformNotInSignatureElement");
            }
            return signatureElement;
    }
    static class EnvelopedNodeFilter implements NodeFilter {
        Node exclude;
        EnvelopedNodeFilter(Node n) {
            exclude=n;
        }
    public int isNodeIncludeDO(Node n, int level) {
        if ((n==exclude))
                        return -1;
        return 1;
    }
        public int isNodeInclude(Node n) {
                if ((n==exclude) || XMLUtils.isDescendantOrSelf(exclude,n))
                        return -1;
                return 1;
        }
    }
}
