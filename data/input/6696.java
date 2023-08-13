public class EncKDCRepPart {
    public EncryptionKey key;
    public LastReq lastReq;
    public int nonce;
    public KerberosTime keyExpiration; 
    public TicketFlags flags;
    public KerberosTime authtime;
    public KerberosTime starttime; 
    public KerberosTime endtime;
    public KerberosTime renewTill; 
    public Realm srealm;
    public PrincipalName sname;
    public HostAddresses caddr; 
    public int msgType; 
    public EncKDCRepPart(
            EncryptionKey new_key,
            LastReq new_lastReq,
            int new_nonce,
            KerberosTime new_keyExpiration,
            TicketFlags new_flags,
            KerberosTime new_authtime,
            KerberosTime new_starttime,
            KerberosTime new_endtime,
            KerberosTime new_renewTill,
            Realm new_srealm,
            PrincipalName new_sname,
            HostAddresses new_caddr,
            int new_msgType) {
        key = new_key;
        lastReq = new_lastReq;
        nonce = new_nonce;
        keyExpiration = new_keyExpiration;
        flags = new_flags;
        authtime = new_authtime;
        starttime = new_starttime;
        endtime = new_endtime;
        renewTill = new_renewTill;
        srealm = new_srealm;
        sname = new_sname;
        caddr = new_caddr;
        msgType = new_msgType;
    }
    public EncKDCRepPart() {
    }
    public EncKDCRepPart(byte[] data, int rep_type)
            throws Asn1Exception, IOException, RealmException {
        init(new DerValue(data), rep_type);
    }
    public EncKDCRepPart(DerValue encoding, int rep_type)
            throws Asn1Exception, IOException, RealmException {
        init(encoding, rep_type);
    }
    protected void init(DerValue encoding, int rep_type)
            throws Asn1Exception, IOException, RealmException {
        DerValue der, subDer;
        msgType = (encoding.getTag() & (byte) 0x1F);
        if (msgType != Krb5.KRB_ENC_AS_REP_PART &&
                msgType != Krb5.KRB_ENC_TGS_REP_PART) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        key = EncryptionKey.parse(der.getData(), (byte) 0x00, false);
        lastReq = LastReq.parse(der.getData(), (byte) 0x01, false);
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & (byte) 0x1F) == (byte) 0x02) {
            nonce = subDer.getData().getBigInteger().intValue();
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        keyExpiration = KerberosTime.parse(der.getData(), (byte) 0x03, true);
        flags = TicketFlags.parse(der.getData(), (byte) 0x04, false);
        authtime = KerberosTime.parse(der.getData(), (byte) 0x05, false);
        starttime = KerberosTime.parse(der.getData(), (byte) 0x06, true);
        endtime = KerberosTime.parse(der.getData(), (byte) 0x07, false);
        renewTill = KerberosTime.parse(der.getData(), (byte) 0x08, true);
        srealm = Realm.parse(der.getData(), (byte) 0x09, false);
        sname = PrincipalName.parse(der.getData(), (byte) 0x0A, false);
        if (der.getData().available() > 0) {
            caddr = HostAddresses.parse(der.getData(), (byte) 0x0B, true);
        }
        if (der.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode(int rep_type) throws Asn1Exception,
            IOException {
        DerOutputStream temp = new DerOutputStream();
        DerOutputStream bytes = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x00), key.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x01), lastReq.asn1Encode());
        temp.putInteger(BigInteger.valueOf(nonce));
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x02), temp);
        if (keyExpiration != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x03), keyExpiration.asn1Encode());
        }
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x04), flags.asn1Encode());
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
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x09), srealm.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                true, (byte) 0x0A), sname.asn1Encode());
        if (caddr != null) {
            bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                    true, (byte) 0x0B), caddr.asn1Encode());
        }
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        bytes = new DerOutputStream();
        bytes.write(DerValue.createTag(DerValue.TAG_APPLICATION,
                true, (byte) msgType), temp);
        return bytes.toByteArray();
    }
}
