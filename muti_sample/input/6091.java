public class Ticket implements Cloneable {
    public int tkt_vno;
    public Realm realm;
    public PrincipalName sname;
    public EncryptedData encPart;
    private Ticket() {
    }
    public Object clone() {
        Ticket new_ticket = new Ticket();
        new_ticket.realm = (Realm)realm.clone();
        new_ticket.sname = (PrincipalName)sname.clone();
        new_ticket.encPart = (EncryptedData)encPart.clone();
        new_ticket.tkt_vno = tkt_vno;
        return new_ticket;
    }
    public Ticket(
                  Realm new_realm,
                  PrincipalName new_sname,
                  EncryptedData new_encPart
                      ) {
        tkt_vno = Krb5.TICKET_VNO;
        realm = new_realm;
        sname = new_sname;
        encPart = new_encPart;
    }
    public Ticket(byte[] data) throws Asn1Exception,
    RealmException, KrbApErrException, IOException {
        init(new DerValue(data));
    }
    public Ticket(DerValue encoding) throws Asn1Exception,
    RealmException, KrbApErrException, IOException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception,
    RealmException, KrbApErrException, IOException {
        DerValue der;
        DerValue subDer;
        if (((encoding.getTag() & (byte)0x1F) != Krb5.KRB_TKT)
            || (encoding.isApplication() != true)
            || (encoding.isConstructed() != true))
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        der = encoding.getData().getDerValue();
        if (der.getTag() != DerValue.tag_Sequence)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        subDer = der.getData().getDerValue();
        if ((subDer.getTag() & (byte)0x1F) != (byte)0x00)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        tkt_vno = subDer.getData().getBigInteger().intValue();
        if (tkt_vno != Krb5.TICKET_VNO)
            throw new KrbApErrException(Krb5.KRB_AP_ERR_BADVERSION);
        realm = Realm.parse(der.getData(), (byte)0x01, false);
        sname = PrincipalName.parse(der.getData(), (byte)0x02, false);
        encPart = EncryptedData.parse(der.getData(), (byte)0x03, false);
        if (der.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        DerValue der[] = new DerValue[4];
        temp.putInteger(BigInteger.valueOf(tkt_vno));
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), temp);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), realm.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x02), sname.asn1Encode());
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x03), encPart.asn1Encode());
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        DerOutputStream ticket = new DerOutputStream();
        ticket.write(DerValue.createTag(DerValue.TAG_APPLICATION, true, (byte)0x01), temp);
        return ticket.toByteArray();
    }
    public static Ticket parse(DerInputStream data, byte explicitTag, boolean optional) throws Asn1Exception, IOException, RealmException, KrbApErrException {
        if ((optional) && (((byte)data.peekByte() & (byte)0x1F)!= explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        else {
            DerValue subDer = der.getData().getDerValue();
            return new Ticket(subDer);
        }
    }
}
