public class KDCReqBody {
    public KDCOptions kdcOptions;
    public PrincipalName cname; 
    public Realm crealm;
    public PrincipalName sname; 
    public KerberosTime from; 
    public KerberosTime till;
    public KerberosTime rtime; 
    public HostAddresses addresses; 
    private int nonce;
    private int[] eType = null; 
    private EncryptedData encAuthorizationData; 
    private Ticket[] additionalTickets; 
    public KDCReqBody(
            KDCOptions new_kdcOptions,
            PrincipalName new_cname, 
            Realm new_crealm,
            PrincipalName new_sname, 
            KerberosTime new_from, 
            KerberosTime new_till,
            KerberosTime new_rtime, 
            int new_nonce,
            int[] new_eType, 
            HostAddresses new_addresses, 
            EncryptedData new_encAuthorizationData, 
            Ticket[] new_additionalTickets 
            ) throws IOException {
        kdcOptions = new_kdcOptions;
        cname = new_cname;
        crealm = new_crealm;
        sname = new_sname;
        from = new_from;
        till = new_till;
        rtime = new_rtime;
        nonce = new_nonce;
        if (new_eType != null) {
            eType = new_eType.clone();
        }
        addresses = new_addresses;
        encAuthorizationData = new_encAuthorizationData;
        if (new_additionalTickets != null) {
            additionalTickets = new Ticket[new_additionalTickets.length];
            for (int i = 0; i < new_additionalTickets.length; i++) {
                if (new_additionalTickets[i] == null) {
                    throw new IOException("Cannot create a KDCReqBody");
                } else {
                    additionalTickets[i] = (Ticket)new_additionalTickets[i].clone();
                }
            }
        }
    }
    public KDCReqBody(DerValue encoding, int msgType)
            throws Asn1Exception, RealmException, KrbException, IOException {
        DerValue der, subDer;
        addresses = null;
        encAuthorizationData = null;
        additionalTickets = null;
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        kdcOptions = KDCOptions.parse(encoding.getData(), (byte)0x00, false);
        cname = PrincipalName.parse(encoding.getData(), (byte)0x01, true);
        if ((msgType != Krb5.KRB_AS_REQ) && (cname != null)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        crealm = Realm.parse(encoding.getData(), (byte)0x02, false);
        sname = PrincipalName.parse(encoding.getData(), (byte)0x03, true);
        from = KerberosTime.parse(encoding.getData(), (byte)0x04, true);
        till = KerberosTime.parse(encoding.getData(), (byte)0x05, false);
        rtime = KerberosTime.parse(encoding.getData(), (byte)0x06, true);
        der = encoding.getData().getDerValue();
        if ((der.getTag() & (byte)0x1F) == (byte)0x07) {
            nonce = der.getData().getBigInteger().intValue();
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        Vector<Integer> v = new Vector<>();
        if ((der.getTag() & (byte)0x1F) == (byte)0x08) {
            subDer = der.getData().getDerValue();
            if (subDer.getTag() == DerValue.tag_SequenceOf) {
                while(subDer.getData().available() > 0) {
                    v.addElement(subDer.getData().getBigInteger().intValue());
                }
                eType = new int[v.size()];
                for (int i = 0; i < v.size(); i++) {
                    eType[i] = v.elementAt(i);
                }
            } else {
                throw new Asn1Exception(Krb5.ASN1_BAD_ID);
            }
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        if (encoding.getData().available() > 0) {
            addresses = HostAddresses.parse(encoding.getData(), (byte)0x09, true);
        }
        if (encoding.getData().available() > 0) {
            encAuthorizationData = EncryptedData.parse(encoding.getData(), (byte)0x0A, true);
        }
        if (encoding.getData().available() > 0) {
            Vector<Ticket> tempTickets = new Vector<>();
            der = encoding.getData().getDerValue();
            if ((der.getTag() & (byte)0x1F) == (byte)0x0B) {
                subDer = der.getData().getDerValue();
                if (subDer.getTag() == DerValue.tag_SequenceOf) {
                    while (subDer.getData().available() > 0) {
                        tempTickets.addElement(new Ticket(subDer.getData().getDerValue()));
                    }
                } else {
                    throw new Asn1Exception(Krb5.ASN1_BAD_ID);
                }
                if (tempTickets.size() > 0) {
                    additionalTickets = new Ticket[tempTickets.size()];
                    tempTickets.copyInto(additionalTickets);
                }
            } else {
                throw new Asn1Exception(Krb5.ASN1_BAD_ID);
            }
        }
        if (encoding.getData().available() > 0) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }
    public byte[] asn1Encode(int msgType) throws Asn1Exception, IOException {
        Vector<DerValue> v = new Vector<>();
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), kdcOptions.asn1Encode()));
        if (msgType == Krb5.KRB_AS_REQ) {
            if (cname != null) {
                v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), cname.asn1Encode()));
            }
        }
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x02), crealm.asn1Encode()));
        if (sname != null) {
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x03), sname.asn1Encode()));
        }
        if (from != null) {
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x04), from.asn1Encode()));
        }
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x05), till.asn1Encode()));
        if (rtime != null) {
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x06), rtime.asn1Encode()));
        }
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(BigInteger.valueOf(nonce));
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x07), temp.toByteArray()));
        temp = new DerOutputStream();
        for (int i = 0; i < eType.length; i++) {
            temp.putInteger(BigInteger.valueOf(eType[i]));
        }
        DerOutputStream eTypetemp = new DerOutputStream();
        eTypetemp.write(DerValue.tag_SequenceOf, temp);
        v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x08), eTypetemp.toByteArray()));
        if (addresses != null) {
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x09), addresses.asn1Encode()));
        }
        if (encAuthorizationData != null) {
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x0A), encAuthorizationData.asn1Encode()));
        }
        if (additionalTickets != null && additionalTickets.length > 0) {
            temp = new DerOutputStream();
            for (int i = 0; i < additionalTickets.length; i++) {
                temp.write(additionalTickets[i].asn1Encode());
            }
            DerOutputStream ticketsTemp = new DerOutputStream();
            ticketsTemp.write(DerValue.tag_SequenceOf, temp);
            v.addElement(new DerValue(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x0B), ticketsTemp.toByteArray()));
        }
        DerValue der[] = new DerValue[v.size()];
        v.copyInto(der);
        temp = new DerOutputStream();
        temp.putSequence(der);
        return temp.toByteArray();
    }
    public int getNonce() {
        return nonce;
    }
}
