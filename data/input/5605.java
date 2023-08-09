public class OIDName implements GeneralNameInterface {
     private ObjectIdentifier oid;
    public OIDName(DerValue derValue) throws IOException {
        oid = derValue.getOID();
    }
    public OIDName(ObjectIdentifier oid) {
        this.oid = oid;
    }
    public OIDName(String name) throws IOException {
        try {
            oid = new ObjectIdentifier(name);
        } catch (Exception e) {
            throw new IOException("Unable to create OIDName: " + e);
        }
    }
    public int getType() {
        return (GeneralNameInterface.NAME_OID);
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putOID(oid);
    }
    public String toString() {
        return ("OIDName: " + oid.toString());
    }
    public ObjectIdentifier getOID() {
        return oid;
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof OIDName))
            return false;
        OIDName other = (OIDName)obj;
        return oid.equals(other.oid);
    }
    public int hashCode() {
        return oid.hashCode();
    }
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null)
            constraintType = NAME_DIFF_TYPE;
        else if (inputName.getType() != NAME_OID)
            constraintType = NAME_DIFF_TYPE;
        else if (this.equals((OIDName)inputName))
            constraintType = NAME_MATCH;
        else
            throw new UnsupportedOperationException("Narrowing and widening are not supported for OIDNames");
        return constraintType;
    }
    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not supported for OIDName.");
   }
}
