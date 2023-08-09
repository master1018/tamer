public class ASRep extends KDCRep {
    public ASRep(
            PAData[] new_pAData,
            Realm new_crealm,
            PrincipalName new_cname,
            Ticket new_ticket,
            EncryptedData new_encPart) throws IOException {
        super(new_pAData, new_crealm, new_cname, new_ticket,
                new_encPart, Krb5.KRB_AS_REP);
    }
    public ASRep(byte[] data) throws Asn1Exception,
            RealmException, KrbApErrException, IOException {
        init(new DerValue(data));
    }
    public ASRep(DerValue encoding) throws Asn1Exception,
            RealmException, KrbApErrException, IOException {
        init(encoding);
    }
    private void init(DerValue encoding) throws Asn1Exception,
            RealmException, KrbApErrException, IOException {
        init(encoding, Krb5.KRB_AS_REP);
    }
}
