class KeySelectors {
    static class SecretKeySelector extends KeySelector {
        private SecretKey key;
        SecretKeySelector(byte[] bytes) {
            key = wrapBytes(bytes);
        }
        SecretKeySelector(SecretKey key) {
            this.key = key;
        }
        public KeySelectorResult select(KeyInfo ki,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
            throws KeySelectorException {
            return new SimpleKSResult(key);
        }
        private SecretKey wrapBytes(final byte[] bytes) {
            return new SecretKey() {
                public String getFormat() {
                    return "RAW";
                }
                public String getAlgorithm() {
                    return "Secret key";
                }
                public byte[] getEncoded() {
                    return (byte[]) bytes.clone();
                }
            };
        }
    }
    static class RawX509KeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
            throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }
            Iterator iter = keyInfo.getContent().iterator();
            while (iter.hasNext()) {
                XMLStructure kiType = (XMLStructure) iter.next();
                if (kiType instanceof X509Data) {
                    X509Data xd = (X509Data) kiType;
                    Object[] entries = xd.getContent().toArray();
                    X509CRL crl = null;
                    for (int i = 0; (i<entries.length&&crl != null); i++) {
                        if (entries[i] instanceof X509CRL) {
                            crl = (X509CRL) entries[i];
                        }
                    }
                    Iterator xi = xd.getContent().iterator();
                    boolean hasCRL = false;
                    while (xi.hasNext()) {
                        Object o = xi.next();
                        if (o instanceof X509Certificate) {
                            if ((purpose != KeySelector.Purpose.VERIFY) &&
                                (crl != null) &&
                                crl.isRevoked((X509Certificate)o)) {
                                continue;
                            } else {
                                return new SimpleKSResult
                                    (((X509Certificate)o).getPublicKey());
                            }
                        }
                    }
                }
            }
            throw new KeySelectorException("No X509Certificate found!");
        }
    }
    static class KeyValueKeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
            throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }
            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent();
            for (int i = 0; i < list.size(); i++) {
                XMLStructure xmlStructure = (XMLStructure) list.get(i);
                if (xmlStructure instanceof KeyValue) {
                    PublicKey pk = null;
                    try {
                        pk = ((KeyValue)xmlStructure).getPublicKey();
                    } catch (KeyException ke) {
                        throw new KeySelectorException(ke);
                    }
                    if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        return new SimpleKSResult(pk);
                    }
                }
            }
            throw new KeySelectorException("No KeyValue element found!");
        }
        static boolean algEquals(String algURI, String algName) {
            if (algName.equalsIgnoreCase("DSA") &&
                algURI.equals(SignatureMethod.DSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("RSA") &&
                (algURI.equals(SignatureMethod.RSA_SHA1) ||
                 algURI.equals
                    ("http:
                 algURI.equals
                    ("http:
                 algURI.equals
                    ("http:
                return true;
            } else {
                return false;
            }
        }
    }
    static class CollectionKeySelector extends KeySelector {
        private CertificateFactory certFac;
        private File certDir;
        private Vector certs;
        private static final int MATCH_SUBJECT = 0;
        private static final int MATCH_ISSUER = 1;
        private static final int MATCH_SERIAL = 2;
        private static final int MATCH_SUBJECT_KEY_ID = 3;
        private static final int MATCH_CERTIFICATE = 4;
        CollectionKeySelector(File dir) {
            certDir = dir;
            try {
                certFac = CertificateFactory.getInstance("X509");
            } catch (CertificateException ex) {
            }
            certs = new Vector();
            File[] files = new File(certDir, "certs").listFiles();
            for (int i = 0; i < files.length; i++) {
                try {
                    certs.add(certFac.generateCertificate
                              (new FileInputStream(files[i])));
                } catch (Exception ex) { }
            }
        }
        Vector match(int matchType, Object value, Vector pool) {
            Vector matchResult = new Vector();
            for (int j=0; j < pool.size(); j++) {
                X509Certificate c = (X509Certificate) pool.get(j);
                switch (matchType) {
                case MATCH_SUBJECT:
                    try {
                        if (c.getSubjectDN().equals(new X500Name((String)value))) {
                            matchResult.add(c);
                        }
                    } catch (IOException ioe) { }
                    break;
                case MATCH_ISSUER:
                    try {
                        if (c.getIssuerDN().equals(new X500Name((String)value))) {
                            matchResult.add(c);
                        }
                    } catch (IOException ioe) { }
                    break;
                case MATCH_SERIAL:
                    if (c.getSerialNumber().equals(value)) {
                        matchResult.add(c);
                    }
                    break;
                case MATCH_SUBJECT_KEY_ID:
                    byte[] extension = c.getExtensionValue("2.5.29.14");
                    if (extension != null) {
                        try {
                            DerValue derValue = new DerValue(extension);
                            DerValue derValue2 = new DerValue(derValue.getOctetString());
                            byte[] extVal = derValue2.getOctetString();
                            if (Arrays.equals(extVal, (byte[]) value)) {
                                matchResult.add(c);
                            }
                        } catch (IOException ex) { }
                    }
                    break;
                case MATCH_CERTIFICATE:
                    if (c.equals(value)) {
                        matchResult.add(c);
                    }
                    break;
                }
            }
            return matchResult;
        }
        public KeySelectorResult select(KeyInfo keyInfo,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
            throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }
            Iterator iter = keyInfo.getContent().iterator();
            while (iter.hasNext()) {
                XMLStructure xmlStructure = (XMLStructure) iter.next();
                try {
                    if (xmlStructure instanceof KeyName) {
                        String name = ((KeyName)xmlStructure).getName();
                        PublicKey pk = null;
                        try {
                            File certFile = new File(new File(certDir, "certs"),
                                name.toLowerCase()+".crt");
                            X509Certificate cert = (X509Certificate)
                                certFac.generateCertificate
                                (new FileInputStream(certFile));
                            pk = cert.getPublicKey();
                        } catch (FileNotFoundException e) {
                            Vector result =
                                match(MATCH_SUBJECT, name, certs);
                            int numOfMatches = (result==null? 0:result.size());
                            if (numOfMatches != 1) {
                                throw new KeySelectorException
                                    ((numOfMatches==0?"No":"More than one") +
                                     " match found");
                            }
                            pk =((X509Certificate)result.get(0)).getPublicKey();
                        }
                        return new SimpleKSResult(pk);
                    } else if (xmlStructure instanceof RetrievalMethod) {
                        RetrievalMethod rm = (RetrievalMethod) xmlStructure;
                        String type = rm.getType();
                        if (type.equals(X509Data.RAW_X509_CERTIFICATE_TYPE)) {
                            String uri = rm.getURI();
                            X509Certificate cert = (X509Certificate)
                                certFac.generateCertificate
                                (new FileInputStream(new File(certDir, uri)));
                            return new SimpleKSResult(cert.getPublicKey());
                        } else {
                            throw new KeySelectorException
                                ("Unsupported RetrievalMethod type");
                        }
                    } else if (xmlStructure instanceof X509Data) {
                        List content = ((X509Data)xmlStructure).getContent();
                        int size = content.size();
                        Vector result = null;
                        for (int k = 0; k<size; k++) {
                            Object obj = content.get(k);
                            if (obj instanceof String) {
                                result = match(MATCH_SUBJECT, obj, certs);
                            } else if (obj instanceof byte[]) {
                                result = match(MATCH_SUBJECT_KEY_ID, obj,
                                               certs);
                            } else if (obj instanceof X509Certificate) {
                                result = match(MATCH_CERTIFICATE, obj, certs);
                            } else if (obj instanceof X509IssuerSerial) {
                                X509IssuerSerial is = (X509IssuerSerial) obj;
                                result = match(MATCH_SERIAL,
                                               is.getSerialNumber(), certs);
                                result = match(MATCH_ISSUER,
                                               is.getIssuerName(), result);
                            } else {
                                throw new KeySelectorException("Unsupported X509Data: " + obj);
                            }
                        }
                        int numOfMatches = (result==null? 0:result.size());
                        if (numOfMatches != 1) {
                            throw new KeySelectorException
                                ((numOfMatches==0?"No":"More than one") +
                                 " match found");
                        }
                        return new SimpleKSResult(((X509Certificate)
                                          result.get(0)).getPublicKey());
                    }
                } catch (Exception ex) {
                    throw new KeySelectorException(ex);
                }
            }
            throw new KeySelectorException("No matching key found!");
        }
    }
    static class ByteUtil {
        private static String mapping = "0123456789ABCDEF";
        private static int numBytesPerRow = 6;
        private static String getHex(byte value) {
            int low = value & 0x0f;
            int high = ((value >> 4) & 0x0f);
            char[] res = new char[2];
            res[0] = mapping.charAt(high);
            res[1] = mapping.charAt(low);
            return new String(res);
        }
        static String dumpArray(byte[] in) {
            int numDumped = 0;
            StringBuffer buf = new StringBuffer(512);
            buf.append("{");
            for (int i=0;i<(in.length/numBytesPerRow); i++) {
                for (int j=0; j<(numBytesPerRow); j++) {
                    buf.append("(byte)0x" + getHex(in[i*numBytesPerRow+j]) +
                               ", ");
                }
                numDumped += numBytesPerRow;
            }
            while (numDumped < in.length) {
                buf.append("(byte)0x" + getHex(in[numDumped]) + " ");
                numDumped += 1;
            }
            buf.append("}");
            return buf.toString();
        }
    }
}
class SimpleKSResult implements KeySelectorResult {
    private final Key key;
    SimpleKSResult(Key key) { this.key = key; }
    public Key getKey() { return key; }
}
