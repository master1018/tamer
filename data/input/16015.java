public class XMLCipher {
    private static java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger(XMLCipher.class.getName());
    public static final String TRIPLEDES =
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES;
    public static final String AES_128 =
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
    public static final String AES_256 =
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256;
    public static final String AES_192 =
        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192;
    public static final String RSA_v1dot5 =
        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
    public static final String RSA_OAEP =
        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;
    public static final String DIFFIE_HELLMAN =
        EncryptionConstants.ALGO_ID_KEYAGREEMENT_DH;
    public static final String TRIPLEDES_KeyWrap =
        EncryptionConstants.ALGO_ID_KEYWRAP_TRIPLEDES;
    public static final String AES_128_KeyWrap =
        EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
    public static final String AES_256_KeyWrap =
        EncryptionConstants.ALGO_ID_KEYWRAP_AES256;
    public static final String AES_192_KeyWrap =
        EncryptionConstants.ALGO_ID_KEYWRAP_AES192;
    public static final String SHA1 =
        Constants.ALGO_ID_DIGEST_SHA1;
    public static final String SHA256 =
        MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;
    public static final String SHA512 =
        MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512;
    public static final String RIPEMD_160 =
        MessageDigestAlgorithm.ALGO_ID_DIGEST_RIPEMD160;
    public static final String XML_DSIG =
        Constants.SignatureSpecNS;
    public static final String N14C_XML =
        Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    public static final String N14C_XML_WITH_COMMENTS =
        Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    public static final String EXCL_XML_N14C =
        Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    public static final String EXCL_XML_N14C_WITH_COMMENTS =
        Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
    public static final String BASE64_ENCODING =
        com.sun.org.apache.xml.internal.security.transforms.Transforms.TRANSFORM_BASE64_DECODE;
    public static final int ENCRYPT_MODE = Cipher.ENCRYPT_MODE;
    public static final int DECRYPT_MODE = Cipher.DECRYPT_MODE;
    public static final int UNWRAP_MODE  = Cipher.UNWRAP_MODE;
    public static final int WRAP_MODE    = Cipher.WRAP_MODE;
    private static final String ENC_ALGORITHMS = TRIPLEDES + "\n" +
        AES_128 + "\n" + AES_256 + "\n" + AES_192 + "\n" + RSA_v1dot5 + "\n" +
        RSA_OAEP + "\n" + TRIPLEDES_KeyWrap + "\n" + AES_128_KeyWrap + "\n" +
        AES_256_KeyWrap + "\n" + AES_192_KeyWrap+ "\n";
    private Cipher _contextCipher;
    private int _cipherMode = Integer.MIN_VALUE;
    private String _algorithm = null;
        private String _requestedJCEProvider = null;
        private Canonicalizer _canon;
    private Document _contextDocument;
    private Factory _factory;
    private Serializer _serializer;
        private Key _key;
        private Key _kek;
        private EncryptedKey _ek;
        private EncryptedData _ed;
    private XMLCipher() {
        logger.log(java.util.logging.Level.FINE, "Constructing XMLCipher...");
        _factory = new Factory();
        _serializer = new Serializer();
    }
    private static boolean isValidEncryptionAlgorithm(String algorithm) {
        boolean result = (
            algorithm.equals(TRIPLEDES) ||
            algorithm.equals(AES_128) ||
            algorithm.equals(AES_256) ||
            algorithm.equals(AES_192) ||
            algorithm.equals(RSA_v1dot5) ||
            algorithm.equals(RSA_OAEP) ||
            algorithm.equals(TRIPLEDES_KeyWrap) ||
            algorithm.equals(AES_128_KeyWrap) ||
            algorithm.equals(AES_256_KeyWrap) ||
            algorithm.equals(AES_192_KeyWrap)
        );
        return (result);
    }
    public static XMLCipher getInstance(String transformation) throws
            XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Getting XMLCipher...");
        if (null == transformation)
            logger.log(java.util.logging.Level.SEVERE, "Transformation unexpectedly null...");
        if(!isValidEncryptionAlgorithm(transformation))
            logger.log(java.util.logging.Level.WARNING, "Algorithm non-standard, expected one of " + ENC_ALGORITHMS);
                XMLCipher instance = new XMLCipher();
        instance._algorithm = transformation;
                instance._key = null;
                instance._kek = null;
                try {
                        instance._canon = Canonicalizer.getInstance
                                (Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
                } catch (InvalidCanonicalizerException ice) {
                        throw new XMLEncryptionException("empty", ice);
                }
                String jceAlgorithm = JCEMapper.translateURItoJCEID(transformation);
                try {
            instance._contextCipher = Cipher.getInstance(jceAlgorithm);
            logger.log(java.util.logging.Level.FINE, "cihper.algoritm = " +
                instance._contextCipher.getAlgorithm());
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLEncryptionException("empty", nsae);
        } catch (NoSuchPaddingException nspe) {
            throw new XMLEncryptionException("empty", nspe);
        }
        return (instance);
    }
        public static XMLCipher getInstance(String transformation, String canon)
                throws XMLEncryptionException {
                XMLCipher instance = XMLCipher.getInstance(transformation);
                if (canon != null) {
                        try {
                                instance._canon = Canonicalizer.getInstance(canon);
                        } catch (InvalidCanonicalizerException ice) {
                                throw new XMLEncryptionException("empty", ice);
                        }
                }
                return instance;
        }
    public static XMLCipher getInstance(String transformation,Cipher cipher) throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Getting XMLCipher...");
        if (null == transformation)
            logger.log(java.util.logging.Level.SEVERE, "Transformation unexpectedly null...");
        if(!isValidEncryptionAlgorithm(transformation))
            logger.log(java.util.logging.Level.WARNING, "Algorithm non-standard, expected one of " + ENC_ALGORITHMS);
        XMLCipher instance = new XMLCipher();
        instance._algorithm = transformation;
        instance._key = null;
        instance._kek = null;
        try {
            instance._canon = Canonicalizer.getInstance
                    (Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
        } catch (InvalidCanonicalizerException ice) {
            throw new XMLEncryptionException("empty", ice);
        }
        String jceAlgorithm = JCEMapper.translateURItoJCEID(transformation);
        try {
            instance._contextCipher = cipher;
            logger.log(java.util.logging.Level.FINE, "cihper.algoritm = " +
                    instance._contextCipher.getAlgorithm());
        }catch(Exception ex) {
            throw new XMLEncryptionException("empty", ex);
        }
        return (instance);
    }
    public static XMLCipher getProviderInstance(String transformation, String provider)
            throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Getting XMLCipher...");
        if (null == transformation)
            logger.log(java.util.logging.Level.SEVERE, "Transformation unexpectedly null...");
        if(null == provider)
            logger.log(java.util.logging.Level.SEVERE, "Provider unexpectedly null..");
        if("" == provider)
            logger.log(java.util.logging.Level.SEVERE, "Provider's value unexpectedly not specified...");
        if(!isValidEncryptionAlgorithm(transformation))
            logger.log(java.util.logging.Level.WARNING, "Algorithm non-standard, expected one of " + ENC_ALGORITHMS);
                XMLCipher instance = new XMLCipher();
        instance._algorithm = transformation;
                instance._requestedJCEProvider = provider;
                instance._key = null;
                instance._kek = null;
                try {
                        instance._canon = Canonicalizer.getInstance
                                (Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
                } catch (InvalidCanonicalizerException ice) {
                        throw new XMLEncryptionException("empty", ice);
                }
        try {
                        String jceAlgorithm =
                                JCEMapper.translateURItoJCEID(transformation);
            instance._contextCipher = Cipher.getInstance(jceAlgorithm, provider);
            logger.log(java.util.logging.Level.FINE, "cipher._algorithm = " +
                instance._contextCipher.getAlgorithm());
            logger.log(java.util.logging.Level.FINE, "provider.name = " + provider);
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLEncryptionException("empty", nsae);
        } catch (NoSuchProviderException nspre) {
            throw new XMLEncryptionException("empty", nspre);
        } catch (NoSuchPaddingException nspe) {
            throw new XMLEncryptionException("empty", nspe);
        }
        return (instance);
    }
        public static XMLCipher getProviderInstance(
                String transformation,
                String provider,
                String canon)
                throws XMLEncryptionException {
                XMLCipher instance = XMLCipher.getProviderInstance(transformation, provider);
                if (canon != null) {
                        try {
                                instance._canon = Canonicalizer.getInstance(canon);
                        } catch (InvalidCanonicalizerException ice) {
                                throw new XMLEncryptionException("empty", ice);
                        }
                }
                return instance;
        }
    public static XMLCipher getInstance()
            throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Getting XMLCipher for no transformation...");
                XMLCipher instance = new XMLCipher();
        instance._algorithm = null;
                instance._requestedJCEProvider = null;
                instance._key = null;
                instance._kek = null;
                instance._contextCipher = null;
                try {
                        instance._canon = Canonicalizer.getInstance
                                (Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
                } catch (InvalidCanonicalizerException ice) {
                        throw new XMLEncryptionException("empty", ice);
                }
        return (instance);
    }
    public static XMLCipher getProviderInstance(String provider)
            throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Getting XMLCipher, provider but no transformation");
        if(null == provider)
            logger.log(java.util.logging.Level.SEVERE, "Provider unexpectedly null..");
        if("" == provider)
            logger.log(java.util.logging.Level.SEVERE, "Provider's value unexpectedly not specified...");
                XMLCipher instance = new XMLCipher();
        instance._algorithm = null;
                instance._requestedJCEProvider = provider;
                instance._key = null;
                instance._kek = null;
                instance._contextCipher = null;
                try {
                        instance._canon = Canonicalizer.getInstance
                                (Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
                } catch (InvalidCanonicalizerException ice) {
                        throw new XMLEncryptionException("empty", ice);
                }
        return (instance);
    }
    public void init(int opmode, Key key) throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Initializing XMLCipher...");
                _ek = null;
                _ed = null;
                switch (opmode) {
                case ENCRYPT_MODE :
                        logger.log(java.util.logging.Level.FINE, "opmode = ENCRYPT_MODE");
                        _ed = createEncryptedData(CipherData.VALUE_TYPE, "NO VALUE YET");
                        break;
                case DECRYPT_MODE :
                        logger.log(java.util.logging.Level.FINE, "opmode = DECRYPT_MODE");
                        break;
                case WRAP_MODE :
                        logger.log(java.util.logging.Level.FINE, "opmode = WRAP_MODE");
                        _ek = createEncryptedKey(CipherData.VALUE_TYPE, "NO VALUE YET");
                        break;
                case UNWRAP_MODE :
                        logger.log(java.util.logging.Level.FINE, "opmode = UNWRAP_MODE");
                        break;
                default :
                        logger.log(java.util.logging.Level.SEVERE, "Mode unexpectedly invalid");
                        throw new XMLEncryptionException("Invalid mode in init");
                }
        _cipherMode = opmode;
                _key = key;
    }
        public EncryptedData getEncryptedData() {
                logger.log(java.util.logging.Level.FINE, "Returning EncryptedData");
                return _ed;
        }
        public EncryptedKey getEncryptedKey() {
                logger.log(java.util.logging.Level.FINE, "Returning EncryptedKey");
                return _ek;
        }
        public void setKEK(Key kek) {
                _kek = kek;
        }
        public Element martial(EncryptedData encryptedData) {
                return (_factory.toElement (encryptedData));
        }
        public Element martial(EncryptedKey encryptedKey) {
                return (_factory.toElement (encryptedKey));
        }
        public Element martial(Document context, EncryptedData encryptedData) {
                _contextDocument = context;
                return (_factory.toElement (encryptedData));
        }
        public Element martial(Document context, EncryptedKey encryptedKey) {
                _contextDocument = context;
                return (_factory.toElement (encryptedKey));
        }
    private Document encryptElement(Element element) throws Exception{
        logger.log(java.util.logging.Level.FINE, "Encrypting element...");
        if(null == element)
            logger.log(java.util.logging.Level.SEVERE, "Element unexpectedly null...");
        if(_cipherMode != ENCRYPT_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in ENCRYPT_MODE...");
                if (_algorithm == null) {
                throw new XMLEncryptionException("XMLCipher instance without transformation specified");
                }
                encryptData(_contextDocument, element, false);
        Element encryptedElement = _factory.toElement(_ed);
        Node sourceParent = element.getParentNode();
        sourceParent.replaceChild(encryptedElement, element);
        return (_contextDocument);
    }
    private Document encryptElementContent(Element element) throws
            Exception {
        logger.log(java.util.logging.Level.FINE, "Encrypting element content...");
        if(null == element)
            logger.log(java.util.logging.Level.SEVERE, "Element unexpectedly null...");
        if(_cipherMode != ENCRYPT_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in ENCRYPT_MODE...");
                if (_algorithm == null) {
                throw new XMLEncryptionException("XMLCipher instance without transformation specified");
                }
                encryptData(_contextDocument, element, true);
        Element encryptedElement = _factory.toElement(_ed);
        removeContent(element);
        element.appendChild(encryptedElement);
        return (_contextDocument);
    }
    public Document doFinal(Document context, Document source) throws
            Exception {
        logger.log(java.util.logging.Level.FINE, "Processing source document...");
        if(null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if(null == source)
            logger.log(java.util.logging.Level.SEVERE, "Source document unexpectedly null...");
        _contextDocument = context;
        Document result = null;
        switch (_cipherMode) {
        case DECRYPT_MODE:
            result = decryptElement(source.getDocumentElement());
            break;
        case ENCRYPT_MODE:
            result = encryptElement(source.getDocumentElement());
            break;
        case UNWRAP_MODE:
            break;
        case WRAP_MODE:
            break;
        default:
            throw new XMLEncryptionException(
                "empty", new IllegalStateException());
        }
        return (result);
    }
    public Document doFinal(Document context, Element element) throws
            Exception {
        logger.log(java.util.logging.Level.FINE, "Processing source element...");
        if(null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if(null == element)
            logger.log(java.util.logging.Level.SEVERE, "Source element unexpectedly null...");
        _contextDocument = context;
        Document result = null;
        switch (_cipherMode) {
        case DECRYPT_MODE:
            result = decryptElement(element);
            break;
        case ENCRYPT_MODE:
            result = encryptElement(element);
            break;
        case UNWRAP_MODE:
            break;
        case WRAP_MODE:
            break;
        default:
            throw new XMLEncryptionException(
                "empty", new IllegalStateException());
        }
        return (result);
    }
    public Document doFinal(Document context, Element element, boolean content)
            throws  Exception {
        logger.log(java.util.logging.Level.FINE, "Processing source element...");
        if(null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if(null == element)
            logger.log(java.util.logging.Level.SEVERE, "Source element unexpectedly null...");
        _contextDocument = context;
        Document result = null;
        switch (_cipherMode) {
        case DECRYPT_MODE:
            if (content) {
                result = decryptElementContent(element);
            } else {
                result = decryptElement(element);
            }
            break;
        case ENCRYPT_MODE:
            if (content) {
                result = encryptElementContent(element);
            } else {
                result = encryptElement(element);
            }
            break;
        case UNWRAP_MODE:
            break;
        case WRAP_MODE:
            break;
        default:
            throw new XMLEncryptionException(
                "empty", new IllegalStateException());
        }
        return (result);
    }
    public EncryptedData encryptData(Document context, Element element) throws
            Exception {
        return encryptData(context, element, false);
    }
    public EncryptedData encryptData(Document context, String type,
        InputStream serializedData) throws Exception {
        logger.log(java.util.logging.Level.FINE, "Encrypting element...");
        if (null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if (null == serializedData)
            logger.log(java.util.logging.Level.SEVERE, "Serialized data unexpectedly null...");
        if (_cipherMode != ENCRYPT_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in ENCRYPT_MODE...");
        return encryptData(context, null, type, serializedData);
    }
    public EncryptedData encryptData(
        Document context, Element element, boolean contentMode)
        throws  Exception {
        logger.log(java.util.logging.Level.FINE, "Encrypting element...");
        if (null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if (null == element)
            logger.log(java.util.logging.Level.SEVERE, "Element unexpectedly null...");
        if (_cipherMode != ENCRYPT_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in ENCRYPT_MODE...");
        if (contentMode) {
            return encryptData
                (context, element, EncryptionConstants.TYPE_CONTENT, null);
        } else {
            return encryptData
                (context, element, EncryptionConstants.TYPE_ELEMENT, null);
        }
    }
    private EncryptedData encryptData(
        Document context, Element element, String type,
        InputStream serializedData) throws  Exception {
        _contextDocument = context;
        if (_algorithm == null) {
            throw new XMLEncryptionException
                ("XMLCipher instance without transformation specified");
        }
        String serializedOctets = null;
        if (serializedData == null) {
            if (type == EncryptionConstants.TYPE_CONTENT) {
                NodeList children = element.getChildNodes();
                if (null != children) {
                    serializedOctets = _serializer.serialize(children);
                } else {
                    Object exArgs[] = { "Element has no content." };
                    throw new XMLEncryptionException("empty", exArgs);
                }
            } else {
                serializedOctets = _serializer.serialize(element);
            }
            logger.log(java.util.logging.Level.FINE, "Serialized octets:\n" + serializedOctets);
        }
        byte[] encryptedBytes = null;
        Cipher c;
        if (_contextCipher == null) {
            String jceAlgorithm = JCEMapper.translateURItoJCEID(_algorithm);
            logger.log(java.util.logging.Level.FINE, "alg = " + jceAlgorithm);
            try {
                if (_requestedJCEProvider == null)
                    c = Cipher.getInstance(jceAlgorithm);
                else
                    c = Cipher.getInstance(jceAlgorithm, _requestedJCEProvider);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XMLEncryptionException("empty", nsae);
            } catch (NoSuchProviderException nspre) {
                throw new XMLEncryptionException("empty", nspre);
            } catch (NoSuchPaddingException nspae) {
                throw new XMLEncryptionException("empty", nspae);
            }
        } else {
            c = _contextCipher;
        }
        try {
            c.init(_cipherMode, _key);
        } catch (InvalidKeyException ike) {
            throw new XMLEncryptionException("empty", ike);
        }
        try {
            if (serializedData != null) {
                int numBytes;
                byte[] buf = new byte[8192];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((numBytes = serializedData.read(buf)) != -1) {
                    byte[] data = c.update(buf, 0, numBytes);
                    baos.write(data);
                }
                baos.write(c.doFinal());
                encryptedBytes = baos.toByteArray();
            } else {
                encryptedBytes = c.doFinal(serializedOctets.getBytes("UTF-8"));
                logger.log(java.util.logging.Level.FINE, "Expected cipher.outputSize = " +
                    Integer.toString(c.getOutputSize(
                        serializedOctets.getBytes().length)));
            }
            logger.log(java.util.logging.Level.FINE, "Actual cipher.outputSize = " +
                Integer.toString(encryptedBytes.length));
        } catch (IllegalStateException ise) {
            throw new XMLEncryptionException("empty", ise);
        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (BadPaddingException bpe) {
            throw new XMLEncryptionException("empty", bpe);
        } catch (UnsupportedEncodingException uee) {
            throw new XMLEncryptionException("empty", uee);
        }
        byte[] iv = c.getIV();
        byte[] finalEncryptedBytes =
                new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, finalEncryptedBytes, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, finalEncryptedBytes, iv.length,
                         encryptedBytes.length);
        String base64EncodedEncryptedOctets = Base64.encode(finalEncryptedBytes);
        logger.log(java.util.logging.Level.FINE, "Encrypted octets:\n" + base64EncodedEncryptedOctets);
        logger.log(java.util.logging.Level.FINE, "Encrypted octets length = " +
            base64EncodedEncryptedOctets.length());
        try {
            CipherData cd = _ed.getCipherData();
            CipherValue cv = cd.getCipherValue();
            cv.setValue(base64EncodedEncryptedOctets);
            if (type != null) {
                _ed.setType(new URI(type).toString());
            }
            EncryptionMethod method =
                _factory.newEncryptionMethod(new URI(_algorithm).toString());
            _ed.setEncryptionMethod(method);
        } catch (URI.MalformedURIException mfue) {
            throw new XMLEncryptionException("empty", mfue);
        }
        return (_ed);
    }
    public EncryptedData loadEncryptedData(Document context, Element element)
                throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Loading encrypted element...");
        if(null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if(null == element)
            logger.log(java.util.logging.Level.SEVERE, "Element unexpectedly null...");
        if(_cipherMode != DECRYPT_MODE)
            logger.log(java.util.logging.Level.SEVERE, "XMLCipher unexpectedly not in DECRYPT_MODE...");
        _contextDocument = context;
        _ed = _factory.newEncryptedData(element);
                return (_ed);
    }
    public EncryptedKey loadEncryptedKey(Document context, Element element)
                throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Loading encrypted key...");
        if(null == context)
            logger.log(java.util.logging.Level.SEVERE, "Context document unexpectedly null...");
        if(null == element)
            logger.log(java.util.logging.Level.SEVERE, "Element unexpectedly null...");
        if(_cipherMode != UNWRAP_MODE && _cipherMode != DECRYPT_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in UNWRAP_MODE or DECRYPT_MODE...");
        _contextDocument = context;
        _ek = _factory.newEncryptedKey(element);
                return (_ek);
    }
    public EncryptedKey loadEncryptedKey(Element element)
                throws XMLEncryptionException {
                return (loadEncryptedKey(element.getOwnerDocument(), element));
    }
    public EncryptedKey encryptKey(Document doc, Key key) throws
            XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Encrypting key ...");
        if(null == key)
            logger.log(java.util.logging.Level.SEVERE, "Key unexpectedly null...");
        if(_cipherMode != WRAP_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in WRAP_MODE...");
                if (_algorithm == null) {
                        throw new XMLEncryptionException("XMLCipher instance without transformation specified");
                }
                _contextDocument = doc;
                byte[] encryptedBytes = null;
                Cipher c;
                if (_contextCipher == null) {
                        String jceAlgorithm =
                                JCEMapper.translateURItoJCEID(_algorithm);
                        logger.log(java.util.logging.Level.FINE, "alg = " + jceAlgorithm);
                        try {
                            if (_requestedJCEProvider == null)
                                c = Cipher.getInstance(jceAlgorithm);
                            else
                                c = Cipher.getInstance(jceAlgorithm, _requestedJCEProvider);
                        } catch (NoSuchAlgorithmException nsae) {
                                throw new XMLEncryptionException("empty", nsae);
                        } catch (NoSuchProviderException nspre) {
                                throw new XMLEncryptionException("empty", nspre);
                        } catch (NoSuchPaddingException nspae) {
                                throw new XMLEncryptionException("empty", nspae);
                        }
                } else {
                        c = _contextCipher;
                }
                try {
                        c.init(Cipher.WRAP_MODE, _key);
                        encryptedBytes = c.wrap(key);
                } catch (InvalidKeyException ike) {
                        throw new XMLEncryptionException("empty", ike);
                } catch (IllegalBlockSizeException ibse) {
                        throw new XMLEncryptionException("empty", ibse);
                }
        String base64EncodedEncryptedOctets = Base64.encode(encryptedBytes);
        logger.log(java.util.logging.Level.FINE, "Encrypted key octets:\n" + base64EncodedEncryptedOctets);
        logger.log(java.util.logging.Level.FINE, "Encrypted key octets length = " +
            base64EncodedEncryptedOctets.length());
                CipherValue cv = _ek.getCipherData().getCipherValue();
                cv.setValue(base64EncodedEncryptedOctets);
        try {
            EncryptionMethod method = _factory.newEncryptionMethod(
                new URI(_algorithm).toString());
            _ek.setEncryptionMethod(method);
        } catch (URI.MalformedURIException mfue) {
            throw new XMLEncryptionException("empty", mfue);
        }
                return _ek;
    }
        public Key decryptKey(EncryptedKey encryptedKey, String algorithm) throws
                    XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Decrypting key from previously loaded EncryptedKey...");
        if(_cipherMode != UNWRAP_MODE)
            logger.log(java.util.logging.Level.FINE, "XMLCipher unexpectedly not in UNWRAP_MODE...");
                if (algorithm == null) {
                        throw new XMLEncryptionException("Cannot decrypt a key without knowing the algorithm");
                }
                if (_key == null) {
                        logger.log(java.util.logging.Level.FINE, "Trying to find a KEK via key resolvers");
                        KeyInfo ki = encryptedKey.getKeyInfo();
                        if (ki != null) {
                                try {
                                        _key = ki.getSecretKey();
                                }
                                catch (Exception e) {
                                }
                        }
                        if (_key == null) {
                                logger.log(java.util.logging.Level.SEVERE, "XMLCipher::decryptKey called without a KEK and cannot resolve");
                                throw new XMLEncryptionException("Unable to decrypt without a KEK");
                        }
                }
                XMLCipherInput cipherInput = new XMLCipherInput(encryptedKey);
                byte [] encryptedBytes = cipherInput.getBytes();
                String jceKeyAlgorithm =
                        JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);
                Cipher c;
                if (_contextCipher == null) {
                        String jceAlgorithm =
                                JCEMapper.translateURItoJCEID(
                                        encryptedKey.getEncryptionMethod().getAlgorithm());
                        logger.log(java.util.logging.Level.FINE, "JCE Algorithm = " + jceAlgorithm);
                        try {
                            if (_requestedJCEProvider == null)
                                c = Cipher.getInstance(jceAlgorithm);
                            else
                                c = Cipher.getInstance(jceAlgorithm, _requestedJCEProvider);
                        } catch (NoSuchAlgorithmException nsae) {
                                throw new XMLEncryptionException("empty", nsae);
                        } catch (NoSuchProviderException nspre) {
                                throw new XMLEncryptionException("empty", nspre);
                        } catch (NoSuchPaddingException nspae) {
                                throw new XMLEncryptionException("empty", nspae);
                        }
                } else {
                        c = _contextCipher;
                }
                Key ret;
                try {
                        c.init(Cipher.UNWRAP_MODE, _key);
                        ret = c.unwrap(encryptedBytes, jceKeyAlgorithm, Cipher.SECRET_KEY);
                } catch (InvalidKeyException ike) {
                        throw new XMLEncryptionException("empty", ike);
                } catch (NoSuchAlgorithmException nsae) {
                        throw new XMLEncryptionException("empty", nsae);
                }
                logger.log(java.util.logging.Level.FINE, "Decryption of key type " + algorithm + " OK");
                return ret;
    }
        public Key decryptKey(EncryptedKey encryptedKey) throws
                    XMLEncryptionException {
                return decryptKey(encryptedKey, _ed.getEncryptionMethod().getAlgorithm());
        }
    private static void removeContent(Node node) {
       while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }
    private Document decryptElement(Element element) throws
            XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Decrypting element...");
        if(_cipherMode != DECRYPT_MODE)
            logger.log(java.util.logging.Level.SEVERE, "XMLCipher unexpectedly not in DECRYPT_MODE...");
                String octets;
                try {
                        octets = new String(decryptToByteArray(element), "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                        throw new XMLEncryptionException("empty", uee);
                }
        logger.log(java.util.logging.Level.FINE, "Decrypted octets:\n" + octets);
        Node sourceParent =  element.getParentNode();
        DocumentFragment decryptedFragment =
                        _serializer.deserialize(octets, sourceParent);
                if (sourceParent instanceof Document) {
                    _contextDocument.removeChild(_contextDocument.getDocumentElement());
                    _contextDocument.appendChild(decryptedFragment);
                }
                else {
                    sourceParent.replaceChild(decryptedFragment, element);
                }
        return (_contextDocument);
    }
    private Document decryptElementContent(Element element) throws
                XMLEncryptionException {
        Element e = (Element) element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);
        if (null == e) {
                throw new XMLEncryptionException("No EncryptedData child element.");
        }
        return (decryptElement(e));
    }
        public byte[] decryptToByteArray(Element element)
                throws XMLEncryptionException {
        logger.log(java.util.logging.Level.FINE, "Decrypting to ByteArray...");
        if(_cipherMode != DECRYPT_MODE)
            logger.log(java.util.logging.Level.SEVERE, "XMLCipher unexpectedly not in DECRYPT_MODE...");
        EncryptedData encryptedData = _factory.newEncryptedData(element);
                if (_key == null) {
                        KeyInfo ki = encryptedData.getKeyInfo();
                        if (ki != null) {
                                try {
                                        ki.registerInternalKeyResolver(
                                     new EncryptedKeyResolver(encryptedData.
                                                                                                  getEncryptionMethod().
                                                                                                  getAlgorithm(),
                                                                                                  _kek));
                                        _key = ki.getSecretKey();
                                } catch (KeyResolverException kre) {
                                }
                        }
                        if (_key == null) {
                                logger.log(java.util.logging.Level.SEVERE, "XMLCipher::decryptElement called without a key and unable to resolve");
                                throw new XMLEncryptionException("encryption.nokey");
                        }
                }
                XMLCipherInput cipherInput = new XMLCipherInput(encryptedData);
                byte [] encryptedBytes = cipherInput.getBytes();
                String jceAlgorithm =
                        JCEMapper.translateURItoJCEID(encryptedData.getEncryptionMethod().getAlgorithm());
                Cipher c;
                try {
                    if (_requestedJCEProvider == null)
                        c = Cipher.getInstance(jceAlgorithm);
                    else
                        c = Cipher.getInstance(jceAlgorithm, _requestedJCEProvider);
                } catch (NoSuchAlgorithmException nsae) {
                        throw new XMLEncryptionException("empty", nsae);
                } catch (NoSuchProviderException nspre) {
                        throw new XMLEncryptionException("empty", nspre);
                } catch (NoSuchPaddingException nspae) {
                        throw new XMLEncryptionException("empty", nspae);
                }
                int ivLen = c.getBlockSize();
                byte[] ivBytes = new byte[ivLen];
                System.arraycopy(encryptedBytes, 0, ivBytes, 0, ivLen);
                IvParameterSpec iv = new IvParameterSpec(ivBytes);
                try {
                        c.init(_cipherMode, _key, iv);
                } catch (InvalidKeyException ike) {
                        throw new XMLEncryptionException("empty", ike);
                } catch (InvalidAlgorithmParameterException iape) {
                        throw new XMLEncryptionException("empty", iape);
                }
                byte[] plainBytes;
        try {
            plainBytes = c.doFinal(encryptedBytes,
                                                                   ivLen,
                                                                   encryptedBytes.length - ivLen);
        } catch (IllegalBlockSizeException ibse) {
            throw new XMLEncryptionException("empty", ibse);
        } catch (BadPaddingException bpe) {
            throw new XMLEncryptionException("empty", bpe);
        }
        return (plainBytes);
    }
    public EncryptedData createEncryptedData(int type, String value) throws
            XMLEncryptionException {
        EncryptedData result = null;
        CipherData data = null;
        switch (type) {
            case CipherData.REFERENCE_TYPE:
                CipherReference cipherReference = _factory.newCipherReference(
                    value);
                data = _factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result = _factory.newEncryptedData(data);
                                break;
            case CipherData.VALUE_TYPE:
                CipherValue cipherValue = _factory.newCipherValue(value);
                data = _factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result = _factory.newEncryptedData(data);
        }
        return (result);
    }
    public EncryptedKey createEncryptedKey(int type, String value) throws
            XMLEncryptionException {
        EncryptedKey result = null;
        CipherData data = null;
        switch (type) {
            case CipherData.REFERENCE_TYPE:
                CipherReference cipherReference = _factory.newCipherReference(
                    value);
                data = _factory.newCipherData(type);
                data.setCipherReference(cipherReference);
                result = _factory.newEncryptedKey(data);
                                break;
            case CipherData.VALUE_TYPE:
                CipherValue cipherValue = _factory.newCipherValue(value);
                data = _factory.newCipherData(type);
                data.setCipherValue(cipherValue);
                result = _factory.newEncryptedKey(data);
        }
        return (result);
    }
        public AgreementMethod createAgreementMethod(String algorithm) {
                return (_factory.newAgreementMethod(algorithm));
        }
        public CipherData createCipherData(int type) {
                return (_factory.newCipherData(type));
        }
        public CipherReference createCipherReference(String uri) {
                return (_factory.newCipherReference(uri));
        }
        public CipherValue createCipherValue(String value) {
                return (_factory.newCipherValue(value));
        }
        public EncryptionMethod createEncryptionMethod(String algorithm) {
                return (_factory.newEncryptionMethod(algorithm));
        }
        public EncryptionProperties createEncryptionProperties() {
                return (_factory.newEncryptionProperties());
        }
        public EncryptionProperty createEncryptionProperty() {
                return (_factory.newEncryptionProperty());
        }
        public ReferenceList createReferenceList(int type) {
                return (_factory.newReferenceList(type));
        }
        public Transforms createTransforms() {
                return (_factory.newTransforms());
        }
        public Transforms createTransforms(Document doc) {
                return (_factory.newTransforms(doc));
        }
    private class Serializer {
        Serializer() {
        }
        String serialize(Document document) throws Exception {
            return canonSerialize(document);
        }
                String serialize(Element element) throws Exception {
            return canonSerialize(element);
                }
        String serialize(NodeList content) throws Exception { 
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            _canon.setWriter(baos);
            _canon.notReset();
            for (int i = 0; i < content.getLength(); i++) {
                _canon.canonicalizeSubtree(content.item(i));
            }
            baos.close();
            return baos.toString("UTF-8");
        }
                String canonSerialize(Node node) throws Exception {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        _canon.setWriter(baos);
            _canon.notReset();
                        _canon.canonicalizeSubtree(node);
                        baos.close();
                        return baos.toString("UTF-8");
                }
        DocumentFragment deserialize(String source, Node ctx) throws XMLEncryptionException {
                        DocumentFragment result;
            final String tagname = "fragment";
                        StringBuffer sb;
                        sb = new StringBuffer();
                        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><"+tagname);
                        Node wk = ctx;
                        while (wk != null) {
                                NamedNodeMap atts = wk.getAttributes();
                                int length;
                                if (atts != null)
                                        length = atts.getLength();
                                else
                                        length = 0;
                                for (int i = 0 ; i < length ; ++i) {
                                        Node att = atts.item(i);
                                        if (att.getNodeName().startsWith("xmlns:") ||
                                                att.getNodeName().equals("xmlns")) {
                                                Node p = ctx;
                                                boolean found = false;
                                                while (p != wk) {
                                                        NamedNodeMap tstAtts = p.getAttributes();
                                                        if (tstAtts != null &&
                                                                tstAtts.getNamedItem(att.getNodeName()) != null) {
                                                                found = true;
                                                                break;
                                                        }
                                                        p = p.getParentNode();
                                                }
                                                if (found == false) {
                                                        sb.append(" " + att.getNodeName() + "=\"" +
                                                                          att.getNodeValue() + "\"");
                                                }
                                        }
                                }
                                wk = wk.getParentNode();
                        }
                        sb.append(">" + source + "</" + tagname + ">");
                        String fragment = sb.toString();
            try {
                DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
                                dbf.setNamespaceAware(true);
                                dbf.setAttribute("http:
                                DocumentBuilder db = dbf.newDocumentBuilder();
                                Document d = db.parse(
                                    new InputSource(new StringReader(fragment)));
                                Element fragElt = (Element) _contextDocument.importNode(
                                                 d.getDocumentElement(), true);
                                result = _contextDocument.createDocumentFragment();
                                Node child = fragElt.getFirstChild();
                                while (child != null) {
                                        fragElt.removeChild(child);
                                        result.appendChild(child);
                                        child = fragElt.getFirstChild();
                                }
            } catch (SAXException se) {
                throw new XMLEncryptionException("empty", se);
            } catch (ParserConfigurationException pce) {
                throw new XMLEncryptionException("empty", pce);
            } catch (IOException ioe) {
                throw new XMLEncryptionException("empty", ioe);
            }
            return (result);
        }
    }
    private class Factory {
        AgreementMethod newAgreementMethod(String algorithm)  {
            return (new AgreementMethodImpl(algorithm));
        }
        CipherData newCipherData(int type) {
            return (new CipherDataImpl(type));
        }
        CipherReference newCipherReference(String uri)  {
            return (new CipherReferenceImpl(uri));
        }
        CipherValue newCipherValue(String value) {
            return (new CipherValueImpl(value));
        }
        EncryptedData newEncryptedData(CipherData data) {
            return (new EncryptedDataImpl(data));
        }
        EncryptedKey newEncryptedKey(CipherData data) {
            return (new EncryptedKeyImpl(data));
        }
        EncryptionMethod newEncryptionMethod(String algorithm) {
            return (new EncryptionMethodImpl(algorithm));
        }
        EncryptionProperties newEncryptionProperties() {
            return (new EncryptionPropertiesImpl());
        }
        EncryptionProperty newEncryptionProperty() {
            return (new EncryptionPropertyImpl());
        }
        ReferenceList newReferenceList(int type) {
            return (new ReferenceListImpl(type));
        }
        Transforms newTransforms() {
            return (new TransformsImpl());
        }
        Transforms newTransforms(Document doc) {
            return (new TransformsImpl(doc));
        }
        AgreementMethod newAgreementMethod(Element element) throws
                XMLEncryptionException {
            if (null == element) {
                throw new NullPointerException("element is null");
            }
            String algorithm = element.getAttributeNS(null,
                EncryptionConstants._ATT_ALGORITHM);
            AgreementMethod result = newAgreementMethod(algorithm);
            Element kaNonceElement = (Element) element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_KA_NONCE).item(0);
            if (null != kaNonceElement) {
                result.setKANonce(kaNonceElement.getNodeValue().getBytes());
            }
            Element originatorKeyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ORIGINATORKEYINFO).item(0);
            if (null != originatorKeyInfoElement) {
                try {
                    result.setOriginatorKeyInfo(
                        new KeyInfo(originatorKeyInfoElement, null));
                } catch (XMLSecurityException xse) {
                    throw new XMLEncryptionException("empty", xse);
                }
            }
            Element recipientKeyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_RECIPIENTKEYINFO).item(0);
            if (null != recipientKeyInfoElement) {
                try {
                    result.setRecipientKeyInfo(
                        new KeyInfo(recipientKeyInfoElement, null));
                } catch (XMLSecurityException xse) {
                    throw new XMLEncryptionException("empty", xse);
                }
            }
            return (result);
        }
        CipherData newCipherData(Element element) throws
                XMLEncryptionException {
            if (null == element) {
                throw new NullPointerException("element is null");
            }
            int type = 0;
            Element e = null;
            if (element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_CIPHERVALUE).getLength() > 0) {
                type = CipherData.VALUE_TYPE;
                e = (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERVALUE).item(0);
            } else if (element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_CIPHERREFERENCE).getLength() > 0) {
                type = CipherData.REFERENCE_TYPE;
                e = (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERREFERENCE).item(0);
            }
            CipherData result = newCipherData(type);
            if (type == CipherData.VALUE_TYPE) {
                result.setCipherValue(newCipherValue(e));
            } else if (type == CipherData.REFERENCE_TYPE) {
                result.setCipherReference(newCipherReference(e));
            }
            return (result);
        }
        CipherReference newCipherReference(Element element) throws
                XMLEncryptionException {
                        Attr URIAttr =
                                element.getAttributeNodeNS(null, EncryptionConstants._ATT_URI);
                        CipherReference result = new CipherReferenceImpl(URIAttr);
                        NodeList transformsElements = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_TRANSFORMS);
            Element transformsElement =
                                (Element) transformsElements.item(0);
                        if (transformsElement != null) {
                                logger.log(java.util.logging.Level.FINE, "Creating a DSIG based Transforms element");
                                try {
                                        result.setTransforms(new TransformsImpl(transformsElement));
                                }
                                catch (XMLSignatureException xse) {
                                        throw new XMLEncryptionException("empty", xse);
                                } catch (InvalidTransformException ite) {
                                        throw new XMLEncryptionException("empty", ite);
                                } catch (XMLSecurityException xse) {
                                        throw new XMLEncryptionException("empty", xse);
                                }
                        }
                        return result;
        }
        CipherValue newCipherValue(Element element) {
            String value = XMLUtils.getFullTextChildrenFromElement(element);
            CipherValue result = newCipherValue(value);
            return (result);
        }
        EncryptedData newEncryptedData(Element element) throws
                        XMLEncryptionException {
            EncryptedData result = null;
            NodeList dataElements = element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_CIPHERDATA);
            Element dataElement =
                (Element) dataElements.item(dataElements.getLength() - 1);
            CipherData data = newCipherData(dataElement);
            result = newEncryptedData(data);
            result.setId(element.getAttributeNS(
                null, EncryptionConstants._ATT_ID));
            result.setType(
                element.getAttributeNS(null, EncryptionConstants._ATT_TYPE));
            result.setMimeType(element.getAttributeNS(
                null, EncryptionConstants._ATT_MIMETYPE));
            result.setEncoding(
                element.getAttributeNS(null, Constants._ATT_ENCODING));
            Element encryptionMethodElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONMETHOD).item(0);
            if (null != encryptionMethodElement) {
                result.setEncryptionMethod(newEncryptionMethod(
                    encryptionMethodElement));
            }
            Element keyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    Constants.SignatureSpecNS, Constants._TAG_KEYINFO).item(0);
            if (null != keyInfoElement) {
                try {
                    result.setKeyInfo(new KeyInfo(keyInfoElement, null));
                } catch (XMLSecurityException xse) {
                    throw new XMLEncryptionException("Error loading Key Info",
                                                     xse);
                }
            }
            Element encryptionPropertiesElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTIES).item(0);
            if (null != encryptionPropertiesElement) {
                result.setEncryptionProperties(
                    newEncryptionProperties(encryptionPropertiesElement));
            }
            return (result);
        }
        EncryptedKey newEncryptedKey(Element element) throws
                XMLEncryptionException {
            EncryptedKey result = null;
            NodeList dataElements = element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_CIPHERDATA);
            Element dataElement =
                (Element) dataElements.item(dataElements.getLength() - 1);
            CipherData data = newCipherData(dataElement);
            result = newEncryptedKey(data);
            result.setId(element.getAttributeNS(
                null, EncryptionConstants._ATT_ID));
            result.setType(
                element.getAttributeNS(null, EncryptionConstants._ATT_TYPE));
            result.setMimeType(element.getAttributeNS(
                null, EncryptionConstants._ATT_MIMETYPE));
            result.setEncoding(
                element.getAttributeNS(null, Constants._ATT_ENCODING));
            result.setRecipient(element.getAttributeNS(
                null, EncryptionConstants._ATT_RECIPIENT));
            Element encryptionMethodElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONMETHOD).item(0);
            if (null != encryptionMethodElement) {
                result.setEncryptionMethod(newEncryptionMethod(
                    encryptionMethodElement));
            }
            Element keyInfoElement =
                (Element) element.getElementsByTagNameNS(
                    Constants.SignatureSpecNS, Constants._TAG_KEYINFO).item(0);
            if (null != keyInfoElement) {
                try {
                    result.setKeyInfo(new KeyInfo(keyInfoElement, null));
                } catch (XMLSecurityException xse) {
                    throw new XMLEncryptionException
                        ("Error loading Key Info", xse);
                }
            }
            Element encryptionPropertiesElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTIES).item(0);
            if (null != encryptionPropertiesElement) {
                result.setEncryptionProperties(
                    newEncryptionProperties(encryptionPropertiesElement));
            }
            Element referenceListElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_REFERENCELIST).item(0);
            if (null != referenceListElement) {
                result.setReferenceList(newReferenceList(referenceListElement));
            }
            Element carriedNameElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CARRIEDKEYNAME).item(0);
            if (null != carriedNameElement) {
                result.setCarriedName
                    (carriedNameElement.getFirstChild().getNodeValue());
            }
            return (result);
        }
        EncryptionMethod newEncryptionMethod(Element element) {
            String algorithm = element.getAttributeNS(
                null, EncryptionConstants._ATT_ALGORITHM);
            EncryptionMethod result = newEncryptionMethod(algorithm);
            Element keySizeElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_KEYSIZE).item(0);
            if (null != keySizeElement) {
                result.setKeySize(
                    Integer.valueOf(
                        keySizeElement.getFirstChild().getNodeValue()).intValue());
            }
            Element oaepParamsElement =
                (Element) element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_OAEPPARAMS).item(0);
            if (null != oaepParamsElement) {
                result.setOAEPparams(
                    oaepParamsElement.getNodeValue().getBytes());
            }
            return (result);
        }
        EncryptionProperties newEncryptionProperties(Element element) {
            EncryptionProperties result = newEncryptionProperties();
            result.setId(element.getAttributeNS(
                null, EncryptionConstants._ATT_ID));
            NodeList encryptionPropertyList =
                element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTY);
            for(int i = 0; i < encryptionPropertyList.getLength(); i++) {
                Node n = encryptionPropertyList.item(i);
                if (null != n) {
                    result.addEncryptionProperty(
                        newEncryptionProperty((Element) n));
                }
            }
            return (result);
        }
        EncryptionProperty newEncryptionProperty(Element element) {
            EncryptionProperty result = newEncryptionProperty();
            result.setTarget(
                element.getAttributeNS(null, EncryptionConstants._ATT_TARGET));
            result.setId(element.getAttributeNS(
                null, EncryptionConstants._ATT_ID));
            return (result);
        }
        ReferenceList newReferenceList(Element element) {
            int type = 0;
            if (null != element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_DATAREFERENCE).item(0)) {
                type = ReferenceList.DATA_REFERENCE;
            } else if (null != element.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS,
                EncryptionConstants._TAG_KEYREFERENCE).item(0)) {
                type = ReferenceList.KEY_REFERENCE;
            } else {
            }
            ReferenceList result = new ReferenceListImpl(type);
            NodeList list = null;
            switch (type) {
            case ReferenceList.DATA_REFERENCE:
                list = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_DATAREFERENCE);
                for (int i = 0; i < list.getLength() ; i++) {
                    String uri = ((Element) list.item(i)).getAttribute("URI");
                    result.add(result.newDataReference(uri));
                }
                break;
            case ReferenceList.KEY_REFERENCE:
                list = element.getElementsByTagNameNS(
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_KEYREFERENCE);
                for (int i = 0; i < list.getLength() ; i++) {
                    String uri = ((Element) list.item(i)).getAttribute("URI");
                    result.add(result.newKeyReference(uri));
                }
            }
            return (result);
        }
        Transforms newTransforms(Element element) {
            return (null);
        }
        Element toElement(AgreementMethod agreementMethod) {
            return ((AgreementMethodImpl) agreementMethod).toElement();
        }
        Element toElement(CipherData cipherData) {
            return ((CipherDataImpl) cipherData).toElement();
        }
        Element toElement(CipherReference cipherReference) {
            return ((CipherReferenceImpl) cipherReference).toElement();
        }
        Element toElement(CipherValue cipherValue) {
            return ((CipherValueImpl) cipherValue).toElement();
        }
        Element toElement(EncryptedData encryptedData) {
            return ((EncryptedDataImpl) encryptedData).toElement();
        }
        Element toElement(EncryptedKey encryptedKey) {
            return ((EncryptedKeyImpl) encryptedKey).toElement();
        }
        Element toElement(EncryptionMethod encryptionMethod) {
            return ((EncryptionMethodImpl) encryptionMethod).toElement();
        }
        Element toElement(EncryptionProperties encryptionProperties) {
            return ((EncryptionPropertiesImpl) encryptionProperties).toElement();
        }
        Element toElement(EncryptionProperty encryptionProperty) {
            return ((EncryptionPropertyImpl) encryptionProperty).toElement();
        }
        Element toElement(ReferenceList referenceList) {
            return ((ReferenceListImpl) referenceList).toElement();
        }
        Element toElement(Transforms transforms) {
            return ((TransformsImpl) transforms).toElement();
        }
        private class AgreementMethodImpl implements AgreementMethod {
            private byte[] kaNonce = null;
            private List agreementMethodInformation = null;
            private KeyInfo originatorKeyInfo = null;
            private KeyInfo recipientKeyInfo = null;
            private String algorithmURI = null;
            public AgreementMethodImpl(String algorithm) {
                agreementMethodInformation = new LinkedList();
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URI.MalformedURIException fmue) {
                }
                algorithmURI = tmpAlgorithm.toString();
            }
            public byte[] getKANonce() {
                return (kaNonce);
            }
            public void setKANonce(byte[] kanonce) {
                kaNonce = kanonce;
            }
            public Iterator getAgreementMethodInformation() {
                return (agreementMethodInformation.iterator());
            }
            public void addAgreementMethodInformation(Element info) {
                agreementMethodInformation.add(info);
            }
            public void revoveAgreementMethodInformation(Element info) {
                agreementMethodInformation.remove(info);
            }
            public KeyInfo getOriginatorKeyInfo() {
                return (originatorKeyInfo);
            }
            public void setOriginatorKeyInfo(KeyInfo keyInfo) {
                originatorKeyInfo = keyInfo;
            }
            public KeyInfo getRecipientKeyInfo() {
                return (recipientKeyInfo);
            }
            public void setRecipientKeyInfo(KeyInfo keyInfo) {
                recipientKeyInfo = keyInfo;
            }
            public String getAlgorithm() {
                return (algorithmURI);
            }
            public void setAlgorithm(String algorithm) {
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URI.MalformedURIException mfue) {
                }
                algorithmURI = tmpAlgorithm.toString();
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument,
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_AGREEMENTMETHOD);
                result.setAttributeNS(
                    null, EncryptionConstants._ATT_ALGORITHM, algorithmURI);
                if (null != kaNonce) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(
                            _contextDocument,
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_KA_NONCE)).appendChild(
                            _contextDocument.createTextNode(new String(kaNonce)));
                }
                if (!agreementMethodInformation.isEmpty()) {
                    Iterator itr = agreementMethodInformation.iterator();
                    while (itr.hasNext()) {
                        result.appendChild((Element) itr.next());
                    }
                }
                if (null != originatorKeyInfo) {
                    result.appendChild(originatorKeyInfo.getElement());
                }
                if (null != recipientKeyInfo) {
                    result.appendChild(recipientKeyInfo.getElement());
                }
                return (result);
            }
        }
        private class CipherDataImpl implements CipherData {
            private static final String valueMessage =
                "Data type is reference type.";
            private static final String referenceMessage =
                "Data type is value type.";
            private CipherValue cipherValue = null;
            private CipherReference cipherReference = null;
            private int cipherType = Integer.MIN_VALUE;
            public CipherDataImpl(int type) {
                cipherType = type;
            }
            public CipherValue getCipherValue() {
                return (cipherValue);
            }
            public void setCipherValue(CipherValue value) throws
                    XMLEncryptionException {
                if (cipherType == REFERENCE_TYPE) {
                    throw new XMLEncryptionException("empty",
                        new UnsupportedOperationException(valueMessage));
                }
                cipherValue = value;
            }
            public CipherReference getCipherReference() {
                return (cipherReference);
            }
            public void setCipherReference(CipherReference reference) throws
                    XMLEncryptionException {
                if (cipherType == VALUE_TYPE) {
                    throw new XMLEncryptionException("empty",
                        new UnsupportedOperationException(referenceMessage));
                }
                cipherReference = reference;
            }
            public int getDataType() {
                return (cipherType);
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument,
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERDATA);
                if (cipherType == VALUE_TYPE) {
                    result.appendChild(
                        ((CipherValueImpl) cipherValue).toElement());
                } else if (cipherType == REFERENCE_TYPE) {
                    result.appendChild(
                        ((CipherReferenceImpl) cipherReference).toElement());
                } else {
                }
                return (result);
            }
        }
        private class CipherReferenceImpl implements CipherReference {
            private String referenceURI = null;
            private Transforms referenceTransforms = null;
                        private Attr referenceNode = null;
            public CipherReferenceImpl(String uri) {
                referenceURI = uri;
                                referenceNode = null;
            }
                        public CipherReferenceImpl(Attr uri) {
                                referenceURI = uri.getNodeValue();
                                referenceNode = uri;
                        }
            public String getURI() {
                return (referenceURI);
            }
                        public Attr getURIAsAttr() {
                                return (referenceNode);
                        }
            public Transforms getTransforms() {
                return (referenceTransforms);
            }
            public void setTransforms(Transforms transforms) {
                referenceTransforms = transforms;
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument,
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERREFERENCE);
                result.setAttributeNS(
                    null, EncryptionConstants._ATT_URI, referenceURI);
                if (null != referenceTransforms) {
                    result.appendChild(
                        ((TransformsImpl) referenceTransforms).toElement());
                }
                return (result);
            }
        }
        private class CipherValueImpl implements CipherValue {
                        private String cipherValue = null;
            public CipherValueImpl(String value) {
                                cipherValue = value;
            }
                        public String getValue() {
                return (cipherValue);
            }
            public void setValue(String value) {
                                cipherValue = value;
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_CIPHERVALUE);
                result.appendChild(_contextDocument.createTextNode(
                    cipherValue));
                return (result);
            }
        }
        private class EncryptedDataImpl extends EncryptedTypeImpl implements
                EncryptedData {
            public EncryptedDataImpl(CipherData data) {
                super(data);
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTEDDATA);
                if (null != super.getId()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_ID, super.getId());
                }
                if (null != super.getType()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_TYPE, super.getType());
                }
                if (null != super.getMimeType()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_MIMETYPE,
                        super.getMimeType());
                }
                if (null != super.getEncoding()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_ENCODING,
                        super.getEncoding());
                }
                if (null != super.getEncryptionMethod()) {
                    result.appendChild(((EncryptionMethodImpl)
                        super.getEncryptionMethod()).toElement());
                }
                if (null != super.getKeyInfo()) {
                    result.appendChild(super.getKeyInfo().getElement());
                }
                result.appendChild(
                    ((CipherDataImpl) super.getCipherData()).toElement());
                if (null != super.getEncryptionProperties()) {
                    result.appendChild(((EncryptionPropertiesImpl)
                        super.getEncryptionProperties()).toElement());
                }
                return (result);
            }
        }
        private class EncryptedKeyImpl extends EncryptedTypeImpl implements
                EncryptedKey {
            private String keyRecipient = null;
            private ReferenceList referenceList = null;
            private String carriedName = null;
            public EncryptedKeyImpl(CipherData data) {
                super(data);
            }
            public String getRecipient() {
                return (keyRecipient);
            }
            public void setRecipient(String recipient) {
                keyRecipient = recipient;
            }
            public ReferenceList getReferenceList() {
                return (referenceList);
            }
            public void setReferenceList(ReferenceList list) {
                referenceList = list;
            }
            public String getCarriedName() {
                return (carriedName);
            }
            public void setCarriedName(String name) {
                carriedName = name;
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTEDKEY);
                if (null != super.getId()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_ID, super.getId());
                }
                if (null != super.getType()) {
                    result.setAttributeNS(
                        null, EncryptionConstants._ATT_TYPE, super.getType());
                }
                if (null != super.getMimeType()) {
                    result.setAttributeNS(null,
                        EncryptionConstants._ATT_MIMETYPE, super.getMimeType());
                }
                if (null != super.getEncoding()) {
                    result.setAttributeNS(null, Constants._ATT_ENCODING,
                        super.getEncoding());
                }
                if (null != getRecipient()) {
                    result.setAttributeNS(null,
                        EncryptionConstants._ATT_RECIPIENT, getRecipient());
                }
                if (null != super.getEncryptionMethod()) {
                    result.appendChild(((EncryptionMethodImpl)
                        super.getEncryptionMethod()).toElement());
                }
                if (null != super.getKeyInfo()) {
                    result.appendChild(super.getKeyInfo().getElement());
                }
                result.appendChild(
                    ((CipherDataImpl) super.getCipherData()).toElement());
                if (null != super.getEncryptionProperties()) {
                    result.appendChild(((EncryptionPropertiesImpl)
                        super.getEncryptionProperties()).toElement());
                }
                if (referenceList != null && !referenceList.isEmpty()) {
                    result.appendChild(((ReferenceListImpl)
                        getReferenceList()).toElement());
                }
                if (null != carriedName) {
                    Element element = ElementProxy.createElementForFamily(
                        _contextDocument,
                        EncryptionConstants.EncryptionSpecNS,
                        EncryptionConstants._TAG_CARRIEDKEYNAME);
                    Node node = _contextDocument.createTextNode(carriedName);
                    element.appendChild(node);
                    result.appendChild(element);
                }
                return (result);
            }
        }
        private abstract class EncryptedTypeImpl {
            private String id =  null;
            private String type = null;
            private String mimeType = null;
            private String encoding = null;
            private EncryptionMethod encryptionMethod = null;
            private KeyInfo keyInfo = null;
            private CipherData cipherData = null;
            private EncryptionProperties encryptionProperties = null;
            protected EncryptedTypeImpl(CipherData data) {
                cipherData = data;
            }
            public String getId() {
                return (id);
            }
            public void setId(String id) {
                this.id = id;
            }
            public String getType() {
                return (type);
            }
            public void setType(String type) {
                if (type == null || type.length() == 0) {
                    this.type = null;
                } else {
                    URI tmpType = null;
                    try {
                        tmpType = new URI(type);
                    } catch (URI.MalformedURIException mfue) {
                    }
                    this.type = tmpType.toString();
                }
            }
            public String getMimeType() {
                return (mimeType);
            }
            public void setMimeType(String type) {
                mimeType = type;
            }
            public String getEncoding() {
                return (encoding);
            }
            public void setEncoding(String encoding) {
                if (encoding == null || encoding.length() == 0) {
                    this.encoding = null;
                } else {
                    URI tmpEncoding = null;
                    try {
                        tmpEncoding = new URI(encoding);
                    } catch (URI.MalformedURIException mfue) {
                    }
                    this.encoding = tmpEncoding.toString();
                }
            }
            public EncryptionMethod getEncryptionMethod() {
                return (encryptionMethod);
            }
            public void setEncryptionMethod(EncryptionMethod method) {
                encryptionMethod = method;
            }
            public KeyInfo getKeyInfo() {
                return (keyInfo);
            }
            public void setKeyInfo(KeyInfo info) {
                keyInfo = info;
            }
            public CipherData getCipherData() {
                return (cipherData);
            }
            public EncryptionProperties getEncryptionProperties() {
                return (encryptionProperties);
            }
            public void setEncryptionProperties(
                    EncryptionProperties properties) {
                encryptionProperties = properties;
            }
        }
        private class EncryptionMethodImpl implements EncryptionMethod {
            private String algorithm = null;
            private int keySize = Integer.MIN_VALUE;
            private byte[] oaepParams = null;
            private List encryptionMethodInformation = null;
            public EncryptionMethodImpl(String algorithm) {
                URI tmpAlgorithm = null;
                try {
                    tmpAlgorithm = new URI(algorithm);
                } catch (URI.MalformedURIException mfue) {
                }
                this.algorithm = tmpAlgorithm.toString();
                encryptionMethodInformation = new LinkedList();
            }
            public String getAlgorithm() {
                return (algorithm);
            }
            public int getKeySize() {
                return (keySize);
            }
            public void setKeySize(int size) {
                keySize = size;
            }
            public byte[] getOAEPparams() {
                return (oaepParams);
            }
            public void setOAEPparams(byte[] params) {
                oaepParams = params;
            }
            public Iterator getEncryptionMethodInformation() {
                return (encryptionMethodInformation.iterator());
            }
            public void addEncryptionMethodInformation(Element info) {
                encryptionMethodInformation.add(info);
            }
            public void removeEncryptionMethodInformation(Element info) {
                encryptionMethodInformation.remove(info);
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONMETHOD);
                result.setAttributeNS(null, EncryptionConstants._ATT_ALGORITHM,
                    algorithm);
                if (keySize > 0) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(_contextDocument,
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_KEYSIZE).appendChild(
                            _contextDocument.createTextNode(
                                String.valueOf(keySize))));
                }
                if (null != oaepParams) {
                    result.appendChild(
                        ElementProxy.createElementForFamily(_contextDocument,
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_OAEPPARAMS).appendChild(
                            _contextDocument.createTextNode(
                                new String(oaepParams))));
                }
                if (!encryptionMethodInformation.isEmpty()) {
                    Iterator itr = encryptionMethodInformation.iterator();
                    result.appendChild((Element) itr.next());
                }
                return (result);
            }
        }
        private class EncryptionPropertiesImpl implements EncryptionProperties {
            private String id = null;
            private List encryptionProperties = null;
            public EncryptionPropertiesImpl() {
                encryptionProperties = new LinkedList();
            }
            public String getId() {
                return (id);
            }
            public void setId(String id) {
                this.id = id;
            }
            public Iterator getEncryptionProperties() {
                return (encryptionProperties.iterator());
            }
            public void addEncryptionProperty(EncryptionProperty property) {
                encryptionProperties.add(property);
            }
            public void removeEncryptionProperty(EncryptionProperty property) {
                encryptionProperties.remove(property);
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTIES);
                if (null != id) {
                    result.setAttributeNS(null, EncryptionConstants._ATT_ID, id);
                }
                Iterator itr = getEncryptionProperties();
                while (itr.hasNext()) {
                    result.appendChild(((EncryptionPropertyImpl)
                        itr.next()).toElement());
                }
                return (result);
            }
        }
        private class EncryptionPropertyImpl implements EncryptionProperty {
            private String target = null;
            private String id = null;
            private HashMap attributeMap = new HashMap();
            private List encryptionInformation = null;
            public EncryptionPropertyImpl() {
                encryptionInformation = new LinkedList();
            }
            public String getTarget() {
                return (target);
            }
            public void setTarget(String target) {
                if (target == null || target.length() == 0) {
                    this.target = null;
                } else if (target.startsWith("#")) {
                    this.target = target;
                } else {
                    URI tmpTarget = null;
                    try {
                        tmpTarget = new URI(target);
                    } catch (URI.MalformedURIException mfue) {
                    }
                    this.target = tmpTarget.toString();
                }
            }
            public String getId() {
                return (id);
            }
            public void setId(String id) {
                this.id = id;
            }
            public String getAttribute(String attribute) {
                return (String) attributeMap.get(attribute);
            }
            public void setAttribute(String attribute, String value) {
                attributeMap.put(attribute, value);
            }
            public Iterator getEncryptionInformation() {
                return (encryptionInformation.iterator());
            }
            public void addEncryptionInformation(Element info) {
                encryptionInformation.add(info);
            }
            public void removeEncryptionInformation(Element info) {
                encryptionInformation.remove(info);
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument, EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_ENCRYPTIONPROPERTY);
                if (null != target) {
                    result.setAttributeNS(null, EncryptionConstants._ATT_TARGET,
                        target);
                }
                if (null != id) {
                    result.setAttributeNS(null, EncryptionConstants._ATT_ID,
                        id);
                }
                return (result);
            }
        }
        private class TransformsImpl extends
                       com.sun.org.apache.xml.internal.security.transforms.Transforms
                       implements Transforms {
                        public TransformsImpl() {
                                super(_contextDocument);
                        }
                        public TransformsImpl(Document doc) {
                                if (doc == null) {
                                 throw new RuntimeException("Document is null");
                              }
                              this._doc = doc;
                              this._constructionElement =  createElementForFamilyLocal(this._doc,
                                          this.getBaseNamespace(), this.getBaseLocalName());
                        }
                        public TransformsImpl(Element element)
                                throws XMLSignatureException,
                                   InvalidTransformException,
                                       XMLSecurityException,
                                       TransformationException {
                                super(element, "");
                        }
                        public Element toElement() {
                                if (_doc == null)
                                        _doc = _contextDocument;
                                return getElement();
                        }
                        public com.sun.org.apache.xml.internal.security.transforms.Transforms getDSTransforms() {
                                return (this);
                        }
                        public String getBaseNamespace() {
                                return EncryptionConstants.EncryptionSpecNS;
                        }
        }
        private class ReferenceListImpl implements ReferenceList {
            private Class sentry;
            private List references;
            public ReferenceListImpl(int type) {
                if (type == ReferenceList.DATA_REFERENCE) {
                    sentry = DataReference.class;
                } else if (type == ReferenceList.KEY_REFERENCE) {
                    sentry = KeyReference.class;
                } else {
                    throw new IllegalArgumentException();
                }
                references = new LinkedList();
            }
            public void add(Reference reference) {
                if (!reference.getClass().equals(sentry)) {
                    throw new IllegalArgumentException();
                }
                 references.add(reference);
            }
            public void remove(Reference reference) {
                if (!reference.getClass().equals(sentry)) {
                    throw new IllegalArgumentException();
                }
                references.remove(reference);
            }
            public int size() {
                return (references.size());
            }
            public boolean isEmpty() {
                return (references.isEmpty());
            }
            public Iterator getReferences() {
                return (references.iterator());
            }
            Element toElement() {
                Element result = ElementProxy.createElementForFamily(
                    _contextDocument,
                    EncryptionConstants.EncryptionSpecNS,
                    EncryptionConstants._TAG_REFERENCELIST);
                Iterator eachReference = references.iterator();
                while (eachReference.hasNext()) {
                    Reference reference = (Reference) eachReference.next();
                    result.appendChild(
                        ((ReferenceImpl) reference).toElement());
                }
                return (result);
            }
            public Reference newDataReference(String uri) {
                return (new DataReference(uri));
            }
            public Reference newKeyReference(String uri) {
                return (new KeyReference(uri));
            }
            private abstract class ReferenceImpl implements Reference {
                private String uri;
                private List referenceInformation;
                ReferenceImpl(String _uri) {
                    this.uri = _uri;
                    referenceInformation = new LinkedList();
                }
                public String getURI() {
                    return (uri);
                }
                public Iterator getElementRetrievalInformation() {
                    return (referenceInformation.iterator());
                }
                public void setURI(String _uri) {
                        this.uri = _uri;
                }
                public void removeElementRetrievalInformation(Element node) {
                    referenceInformation.remove(node);
                }
                public void addElementRetrievalInformation(Element node) {
                    referenceInformation.add(node);
                }
                public abstract Element toElement();
                Element toElement(String tagName) {
                    Element result = ElementProxy.createElementForFamily(
                        _contextDocument,
                        EncryptionConstants.EncryptionSpecNS,
                        tagName);
                    result.setAttribute(EncryptionConstants._ATT_URI, uri);
                    return (result);
                }
            }
            private class DataReference extends ReferenceImpl {
                DataReference(String uri) {
                    super(uri);
                }
                public Element toElement() {
                    return super.toElement(EncryptionConstants._TAG_DATAREFERENCE);
                }
            }
            private class KeyReference extends ReferenceImpl {
                KeyReference(String uri) {
                    super (uri);
                }
                public Element toElement() {
                    return super.toElement(EncryptionConstants._TAG_KEYREFERENCE);
                }
            }
        }
    }
}
