public class DSAKeyValue extends SignatureElementProxy
        implements KeyValueContent {
   public DSAKeyValue(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public DSAKeyValue(Document doc, BigInteger P, BigInteger Q, BigInteger G,
                      BigInteger Y) {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
      this.addBigIntegerElement(P, Constants._TAG_P);
      this.addBigIntegerElement(Q, Constants._TAG_Q);
      this.addBigIntegerElement(G, Constants._TAG_G);
      this.addBigIntegerElement(Y, Constants._TAG_Y);
   }
   public DSAKeyValue(Document doc, Key key) throws IllegalArgumentException {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
      if (key instanceof java.security.interfaces.DSAPublicKey) {
         this.addBigIntegerElement(((DSAPublicKey) key).getParams().getP(),
                                   Constants._TAG_P);
         this.addBigIntegerElement(((DSAPublicKey) key).getParams().getQ(),
                                   Constants._TAG_Q);
         this.addBigIntegerElement(((DSAPublicKey) key).getParams().getG(),
                                   Constants._TAG_G);
         this.addBigIntegerElement(((DSAPublicKey) key).getY(),
                                   Constants._TAG_Y);
      } else {
         Object exArgs[] = { Constants._TAG_DSAKEYVALUE,
                             key.getClass().getName() };
         throw new IllegalArgumentException(I18n
            .translate("KeyValue.IllegalArgument", exArgs));
      }
   }
   public PublicKey getPublicKey() throws XMLSecurityException {
      try {
         DSAPublicKeySpec pkspec =
            new DSAPublicKeySpec(this
               .getBigIntegerFromChildElement(Constants._TAG_Y, Constants
               .SignatureSpecNS), this
                  .getBigIntegerFromChildElement(Constants._TAG_P, Constants
                  .SignatureSpecNS), this
                     .getBigIntegerFromChildElement(Constants._TAG_Q, Constants
                     .SignatureSpecNS), this
                        .getBigIntegerFromChildElement(Constants
                           ._TAG_G, Constants.SignatureSpecNS));
         KeyFactory dsaFactory = KeyFactory.getInstance("DSA");
         PublicKey pk = dsaFactory.generatePublic(pkspec);
         return pk;
      } catch (NoSuchAlgorithmException ex) {
         throw new XMLSecurityException("empty", ex);
      } catch (InvalidKeySpecException ex) {
         throw new XMLSecurityException("empty", ex);
      }
   }
   public String getBaseLocalName() {
      return Constants._TAG_DSAKEYVALUE;
   }
}
