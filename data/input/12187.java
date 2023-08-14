public final class XMLSignature extends SignatureElementProxy {
   static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(XMLSignature.class.getName());
   public static final String ALGO_ID_MAC_HMAC_SHA1 = Constants.SignatureSpecNS + "hmac-sha1";
   public static final String ALGO_ID_SIGNATURE_DSA = Constants.SignatureSpecNS + "dsa-sha1";
   public static final String ALGO_ID_SIGNATURE_RSA = Constants.SignatureSpecNS + "rsa-sha1";
   public static final String ALGO_ID_SIGNATURE_RSA_SHA1 = Constants.SignatureSpecNS + "rsa-sha1";
   public static final String ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5 = Constants.MoreAlgorithmsSpecNS + "rsa-md5";
   public static final String ALGO_ID_SIGNATURE_RSA_RIPEMD160 = Constants.MoreAlgorithmsSpecNS + "rsa-ripemd160";
   public static final String ALGO_ID_SIGNATURE_RSA_SHA256 = Constants.MoreAlgorithmsSpecNS + "rsa-sha256";
   public static final String ALGO_ID_SIGNATURE_RSA_SHA384 = Constants.MoreAlgorithmsSpecNS + "rsa-sha384";
   public static final String ALGO_ID_SIGNATURE_RSA_SHA512 = Constants.MoreAlgorithmsSpecNS + "rsa-sha512";
   public static final String ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5 = Constants.MoreAlgorithmsSpecNS + "hmac-md5";
   public static final String ALGO_ID_MAC_HMAC_RIPEMD160 = Constants.MoreAlgorithmsSpecNS + "hmac-ripemd160";
   public static final String ALGO_ID_MAC_HMAC_SHA256 = Constants.MoreAlgorithmsSpecNS + "hmac-sha256";
   public static final String ALGO_ID_MAC_HMAC_SHA384 = Constants.MoreAlgorithmsSpecNS + "hmac-sha384";
   public static final String ALGO_ID_MAC_HMAC_SHA512 = Constants.MoreAlgorithmsSpecNS + "hmac-sha512";
   public static final String ALGO_ID_SIGNATURE_ECDSA_SHA1 = "http:
   private SignedInfo _signedInfo = null;
   private KeyInfo _keyInfo = null;
   private boolean _followManifestsDuringValidation = false;
private Element signatureValueElement;
   public XMLSignature(Document doc, String BaseURI, String SignatureMethodURI)
           throws XMLSecurityException {
      this(doc, BaseURI, SignatureMethodURI, 0,
           Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
   }
   public XMLSignature(
           Document doc, String BaseURI, String SignatureMethodURI, int HMACOutputLength)
              throws XMLSecurityException {
      this(doc, BaseURI, SignatureMethodURI, HMACOutputLength,
           Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
   }
   public XMLSignature(
           Document doc, String BaseURI, String SignatureMethodURI, String CanonicalizationMethodURI)
              throws XMLSecurityException {
      this(doc, BaseURI, SignatureMethodURI, 0, CanonicalizationMethodURI);
   }
   public XMLSignature(
           Document doc, String BaseURI, String SignatureMethodURI, int HMACOutputLength, String CanonicalizationMethodURI)
              throws XMLSecurityException {
      super(doc);
      String xmlnsDsPrefix =
         getDefaultPrefixBindings(Constants.SignatureSpecNS);
      if (xmlnsDsPrefix == null) {
         this._constructionElement.setAttributeNS
            (Constants.NamespaceSpecNS, "xmlns", Constants.SignatureSpecNS);
      } else {
         this._constructionElement.setAttributeNS
            (Constants.NamespaceSpecNS, xmlnsDsPrefix, Constants.SignatureSpecNS);
      }
      XMLUtils.addReturnToElement(this._constructionElement);
      this._baseURI = BaseURI;
      this._signedInfo = new SignedInfo(this._doc, SignatureMethodURI,
                                        HMACOutputLength,
                                        CanonicalizationMethodURI);
      this._constructionElement.appendChild(this._signedInfo.getElement());
      XMLUtils.addReturnToElement(this._constructionElement);
      signatureValueElement =
         XMLUtils.createElementInSignatureSpace(this._doc,
                                                Constants._TAG_SIGNATUREVALUE);
      this._constructionElement.appendChild(signatureValueElement);
      XMLUtils.addReturnToElement(this._constructionElement);
   }
   public XMLSignature(
           Document doc, String BaseURI, Element SignatureMethodElem, Element CanonicalizationMethodElem)
              throws XMLSecurityException {
      super(doc);
      String xmlnsDsPrefix =
         getDefaultPrefixBindings(Constants.SignatureSpecNS);
      if (xmlnsDsPrefix == null) {
         this._constructionElement.setAttributeNS
            (Constants.NamespaceSpecNS, "xmlns", Constants.SignatureSpecNS);
      } else {
         this._constructionElement.setAttributeNS
            (Constants.NamespaceSpecNS, xmlnsDsPrefix, Constants.SignatureSpecNS);
      }
      XMLUtils.addReturnToElement(this._constructionElement);
      this._baseURI = BaseURI;
      this._signedInfo = new SignedInfo(this._doc, SignatureMethodElem, CanonicalizationMethodElem);
      this._constructionElement.appendChild(this._signedInfo.getElement());
      XMLUtils.addReturnToElement(this._constructionElement);
      signatureValueElement =
         XMLUtils.createElementInSignatureSpace(this._doc,
                                                Constants._TAG_SIGNATUREVALUE);
      this._constructionElement.appendChild(signatureValueElement);
      XMLUtils.addReturnToElement(this._constructionElement);
   }
   public XMLSignature(Element element, String BaseURI)
           throws XMLSignatureException, XMLSecurityException {
      super(element, BaseURI);
      Element signedInfoElem = XMLUtils.getNextElement(element.getFirstChild());
      if (signedInfoElem == null) {
         Object exArgs[] = { Constants._TAG_SIGNEDINFO,
                             Constants._TAG_SIGNATURE };
         throw new XMLSignatureException("xml.WrongContent", exArgs);
      }
      this._signedInfo = new SignedInfo(signedInfoElem, BaseURI);
      this.signatureValueElement =XMLUtils.getNextElement(signedInfoElem.getNextSibling()); 
      if (signatureValueElement == null) {
         Object exArgs[] = { Constants._TAG_SIGNATUREVALUE,
                             Constants._TAG_SIGNATURE };
         throw new XMLSignatureException("xml.WrongContent", exArgs);
      }
      Element keyInfoElem = XMLUtils.getNextElement(signatureValueElement.getNextSibling());
      if ((keyInfoElem != null) && (keyInfoElem.getNamespaceURI().equals(Constants.SignatureSpecNS) &&
                  keyInfoElem.getLocalName().equals(Constants._TAG_KEYINFO)) ) {
         this._keyInfo = new KeyInfo(keyInfoElem, BaseURI);
      }
   }
   public void setId(String Id) {
      if ( (Id != null)) {
         this._constructionElement.setAttributeNS(null, Constants._ATT_ID, Id);
         IdResolver.registerElementById(this._constructionElement, Id);
      }
   }
   public String getId() {
      return this._constructionElement.getAttributeNS(null, Constants._ATT_ID);
   }
   public SignedInfo getSignedInfo() {
      return this._signedInfo;
   }
   public byte[] getSignatureValue() throws XMLSignatureException {
      try {
          byte[] signatureValue = Base64.decode(signatureValueElement);
         return signatureValue;
      } catch (Base64DecodingException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
    private void setSignatureValueElement(byte[] bytes) {
        while (signatureValueElement.hasChildNodes()) {
            signatureValueElement.removeChild
                (signatureValueElement.getFirstChild());
        }
        String base64codedValue = Base64.encode(bytes);
        if (base64codedValue.length() > 76 && !XMLUtils.ignoreLineBreaks()) {
            base64codedValue = "\n" + base64codedValue + "\n";
        }
        Text t = this._doc.createTextNode(base64codedValue);
        signatureValueElement.appendChild(t);
    }
   public KeyInfo getKeyInfo() {
      if ( (this._keyInfo == null)) {
         this._keyInfo = new KeyInfo(this._doc);
         Element keyInfoElement = this._keyInfo.getElement();
         Element firstObject=null;
         Node sibling= this._constructionElement.getFirstChild();
         firstObject = XMLUtils.selectDsNode(sibling,Constants._TAG_OBJECT,0);
            if (firstObject != null) {
               this._constructionElement.insertBefore(keyInfoElement,
                                                      firstObject);
               XMLUtils.addReturnBeforeChild(this._constructionElement, firstObject);
            } else {
               this._constructionElement.appendChild(keyInfoElement);
               XMLUtils.addReturnToElement(this._constructionElement);
            }
      }
      return this._keyInfo;
   }
   public void appendObject(ObjectContainer object)
           throws XMLSignatureException {
         this._constructionElement.appendChild(object.getElement());
         XMLUtils.addReturnToElement(this._constructionElement);
   }
   public ObjectContainer getObjectItem(int i) {
      Element objElem = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(),
            Constants._TAG_OBJECT,i);
      try {
         return new ObjectContainer(objElem, this._baseURI);
      } catch (XMLSecurityException ex) {
         return null;
      }
   }
   public int getObjectLength() {
      return this.length(Constants.SignatureSpecNS, Constants._TAG_OBJECT);
   }
   public void sign(Key signingKey) throws XMLSignatureException {
      if (signingKey instanceof PublicKey) {
         throw new IllegalArgumentException(I18n
            .translate("algorithms.operationOnlyVerification"));
      }
      try {
                SignedInfo si = this.getSignedInfo();
            SignatureAlgorithm sa = si.getSignatureAlgorithm();
            sa.initSign(signingKey);
            si.generateDigestValues();
            OutputStream so=new UnsyncBufferedOutputStream(new SignerOutputStream(sa));
            try {
                so.close();
            } catch (IOException e) {
            }
            si.signInOctectStream(so);
            byte jcebytes[] = sa.sign();
            this.setSignatureValueElement(jcebytes);
      } catch (CanonicalizationException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (InvalidCanonicalizerException ex) {
         throw new XMLSignatureException("empty", ex);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public void addResourceResolver(ResourceResolver resolver) {
      this.getSignedInfo().addResourceResolver(resolver);
   }
   public void addResourceResolver(ResourceResolverSpi resolver) {
      this.getSignedInfo().addResourceResolver(resolver);
   }
   public boolean checkSignatureValue(X509Certificate cert)
           throws XMLSignatureException {
      if (cert != null) {
         return this.checkSignatureValue(cert.getPublicKey());
      }
      Object exArgs[] = { "Didn't get a certificate" };
      throw new XMLSignatureException("empty", exArgs);
   }
   public boolean checkSignatureValue(Key pk) throws XMLSignatureException {
      if (pk == null) {
         Object exArgs[] = { "Didn't get a key" };
         throw new XMLSignatureException("empty", exArgs);
      }
      try {
         SignedInfo si=this.getSignedInfo();
         SignatureAlgorithm sa =si.getSignatureAlgorithm();
         if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "SignatureMethodURI = " + sa.getAlgorithmURI());
                log.log(java.util.logging.Level.FINE, "jceSigAlgorithm    = " + sa.getJCEAlgorithmString());
                log.log(java.util.logging.Level.FINE, "jceSigProvider     = " + sa.getJCEProviderName());
                log.log(java.util.logging.Level.FINE, "PublicKey = " + pk);
         }
         sa.initVerify(pk);
         SignerOutputStream so=new SignerOutputStream(sa);
         OutputStream bos=new UnsyncBufferedOutputStream(so);
         si.signInOctectStream(bos);
         try {
                bos.close();
         } catch (IOException e) {
         }
         byte sigBytes[] = this.getSignatureValue();
         if (!sa.verify(sigBytes)) {
            log.log(java.util.logging.Level.WARNING, "Signature verification failed.");
            return false;
         }
         return si.verify(this._followManifestsDuringValidation);
      } catch (XMLSecurityException ex) {
         throw new XMLSignatureException("empty", ex);
      }
   }
   public void addDocument(
           String referenceURI, Transforms trans, String digestURI, String ReferenceId, String ReferenceType)
              throws XMLSignatureException {
      this._signedInfo.addDocument(this._baseURI, referenceURI, trans,
                                   digestURI, ReferenceId, ReferenceType);
   }
   public void addDocument(
           String referenceURI, Transforms trans, String digestURI)
              throws XMLSignatureException {
      this._signedInfo.addDocument(this._baseURI, referenceURI, trans,
                                   digestURI, null, null);
   }
   public void addDocument(String referenceURI, Transforms trans)
           throws XMLSignatureException {
      this._signedInfo.addDocument(this._baseURI, referenceURI, trans,
                                   Constants.ALGO_ID_DIGEST_SHA1, null, null);
   }
   public void addDocument(String referenceURI) throws XMLSignatureException {
      this._signedInfo.addDocument(this._baseURI, referenceURI, null,
                                   Constants.ALGO_ID_DIGEST_SHA1, null, null);
   }
   public void addKeyInfo(X509Certificate cert) throws XMLSecurityException {
      X509Data x509data = new X509Data(this._doc);
      x509data.addCertificate(cert);
      this.getKeyInfo().add(x509data);
   }
   public void addKeyInfo(PublicKey pk) {
      this.getKeyInfo().add(pk);
   }
   public SecretKey createSecretKey(byte[] secretKeyBytes)
   {
      return this.getSignedInfo().createSecretKey(secretKeyBytes);
   }
   public void setFollowNestedManifests(boolean followManifests) {
      this._followManifestsDuringValidation = followManifests;
   }
   public String getBaseLocalName() {
      return Constants._TAG_SIGNATURE;
   }
}
