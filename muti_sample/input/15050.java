public class RetrievalMethod extends SignatureElementProxy
        implements KeyInfoContent {
   public static final String TYPE_DSA     = Constants.SignatureSpecNS + "DSAKeyValue";
   public static final String TYPE_RSA     = Constants.SignatureSpecNS + "RSAKeyValue";
   public static final String TYPE_PGP     = Constants.SignatureSpecNS + "PGPData";
   public static final String TYPE_SPKI    = Constants.SignatureSpecNS + "SPKIData";
   public static final String TYPE_MGMT    = Constants.SignatureSpecNS + "MgmtData";
   public static final String TYPE_X509    = Constants.SignatureSpecNS + "X509Data";
   public static final String TYPE_RAWX509 = Constants.SignatureSpecNS + "rawX509Certificate";
   public RetrievalMethod(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public RetrievalMethod(Document doc, String URI, Transforms transforms,
                          String Type) {
      super(doc);
      this._constructionElement.setAttributeNS(null, Constants._ATT_URI, URI);
      if (Type != null) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_TYPE, Type);
      }
      if (transforms != null) {
         this._constructionElement.appendChild(transforms.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
      }
   }
   public Attr getURIAttr() {
      return this._constructionElement.getAttributeNodeNS(null, Constants._ATT_URI);
   }
   public String getURI() {
      return this.getURIAttr().getNodeValue();
   }
   public String getType() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_TYPE);
   }
   public Transforms getTransforms() throws XMLSecurityException {
      try {
       Element transformsElem =
             XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
                                                Constants
                                                   ._TAG_TRANSFORMS, 0);
         if (transformsElem != null) {
            return new Transforms(transformsElem, this._baseURI);
         }
         return null;
      } catch (XMLSignatureException ex) {
         throw new XMLSecurityException("empty", ex);
      }
   }
   public String getBaseLocalName() {
      return Constants._TAG_RETRIEVALMETHOD;
   }
}
