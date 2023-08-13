class EndEntityChecker {
    private final static String OID_EXTENDED_KEY_USAGE =
                                SimpleValidator.OID_EXTENDED_KEY_USAGE;
    private final static String OID_EKU_TLS_SERVER = "1.3.6.1.5.5.7.3.1";
    private final static String OID_EKU_TLS_CLIENT = "1.3.6.1.5.5.7.3.2";
    private final static String OID_EKU_CODE_SIGNING = "1.3.6.1.5.5.7.3.3";
    private final static String OID_EKU_TIME_STAMPING = "1.3.6.1.5.5.7.3.8";
    private final static String OID_EKU_ANY_USAGE = "2.5.29.37.0";
    private final static String OID_EKU_NS_SGC = "2.16.840.1.113730.4.1";
    private final static String OID_EKU_MS_SGC = "1.3.6.1.4.1.311.10.3.3";
    private final static String OID_SUBJECT_ALT_NAME = "2.5.29.17";
    private final static String NSCT_SSL_CLIENT =
                                NetscapeCertTypeExtension.SSL_CLIENT;
    private final static String NSCT_SSL_SERVER =
                                NetscapeCertTypeExtension.SSL_SERVER;
    private final static String NSCT_CODE_SIGNING =
                                NetscapeCertTypeExtension.OBJECT_SIGNING;
    private final static int KU_SIGNATURE = 0;
    private final static int KU_KEY_ENCIPHERMENT = 2;
    private final static int KU_KEY_AGREEMENT = 4;
    private final static Collection<String> KU_SERVER_SIGNATURE =
        Arrays.asList("DHE_DSS", "DHE_RSA", "ECDHE_ECDSA", "ECDHE_RSA",
            "RSA_EXPORT", "UNKNOWN");
    private final static Collection<String> KU_SERVER_ENCRYPTION =
        Arrays.asList("RSA");
    private final static Collection<String> KU_SERVER_KEY_AGREEMENT =
        Arrays.asList("DH_DSS", "DH_RSA", "ECDH_ECDSA", "ECDH_RSA");
    private final String variant;
    private final String type;
    private EndEntityChecker(String type, String variant) {
        this.type = type;
        this.variant = variant;
    }
    static EndEntityChecker getInstance(String type, String variant) {
        return new EndEntityChecker(type, variant);
    }
    void check(X509Certificate cert, Object parameter)
            throws CertificateException {
        if (variant.equals(Validator.VAR_GENERIC)) {
            return;
        } else if (variant.equals(Validator.VAR_TLS_SERVER)) {
            checkTLSServer(cert, (String)parameter);
        } else if (variant.equals(Validator.VAR_TLS_CLIENT)) {
            checkTLSClient(cert);
        } else if (variant.equals(Validator.VAR_CODE_SIGNING)) {
            checkCodeSigning(cert);
        } else if (variant.equals(Validator.VAR_JCE_SIGNING)) {
            checkCodeSigning(cert);
        } else if (variant.equals(Validator.VAR_PLUGIN_CODE_SIGNING)) {
            checkCodeSigning(cert);
        } else if (variant.equals(Validator.VAR_TSA_SERVER)) {
            checkTSAServer(cert);
        } else {
            throw new CertificateException("Unknown variant: " + variant);
        }
    }
    private Set<String> getCriticalExtensions(X509Certificate cert) {
        Set<String> exts = cert.getCriticalExtensionOIDs();
        if (exts == null) {
            exts = Collections.emptySet();
        }
        return exts;
    }
    private void checkRemainingExtensions(Set<String> exts)
            throws CertificateException {
        exts.remove(SimpleValidator.OID_BASIC_CONSTRAINTS);
        exts.remove(OID_SUBJECT_ALT_NAME);
        if (!exts.isEmpty()) {
            throw new CertificateException("Certificate contains unsupported "
                + "critical extensions: " + exts);
        }
    }
    private boolean checkEKU(X509Certificate cert, Set<String> exts,
            String expectedEKU) throws CertificateException {
        List<String> eku = cert.getExtendedKeyUsage();
        if (eku == null) {
            return true;
        }
        return eku.contains(expectedEKU) || eku.contains(OID_EKU_ANY_USAGE);
    }
    private boolean checkKeyUsage(X509Certificate cert, int bit)
            throws CertificateException {
        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage == null) {
            return true;
        }
        return (keyUsage.length > bit) && keyUsage[bit];
    }
    private void checkTLSClient(X509Certificate cert)
            throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (checkKeyUsage(cert, KU_SIGNATURE) == false) {
            throw new ValidatorException
                ("KeyUsage does not allow digital signatures",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (checkEKU(cert, exts, OID_EKU_TLS_CLIENT) == false) {
            throw new ValidatorException("Extended key usage does not "
                + "permit use for TLS client authentication",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_CLIENT)) {
            throw new ValidatorException
                ("Netscape cert type does not permit use for SSL client",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
        exts.remove(SimpleValidator.OID_NETSCAPE_CERT_TYPE);
        checkRemainingExtensions(exts);
    }
    private void checkTLSServer(X509Certificate cert, String parameter)
            throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (KU_SERVER_ENCRYPTION.contains(parameter)) {
            if (checkKeyUsage(cert, KU_KEY_ENCIPHERMENT) == false) {
                throw new ValidatorException
                        ("KeyUsage does not allow key encipherment",
                        ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else if (KU_SERVER_SIGNATURE.contains(parameter)) {
            if (checkKeyUsage(cert, KU_SIGNATURE) == false) {
                throw new ValidatorException
                        ("KeyUsage does not allow digital signatures",
                        ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else if (KU_SERVER_KEY_AGREEMENT.contains(parameter)) {
            if (checkKeyUsage(cert, KU_KEY_AGREEMENT) == false) {
                throw new ValidatorException
                        ("KeyUsage does not allow key agreement",
                        ValidatorException.T_EE_EXTENSIONS, cert);
            }
        } else {
            throw new CertificateException("Unknown authType: " + parameter);
        }
        if (checkEKU(cert, exts, OID_EKU_TLS_SERVER) == false) {
            if ((checkEKU(cert, exts, OID_EKU_MS_SGC) == false) &&
                (checkEKU(cert, exts, OID_EKU_NS_SGC) == false)) {
                throw new ValidatorException
                    ("Extended key usage does not permit use for TLS "
                    + "server authentication",
                    ValidatorException.T_EE_EXTENSIONS, cert);
            }
        }
        if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_SSL_SERVER)) {
            throw new ValidatorException
                ("Netscape cert type does not permit use for SSL server",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
        exts.remove(SimpleValidator.OID_NETSCAPE_CERT_TYPE);
        checkRemainingExtensions(exts);
    }
    private void checkCodeSigning(X509Certificate cert)
            throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (checkKeyUsage(cert, KU_SIGNATURE) == false) {
            throw new ValidatorException
                ("KeyUsage does not allow digital signatures",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (checkEKU(cert, exts, OID_EKU_CODE_SIGNING) == false) {
            throw new ValidatorException
                ("Extended key usage does not permit use for code signing",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (variant.equals(Validator.VAR_JCE_SIGNING) == false) {
            if (!SimpleValidator.getNetscapeCertTypeBit(cert, NSCT_CODE_SIGNING)) {
                throw new ValidatorException
                    ("Netscape cert type does not permit use for code signing",
                    ValidatorException.T_EE_EXTENSIONS, cert);
            }
            exts.remove(SimpleValidator.OID_NETSCAPE_CERT_TYPE);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
        checkRemainingExtensions(exts);
    }
    private void checkTSAServer(X509Certificate cert)
            throws CertificateException {
        Set<String> exts = getCriticalExtensions(cert);
        if (checkKeyUsage(cert, KU_SIGNATURE) == false) {
            throw new ValidatorException
                ("KeyUsage does not allow digital signatures",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (cert.getExtendedKeyUsage() == null) {
            throw new ValidatorException
                ("Certificate does not contain an extended key usage " +
                "extension required for a TSA server",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        if (checkEKU(cert, exts, OID_EKU_TIME_STAMPING) == false) {
            throw new ValidatorException
                ("Extended key usage does not permit use for TSA server",
                ValidatorException.T_EE_EXTENSIONS, cert);
        }
        exts.remove(SimpleValidator.OID_KEY_USAGE);
        exts.remove(SimpleValidator.OID_EXTENDED_KEY_USAGE);
        checkRemainingExtensions(exts);
    }
}
