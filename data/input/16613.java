public class OtherName implements GeneralNameInterface {
    private String name;
    private ObjectIdentifier oid;
    private byte[] nameValue = null;
    private GeneralNameInterface gni = null;
    private static final byte TAG_VALUE = 0;
    private int myhash = -1;
    public OtherName(ObjectIdentifier oid, byte[] value) throws IOException {
        if (oid == null || value == null) {
            throw new NullPointerException("parameters may not be null");
        }
        this.oid = oid;
        this.nameValue = value;
        gni = getGNI(oid, value);
        if (gni != null) {
            name = gni.toString();
        } else {
            name = "Unrecognized ObjectIdentifier: " + oid.toString();
        }
    }
    public OtherName(DerValue derValue) throws IOException {
        DerInputStream in = derValue.toDerInputStream();
        oid = in.getOID();
        DerValue val = in.getDerValue();
        nameValue = val.toByteArray();
        gni = getGNI(oid, nameValue);
        if (gni != null) {
            name = gni.toString();
        } else {
            name = "Unrecognized ObjectIdentifier: " + oid.toString();
        }
    }
    public ObjectIdentifier getOID() {
        return oid;
    }
    public byte[] getNameValue() {
        return nameValue.clone();
    }
    private GeneralNameInterface getGNI(ObjectIdentifier oid, byte[] nameValue)
            throws IOException {
        try {
            Class extClass = OIDMap.getClass(oid);
            if (extClass == null) {   
                return null;
            }
            Class[] params = { Object.class };
            Constructor cons = ((Class<?>)extClass).getConstructor(params);
            Object[] passed = new Object[] { nameValue };
            GeneralNameInterface gni =
                       (GeneralNameInterface)cons.newInstance(passed);
            return gni;
        } catch (Exception e) {
            throw (IOException)new IOException("Instantiation error: " + e).initCause(e);
        }
    }
    public int getType() {
        return GeneralNameInterface.NAME_ANY;
    }
    public void encode(DerOutputStream out) throws IOException {
        if (gni != null) {
            gni.encode(out);
            return;
        } else {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putOID(oid);
            tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_VALUE), nameValue);
            out.write(DerValue.tag_Sequence, tmp);
        }
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OtherName)) {
            return false;
        }
        OtherName otherOther = (OtherName)other;
        if (!(otherOther.oid.equals(oid))) {
            return false;
        }
        GeneralNameInterface otherGNI = null;
        try {
            otherGNI = getGNI(otherOther.oid, otherOther.nameValue);
        } catch (IOException ioe) {
            return false;
        }
        boolean result;
        if (otherGNI != null) {
            try {
                result = (otherGNI.constrains(this) == NAME_MATCH);
            } catch (UnsupportedOperationException ioe) {
                result = false;
            }
        } else {
            result = Arrays.equals(nameValue, otherOther.nameValue);
        }
        return result;
    }
    public int hashCode() {
        if (myhash == -1) {
            myhash = 37 + oid.hashCode();
            for (int i = 0; i < nameValue.length; i++) {
                myhash = 37 * myhash + nameValue[i];
            }
        }
        return myhash;
    }
    public String toString() {
        return "Other-Name: " + name;
    }
    public int constrains(GeneralNameInterface inputName) {
        int constraintType;
        if (inputName == null) {
            constraintType = NAME_DIFF_TYPE;
        } else if (inputName.getType() != NAME_ANY) {
            constraintType = NAME_DIFF_TYPE;
        } else {
            throw new UnsupportedOperationException("Narrowing, widening, "
                + "and matching are not supported for OtherName.");
        }
        return constraintType;
    }
    public int subtreeDepth() {
        throw new UnsupportedOperationException
            ("subtreeDepth() not supported for generic OtherName");
    }
}
