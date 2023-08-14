public class SignatureAlgorithm extends Algorithm {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(SignatureAlgorithm.class.getName());
   static boolean _alreadyInitialized = false;
   static HashMap _algorithmHash = null;
   static ThreadLocal instancesSigning=new ThreadLocal() {
           protected Object initialValue() {
                   return new HashMap();
           };
   };
   static ThreadLocal instancesVerify=new ThreadLocal() {
           protected Object initialValue() {
                   return new HashMap();
           };
   };
   static ThreadLocal keysSigning=new ThreadLocal() {
           protected Object initialValue() {
                   return new HashMap();
           };
   };
   static ThreadLocal keysVerify=new ThreadLocal() {
           protected Object initialValue() {
                   return new HashMap();
           };
   };
   protected SignatureAlgorithmSpi _signatureAlgorithm = null;
   private String algorithmURI;
   public SignatureAlgorithm(Document doc, String algorithmURI)
           throws XMLSecurityException {
      super(doc, algorithmURI);
      this.algorithmURI = algorithmURI;
   }
   private void initializeAlgorithm(boolean isForSigning) throws XMLSignatureException {
           if (_signatureAlgorithm!=null) {
                   return;
           }
           _signatureAlgorithm=isForSigning ? getInstanceForSigning(algorithmURI) : getInstanceForVerify(algorithmURI);
                this._signatureAlgorithm
                      .engineGetContextFromElement(this._constructionElement);
   }
   private static SignatureAlgorithmSpi getInstanceForSigning(String algorithmURI) throws XMLSignatureException {
           SignatureAlgorithmSpi result=(SignatureAlgorithmSpi) ((Map)instancesSigning.get()).get(algorithmURI);
           if (result!=null) {
                   result.reset();
                   return result;
           }
           result=buildSigner(algorithmURI, result);
           ((Map)instancesSigning.get()).put(algorithmURI,result);
           return result;
   }
   private static SignatureAlgorithmSpi getInstanceForVerify(String algorithmURI) throws XMLSignatureException {
           SignatureAlgorithmSpi result=(SignatureAlgorithmSpi) ((Map)instancesVerify.get()).get(algorithmURI);
           if (result!=null) {
                   result.reset();
                   return result;
           }
           result=buildSigner(algorithmURI, result);
           ((Map)instancesVerify.get()).put(algorithmURI,result);
           return result;
   }
   private static SignatureAlgorithmSpi buildSigner(String algorithmURI, SignatureAlgorithmSpi result) throws XMLSignatureException {
        try {
         Class implementingClass =
            SignatureAlgorithm.getImplementingClass(algorithmURI);
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Create URI \"" + algorithmURI + "\" class \""
                   + implementingClass + "\"");
         result=(SignatureAlgorithmSpi) implementingClass.newInstance();
         return   result;
      }  catch (IllegalAccessException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };
         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      } catch (InstantiationException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };
         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      } catch (NullPointerException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };
         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      }
}
   public SignatureAlgorithm(
           Document doc, String algorithmURI, int HMACOutputLength)
              throws XMLSecurityException {
      this(doc, algorithmURI);
      this.algorithmURI=algorithmURI;
      initializeAlgorithm(true);
      this._signatureAlgorithm.engineSetHMACOutputLength(HMACOutputLength);
      ((IntegrityHmac)this._signatureAlgorithm)
         .engineAddContextToElement(this._constructionElement);
   }
   public SignatureAlgorithm(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
      algorithmURI = this.getURI();
   }
   public byte[] sign() throws XMLSignatureException {
      return this._signatureAlgorithm.engineSign();
   }
   public String getJCEAlgorithmString() {
      try {
                return getInstanceForVerify(algorithmURI).engineGetJCEAlgorithmString();
        } catch (XMLSignatureException e) {
                return null;
        }
   }
   public String getJCEProviderName() {
      try {
                return getInstanceForVerify(algorithmURI).engineGetJCEProviderName();
        } catch (XMLSignatureException e) {
                return null;
        }
   }
   public void update(byte[] input) throws XMLSignatureException {
      this._signatureAlgorithm.engineUpdate(input);
   }
   public void update(byte input) throws XMLSignatureException {
      this._signatureAlgorithm.engineUpdate(input);
   }
   public void update(byte buf[], int offset, int len)
           throws XMLSignatureException {
      this._signatureAlgorithm.engineUpdate(buf, offset, len);
   }
   public void initSign(Key signingKey) throws XMLSignatureException {
           initializeAlgorithm(true);
           Map map=(Map)keysSigning.get();
       if (map.get(this.algorithmURI)==signingKey) {
           return;
       }
       map.put(this.algorithmURI,signingKey);
           this._signatureAlgorithm.engineInitSign(signingKey);
   }
   public void initSign(Key signingKey, SecureRandom secureRandom)
           throws XMLSignatureException {
           initializeAlgorithm(true);
      this._signatureAlgorithm.engineInitSign(signingKey, secureRandom);
   }
   public void initSign(
           Key signingKey, AlgorithmParameterSpec algorithmParameterSpec)
              throws XMLSignatureException {
           initializeAlgorithm(true);
      this._signatureAlgorithm.engineInitSign(signingKey,
                                              algorithmParameterSpec);
   }
   public void setParameter(AlgorithmParameterSpec params)
           throws XMLSignatureException {
      this._signatureAlgorithm.engineSetParameter(params);
   }
   public void initVerify(Key verificationKey) throws XMLSignatureException {
           initializeAlgorithm(false);
           Map map=(Map)keysVerify.get();
           if (map.get(this.algorithmURI)==verificationKey) {
           return;
       }
           map.put(this.algorithmURI,verificationKey);
           this._signatureAlgorithm.engineInitVerify(verificationKey);
   }
   public boolean verify(byte[] signature) throws XMLSignatureException {
      return this._signatureAlgorithm.engineVerify(signature);
   }
   public final String getURI() {
      return this._constructionElement.getAttributeNS(null,
              Constants._ATT_ALGORITHM);
   }
   public static void providerInit() {
      if (SignatureAlgorithm.log == null) {
         SignatureAlgorithm.log =
            java.util.logging.Logger
               .getLogger(SignatureAlgorithm.class.getName());
      }
      log.log(java.util.logging.Level.FINE, "Init() called");
      if (!SignatureAlgorithm._alreadyInitialized) {
         SignatureAlgorithm._algorithmHash = new HashMap(10);
         SignatureAlgorithm._alreadyInitialized = true;
      }
   }
   public static void register(String algorithmURI, String implementingClass)
           throws AlgorithmAlreadyRegisteredException,XMLSignatureException {
      {
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Try to register " + algorithmURI + " " + implementingClass);
         Class registeredClassClass =
            SignatureAlgorithm.getImplementingClass(algorithmURI);
                 if (registeredClassClass!=null) {
                         String registeredClass = registeredClassClass.getName();
                         if ((registeredClass != null) && (registeredClass.length() != 0)) {
                                 Object exArgs[] = { algorithmURI, registeredClass };
                                 throw new AlgorithmAlreadyRegisteredException(
                                                 "algorithm.alreadyRegistered", exArgs);
                         }
                 }
                 try {
                         SignatureAlgorithm._algorithmHash.put(algorithmURI, Class.forName(implementingClass));
              } catch (ClassNotFoundException ex) {
                 Object exArgs[] = { algorithmURI, ex.getMessage() };
                 throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                                 ex);
              } catch (NullPointerException ex) {
                 Object exArgs[] = { algorithmURI, ex.getMessage() };
                 throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                                 ex);
              }
      }
   }
   private static Class getImplementingClass(String URI) {
      if (SignatureAlgorithm._algorithmHash == null) {
         return null;
      }
      return (Class) SignatureAlgorithm._algorithmHash.get(URI);
   }
   public String getBaseNamespace() {
      return Constants.SignatureSpecNS;
   }
   public String getBaseLocalName() {
      return Constants._TAG_SIGNATUREMETHOD;
   }
}
