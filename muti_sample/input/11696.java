class AdaptableX509CertSelector extends X509CertSelector {
    private Date startDate;
    private Date endDate;
    private boolean isSKIDSensitive = false;
    private boolean isSNSensitive = false;
    AdaptableX509CertSelector() {
        super();
    }
    void setValidityPeriod(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    void parseAuthorityKeyIdentifierExtension(
            AuthorityKeyIdentifierExtension akidext) throws IOException {
        if (akidext != null) {
            KeyIdentifier akid = (KeyIdentifier)akidext.get(akidext.KEY_ID);
            if (akid != null) {
                if (isSKIDSensitive || getSubjectKeyIdentifier() == null) {
                    DerOutputStream derout = new DerOutputStream();
                    derout.putOctetString(akid.getIdentifier());
                    super.setSubjectKeyIdentifier(derout.toByteArray());
                    isSKIDSensitive = true;
                }
            }
            SerialNumber asn =
                (SerialNumber)akidext.get(akidext.SERIAL_NUMBER);
            if (asn != null) {
                if (isSNSensitive || getSerialNumber() == null) {
                    super.setSerialNumber(asn.getNumber());
                    isSNSensitive = true;
                }
            }
        }
    }
    @Override
    public boolean match(Certificate cert) {
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate xcert = (X509Certificate)cert;
        int version = xcert.getVersion();
        if (version < 3) {
            if (startDate != null) {
                try {
                    xcert.checkValidity(startDate);
                } catch (CertificateException ce) {
                    return false;
                }
            }
            if (endDate != null) {
                try {
                    xcert.checkValidity(endDate);
                } catch (CertificateException ce) {
                    return false;
                }
            }
        }
        if (isSKIDSensitive &&
            (version < 3 || xcert.getExtensionValue("2.5.29.14") == null)) {
            setSubjectKeyIdentifier(null);
        }
        if (isSNSensitive && version < 3) {
            setSerialNumber(null);
        }
        return super.match(cert);
    }
    @Override
    public Object clone() {
        AdaptableX509CertSelector copy =
                        (AdaptableX509CertSelector)super.clone();
        if (startDate != null) {
            copy.startDate = (Date)startDate.clone();
        }
        if (endDate != null) {
            copy.endDate = (Date)endDate.clone();
        }
        return copy;
    }
}
