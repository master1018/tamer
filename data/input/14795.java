public class EncTicketPart {
    public TicketFlags flags;
    public EncryptionKey key;
    public Realm crealm;
    public PrincipalName cname;
    public TransitedEncoding transited;
    public KerberosTime authtime;
    public KerberosTime starttime; 
    public KerberosTime endtime;
    public KerberosTime renewTill; 
    public HostAddresses caddr; 
    public AuthorizationData authorizationData; 
    public EncTicketPart(
            TicketFlags new_flags,
            EncryptionKey new_key,
            Realm new_crealm,
            PrincipalName new_cname,
            TransitedEncoding new_transited,
            KerberosTime new_authtime,
            KerberosTime new_starttime,
            KerberosTime new_endtime,
            KerberosTime new_renewTill,
            HostAddresses new_caddr,
            AuthorizationData new_authorizationData) {
        flags = new_flags;
        key = new_key;
        crealm = new_crealm;
        cname = new_cname;
        transited = new_transited;
        authtime = new_authtime;
        starttime = new_starttime;
        endtime = new_endtime;
        renewTill = new_renewTill;
        caddr = new_caddr;
        authorizationData = new_authorizationData;
    }
    public EncTicketPart(byte[] data)
            throws Asn1Exception, KrbException, IOException {
        init(new DerValue(data));
    }
    public EncTicketPart(DerValue encoding)
            throws Asn1Exception, KrbException, IOException {
        init(encoding);
    }
    private static String getHexBytes(byte[] bytes, int len)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int b1 = (bytes[i] >> 4) & 0x0f;
            int b2 = bytes[i] & 0x0f;
            sb.append(Integer.toHexString(b1));
            sb.append(Integer.toHexString(b2));
            sb.append(' ');
        }
        return sb.toString();
    }
    private void init(DerValue encoding)
            throws Asn1Exception, IOException, RealmException {
        DerValue der, subDer;
        renewTill = null;
        caddr = null;
        authorizationData = null;
        if (((encoding.getTag() & (byte) 0x1F) != (byte) 0x03)
                || (encoding.isApplication() != true)
                || (encoding.isConstructed() != true)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        flags = TicketFlags.parse(der.getData(), (byte) 0x00, false);
        key = EncryptionKey.parse(der.getData(), (byte) 0x01, false);
        crealm = Realm.parse(der.getData(), (byte) 0x02, false);
        cname = PrincipalName.parse(der.getData(), (byte) 0x03, false);
        transited = TransitedEncoding.parse(der.getData(), (byte) 0x04, false);
        authtime = KerberosTime.parse(der.getData(), (byte) 0x05, false);
        starttime = KerberosTime.parse(der.getData(), (byte) 0x06, true);
        endtime = KerberosTime.parse(der.getData(), (byte) 0x07, false);
        if (der.getData().available() > 0) {
            renewTill = KerberosTime.parse(der.getData(), (byte) 0x08, true);
        }
        if (der.getData().available() > 0) {
            caddr = HostAddresses.parse(der.getData(), (byte) 0x09, true);
        }
        if (der.getData().available() > 0) {
            authorizationData = AuthorizationData.parse(der.getData(), (byte) 0x0A, true);
        }
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x00), flags.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x01), key.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x02), crealm.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x03), cname.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x04), transited.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x05), authtime.asn1Encode());
        if (starttime != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x06), starttime.asn1Encode());
        }
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x07), endtime.asn1Encode());
        if (renewTill != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x08), renewTill.asn1Encode());
        }
        if (caddr != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x09), caddr.asn1Encode());
        }
        if (authorizationData != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x0A), authorizationData.asn1Encode());
        }
        temp.write(DerValue.tag_Sequence, bytes);
        bytes = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_APPLICATION,
                true, (byte) 0x03), temp);
        return bytes.toByteArray();
    }
}
