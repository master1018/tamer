public class KrbCredInfo {
    public EncryptionKey key;
    public Realm prealm; 
    public PrincipalName pname; 
    public TicketFlags flags; 
    public KerberosTime authtime; 
    public KerberosTime starttime; 
    public KerberosTime endtime; 
    public KerberosTime renewTill; 
    public Realm srealm; 
    public PrincipalName sname; 
    public HostAddresses caddr; 
    private KrbCredInfo() {
    }
    public KrbCredInfo(
                       EncryptionKey new_key,
                       Realm new_prealm,
                       PrincipalName new_pname,
                       TicketFlags new_flags,
                       KerberosTime new_authtime,
                       KerberosTime new_starttime,
                       KerberosTime new_endtime,
                       KerberosTime new_renewTill,
                       Realm new_srealm,
                       PrincipalName new_sname,
                       HostAddresses new_caddr
                           ) {
        key = new_key;
        prealm = new_prealm;
        pname = new_pname;
        flags = new_flags;
        authtime = new_authtime;
        starttime = new_starttime;
        endtime = new_endtime;
        renewTill = new_renewTill;
        srealm = new_srealm;
        sname = new_sname;
        caddr = new_caddr;
    }
    public KrbCredInfo(DerValue encoding)
            throws Asn1Exception, IOException, RealmException{
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        prealm = null;
        pname = null;
        flags = null;
        authtime = null;
        starttime = null;
        endtime = null;
        renewTill = null;
        srealm = null;
        sname = null;
        caddr = null;
        key = EncryptionKey.parse(encoding.getData(), (byte)0x00, false);
        if (encoding.getData().available() > 0)
            prealm = Realm.parse(encoding.getData(), (byte)0x01, true);
        if (encoding.getData().available() > 0)
            pname = PrincipalName.parse(encoding.getData(), (byte)0x02, true);
        if (encoding.getData().available() > 0)
            flags = TicketFlags.parse(encoding.getData(), (byte)0x03, true);
        if (encoding.getData().available() > 0)
            authtime = KerberosTime.parse(encoding.getData(), (byte)0x04, true);
        if (encoding.getData().available() > 0)
            starttime = KerberosTime.parse(encoding.getData(), (byte)0x05, true);
        if (encoding.getData().available() > 0)
            endtime = KerberosTime.parse(encoding.getData(), (byte)0x06, true);
        if (encoding.getData().available() > 0)
            renewTill = KerberosTime.parse(encoding.getData(), (byte)0x07, true);
        if (encoding.getData().available() > 0)
            srealm = Realm.parse(encoding.getData(), (byte)0x08, true);
        if (encoding.getData().available() > 0)
            sname = PrincipalName.parse(encoding.getData(), (byte)0x09, true);
        if (encoding.getData().available() > 0)
            caddr = HostAddresses.parse(encoding.getData(), (byte)0x0A, true);
        if (encoding.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        Vector<DerValue> v = new Vector<>();
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), key.asn1Encode()));
        if (prealm != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), prealm.asn1Encode()));
        if (pname != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x02), pname.asn1Encode()));
        if (flags != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x03), flags.asn1Encode()));
        if (authtime != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x04), authtime.asn1Encode()));
        if (starttime != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x05), starttime.asn1Encode()));
        if (endtime != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x06), endtime.asn1Encode()));
        if (renewTill != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x07), renewTill.asn1Encode()));
        if (srealm != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x08), srealm.asn1Encode()));
        if (sname != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x09), sname.asn1Encode()));
        if (caddr != null)
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x0A), caddr.asn1Encode()));
        DerValue der[] = new DerValue[v.size()];
        v.copyInto(der);
        DerOutputStream out = new DerOutputStream();
        out.putSequence(der);
        return out.toByteArray();
    }
    public Object clone() {
        KrbCredInfo kcred = new KrbCredInfo();
        kcred.key = (EncryptionKey)key.clone();
        if (prealm != null)
            kcred.prealm = (Realm)prealm.clone();
        if (pname != null)
            kcred.pname = (PrincipalName)pname.clone();
        if (flags != null)
            kcred.flags = (TicketFlags)flags.clone();
        if (authtime != null)
            kcred.authtime = (KerberosTime)authtime.clone();
        if (starttime != null)
            kcred.starttime = (KerberosTime)starttime.clone();
        if (endtime != null)
            kcred.endtime = (KerberosTime)endtime.clone();
        if (renewTill != null)
            kcred.renewTill = (KerberosTime)renewTill.clone();
        if (srealm != null)
            kcred.srealm = (Realm)srealm.clone();
        if (sname != null)
            kcred.sname = (PrincipalName)sname.clone();
        if (caddr != null)
            kcred.caddr = (HostAddresses)caddr.clone();
        return kcred;
    }
}
