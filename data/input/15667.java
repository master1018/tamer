public class RSAKeyValue extends SignatureElementProxy
        implements KeyValueContent {
   public RSAKeyValue(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }
   public RSAKeyValue(Document doc, BigInteger modulus, BigInteger exponent) {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
      this.addBigIntegerElement(modulus, Constants._TAG_MODULUS);
      this.addBigIntegerElement(exponent, Constants._TAG_EXPONENT);
   }
   public RSAKeyValue(Document doc, Key key) throws IllegalArgumentException {
      super(doc);
      XMLUtils.addReturnToElement(this._constructionElement);
      if (key instanceof java.security.interfaces.RSAPublicKey ) {
         this.addBigIntegerElement(((RSAPublicKey) key).getModulus(),
                                   Constants._TAG_MODULUS);
         this.addBigIntegerElement(((RSAPublicKey) key).getPublicExponent(),
                                   Constants._TAG_EXPONENT);
      } else {
         Object exArgs[] = { Constants._TAG_RSAKEYVALUE,
                             key.getClass().getName() };
         throw new IllegalArgumentException(I18n
            .translate("KeyValue.IllegalArgument", exArgs));
      }
   }
   public PublicKey getPublicKey() throws XMLSecurityException {
      try {
         KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
         RSAPublicKeySpec rsaKeyspec =
            new RSAPublicKeySpec(this
               .getBigIntegerFromChildElement(Constants._TAG_MODULUS, Constants
               .SignatureSpecNS), this
                  .getBigIntegerFromChildElement(Constants
                     ._TAG_EXPONENT, Constants.SignatureSpecNS));
         PublicKey pk = rsaFactory.generatePublic(rsaKeyspec);
         return pk;
      } catch (NoSuchAlgorithmException ex) {
         throw new XMLSecurityException("empty", ex);
      } catch (InvalidKeySpecException ex) {
         throw new XMLSecurityException("empty", ex);
      }
   }
   public String getBaseLocalName() {
      return Constants._TAG_RSAKEYVALUE;
   }
}
