class KrbSafe extends KrbAppMessage {
    private byte[] obuf;
    private byte[] userData;
    public KrbSafe(byte[] userData,
                   Credentials creds,
                   EncryptionKey subKey,
                   KerberosTime timestamp,
                   SeqNumber seqNumber,
                   HostAddress saddr,
                   HostAddress raddr
                   )  throws KrbException, IOException {
        EncryptionKey reqKey = null;
        if (subKey != null)
            reqKey = subKey;
        else
            reqKey = creds.key;
        obuf = mk_safe(userData,
                       reqKey,
                       timestamp,
                       seqNumber,
                       saddr,
                       raddr
                       );
    }
    public KrbSafe(byte[] msg,
                   Credentials creds,
                   EncryptionKey subKey,
                   SeqNumber seqNumber,
                   HostAddress saddr,
                   HostAddress raddr,
                   boolean timestampRequired,
                   boolean seqNumberRequired
                   )  throws KrbException, IOException {
        KRBSafe krb_safe = new KRBSafe(msg);
        EncryptionKey reqKey = null;
        if (subKey != null)
            reqKey = subKey;
        else
            reqKey = creds.key;
        userData = rd_safe(
                           krb_safe,
                           reqKey,
                           seqNumber,
                           saddr,
                           raddr,
                           timestampRequired,
                           seqNumberRequired,
                           creds.client,
                           creds.client.getRealm()
                           );
    }
    public byte[] getMessage() {
        return obuf;
    }
    public byte[] getData() {
        return userData;
    }
    private  byte[] mk_safe(byte[] userData,
                            EncryptionKey key,
                            KerberosTime timestamp,
                            SeqNumber seqNumber,
                            HostAddress sAddress,
                            HostAddress rAddress
                            ) throws Asn1Exception, IOException, KdcErrException,
                            KrbApErrException, KrbCryptoException {
                                Integer usec = null;
                                Integer seqno = null;
                                if (timestamp != null)
                                usec = new Integer(timestamp.getMicroSeconds());
                                if (seqNumber != null) {
                                    seqno = new Integer(seqNumber.current());
                                    seqNumber.step();
                                }
                                KRBSafeBody krb_safeBody =
                                new KRBSafeBody(userData,
                                                timestamp,
                                                usec,
                                                seqno,
                                                sAddress,
                                                rAddress
                                                );
                                byte[] temp = krb_safeBody.asn1Encode();
                                Checksum cksum = new Checksum(
                                    Checksum.SAFECKSUMTYPE_DEFAULT,
                                    temp,
                                    key,
                                    KeyUsage.KU_KRB_SAFE_CKSUM
                                    );
                                KRBSafe krb_safe = new KRBSafe(krb_safeBody, cksum);
                                temp = krb_safe.asn1Encode();
                                return krb_safe.asn1Encode();
                            }
    private byte[] rd_safe(KRBSafe krb_safe,
                           EncryptionKey key,
                           SeqNumber seqNumber,
                           HostAddress sAddress,
                           HostAddress rAddress,
                           boolean timestampRequired,
                           boolean seqNumberRequired,
                           PrincipalName cname,
                           Realm crealm
                           ) throws Asn1Exception, KdcErrException,
                           KrbApErrException, IOException, KrbCryptoException {
                               byte[] temp = krb_safe.safeBody.asn1Encode();
                               if (!krb_safe.cksum.verifyKeyedChecksum(temp, key,
                                   KeyUsage.KU_KRB_SAFE_CKSUM)) {
                                       throw new KrbApErrException(
                                           Krb5.KRB_AP_ERR_MODIFIED);
                               }
                               check(krb_safe.safeBody.timestamp,
                                     krb_safe.safeBody.usec,
                                     krb_safe.safeBody.seqNumber,
                                     krb_safe.safeBody.sAddress,
                                     krb_safe.safeBody.rAddress,
                                     seqNumber,
                                     sAddress,
                                     rAddress,
                                     timestampRequired,
                                     seqNumberRequired,
                                     cname,
                                     crealm
                                     );
                               return krb_safe.safeBody.userData;
                           }
}
