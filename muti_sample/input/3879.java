public class EncryptionConstants {
   public static final String _ATT_ALGORITHM              = Constants._ATT_ALGORITHM;
   public static final String _ATT_ID                     = Constants._ATT_ID;
   public static final String _ATT_TARGET                 = Constants._ATT_TARGET;
   public static final String _ATT_TYPE                   = Constants._ATT_TYPE;
   public static final String _ATT_URI                    = Constants._ATT_URI;
   public static final String _ATT_ENCODING               = "Encoding";
   public static final String _ATT_RECIPIENT              = "Recipient";
   public static final String _ATT_MIMETYPE               = "MimeType";
   public static final String _TAG_CARRIEDKEYNAME         = "CarriedKeyName";
   public static final String _TAG_CIPHERDATA             = "CipherData";
   public static final String _TAG_CIPHERREFERENCE        = "CipherReference";
   public static final String _TAG_CIPHERVALUE            = "CipherValue";
   public static final String _TAG_DATAREFERENCE          = "DataReference";
   public static final String _TAG_ENCRYPTEDDATA          = "EncryptedData";
   public static final String _TAG_ENCRYPTEDKEY           = "EncryptedKey";
   public static final String _TAG_ENCRYPTIONMETHOD       = "EncryptionMethod";
   public static final String _TAG_ENCRYPTIONPROPERTIES   = "EncryptionProperties";
   public static final String _TAG_ENCRYPTIONPROPERTY     = "EncryptionProperty";
   public static final String _TAG_KEYREFERENCE           = "KeyReference";
   public static final String _TAG_KEYSIZE                = "KeySize";
   public static final String _TAG_OAEPPARAMS             = "OAEPparams";
   public static final String _TAG_REFERENCELIST          = "ReferenceList";
   public static final String _TAG_TRANSFORMS             = "Transforms";
   public static final String _TAG_AGREEMENTMETHOD        = "AgreementMethod";
   public static final String _TAG_KA_NONCE               = "KA-Nonce";
   public static final String _TAG_ORIGINATORKEYINFO      = "OriginatorKeyInfo";
   public static final String _TAG_RECIPIENTKEYINFO       = "RecipientKeyInfo";
   public static final String ENCRYPTIONSPECIFICATION_URL = "http:
   public static final String TYPE_CONTENT                = EncryptionSpecNS + "Content";
   public static final String TYPE_ELEMENT                = EncryptionSpecNS + "Element";
   public static final String TYPE_MEDIATYPE              = "http:
   public static final String ALGO_ID_BLOCKCIPHER_TRIPLEDES = EncryptionConstants.EncryptionSpecNS + "tripledes-cbc";
   public static final String ALGO_ID_BLOCKCIPHER_AES128 = EncryptionConstants.EncryptionSpecNS + "aes128-cbc";
   public static final String ALGO_ID_BLOCKCIPHER_AES256 = EncryptionConstants.EncryptionSpecNS + "aes256-cbc";
   public static final String ALGO_ID_BLOCKCIPHER_AES192 = EncryptionConstants.EncryptionSpecNS + "aes192-cbc";
   public static final String ALGO_ID_KEYTRANSPORT_RSA15 = EncryptionConstants.EncryptionSpecNS + "rsa-1_5";
   public static final String ALGO_ID_KEYTRANSPORT_RSAOAEP = EncryptionConstants.EncryptionSpecNS + "rsa-oaep-mgf1p";
   public static final String ALGO_ID_KEYAGREEMENT_DH = EncryptionConstants.EncryptionSpecNS + "dh";
   public static final String ALGO_ID_KEYWRAP_TRIPLEDES = EncryptionConstants.EncryptionSpecNS + "kw-tripledes";
   public static final String ALGO_ID_KEYWRAP_AES128 = EncryptionConstants.EncryptionSpecNS + "kw-aes128";
   public static final String ALGO_ID_KEYWRAP_AES256 = EncryptionConstants.EncryptionSpecNS + "kw-aes256";
   public static final String ALGO_ID_KEYWRAP_AES192 = EncryptionConstants.EncryptionSpecNS + "kw-aes192";
   public static final String ALGO_ID_AUTHENTICATION_XMLSIGNATURE = "http:
   public static final String ALGO_ID_C14N_WITHCOMMENTS = "http:
   public static final String ALGO_ID_C14N_OMITCOMMENTS = "http:
   public static final String ALGO_ID_ENCODING_BASE64 = "http:
   private EncryptionConstants() {
   }
   public static void setEncryptionSpecNSprefix(String newPrefix)
           throws XMLSecurityException {
      ElementProxy.setDefaultPrefix(EncryptionConstants.EncryptionSpecNS,
                                    newPrefix);
   }
   public static String getEncryptionSpecNSprefix() {
      return ElementProxy
         .getDefaultPrefix(EncryptionConstants.EncryptionSpecNS);
   }
}
