public class AuthorizationData implements Cloneable {
    private AuthorizationDataEntry[] entry = null;
    private AuthorizationData() {
    }
    public AuthorizationData(AuthorizationDataEntry[] new_entries)
            throws IOException {
        if (new_entries != null) {
            entry = new AuthorizationDataEntry[new_entries.length];
            for (int i = 0; i < new_entries.length; i++) {
                if (new_entries[i] == null) {
                    throw new IOException("Cannot create an AuthorizationData");
                } else {
                    entry[i] = (AuthorizationDataEntry) new_entries[i].clone();
                }
            }
        }
    }
    public AuthorizationData(AuthorizationDataEntry new_entry) {
        entry = new AuthorizationDataEntry[1];
        entry[0] = new_entry;
    }
    public Object clone() {
        AuthorizationData new_authorizationData =
                new AuthorizationData();
        if (entry != null) {
            new_authorizationData.entry =
                    new AuthorizationDataEntry[entry.length];
            for (int i = 0; i < entry.length; i++) {
                new_authorizationData.entry[i] =
                        (AuthorizationDataEntry) entry[i].clone();
            }
        }
        return new_authorizationData;
    }
    public AuthorizationData(DerValue der) throws Asn1Exception, IOException {
        Vector<AuthorizationDataEntry> v = new Vector<>();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        while (der.getData().available() > 0) {
            v.addElement(new AuthorizationDataEntry(der.getData().getDerValue()));
        }
        if (v.size() > 0) {
            entry = new AuthorizationDataEntry[v.size()];
            v.copyInto(entry);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerValue der[] = new DerValue[entry.length];
        for (int i = 0; i < entry.length; i++) {
            der[i] = new DerValue(entry[i].asn1Encode());
        }
        bytes.putSequence(der);
        return bytes.toByteArray();
    }
    public static AuthorizationData parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException {
        if ((optional) && (((byte) data.peekByte() & (byte) 0x1F) != explicitTag)) {
            return null;
        }
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte) 0x1F)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            return new AuthorizationData(subDer);
        }
    }
    public void writeAuth(CCacheOutputStream cos) throws IOException {
        for (int i = 0; i < entry.length; i++) {
            entry[i].writeEntry(cos);
        }
    }
    public String toString() {
        String retVal = "AuthorizationData:\n";
        for (int i = 0; i < entry.length; i++) {
            retVal += entry[i].toString();
        }
        return retVal;
    }
    public int count() {
        return entry.length;
    }
    public AuthorizationDataEntry item(int i) {
        return (AuthorizationDataEntry)entry[i].clone();
    }
}
