abstract class KrbKdcRep {
    static void check(
                      KDCReq req,
                      KDCRep rep
                      ) throws KrbApErrException {
        if (!req.reqBody.cname.equalsWithoutRealm(rep.cname)) {
            rep.encKDCRepPart.key.destroy();
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        if (!req.reqBody.sname.equalsWithoutRealm(rep.encKDCRepPart.sname)) {
            rep.encKDCRepPart.key.destroy();
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        if (!req.reqBody.crealm.equals(rep.encKDCRepPart.srealm)) {
            rep.encKDCRepPart.key.destroy();
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        if (req.reqBody.getNonce() != rep.encKDCRepPart.nonce) {
            rep.encKDCRepPart.key.destroy();
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        if (
            ((req.reqBody.addresses != null && rep.encKDCRepPart.caddr != null) &&
             !req.reqBody.addresses.equals(rep.encKDCRepPart.caddr))) {
            rep.encKDCRepPart.key.destroy();
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        for (int i = 1; i < 6; i++) {
            if (req.reqBody.kdcOptions.get(i) !=
                rep.encKDCRepPart.flags.get(i)) {
                throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
            }
        }
        if (req.reqBody.kdcOptions.get(KDCOptions.RENEWABLE) !=
            rep.encKDCRepPart.flags.get(KDCOptions.RENEWABLE)) {
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        if ((req.reqBody.from == null) || req.reqBody.from.isZero())
            if ((rep.encKDCRepPart.starttime != null) &&
                !rep.encKDCRepPart.starttime.inClockSkew()) {
                rep.encKDCRepPart.key.destroy();
                throw new KrbApErrException(Krb5.KRB_AP_ERR_SKEW);
            }
        if ((req.reqBody.from != null) && !req.reqBody.from.isZero())
            if ((rep.encKDCRepPart.starttime != null) &&
                !req.reqBody.from.equals(rep.encKDCRepPart.starttime)) {
                rep.encKDCRepPart.key.destroy();
                throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
            }
        if (!req.reqBody.till.isZero() &&
            rep.encKDCRepPart.endtime.greaterThan(req.reqBody.till)) {
            rep.encKDCRepPart.key.destroy();
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
        }
        if (req.reqBody.kdcOptions.get(KDCOptions.RENEWABLE))
            if (req.reqBody.rtime != null && !req.reqBody.rtime.isZero())
                if ((rep.encKDCRepPart.renewTill == null) ||
                    rep.encKDCRepPart.renewTill.greaterThan(req.reqBody.rtime)
                    ) {
                    rep.encKDCRepPart.key.destroy();
                    throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
                }
        if (req.reqBody.kdcOptions.get(KDCOptions.RENEWABLE_OK) &&
            rep.encKDCRepPart.flags.get(KDCOptions.RENEWABLE))
            if (!req.reqBody.till.isZero())
                if ((rep.encKDCRepPart.renewTill == null) ||
                    rep.encKDCRepPart.renewTill.greaterThan(req.reqBody.till)
                    ) {
                    rep.encKDCRepPart.key.destroy();
                    throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
                }
    }
}
