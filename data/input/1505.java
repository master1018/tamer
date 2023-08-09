public class XMLCipherInput {
    private static java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger(XMLCipher.class.getName());
        private CipherData _cipherData;
        private int _mode;
        public XMLCipherInput(CipherData data) throws XMLEncryptionException {
                _cipherData = data;
                _mode = XMLCipher.DECRYPT_MODE;
                if (_cipherData == null) {
                        throw new XMLEncryptionException("CipherData is null");
                }
        }
        public XMLCipherInput(EncryptedType input) throws XMLEncryptionException {
                _cipherData = ((input == null) ? null : input.getCipherData());
                _mode = XMLCipher.DECRYPT_MODE;
                if (_cipherData == null) {
                        throw new XMLEncryptionException("CipherData is null");
                }
        }
        public byte[] getBytes() throws XMLEncryptionException {
                if (_mode == XMLCipher.DECRYPT_MODE) {
                        return getDecryptBytes();
                }
                return null;
        }
    private byte[] getDecryptBytes() throws XMLEncryptionException {
        String base64EncodedEncryptedOctets = null;
        if (_cipherData.getDataType() == CipherData.REFERENCE_TYPE) {
            logger.log(java.util.logging.Level.FINE, "Found a reference type CipherData");
            CipherReference cr = _cipherData.getCipherReference();
            Attr uriAttr = cr.getURIAsAttr();
            XMLSignatureInput input = null;
            try {
                ResourceResolver resolver =
                    ResourceResolver.getInstance(uriAttr, null);
                input = resolver.resolve(uriAttr, null);
            } catch (ResourceResolverException ex) {
                throw new XMLEncryptionException("empty", ex);
            }
            if (input != null) {
                logger.log(java.util.logging.Level.FINE, "Managed to resolve URI \"" + cr.getURI() + "\"");
            } else {
                logger.log(java.util.logging.Level.FINE, "Failed to resolve URI \"" + cr.getURI() + "\"");
            }
            Transforms transforms = cr.getTransforms();
            if (transforms != null) {
                logger.log(java.util.logging.Level.FINE, "Have transforms in cipher reference");
                try {
                    com.sun.org.apache.xml.internal.security.transforms.Transforms dsTransforms =
                        transforms.getDSTransforms();
                    input = dsTransforms.performTransforms(input);
                } catch (TransformationException ex) {
                    throw new XMLEncryptionException("empty", ex);
                }
            }
            try {
                return input.getBytes();
            } catch (IOException ex) {
                throw new XMLEncryptionException("empty", ex);
            } catch (CanonicalizationException ex) {
                throw new XMLEncryptionException("empty", ex);
            }
        } else if (_cipherData.getDataType() == CipherData.VALUE_TYPE) {
            base64EncodedEncryptedOctets =
                _cipherData.getCipherValue().getValue();
        } else {
            throw new XMLEncryptionException("CipherData.getDataType() returned unexpected value");
        }
        logger.log(java.util.logging.Level.FINE, "Encrypted octets:\n" + base64EncodedEncryptedOctets);
        byte[] encryptedBytes = null;
        try {
            encryptedBytes = Base64.decode(base64EncodedEncryptedOctets);
        } catch (Base64DecodingException bde) {
            throw new XMLEncryptionException("empty", bde);
        }
        return (encryptedBytes);
    }
}
