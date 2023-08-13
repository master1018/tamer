public abstract class Validator {
    final static X509Certificate[] CHAIN0 = {};
    public final static String TYPE_SIMPLE = "Simple";
    public final static String TYPE_PKIX = "PKIX";
    public final static String VAR_GENERIC = "generic";
    public final static String VAR_CODE_SIGNING = "code signing";
    public final static String VAR_JCE_SIGNING = "jce signing";
    public final static String VAR_TLS_CLIENT = "tls client";
    public final static String VAR_TLS_SERVER = "tls server";
    public final static String VAR_TSA_SERVER = "tsa server";
    public final static String VAR_PLUGIN_CODE_SIGNING = "plugin code signing";
    final EndEntityChecker endEntityChecker;
    final String variant;
    @Deprecated
    volatile Date validationDate;
    Validator(String type, String variant) {
        this.variant = variant;
        endEntityChecker = EndEntityChecker.getInstance(type, variant);
    }
    public static Validator getInstance(String type, String variant,
            KeyStore ks) {
        return getInstance(type, variant, KeyStores.getTrustedCerts(ks));
    }
    public static Validator getInstance(String type, String variant,
            Collection<X509Certificate> trustedCerts) {
        if (type.equals(TYPE_SIMPLE)) {
            return new SimpleValidator(variant, trustedCerts);
        } else if (type.equals(TYPE_PKIX)) {
            return new PKIXValidator(variant, trustedCerts);
        } else {
            throw new IllegalArgumentException
                ("Unknown validator type: " + type);
        }
    }
    public static Validator getInstance(String type, String variant,
            PKIXBuilderParameters params) {
        if (type.equals(TYPE_PKIX) == false) {
            throw new IllegalArgumentException
                ("getInstance(PKIXBuilderParameters) can only be used "
                + "with PKIX validator");
        }
        return new PKIXValidator(variant, params);
    }
    public final X509Certificate[] validate(X509Certificate[] chain)
            throws CertificateException {
        return validate(chain, null, null);
    }
    public final X509Certificate[] validate(X509Certificate[] chain,
        Collection<X509Certificate> otherCerts) throws CertificateException {
        return validate(chain, otherCerts, null);
    }
    public final X509Certificate[] validate(X509Certificate[] chain,
            Collection<X509Certificate> otherCerts, Object parameter)
            throws CertificateException {
        return validate(chain, otherCerts, null, parameter);
    }
    public final X509Certificate[] validate(X509Certificate[] chain,
                Collection<X509Certificate> otherCerts,
                AlgorithmConstraints constraints,
                Object parameter) throws CertificateException {
        chain = engineValidate(chain, otherCerts, constraints, parameter);
        if (chain.length > 1) {
            endEntityChecker.check(chain[0], parameter);
        }
        return chain;
    }
    abstract X509Certificate[] engineValidate(X509Certificate[] chain,
                Collection<X509Certificate> otherCerts,
                AlgorithmConstraints constraints,
                Object parameter) throws CertificateException;
    public abstract Collection<X509Certificate> getTrustedCertificates();
    @Deprecated
    public void setValidationDate(Date validationDate) {
        this.validationDate = validationDate;
    }
}
