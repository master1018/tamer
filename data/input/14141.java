public class MessageDigestAlgorithm extends Algorithm {
   public static final String ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5 = Constants.MoreAlgorithmsSpecNS + "md5";
   public static final String ALGO_ID_DIGEST_SHA1 = Constants.SignatureSpecNS + "sha1";
   public static final String ALGO_ID_DIGEST_SHA256 = EncryptionConstants.EncryptionSpecNS + "sha256";
   public static final String ALGO_ID_DIGEST_SHA384 = Constants.MoreAlgorithmsSpecNS + "sha384";
   public static final String ALGO_ID_DIGEST_SHA512 = EncryptionConstants.EncryptionSpecNS + "sha512";
   public static final String ALGO_ID_DIGEST_RIPEMD160 = EncryptionConstants.EncryptionSpecNS + "ripemd160";
   java.security.MessageDigest algorithm = null;
   private MessageDigestAlgorithm(Document doc, MessageDigest messageDigest,
                                  String algorithmURI) {
      super(doc, algorithmURI);
      this.algorithm = messageDigest;
   }
   static ThreadLocal instances=new ThreadLocal() {
           protected Object initialValue() {
                   return new HashMap();
           };
   };
   public static MessageDigestAlgorithm getInstance(
           Document doc, String algorithmURI) throws XMLSignatureException {
          MessageDigest md = getDigestInstance(algorithmURI);
      return new MessageDigestAlgorithm(doc, md, algorithmURI);
   }
private static MessageDigest getDigestInstance(String algorithmURI) throws XMLSignatureException {
        MessageDigest result=(MessageDigest) ((Map)instances.get()).get(algorithmURI);
        if (result!=null)
                return result;
    String algorithmID = JCEMapper.translateURItoJCEID(algorithmURI);
          if (algorithmID == null) {
                  Object[] exArgs = { algorithmURI };
                  throw new XMLSignatureException("algorithms.NoSuchMap", exArgs);
          }
      MessageDigest md;
      String provider=JCEMapper.getProviderId();
      try {
         if (provider==null) {
                md = MessageDigest.getInstance(algorithmID);
         } else {
                md = MessageDigest.getInstance(algorithmID,provider);
         }
      } catch (java.security.NoSuchAlgorithmException ex) {
         Object[] exArgs = { algorithmID,
                             ex.getLocalizedMessage() };
         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
      } catch (NoSuchProviderException ex) {
        Object[] exArgs = { algorithmID,
                                                ex.getLocalizedMessage() };
        throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
      ((Map)instances.get()).put(algorithmURI, md);
        return md;
}
   public java.security.MessageDigest getAlgorithm() {
      return this.algorithm;
   }
   public static boolean isEqual(byte[] digesta, byte[] digestb) {
      return java.security.MessageDigest.isEqual(digesta, digestb);
   }
   public byte[] digest() {
      return this.algorithm.digest();
   }
   public byte[] digest(byte input[]) {
      return this.algorithm.digest(input);
   }
   public int digest(byte buf[], int offset, int len)
           throws java.security.DigestException {
      return this.algorithm.digest(buf, offset, len);
   }
   public String getJCEAlgorithmString() {
      return this.algorithm.getAlgorithm();
   }
   public java.security.Provider getJCEProvider() {
      return this.algorithm.getProvider();
   }
   public int getDigestLength() {
      return this.algorithm.getDigestLength();
   }
   public void reset() {
      this.algorithm.reset();
   }
   public void update(byte[] input) {
      this.algorithm.update(input);
   }
   public void update(byte input) {
      this.algorithm.update(input);
   }
   public void update(byte buf[], int offset, int len) {
      this.algorithm.update(buf, offset, len);
   }
   public String getBaseNamespace() {
      return Constants.SignatureSpecNS;
   }
   public String getBaseLocalName() {
      return Constants._TAG_DIGESTMETHOD;
   }
}
