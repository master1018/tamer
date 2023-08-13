public class LastReq {
    private LastReqEntry[] entry = null;
    public LastReq(LastReqEntry[] entries) throws IOException {
        if (entries != null) {
            entry = new LastReqEntry[entries.length];
            for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null) {
                    throw new IOException("Cannot create a LastReqEntry");
                } else {
                    entry[i] = (LastReqEntry)entries[i].clone();
                }
            }
        }
    }
    public LastReq(DerValue encoding) throws Asn1Exception, IOException {
        Vector<LastReqEntry> v= new Vector<>();
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        while (encoding.getData().available() > 0) {
            v.addElement(new LastReqEntry(encoding.getData().getDerValue()));
        }
        if (v.size() > 0) {
            entry = new LastReqEntry[v.size()];
            v.copyInto(entry);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        if (entry != null && entry.length > 0) {
            DerOutputStream temp = new DerOutputStream();
            for (int i = 0; i < entry.length; i++)
                temp.write(entry[i].asn1Encode());
            bytes.write(DerValue.tag_Sequence, temp);
            return bytes.toByteArray();
        }
        return null;
    }
    public static LastReq parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F) != explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        else {
            DerValue subDer = der.getData().getDerValue();
            return new LastReq(subDer);
        }
    }
}
