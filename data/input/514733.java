public class SslCertificate {
    private static String ISO_8601_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    private DName mIssuedTo;
    private DName mIssuedBy;
    private Date mValidNotBefore;
    private Date mValidNotAfter;
    private static final String ISSUED_TO = "issued-to";
    private static final String ISSUED_BY = "issued-by";
    private static final String VALID_NOT_BEFORE = "valid-not-before";
    private static final String VALID_NOT_AFTER = "valid-not-after";
    public static Bundle saveState(SslCertificate certificate) {
        Bundle bundle = null;
        if (certificate != null) {
            bundle = new Bundle();
            bundle.putString(ISSUED_TO, certificate.getIssuedTo().getDName());
            bundle.putString(ISSUED_BY, certificate.getIssuedBy().getDName());
            bundle.putString(VALID_NOT_BEFORE, certificate.getValidNotBefore());
            bundle.putString(VALID_NOT_AFTER, certificate.getValidNotAfter());
        }
        return bundle;
    }
    public static SslCertificate restoreState(Bundle bundle) {
        if (bundle != null) {
            return new SslCertificate(
                bundle.getString(ISSUED_TO),
                bundle.getString(ISSUED_BY),
                bundle.getString(VALID_NOT_BEFORE),
                bundle.getString(VALID_NOT_AFTER));
        }
        return null;
    }
    public SslCertificate(
            String issuedTo, String issuedBy, String validNotBefore, String validNotAfter) {
        this(issuedTo, issuedBy, parseDate(validNotBefore), parseDate(validNotAfter));
    }
    public SslCertificate(
            String issuedTo, String issuedBy, Date validNotBefore, Date validNotAfter) {
        mIssuedTo = new DName(issuedTo);
        mIssuedBy = new DName(issuedBy);
        mValidNotBefore = cloneDate(validNotBefore);
        mValidNotAfter  = cloneDate(validNotAfter);
    }
    public SslCertificate(X509Certificate certificate) {
        this(certificate.getSubjectDN().getName(),
             certificate.getIssuerDN().getName(),
             certificate.getNotBefore(),
             certificate.getNotAfter());
    }
    public Date getValidNotBeforeDate() {
        return cloneDate(mValidNotBefore);
    }
    public String getValidNotBefore() {
        return formatDate(mValidNotBefore);
    }
    public Date getValidNotAfterDate() {
        return cloneDate(mValidNotAfter);
    }
    public String getValidNotAfter() {
        return formatDate(mValidNotAfter);
    }
    public DName getIssuedTo() {
        return mIssuedTo;
    }
    public DName getIssuedBy() {
        return mIssuedBy;
    }
    public String toString() {
        return
            "Issued to: " + mIssuedTo.getDName() + ";\n" +
            "Issued by: " + mIssuedBy.getDName() + ";\n";
    }
    private static Date parseDate(String string) {
        try {
            return new SimpleDateFormat(ISO_8601_DATE_FORMAT).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }
    private static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(date);
    }
    private static Date cloneDate(Date date) {
        if (date == null) {
            return null;
        }
        return (Date) date.clone();
    }
    public class DName {
        private String mDName;
        private String mCName;
        private String mOName;
        private String mUName;
        public DName(String dName) {
            if (dName != null) {
                mDName = dName;
                try {
                    X509Name x509Name = new X509Name(dName);
                    Vector val = x509Name.getValues();
                    Vector oid = x509Name.getOIDs();
                    for (int i = 0; i < oid.size(); i++) {
                        if (oid.elementAt(i).equals(X509Name.CN)) {
                            mCName = (String) val.elementAt(i);
                            continue;
                        }
                        if (oid.elementAt(i).equals(X509Name.O)) {
                            mOName = (String) val.elementAt(i);
                            continue;
                        }
                        if (oid.elementAt(i).equals(X509Name.OU)) {
                            mUName = (String) val.elementAt(i);
                            continue;
                        }
                    }
                } catch (IllegalArgumentException ex) {
                }
            }
        }
        public String getDName() {
            return mDName != null ? mDName : "";
        }
        public String getCName() {
            return mCName != null ? mCName : "";
        }
        public String getOName() {
            return mOName != null ? mOName : "";
        }
        public String getUName() {
            return mUName != null ? mUName : "";
        }
    }
}
