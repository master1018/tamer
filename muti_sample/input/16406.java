public class TransformC14NExclusive extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
   protected String engineGetURI() {
      return implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform
        (XMLSignatureInput input, Transform _transformObject)
           throws CanonicalizationException {
            return enginePerformTransform(input, null, _transformObject);
   }
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input,OutputStream os, Transform _transformObject)
    throws CanonicalizationException {
      try {
         String inclusiveNamespaces = null;
         if (_transformObject
                 .length(InclusiveNamespaces
                    .ExclusiveCanonicalizationNamespace, InclusiveNamespaces
                    ._TAG_EC_INCLUSIVENAMESPACES) == 1) {
            Element inclusiveElement =
                XMLUtils.selectNode(
               _transformObject.getElement().getFirstChild(),
                  InclusiveNamespaces.ExclusiveCanonicalizationNamespace,
                  InclusiveNamespaces._TAG_EC_INCLUSIVENAMESPACES,0);
            inclusiveNamespaces = new InclusiveNamespaces(inclusiveElement,
                    _transformObject.getBaseURI()).getInclusiveNamespaces();
         }
         Canonicalizer20010315ExclOmitComments c14n =
            new Canonicalizer20010315ExclOmitComments();
         if (os!=null) {
            c14n.setWriter(os);
         }
         byte []result;
         result =c14n.engineCanonicalize(input, inclusiveNamespaces);
         XMLSignatureInput output=new XMLSignatureInput(result);
         if (os!=null) {
            output.setOutputStream(os);
         }
         return output;
      } catch (XMLSecurityException ex) {
         throw new CanonicalizationException("empty", ex);
      }
   }
}
