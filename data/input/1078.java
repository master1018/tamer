public class CK_ATTRIBUTE {
    public final static CK_ATTRIBUTE TOKEN_FALSE =
                                    new CK_ATTRIBUTE(CKA_TOKEN, false);
    public final static CK_ATTRIBUTE SENSITIVE_FALSE =
                                    new CK_ATTRIBUTE(CKA_SENSITIVE, false);
    public final static CK_ATTRIBUTE EXTRACTABLE_TRUE =
                                    new CK_ATTRIBUTE(CKA_EXTRACTABLE, true);
    public final static CK_ATTRIBUTE ENCRYPT_TRUE =
                                    new CK_ATTRIBUTE(CKA_ENCRYPT, true);
    public final static CK_ATTRIBUTE DECRYPT_TRUE =
                                    new CK_ATTRIBUTE(CKA_DECRYPT, true);
    public final static CK_ATTRIBUTE WRAP_TRUE =
                                    new CK_ATTRIBUTE(CKA_WRAP, true);
    public final static CK_ATTRIBUTE UNWRAP_TRUE =
                                    new CK_ATTRIBUTE(CKA_UNWRAP, true);
    public final static CK_ATTRIBUTE SIGN_TRUE =
                                    new CK_ATTRIBUTE(CKA_SIGN, true);
    public final static CK_ATTRIBUTE VERIFY_TRUE =
                                    new CK_ATTRIBUTE(CKA_VERIFY, true);
    public final static CK_ATTRIBUTE SIGN_RECOVER_TRUE =
                                    new CK_ATTRIBUTE(CKA_SIGN_RECOVER, true);
    public final static CK_ATTRIBUTE VERIFY_RECOVER_TRUE =
                                    new CK_ATTRIBUTE(CKA_VERIFY_RECOVER, true);
    public final static CK_ATTRIBUTE DERIVE_TRUE =
                                    new CK_ATTRIBUTE(CKA_DERIVE, true);
    public final static CK_ATTRIBUTE ENCRYPT_NULL =
                                    new CK_ATTRIBUTE(CKA_ENCRYPT);
    public final static CK_ATTRIBUTE DECRYPT_NULL =
                                    new CK_ATTRIBUTE(CKA_DECRYPT);
    public final static CK_ATTRIBUTE WRAP_NULL =
                                    new CK_ATTRIBUTE(CKA_WRAP);
    public final static CK_ATTRIBUTE UNWRAP_NULL =
                                    new CK_ATTRIBUTE(CKA_UNWRAP);
    public CK_ATTRIBUTE() {
    }
    public CK_ATTRIBUTE(long type) {
        this.type = type;
    }
    public CK_ATTRIBUTE(long type, Object pValue) {
        this.type = type;
        this.pValue = pValue;
    }
    public CK_ATTRIBUTE(long type, boolean value) {
        this.type = type;
        this.pValue = Boolean.valueOf(value);
    }
    public CK_ATTRIBUTE(long type, long value) {
        this.type = type;
        this.pValue = Long.valueOf(value);
    }
    public CK_ATTRIBUTE(long type, BigInteger value) {
        this.type = type;
        this.pValue = sun.security.pkcs11.P11Util.getMagnitude(value);
    }
    public BigInteger getBigInteger() {
        if (pValue instanceof byte[] == false) {
            throw new RuntimeException("Not a byte[]");
        }
        return new BigInteger(1, (byte[])pValue);
    }
    public boolean getBoolean() {
        if (pValue instanceof Boolean == false) {
            throw new RuntimeException
                ("Not a Boolean: " + pValue.getClass().getName());
        }
        return ((Boolean)pValue).booleanValue();
    }
    public char[] getCharArray() {
        if (pValue instanceof char[] == false) {
            throw new RuntimeException("Not a char[]");
        }
        return (char[])pValue;
    }
    public byte[] getByteArray() {
        if (pValue instanceof byte[] == false) {
            throw new RuntimeException("Not a byte[]");
        }
        return (byte[])pValue;
    }
    public long getLong() {
        if (pValue instanceof Long == false) {
            throw new RuntimeException
                ("Not a Long: " + pValue.getClass().getName());
        }
        return ((Long)pValue).longValue();
    }
    public long type;
    public Object pValue;
    public String toString() {
        String prefix = Functions.getAttributeName(type) + " = ";
        if (type == CKA_CLASS) {
            return prefix + Functions.getObjectClassName(getLong());
        } else if (type == CKA_KEY_TYPE) {
            return prefix + Functions.getKeyName(getLong());
        } else {
            String s;
            if (pValue instanceof char[]) {
                s = new String((char[])pValue);
            } else if (pValue instanceof byte[]) {
                s = Functions.toHexString((byte[])pValue);
            } else {
                s = String.valueOf(pValue);
            }
            return prefix + s;
        }
    }
}
