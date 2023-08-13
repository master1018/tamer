public class KrbApRep {
    private byte[] obuf;
    private byte[] ibuf;
    private EncAPRepPart encPart; 
    private APRep apRepMessg;
    public KrbApRep(KrbApReq incomingReq,
                    boolean useSeqNumber,
        boolean useSubKey) throws KrbException, IOException {
        EncryptionKey subKey =
            (useSubKey?
             new EncryptionKey(incomingReq.getCreds().getSessionKey()):null);
        SeqNumber seqNum = new LocalSeqNumber();
        init(incomingReq, subKey, seqNum);
    }
    public KrbApRep(byte[] message, Credentials tgtCreds,
                    KrbApReq outgoingReq) throws KrbException, IOException {
        this(message, tgtCreds);
        authenticate(outgoingReq);
    }
    private void init(KrbApReq apReq,
              EncryptionKey subKey,
        SeqNumber seqNumber)
        throws KrbException, IOException {
        createMessage(
                      apReq.getCreds().key,
                      apReq.getCtime(),
                      apReq.cusec(),
                      subKey,
                      seqNumber);
        obuf = apRepMessg.asn1Encode();
    }
    private KrbApRep(byte[] msg, Credentials tgs_creds)
        throws KrbException, IOException {
        this(new DerValue(msg), tgs_creds);
    }
    private KrbApRep(DerValue encoding, Credentials tgs_creds)
        throws KrbException, IOException {
        APRep rep = null;
        try {
            rep = new APRep(encoding);
        } catch (Asn1Exception e) {
            rep = null;
            KRBError err = new KRBError(encoding);
            String errStr = err.getErrorString();
            String eText;
            if (errStr.charAt(errStr.length() - 1) == 0)
                eText = errStr.substring(0, errStr.length() - 1);
            else
                eText = errStr;
            KrbException ke = new KrbException(err.getErrorCode(), eText);
            ke.initCause(e);
            throw ke;
        }
        byte[] temp = rep.encPart.decrypt(tgs_creds.key,
            KeyUsage.KU_ENC_AP_REP_PART);
        byte[] enc_ap_rep_part = rep.encPart.reset(temp);
        encoding = new DerValue(enc_ap_rep_part);
        encPart = new EncAPRepPart(encoding);
    }
    private void authenticate(KrbApReq apReq)
        throws KrbException, IOException {
        if (encPart.ctime.getSeconds() != apReq.getCtime().getSeconds() ||
            encPart.cusec != apReq.getCtime().getMicroSeconds())
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MUT_FAIL);
    }
    public EncryptionKey getSubKey() {
        return encPart.getSubKey();
    }
    public Integer getSeqNumber() {
        return encPart.getSeqNumber();
    }
    public byte[] getMessage() {
        return obuf;
    }
    private void createMessage(
                               EncryptionKey key,
                               KerberosTime ctime,
                               int cusec,
                               EncryptionKey subKey,
                               SeqNumber seqNumber)
        throws Asn1Exception, IOException,
               KdcErrException, KrbCryptoException {
        Integer seqno = null;
        if (seqNumber != null)
            seqno = new Integer(seqNumber.current());
        encPart = new EncAPRepPart(ctime,
                                   cusec,
                                   subKey,
                                   seqno);
        byte[] encPartEncoding = encPart.asn1Encode();
        EncryptedData encEncPart = new EncryptedData(key, encPartEncoding,
            KeyUsage.KU_ENC_AP_REP_PART);
        apRepMessg = new APRep(encEncPart);
    }
}
