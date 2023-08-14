class KrbAsRep extends KrbKdcRep {
    private ASRep rep;  
    private Credentials creds;  
    private boolean DEBUG = Krb5.DEBUG;
    KrbAsRep(byte[] ibuf) throws
            KrbException, Asn1Exception, IOException {
        DerValue encoding = new DerValue(ibuf);
        try {
            rep = new ASRep(encoding);
        } catch (Asn1Exception e) {
            rep = null;
            KRBError err = new KRBError(encoding);
            String errStr = err.getErrorString();
            String eText = null; 
            if (errStr != null && errStr.length() > 0) {
                if (errStr.charAt(errStr.length() - 1) == 0)
                    eText = errStr.substring(0, errStr.length() - 1);
                else
                    eText = errStr;
            }
            KrbException ke;
            if (eText == null) {
                ke = new KrbException(err);
            } else {
                if (DEBUG) {
                    System.out.println("KRBError received: " + eText);
                }
                ke = new KrbException(err, eText);
            }
            ke.initCause(e);
            throw ke;
        }
    }
    PAData[] getPA() {
        return rep.pAData;
    }
    void decryptUsingKeyTab(KeyTab ktab, KrbAsReq asReq, PrincipalName cname)
            throws KrbException, Asn1Exception, IOException {
        EncryptionKey dkey = null;
        int encPartKeyType = rep.encPart.getEType();
        Integer encPartKvno = rep.encPart.kvno;
            try {
                dkey = EncryptionKey.findKey(encPartKeyType, encPartKvno,
                        Krb5Util.keysFromJavaxKeyTab(ktab, cname));
            } catch (KrbException ke) {
                if (ke.returnCode() == Krb5.KRB_AP_ERR_BADKEYVER) {
                    dkey = EncryptionKey.findKey(encPartKeyType,
                            Krb5Util.keysFromJavaxKeyTab(ktab, cname));
                }
            }
            if (dkey == null) {
                throw new KrbException(Krb5.API_INVALID_ARG,
                    "Cannot find key for type/kvno to decrypt AS REP - " +
                    EType.toString(encPartKeyType) + "/" + encPartKvno);
            }
        decrypt(dkey, asReq);
    }
    void decryptUsingPassword(char[] password,
            KrbAsReq asReq, PrincipalName cname)
            throws KrbException, Asn1Exception, IOException {
        int encPartKeyType = rep.encPart.getEType();
        PAData.SaltAndParams snp =
                PAData.getSaltAndParams(encPartKeyType, rep.pAData);
        EncryptionKey dkey = null;
        dkey = EncryptionKey.acquireSecretKey(password,
                snp.salt == null ? cname.getSalt() : snp.salt,
                encPartKeyType,
                snp.params);
        decrypt(dkey, asReq);
    }
    private void decrypt(EncryptionKey dkey, KrbAsReq asReq)
            throws KrbException, Asn1Exception, IOException {
        byte[] enc_as_rep_bytes = rep.encPart.decrypt(dkey,
            KeyUsage.KU_ENC_AS_REP_PART);
        byte[] enc_as_rep_part = rep.encPart.reset(enc_as_rep_bytes);
        DerValue encoding = new DerValue(enc_as_rep_part);
        EncASRepPart enc_part = new EncASRepPart(encoding);
        rep.ticket.sname.setRealm(rep.ticket.realm);
        rep.encKDCRepPart = enc_part;
        ASReq req = asReq.getMessage();
        check(req, rep);
        creds = new Credentials(
                                rep.ticket,
                                req.reqBody.cname,
                                rep.ticket.sname,
                                enc_part.key,
                                enc_part.flags,
                                enc_part.authtime,
                                enc_part.starttime,
                                enc_part.endtime,
                                enc_part.renewTill,
                                enc_part.caddr);
        if (DEBUG) {
            System.out.println(">>> KrbAsRep cons in KrbAsReq.getReply " +
                               req.reqBody.cname.getNameString());
        }
    }
    Credentials getCreds() {
        return Objects.requireNonNull(creds, "Creds not available yet.");
    }
    sun.security.krb5.internal.ccache.Credentials getCCreds() {
        return new sun.security.krb5.internal.ccache.Credentials(rep);
    }
}
