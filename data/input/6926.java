public class Oid {
    private ObjectIdentifier oid;
    private byte[] derEncoding;
    public Oid(String strOid) throws GSSException {
        try {
            oid = new ObjectIdentifier(strOid);
            derEncoding = null;
        } catch (Exception e) {
            throw new GSSException(GSSException.FAILURE,
                          "Improperly formatted Object Identifier String - "
                          + strOid);
        }
    }
    public Oid(InputStream derOid) throws GSSException {
        try {
            DerValue derVal = new DerValue(derOid);
            derEncoding = derVal.toByteArray();
            oid = derVal.getOID();
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE,
                          "Improperly formatted ASN.1 DER encoding for Oid");
        }
    }
    public Oid(byte [] data) throws GSSException {
        try {
            DerValue derVal = new DerValue(data);
            derEncoding = derVal.toByteArray();
            oid = derVal.getOID();
        } catch (IOException e) {
            throw new GSSException(GSSException.FAILURE,
                          "Improperly formatted ASN.1 DER encoding for Oid");
        }
    }
    static Oid getInstance(String strOid) {
        Oid retVal = null;
        try {
            retVal =  new Oid(strOid);
        } catch (GSSException e) {
        }
        return retVal;
    }
    public String toString() {
        return oid.toString();
    }
    public boolean equals(Object other) {
        if (this == other)
            return (true);
        if (other instanceof Oid)
            return this.oid.equals(((Oid) other).oid);
        else if (other instanceof ObjectIdentifier)
            return this.oid.equals(other);
        else
            return false;
    }
    public byte[] getDER() throws GSSException {
        if (derEncoding == null) {
            DerOutputStream dout = new DerOutputStream();
            try {
                dout.putOID(oid);
            } catch (IOException e) {
                throw new GSSException(GSSException.FAILURE, e.getMessage());
            }
            derEncoding = dout.toByteArray();
        }
        return derEncoding.clone();
    }
    public boolean containedIn(Oid[] oids) {
        for (int i = 0; i < oids.length; i++) {
            if (oids[i].equals(this))
                return (true);
        }
        return (false);
    }
    public int hashCode() {
        return oid.hashCode();
    }
}
