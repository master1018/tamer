public abstract class IntegrityHmac extends SignatureAlgorithmSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(IntegrityHmacSHA1.class.getName());
   public abstract String engineGetURI();
   abstract int getDigestLength();
   private Mac _macAlgorithm = null;
   private boolean _HMACOutputLengthSet = false;
   int _HMACOutputLength = 0;
   public IntegrityHmac() throws XMLSignatureException {
      String algorithmID = JCEMapper.translateURItoJCEID(this.engineGetURI());
      if (log.isLoggable(java.util.logging.Level.FINE))
        log.log(java.util.logging.Level.FINE, "Created IntegrityHmacSHA1 using " + algorithmID);
      try {
         this._macAlgorithm = Mac.getInstance(algorithmID);
      } catch (java.security.NoSuchAlgorithmException ex) {
         Object[] exArgs = { algorithmID,
                             ex.getLocalizedMessage() };
         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
      }
   }
   protected void engineSetParameter(AlgorithmParameterSpec params)
           throws XMLSignatureException {
      throw new XMLSignatureException("empty");
   }
   public void reset() {
       _HMACOutputLength=0;
       _HMACOutputLengthSet = false;
       _macAlgorithm.reset();
   }
   protected boolean engineVerify(byte[] signature)
           throws XMLSignatureException {
      try {
         if (this._HMACOutputLengthSet && this._HMACOutputLength < getDigestLength()) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE,
                    "HMACOutputLength must not be less than " + getDigestLength());
            }
            throw new XMLSignatureException("errorMessages.XMLSignatureException");
         } else {
            byte[] completeResult = this._macAlgorithm.doFinal();
            return MessageDigestAlgorithm.isEqual(completeResult, signature);
         }
      } catch (IllegalStateException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   protected void engineInitVerify(Key secretKey) throws XMLSignatureException {
      if (!(secretKey instanceof SecretKey)) {
         String supplied = secretKey.getClass().getName();
         String needed = SecretKey.class.getName();
         Object exArgs[] = { supplied, needed };
         throw new XMLSignatureException("algorithms.WrongKeyForThisOperation",
                                         exArgs);
      }
      try {
         this._macAlgorithm.init(secretKey);
      } catch (InvalidKeyException ex) {
            Mac mac = this._macAlgorithm;
            try {
                this._macAlgorithm = Mac.getInstance
                    (_macAlgorithm.getAlgorithm());
            } catch (Exception e) {
                if (log.isLoggable(java.util.logging.Level.FINE)) {
                    log.log(java.util.logging.Level.FINE, "Exception when reinstantiating Mac:" + e);
                }
                this._macAlgorithm = mac;
            }
            throw new XMLSignatureException("empty", ex);
      }
   }
   protected byte[] engineSign() throws XMLSignatureException {
      try {
         if (this._HMACOutputLengthSet && this._HMACOutputLength < getDigestLength()) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE,
                    "HMACOutputLength must not be less than " + getDigestLength());
            }
            throw new XMLSignatureException("errorMessages.XMLSignatureException");
         } else {
            return this._macAlgorithm.doFinal();
         }
      } catch (IllegalStateException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   private static byte[] reduceBitLength(byte completeResult[], int length) {
      int bytes = length / 8;
      int abits = length % 8;
      byte[] strippedResult = new byte[bytes + ((abits == 0)
                                                ? 0
                                                : 1)];
      System.arraycopy(completeResult, 0, strippedResult, 0, bytes);
      if (abits > 0) {
         byte[] MASK = { (byte) 0x00, (byte) 0x80, (byte) 0xC0, (byte) 0xE0,
                         (byte) 0xF0, (byte) 0xF8, (byte) 0xFC, (byte) 0xFE };
         strippedResult[bytes] = (byte) (completeResult[bytes] & MASK[abits]);
      }
      return strippedResult;
   }
   protected void engineInitSign(Key secretKey) throws XMLSignatureException {
      if (!(secretKey instanceof SecretKey)) {
         String supplied = secretKey.getClass().getName();
         String needed = SecretKey.class.getName();
         Object exArgs[] = { supplied, needed };
         throw new XMLSignatureException("algorithms.WrongKeyForThisOperation",
                                         exArgs);
      }
      try {
         this._macAlgorithm.init(secretKey);
      } catch (InvalidKeyException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   protected void engineInitSign(
           Key secretKey, AlgorithmParameterSpec algorithmParameterSpec)
              throws XMLSignatureException {
      if (!(secretKey instanceof SecretKey)) {
         String supplied = secretKey.getClass().getName();
         String needed = SecretKey.class.getName();
         Object exArgs[] = { supplied, needed };
         throw new XMLSignatureException("algorithms.WrongKeyForThisOperation",
                                         exArgs);
      }
      try {
         this._macAlgorithm.init(secretKey, algorithmParameterSpec);
      } catch (InvalidKeyException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (InvalidAlgorithmParameterException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   protected void engineInitSign(Key secretKey, SecureRandom secureRandom)
           throws XMLSignatureException {
      throw new XMLSignatureException("algorithms.CannotUseSecureRandomOnMAC");
   }
   protected void engineUpdate(byte[] input) throws XMLSignatureException {
      try {
         this._macAlgorithm.update(input);
      } catch (IllegalStateException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   protected void engineUpdate(byte input) throws XMLSignatureException {
      try {
         this._macAlgorithm.update(input);
      } catch (IllegalStateException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   protected void engineUpdate(byte buf[], int offset, int len)
           throws XMLSignatureException {
      try {
         this._macAlgorithm.update(buf, offset, len);
      } catch (IllegalStateException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   protected String engineGetJCEAlgorithmString() {
      log.log(java.util.logging.Level.FINE, "engineGetJCEAlgorithmString()");
      return this._macAlgorithm.getAlgorithm();
   }
   protected String engineGetJCEProviderName() {
      return this._macAlgorithm.getProvider().getName();
   }
   protected void engineSetHMACOutputLength(int HMACOutputLength) {
      this._HMACOutputLength = HMACOutputLength;
      this._HMACOutputLengthSet = true;
   }
   protected void engineGetContextFromElement(Element element) {
      super.engineGetContextFromElement(element);
      if (element == null) {
         throw new IllegalArgumentException("element null");
      }
      Text hmaclength =XMLUtils.selectDsNodeText(element.getFirstChild(),
         Constants._TAG_HMACOUTPUTLENGTH,0);
      if (hmaclength != null) {
         this._HMACOutputLength = Integer.parseInt(hmaclength.getData());
         this._HMACOutputLengthSet = true;
      }
   }
   public void engineAddContextToElement(Element element) {
      if (element == null) {
         throw new IllegalArgumentException("null element");
      }
      if (this._HMACOutputLengthSet) {
         Document doc = element.getOwnerDocument();
         Element HMElem = XMLUtils.createElementInSignatureSpace(doc,
                             Constants._TAG_HMACOUTPUTLENGTH);
         Text HMText =
            doc.createTextNode(new Integer(this._HMACOutputLength).toString());
         HMElem.appendChild(HMText);
         XMLUtils.addReturnToElement(element);
         element.appendChild(HMElem);
         XMLUtils.addReturnToElement(element);
      }
   }
   public static class IntegrityHmacSHA1 extends IntegrityHmac {
      public IntegrityHmacSHA1() throws XMLSignatureException {
         super();
      }
      public String engineGetURI() {
         return XMLSignature.ALGO_ID_MAC_HMAC_SHA1;
      }
      int getDigestLength() {
          return 160;
      }
   }
   public static class IntegrityHmacSHA256 extends IntegrityHmac {
      public IntegrityHmacSHA256() throws XMLSignatureException {
         super();
      }
      public String engineGetURI() {
         return XMLSignature.ALGO_ID_MAC_HMAC_SHA256;
      }
      int getDigestLength() {
          return 256;
      }
   }
   public static class IntegrityHmacSHA384 extends IntegrityHmac {
      public IntegrityHmacSHA384() throws XMLSignatureException {
         super();
      }
      public String engineGetURI() {
         return XMLSignature.ALGO_ID_MAC_HMAC_SHA384;
      }
      int getDigestLength() {
          return 384;
      }
   }
   public static class IntegrityHmacSHA512 extends IntegrityHmac {
      public IntegrityHmacSHA512() throws XMLSignatureException {
         super();
      }
      public String engineGetURI() {
         return XMLSignature.ALGO_ID_MAC_HMAC_SHA512;
      }
      int getDigestLength() {
          return 512;
      }
   }
   public static class IntegrityHmacRIPEMD160 extends IntegrityHmac {
      public IntegrityHmacRIPEMD160() throws XMLSignatureException {
         super();
      }
      public String engineGetURI() {
         return XMLSignature.ALGO_ID_MAC_HMAC_RIPEMD160;
      }
      int getDigestLength() {
          return 160;
      }
   }
   public static class IntegrityHmacMD5 extends IntegrityHmac {
      public IntegrityHmacMD5() throws XMLSignatureException {
         super();
      }
      public String engineGetURI() {
         return XMLSignature.ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5;
      }
      int getDigestLength() {
          return 128;
      }
   }
}
