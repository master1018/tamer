public class EncryptionKey
    implements Cloneable {
    public static final EncryptionKey NULL_KEY =
        new EncryptionKey(new byte[] {}, EncryptedData.ETYPE_NULL, null);
    private int keyType;
    private byte[] keyValue;
    private Integer kvno; 
    private static final boolean DEBUG = Krb5.DEBUG;
    public synchronized int getEType() {
        return keyType;
    }
    public final Integer getKeyVersionNumber() {
        return kvno;
    }
    public final byte[] getBytes() {
        return keyValue;
    }
    public synchronized Object clone() {
        return new EncryptionKey(keyValue, keyType, kvno);
    }
    public static EncryptionKey[] acquireSecretKeys(PrincipalName princ,
                                                    String keytab) {
        if (princ == null)
            throw new IllegalArgumentException(
                "Cannot have null pricipal name to look in keytab.");
        KeyTab ktab = KeyTab.getInstance(keytab);
        return ktab.readServiceKeys(princ);
    }
    public static EncryptionKey acquireSecretKey(char[] password,
            String salt, int etype, byte[] s2kparams)
            throws KrbException {
        return new EncryptionKey(
                        stringToKey(password, salt, s2kparams, etype),
                        etype, null);
    }
    public static EncryptionKey[] acquireSecretKeys(char[] password,
            String salt) throws KrbException {
        int[] etypes = EType.getDefaults("default_tkt_enctypes");
        if (etypes == null) {
            etypes = EType.getBuiltInDefaults();
        }
        EncryptionKey[] encKeys = new EncryptionKey[etypes.length];
        for (int i = 0; i < etypes.length; i++) {
            if (EType.isSupported(etypes[i])) {
                encKeys[i] = new EncryptionKey(
                        stringToKey(password, salt, null, etypes[i]),
                        etypes[i], null);
            } else {
                if (DEBUG) {
                    System.out.println("Encryption Type " +
                        EType.toString(etypes[i]) +
                        " is not supported/enabled");
                }
            }
        }
        return encKeys;
    }
    public EncryptionKey(byte[] keyValue,
                         int keyType,
                         Integer kvno) {
        if (keyValue != null) {
            this.keyValue = new byte[keyValue.length];
            System.arraycopy(keyValue, 0, this.keyValue, 0, keyValue.length);
        } else {
            throw new IllegalArgumentException("EncryptionKey: " +
                                               "Key bytes cannot be null!");
        }
        this.keyType = keyType;
        this.kvno = kvno;
    }
    public EncryptionKey(int keyType,
                         byte[] keyValue) {
        this(keyValue, keyType, null);
    }
    private static byte[] stringToKey(char[] password, String salt,
        byte[] s2kparams, int keyType) throws KrbCryptoException {
        char[] slt = salt.toCharArray();
        char[] pwsalt = new char[password.length + slt.length];
        System.arraycopy(password, 0, pwsalt, 0, password.length);
        System.arraycopy(slt, 0, pwsalt, password.length, slt.length);
        Arrays.fill(slt, '0');
        try {
            switch (keyType) {
                case EncryptedData.ETYPE_DES_CBC_CRC:
                case EncryptedData.ETYPE_DES_CBC_MD5:
                        return Des.string_to_key_bytes(pwsalt);
                case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
                        return Des3.stringToKey(pwsalt);
                case EncryptedData.ETYPE_ARCFOUR_HMAC:
                        return ArcFourHmac.stringToKey(password);
                case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
                        return Aes128.stringToKey(password, salt, s2kparams);
                case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
                        return Aes256.stringToKey(password, salt, s2kparams);
                default:
                        throw new IllegalArgumentException("encryption type " +
                        EType.toString(keyType) + " not supported");
            }
        } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
        } finally {
            Arrays.fill(pwsalt, '0');
        }
    }
    public EncryptionKey(char[] password,
                         String salt,
                         String algorithm) throws KrbCryptoException {
        if (algorithm == null || algorithm.equalsIgnoreCase("DES")) {
            keyType = EncryptedData.ETYPE_DES_CBC_MD5;
        } else if (algorithm.equalsIgnoreCase("DESede")) {
            keyType = EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD;
        } else if (algorithm.equalsIgnoreCase("AES128")) {
            keyType = EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96;
        } else if (algorithm.equalsIgnoreCase("ArcFourHmac")) {
            keyType = EncryptedData.ETYPE_ARCFOUR_HMAC;
        } else if (algorithm.equalsIgnoreCase("AES256")) {
            keyType = EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96;
            if (!EType.isSupported(keyType)) {
                throw new IllegalArgumentException("Algorithm " + algorithm +
                        " not enabled");
            }
        } else {
            throw new IllegalArgumentException("Algorithm " + algorithm +
                " not supported");
        }
        keyValue = stringToKey(password, salt, null, keyType);
        kvno = null;
    }
    EncryptionKey(EncryptionKey key) throws KrbCryptoException {
        keyValue = Confounder.bytes(key.keyValue.length);
        for (int i = 0; i < keyValue.length; i++) {
          keyValue[i] ^= key.keyValue[i];
        }
        keyType = key.keyType;
        try {
            if ((keyType == EncryptedData.ETYPE_DES_CBC_MD5) ||
                (keyType == EncryptedData.ETYPE_DES_CBC_CRC)) {
                if (!DESKeySpec.isParityAdjusted(keyValue, 0)) {
                    keyValue = Des.set_parity(keyValue);
                }
                if (DESKeySpec.isWeak(keyValue, 0)) {
                    keyValue[7] = (byte)(keyValue[7] ^ 0xF0);
                }
            }
            if (keyType == EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD) {
                if (!DESedeKeySpec.isParityAdjusted(keyValue, 0)) {
                    keyValue = Des3.parityFix(keyValue);
                }
                byte[] oneKey = new byte[8];
                for (int i=0; i<keyValue.length; i+=8) {
                    System.arraycopy(keyValue, i, oneKey, 0, 8);
                    if (DESKeySpec.isWeak(oneKey, 0)) {
                        keyValue[i+7] = (byte)(keyValue[i+7] ^ 0xF0);
                    }
                }
            }
        } catch (GeneralSecurityException e) {
            KrbCryptoException ke = new KrbCryptoException(e.getMessage());
            ke.initCause(e);
            throw ke;
        }
    }
    public EncryptionKey(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der;
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & (byte)0x1F) == (byte)0x00) {
            keyType = der.getData().getBigInteger().intValue();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        der = encoding.getData().getDerValue();
        if ((der.getTag() & (byte)0x1F) == (byte)0x01) {
            keyValue = der.getData().getOctetString();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public synchronized byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(keyType);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true,
                                       (byte)0x00), temp);
        temp = new DerOutputStream();
        temp.putOctetString(keyValue);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true,
                                       (byte)0x01), temp);
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
    public synchronized void destroy() {
        if (keyValue != null)
            for (int i = 0; i < keyValue.length; i++)
                keyValue[i] = 0;
    }
    public static EncryptionKey parse(DerInputStream data, byte
                                      explicitTag, boolean optional) throws
                                      Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) !=
                           explicitTag)) {
            return null;
        }
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            return new EncryptionKey(subDer);
        }
    }
    public synchronized void writeKey(CCacheOutputStream cos)
        throws IOException {
        cos.write16(keyType);
        cos.write16(keyType); 
        cos.write32(keyValue.length);
        for (int i = 0; i < keyValue.length; i++) {
            cos.write8(keyValue[i]);
        }
    }
    public String toString() {
        return new String("EncryptionKey: keyType=" + keyType
                          + " kvno=" + kvno
                          + " keyValue (hex dump)="
                          + (keyValue == null || keyValue.length == 0 ?
                        " Empty Key" : '\n'
                        + Krb5.hexDumper.encodeBuffer(keyValue)
                        + '\n'));
    }
    public static EncryptionKey findKey(int etype, EncryptionKey[] keys)
            throws KrbException {
        return findKey(etype, null, keys);
    }
    private static boolean versionMatches(Integer v1, Integer v2) {
        if (v1 == null || v1 == 0 || v2 == null || v2 == 0) {
            return true;
        }
        return v1.equals(v2);
    }
    public static EncryptionKey findKey(int etype, Integer kvno, EncryptionKey[] keys)
        throws KrbException {
        if (!EType.isSupported(etype)) {
            throw new KrbException("Encryption type " +
                EType.toString(etype) + " is not supported/enabled");
        }
        int ktype;
        boolean etypeFound = false;
        for (int i = 0; i < keys.length; i++) {
            ktype = keys[i].getEType();
            if (EType.isSupported(ktype)) {
                Integer kv = keys[i].getKeyVersionNumber();
                if (etype == ktype) {
                    etypeFound = true;
                    if (versionMatches(kvno, kv)) {
                        return keys[i];
                    }
                }
            }
        }
        if ((etype == EncryptedData.ETYPE_DES_CBC_CRC ||
            etype == EncryptedData.ETYPE_DES_CBC_MD5)) {
            for (int i = 0; i < keys.length; i++) {
                ktype = keys[i].getEType();
                if (ktype == EncryptedData.ETYPE_DES_CBC_CRC ||
                        ktype == EncryptedData.ETYPE_DES_CBC_MD5) {
                    Integer kv = keys[i].getKeyVersionNumber();
                    etypeFound = true;
                    if (versionMatches(kvno, kv)) {
                        return new EncryptionKey(etype, keys[i].getBytes());
                    }
                }
            }
        }
        if (etypeFound) {
            throw new KrbException(Krb5.KRB_AP_ERR_BADKEYVER);
        }
        return null;
    }
}
