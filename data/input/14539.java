public class AlgorithmId implements Serializable, DerEncoder {
    private static final long serialVersionUID = 7205873507486557157L;
    private ObjectIdentifier algid;
    private AlgorithmParameters algParams;
    private boolean constructedFromDer = true;
    protected DerValue          params;
    @Deprecated
    public AlgorithmId() { }
    public AlgorithmId(ObjectIdentifier oid) {
        algid = oid;
    }
    public AlgorithmId(ObjectIdentifier oid, AlgorithmParameters algparams) {
        algid = oid;
        algParams = algparams;
        constructedFromDer = false;
    }
    private AlgorithmId(ObjectIdentifier oid, DerValue params)
            throws IOException {
        this.algid = oid;
        this.params = params;
        if (this.params != null) {
            decodeParams();
        }
    }
    protected void decodeParams() throws IOException {
        String algidString = algid.toString();
        try {
            algParams = AlgorithmParameters.getInstance(algidString);
        } catch (NoSuchAlgorithmException e) {
            try {
                algParams = AlgorithmParameters.getInstance(algidString,
                                sun.security.ec.ECKeyFactory.ecInternalProvider);
            } catch (NoSuchAlgorithmException ee) {
                algParams = null;
                return;
            }
        }
        algParams.init(params.toByteArray());
    }
    public final void encode(DerOutputStream out) throws IOException {
        derEncode(out);
    }
    public void derEncode (OutputStream out) throws IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        bytes.putOID(algid);
        if (constructedFromDer == false) {
            if (algParams != null) {
                params = new DerValue(algParams.getEncoded());
            } else {
                params = null;
            }
        }
        if (params == null) {
            bytes.putNull();
        } else {
            bytes.putDerValue(params);
        }
        tmp.write(DerValue.tag_Sequence, bytes);
        out.write(tmp.toByteArray());
    }
    public final byte[] encode() throws IOException {
        DerOutputStream out = new DerOutputStream();
        derEncode(out);
        return out.toByteArray();
    }
    public final ObjectIdentifier getOID () {
        return algid;
    }
    public String getName() {
        String algName = nameTable.get(algid);
        if (algName != null) {
            return algName;
        }
        if ((params != null) && algid.equals(specifiedWithECDSA_oid)) {
            try {
                AlgorithmId paramsId =
                        AlgorithmId.parse(new DerValue(getEncodedParams()));
                String paramsName = paramsId.getName();
                if (paramsName.equals("SHA")) {
                    paramsName = "SHA1";
                }
                algName = paramsName + "withECDSA";
            } catch (IOException e) {
            }
        }
        return (algName == null) ? algid.toString() : algName;
    }
    public AlgorithmParameters getParameters() {
        return algParams;
    }
    public byte[] getEncodedParams() throws IOException {
        return (params == null) ? null : params.toByteArray();
    }
    public boolean equals(AlgorithmId other) {
        boolean paramsEqual =
          (params == null ? other.params == null : params.equals(other.params));
        return (algid.equals(other.algid) && paramsEqual);
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof AlgorithmId) {
            return equals((AlgorithmId) other);
        } else if (other instanceof ObjectIdentifier) {
            return equals((ObjectIdentifier) other);
        } else {
            return false;
        }
    }
    public final boolean equals(ObjectIdentifier id) {
        return algid.equals(id);
    }
    public int hashCode() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(algid.toString());
        sbuf.append(paramsToString());
        return sbuf.toString().hashCode();
    }
    protected String paramsToString() {
        if (params == null) {
            return "";
        } else if (algParams != null) {
            return algParams.toString();
        } else {
            return ", params unparsed";
        }
    }
    public String toString() {
        return getName() + paramsToString();
    }
    public static AlgorithmId parse(DerValue val) throws IOException {
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("algid parse error, not a sequence");
        }
        ObjectIdentifier        algid;
        DerValue                params;
        DerInputStream          in = val.toDerInputStream();
        algid = in.getOID();
        if (in.available() == 0) {
            params = null;
        } else {
            params = in.getDerValue();
            if (params.tag == DerValue.tag_Null) {
                if (params.length() != 0) {
                    throw new IOException("invalid NULL");
                }
                params = null;
            }
            if (in.available() != 0) {
                throw new IOException("Invalid AlgorithmIdentifier: extra data");
            }
        }
        return new AlgorithmId(algid, params);
    }
    @Deprecated
    public static AlgorithmId getAlgorithmId(String algname)
            throws NoSuchAlgorithmException {
        return get(algname);
    }
    public static AlgorithmId get(String algname)
            throws NoSuchAlgorithmException {
        ObjectIdentifier oid;
        try {
            oid = algOID(algname);
        } catch (IOException ioe) {
            throw new NoSuchAlgorithmException
                ("Invalid ObjectIdentifier " + algname);
        }
        if (oid == null) {
            throw new NoSuchAlgorithmException
                ("unrecognized algorithm name: " + algname);
        }
        return new AlgorithmId(oid);
    }
    public static AlgorithmId get(AlgorithmParameters algparams)
            throws NoSuchAlgorithmException {
        ObjectIdentifier oid;
        String algname = algparams.getAlgorithm();
        try {
            oid = algOID(algname);
        } catch (IOException ioe) {
            throw new NoSuchAlgorithmException
                ("Invalid ObjectIdentifier " + algname);
        }
        if (oid == null) {
            throw new NoSuchAlgorithmException
                ("unrecognized algorithm name: " + algname);
        }
        return new AlgorithmId(oid, algparams);
    }
    private static ObjectIdentifier algOID(String name) throws IOException {
        if (name.indexOf('.') != -1) {
            if (name.startsWith("OID.")) {
                return new ObjectIdentifier(name.substring("OID.".length()));
            } else {
                return new ObjectIdentifier(name);
            }
        }
        if (name.equalsIgnoreCase("MD5")) {
            return AlgorithmId.MD5_oid;
        }
        if (name.equalsIgnoreCase("MD2")) {
            return AlgorithmId.MD2_oid;
        }
        if (name.equalsIgnoreCase("SHA") || name.equalsIgnoreCase("SHA1")
            || name.equalsIgnoreCase("SHA-1")) {
            return AlgorithmId.SHA_oid;
        }
        if (name.equalsIgnoreCase("SHA-256") ||
            name.equalsIgnoreCase("SHA256")) {
            return AlgorithmId.SHA256_oid;
        }
        if (name.equalsIgnoreCase("SHA-384") ||
            name.equalsIgnoreCase("SHA384")) {
            return AlgorithmId.SHA384_oid;
        }
        if (name.equalsIgnoreCase("SHA-512") ||
            name.equalsIgnoreCase("SHA512")) {
            return AlgorithmId.SHA512_oid;
        }
        if (name.equalsIgnoreCase("RSA")) {
            return AlgorithmId.RSAEncryption_oid;
        }
        if (name.equalsIgnoreCase("Diffie-Hellman")
            || name.equalsIgnoreCase("DH")) {
            return AlgorithmId.DH_oid;
        }
        if (name.equalsIgnoreCase("DSA")) {
            return AlgorithmId.DSA_oid;
        }
        if (name.equalsIgnoreCase("EC")) {
            return EC_oid;
        }
        if (name.equalsIgnoreCase("MD5withRSA")
            || name.equalsIgnoreCase("MD5/RSA")) {
            return AlgorithmId.md5WithRSAEncryption_oid;
        }
        if (name.equalsIgnoreCase("MD2withRSA")
            || name.equalsIgnoreCase("MD2/RSA")) {
            return AlgorithmId.md2WithRSAEncryption_oid;
        }
        if (name.equalsIgnoreCase("SHAwithDSA")
            || name.equalsIgnoreCase("SHA1withDSA")
            || name.equalsIgnoreCase("SHA/DSA")
            || name.equalsIgnoreCase("SHA1/DSA")
            || name.equalsIgnoreCase("DSAWithSHA1")
            || name.equalsIgnoreCase("DSS")
            || name.equalsIgnoreCase("SHA-1/DSA")) {
            return AlgorithmId.sha1WithDSA_oid;
        }
        if (name.equalsIgnoreCase("SHA1WithRSA")
            || name.equalsIgnoreCase("SHA1/RSA")) {
            return AlgorithmId.sha1WithRSAEncryption_oid;
        }
        if (name.equalsIgnoreCase("SHA1withECDSA")
                || name.equalsIgnoreCase("ECDSA")) {
            return AlgorithmId.sha1WithECDSA_oid;
        }
        if (name.equalsIgnoreCase("SHA224withECDSA")) {
            return AlgorithmId.sha224WithECDSA_oid;
        }
        if (name.equalsIgnoreCase("SHA256withECDSA")) {
            return AlgorithmId.sha256WithECDSA_oid;
        }
        if (name.equalsIgnoreCase("SHA384withECDSA")) {
            return AlgorithmId.sha384WithECDSA_oid;
        }
        if (name.equalsIgnoreCase("SHA512withECDSA")) {
            return AlgorithmId.sha512WithECDSA_oid;
        }
        String oidString;
        if (!initOidTable) {
            Provider[] provs = Security.getProviders();
            for (int i=0; i<provs.length; i++) {
                for (Enumeration<Object> enum_ = provs[i].keys();
                     enum_.hasMoreElements(); ) {
                    String alias = (String)enum_.nextElement();
                    String upperCaseAlias = alias.toUpperCase(Locale.ENGLISH);
                    int index;
                    if (upperCaseAlias.startsWith("ALG.ALIAS") &&
                            (index=upperCaseAlias.indexOf("OID.", 0)) != -1) {
                        index += "OID.".length();
                        if (index == alias.length()) {
                            break;
                        }
                        if (oidTable == null) {
                            oidTable = new HashMap<String,ObjectIdentifier>();
                        }
                        oidString = alias.substring(index);
                        String stdAlgName = provs[i].getProperty(alias);
                        if (stdAlgName != null) {
                            stdAlgName = stdAlgName.toUpperCase(Locale.ENGLISH);
                        }
                        if (stdAlgName != null &&
                                oidTable.get(stdAlgName) == null) {
                            oidTable.put(stdAlgName,
                                         new ObjectIdentifier(oidString));
                        }
                    }
                }
            }
            if (oidTable == null) {
                oidTable = new HashMap<String,ObjectIdentifier>(1);
            }
            initOidTable = true;
        }
        return oidTable.get(name.toUpperCase(Locale.ENGLISH));
    }
    private static ObjectIdentifier oid(int ... values) {
        return ObjectIdentifier.newInternal(values);
    }
    private static boolean initOidTable = false;
    private static Map<String,ObjectIdentifier> oidTable;
    private static final Map<ObjectIdentifier,String> nameTable;
    public static final ObjectIdentifier MD2_oid =
    ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 2, 2});
    public static final ObjectIdentifier MD5_oid =
    ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 2, 5});
    public static final ObjectIdentifier SHA_oid =
    ObjectIdentifier.newInternal(new int[] {1, 3, 14, 3, 2, 26});
    public static final ObjectIdentifier SHA256_oid =
    ObjectIdentifier.newInternal(new int[] {2, 16, 840, 1, 101, 3, 4, 2, 1});
    public static final ObjectIdentifier SHA384_oid =
    ObjectIdentifier.newInternal(new int[] {2, 16, 840, 1, 101, 3, 4, 2, 2});
    public static final ObjectIdentifier SHA512_oid =
    ObjectIdentifier.newInternal(new int[] {2, 16, 840, 1, 101, 3, 4, 2, 3});
    private static final int DH_data[] = { 1, 2, 840, 113549, 1, 3, 1 };
    private static final int DH_PKIX_data[] = { 1, 2, 840, 10046, 2, 1 };
    private static final int DSA_OIW_data[] = { 1, 3, 14, 3, 2, 12 };
    private static final int DSA_PKIX_data[] = { 1, 2, 840, 10040, 4, 1 };
    private static final int RSA_data[] = { 2, 5, 8, 1, 1 };
    private static final int RSAEncryption_data[] =
                                 { 1, 2, 840, 113549, 1, 1, 1 };
    public static final ObjectIdentifier DH_oid;
    public static final ObjectIdentifier DH_PKIX_oid;
    public static final ObjectIdentifier DSA_oid;
    public static final ObjectIdentifier DSA_OIW_oid;
    public static final ObjectIdentifier EC_oid = oid(1, 2, 840, 10045, 2, 1);
    public static final ObjectIdentifier RSA_oid;
    public static final ObjectIdentifier RSAEncryption_oid;
    private static final int md2WithRSAEncryption_data[] =
                                       { 1, 2, 840, 113549, 1, 1, 2 };
    private static final int md5WithRSAEncryption_data[] =
                                       { 1, 2, 840, 113549, 1, 1, 4 };
    private static final int sha1WithRSAEncryption_data[] =
                                       { 1, 2, 840, 113549, 1, 1, 5 };
    private static final int sha1WithRSAEncryption_OIW_data[] =
                                       { 1, 3, 14, 3, 2, 29 };
    private static final int sha256WithRSAEncryption_data[] =
                                       { 1, 2, 840, 113549, 1, 1, 11 };
    private static final int sha384WithRSAEncryption_data[] =
                                       { 1, 2, 840, 113549, 1, 1, 12 };
    private static final int sha512WithRSAEncryption_data[] =
                                       { 1, 2, 840, 113549, 1, 1, 13 };
    private static final int shaWithDSA_OIW_data[] =
                                       { 1, 3, 14, 3, 2, 13 };
    private static final int sha1WithDSA_OIW_data[] =
                                       { 1, 3, 14, 3, 2, 27 };
    private static final int dsaWithSHA1_PKIX_data[] =
                                       { 1, 2, 840, 10040, 4, 3 };
    public static final ObjectIdentifier md2WithRSAEncryption_oid;
    public static final ObjectIdentifier md5WithRSAEncryption_oid;
    public static final ObjectIdentifier sha1WithRSAEncryption_oid;
    public static final ObjectIdentifier sha1WithRSAEncryption_OIW_oid;
    public static final ObjectIdentifier sha256WithRSAEncryption_oid;
    public static final ObjectIdentifier sha384WithRSAEncryption_oid;
    public static final ObjectIdentifier sha512WithRSAEncryption_oid;
    public static final ObjectIdentifier shaWithDSA_OIW_oid;
    public static final ObjectIdentifier sha1WithDSA_OIW_oid;
    public static final ObjectIdentifier sha1WithDSA_oid;
    public static final ObjectIdentifier sha1WithECDSA_oid =
                                            oid(1, 2, 840, 10045, 4, 1);
    public static final ObjectIdentifier sha224WithECDSA_oid =
                                            oid(1, 2, 840, 10045, 4, 3, 1);
    public static final ObjectIdentifier sha256WithECDSA_oid =
                                            oid(1, 2, 840, 10045, 4, 3, 2);
    public static final ObjectIdentifier sha384WithECDSA_oid =
                                            oid(1, 2, 840, 10045, 4, 3, 3);
    public static final ObjectIdentifier sha512WithECDSA_oid =
                                            oid(1, 2, 840, 10045, 4, 3, 4);
    public static final ObjectIdentifier specifiedWithECDSA_oid =
                                            oid(1, 2, 840, 10045, 4, 3);
    public static final ObjectIdentifier pbeWithMD5AndDES_oid =
        ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 3});
    public static final ObjectIdentifier pbeWithMD5AndRC2_oid =
        ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 1, 5, 6});
    public static final ObjectIdentifier pbeWithSHA1AndDES_oid =
        ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 1, 5, 10});
    public static final ObjectIdentifier pbeWithSHA1AndRC2_oid =
        ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 1, 5, 11});
    public static ObjectIdentifier pbeWithSHA1AndDESede_oid =
        ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 1, 12, 1, 3});
    public static ObjectIdentifier pbeWithSHA1AndRC2_40_oid =
        ObjectIdentifier.newInternal(new int[] {1, 2, 840, 113549, 1, 12, 1, 6});
    static {
        DH_oid = ObjectIdentifier.newInternal(DH_data);
        DH_PKIX_oid = ObjectIdentifier.newInternal(DH_PKIX_data);
        DSA_OIW_oid = ObjectIdentifier.newInternal(DSA_OIW_data);
        DSA_oid = ObjectIdentifier.newInternal(DSA_PKIX_data);
        RSA_oid = ObjectIdentifier.newInternal(RSA_data);
        RSAEncryption_oid = ObjectIdentifier.newInternal(RSAEncryption_data);
        md2WithRSAEncryption_oid =
            ObjectIdentifier.newInternal(md2WithRSAEncryption_data);
        md5WithRSAEncryption_oid =
            ObjectIdentifier.newInternal(md5WithRSAEncryption_data);
        sha1WithRSAEncryption_oid =
            ObjectIdentifier.newInternal(sha1WithRSAEncryption_data);
        sha1WithRSAEncryption_OIW_oid =
            ObjectIdentifier.newInternal(sha1WithRSAEncryption_OIW_data);
        sha256WithRSAEncryption_oid =
            ObjectIdentifier.newInternal(sha256WithRSAEncryption_data);
        sha384WithRSAEncryption_oid =
            ObjectIdentifier.newInternal(sha384WithRSAEncryption_data);
        sha512WithRSAEncryption_oid =
            ObjectIdentifier.newInternal(sha512WithRSAEncryption_data);
        shaWithDSA_OIW_oid = ObjectIdentifier.newInternal(shaWithDSA_OIW_data);
        sha1WithDSA_OIW_oid = ObjectIdentifier.newInternal(sha1WithDSA_OIW_data);
        sha1WithDSA_oid = ObjectIdentifier.newInternal(dsaWithSHA1_PKIX_data);
        nameTable = new HashMap<ObjectIdentifier,String>();
        nameTable.put(MD5_oid, "MD5");
        nameTable.put(MD2_oid, "MD2");
        nameTable.put(SHA_oid, "SHA");
        nameTable.put(SHA256_oid, "SHA256");
        nameTable.put(SHA384_oid, "SHA384");
        nameTable.put(SHA512_oid, "SHA512");
        nameTable.put(RSAEncryption_oid, "RSA");
        nameTable.put(RSA_oid, "RSA");
        nameTable.put(DH_oid, "Diffie-Hellman");
        nameTable.put(DH_PKIX_oid, "Diffie-Hellman");
        nameTable.put(DSA_oid, "DSA");
        nameTable.put(DSA_OIW_oid, "DSA");
        nameTable.put(EC_oid, "EC");
        nameTable.put(sha1WithECDSA_oid, "SHA1withECDSA");
        nameTable.put(sha224WithECDSA_oid, "SHA224withECDSA");
        nameTable.put(sha256WithECDSA_oid, "SHA256withECDSA");
        nameTable.put(sha384WithECDSA_oid, "SHA384withECDSA");
        nameTable.put(sha512WithECDSA_oid, "SHA512withECDSA");
        nameTable.put(md5WithRSAEncryption_oid, "MD5withRSA");
        nameTable.put(md2WithRSAEncryption_oid, "MD2withRSA");
        nameTable.put(sha1WithDSA_oid, "SHA1withDSA");
        nameTable.put(sha1WithDSA_OIW_oid, "SHA1withDSA");
        nameTable.put(shaWithDSA_OIW_oid, "SHA1withDSA");
        nameTable.put(sha1WithRSAEncryption_oid, "SHA1withRSA");
        nameTable.put(sha1WithRSAEncryption_OIW_oid, "SHA1withRSA");
        nameTable.put(sha256WithRSAEncryption_oid, "SHA256withRSA");
        nameTable.put(sha384WithRSAEncryption_oid, "SHA384withRSA");
        nameTable.put(sha512WithRSAEncryption_oid, "SHA512withRSA");
        nameTable.put(pbeWithMD5AndDES_oid, "PBEWithMD5AndDES");
        nameTable.put(pbeWithMD5AndRC2_oid, "PBEWithMD5AndRC2");
        nameTable.put(pbeWithSHA1AndDES_oid, "PBEWithSHA1AndDES");
        nameTable.put(pbeWithSHA1AndRC2_oid, "PBEWithSHA1AndRC2");
        nameTable.put(pbeWithSHA1AndDESede_oid, "PBEWithSHA1AndDESede");
        nameTable.put(pbeWithSHA1AndRC2_40_oid, "PBEWithSHA1AndRC2_40");
    }
    public static String makeSigAlg(String digAlg, String encAlg) {
        digAlg = digAlg.replace("-", "").toUpperCase(Locale.ENGLISH);
        if (digAlg.equalsIgnoreCase("SHA")) digAlg = "SHA1";
        encAlg = encAlg.toUpperCase(Locale.ENGLISH);
        if (encAlg.equals("EC")) encAlg = "ECDSA";
        return digAlg + "with" + encAlg;
    }
    public static String getEncAlgFromSigAlg(String signatureAlgorithm) {
        signatureAlgorithm = signatureAlgorithm.toUpperCase(Locale.ENGLISH);
        int with = signatureAlgorithm.indexOf("WITH");
        String keyAlgorithm = null;
        if (with > 0) {
            int and = signatureAlgorithm.indexOf("AND", with + 4);
            if (and > 0) {
                keyAlgorithm = signatureAlgorithm.substring(with + 4, and);
            } else {
                keyAlgorithm = signatureAlgorithm.substring(with + 4);
            }
            if (keyAlgorithm.equalsIgnoreCase("ECDSA")) {
                keyAlgorithm = "EC";
            }
        }
        return keyAlgorithm;
    }
    public static String getDigAlgFromSigAlg(String signatureAlgorithm) {
        signatureAlgorithm = signatureAlgorithm.toUpperCase(Locale.ENGLISH);
        int with = signatureAlgorithm.indexOf("WITH");
        if (with > 0) {
            return signatureAlgorithm.substring(0, with);
        }
        return null;
    }
}
