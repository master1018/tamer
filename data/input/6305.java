public class KrbAsReq {
    private ASReq asReqMessg;
    private boolean DEBUG = Krb5.DEBUG;
    public KrbAsReq(EncryptionKey pakey,        
                      KDCOptions options,       
                      PrincipalName cname,      
                      PrincipalName sname,      
                      KerberosTime from,        
                      KerberosTime till,        
                      KerberosTime rtime,       
                      int[] eTypes,             
                      HostAddresses addresses   
                      )
            throws KrbException, IOException {
        if (options == null) {
            options = new KDCOptions();
        }
        if (options.get(KDCOptions.FORWARDED) ||
            options.get(KDCOptions.PROXY) ||
            options.get(KDCOptions.ENC_TKT_IN_SKEY) ||
            options.get(KDCOptions.RENEW) ||
            options.get(KDCOptions.VALIDATE)) {
            throw new KrbException(Krb5.KRB_AP_ERR_REQ_OPTIONS);
        }
        if (options.get(KDCOptions.POSTDATED)) {
        } else {
            if (from != null)  from = null;
        }
        if (options.get(KDCOptions.RENEWABLE)) {
        } else {
            if (rtime != null)  rtime = null;
        }
        PAData[] paData = null;
        if (pakey != null) {
            PAEncTSEnc ts = new PAEncTSEnc();
            byte[] temp = ts.asn1Encode();
            EncryptedData encTs = new EncryptedData(pakey, temp,
                KeyUsage.KU_PA_ENC_TS);
            paData = new PAData[1];
            paData[0] = new PAData( Krb5.PA_ENC_TIMESTAMP,
                                    encTs.asn1Encode());
        }
        if (cname.getRealm() == null) {
            throw new RealmException(Krb5.REALM_NULL,
                                     "default realm not specified ");
        }
        if (DEBUG) {
            System.out.println(">>> KrbAsReq creating message");
        }
        if (addresses == null && Config.getInstance().useAddresses()) {
            addresses = HostAddresses.getLocalAddresses();
        }
        if (sname == null) {
            sname = new PrincipalName("krbtgt" +
                                      PrincipalName.NAME_COMPONENT_SEPARATOR +
                                      cname.getRealmAsString(),
                            PrincipalName.KRB_NT_SRV_INST);
        }
        if (till == null) {
            till = new KerberosTime(0); 
        }
        KDCReqBody kdc_req_body = new KDCReqBody(options,
                                                 cname,
                                                 cname.getRealm(),
                                                 sname,
                                                 from,
                                                 till,
                                                 rtime,
                                                 Nonce.value(),
                                                 eTypes,
                                                 addresses,
                                                 null,
                                                 null);
        asReqMessg = new ASReq(
                         paData,
                         kdc_req_body);
    }
    byte[] encoding() throws IOException, Asn1Exception {
        return asReqMessg.asn1Encode();
    }
    ASReq getMessage() {
        return asReqMessg;
    }
}
