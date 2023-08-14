public class HostnameChecker {
    public final static byte TYPE_TLS = 1;
    private final static HostnameChecker INSTANCE_TLS =
                                        new HostnameChecker(TYPE_TLS);
    public final static byte TYPE_LDAP = 2;
    private final static HostnameChecker INSTANCE_LDAP =
                                        new HostnameChecker(TYPE_LDAP);
    private final static int ALTNAME_DNS = 2;
    private final static int ALTNAME_IP  = 7;
    private final byte checkType;
    private HostnameChecker(byte checkType) {
        this.checkType = checkType;
    }
    public static HostnameChecker getInstance(byte checkType) {
        if (checkType == TYPE_TLS) {
            return INSTANCE_TLS;
        } else if (checkType == TYPE_LDAP) {
            return INSTANCE_LDAP;
        }
        throw new IllegalArgumentException("Unknown check type: " + checkType);
    }
    public void match(String expectedName, X509Certificate cert)
            throws CertificateException {
        if (isIpAddress(expectedName)) {
           matchIP(expectedName, cert);
        } else {
           matchDNS(expectedName, cert);
        }
    }
    public static boolean match(String expectedName, Principal principal) {
        String hostName = getServerName(principal);
        return (expectedName.equalsIgnoreCase(hostName));
    }
    public static String getServerName(Principal principal) {
        return Krb5Helper.getPrincipalHostName(principal);
    }
    private static boolean isIpAddress(String name) {
        if (IPAddressUtil.isIPv4LiteralAddress(name) ||
            IPAddressUtil.isIPv6LiteralAddress(name)) {
            return true;
        } else {
            return false;
        }
    }
    private static void matchIP(String expectedIP, X509Certificate cert)
            throws CertificateException {
        Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
        if (subjAltNames == null) {
            throw new CertificateException
                                ("No subject alternative names present");
        }
        for (List<?> next : subjAltNames) {
            if (((Integer)next.get(0)).intValue() == ALTNAME_IP) {
                String ipAddress = (String)next.get(1);
                if (expectedIP.equalsIgnoreCase(ipAddress)) {
                    return;
                }
            }
        }
        throw new CertificateException("No subject alternative " +
                        "names matching " + "IP address " +
                        expectedIP + " found");
    }
    private void matchDNS(String expectedName, X509Certificate cert)
            throws CertificateException {
        Collection<List<?>> subjAltNames = cert.getSubjectAlternativeNames();
        if (subjAltNames != null) {
            boolean foundDNS = false;
            for ( List<?> next : subjAltNames) {
                if (((Integer)next.get(0)).intValue() == ALTNAME_DNS) {
                    foundDNS = true;
                    String dnsName = (String)next.get(1);
                    if (isMatched(expectedName, dnsName)) {
                        return;
                    }
                }
            }
            if (foundDNS) {
                throw new CertificateException("No subject alternative DNS "
                        + "name matching " + expectedName + " found.");
            }
        }
        X500Name subjectName = getSubjectX500Name(cert);
        DerValue derValue = subjectName.findMostSpecificAttribute
                                                    (X500Name.commonName_oid);
        if (derValue != null) {
            try {
                if (isMatched(expectedName, derValue.getAsString())) {
                    return;
                }
            } catch (IOException e) {
            }
        }
        String msg = "No name matching " + expectedName + " found";
        throw new CertificateException(msg);
    }
    public static X500Name getSubjectX500Name(X509Certificate cert)
            throws CertificateParsingException {
        try {
            Principal subjectDN = cert.getSubjectDN();
            if (subjectDN instanceof X500Name) {
                return (X500Name)subjectDN;
            } else {
                X500Principal subjectX500 = cert.getSubjectX500Principal();
                return new X500Name(subjectX500.getEncoded());
            }
        } catch (IOException e) {
            throw(CertificateParsingException)
                new CertificateParsingException().initCause(e);
        }
    }
    private boolean isMatched(String name, String template) {
        if (checkType == TYPE_TLS) {
            return matchAllWildcards(name, template);
        } else if (checkType == TYPE_LDAP) {
            return matchLeftmostWildcard(name, template);
        } else {
            return false;
        }
    }
    private static boolean matchAllWildcards(String name,
         String template) {
        name = name.toLowerCase();
        template = template.toLowerCase();
        StringTokenizer nameSt = new StringTokenizer(name, ".");
        StringTokenizer templateSt = new StringTokenizer(template, ".");
        if (nameSt.countTokens() != templateSt.countTokens()) {
            return false;
        }
        while (nameSt.hasMoreTokens()) {
            if (!matchWildCards(nameSt.nextToken(),
                        templateSt.nextToken())) {
                return false;
            }
        }
        return true;
    }
    private static boolean matchLeftmostWildcard(String name,
                         String template) {
        name = name.toLowerCase();
        template = template.toLowerCase();
        int templateIdx = template.indexOf(".");
        int nameIdx = name.indexOf(".");
        if (templateIdx == -1)
            templateIdx = template.length();
        if (nameIdx == -1)
            nameIdx = name.length();
        if (matchWildCards(name.substring(0, nameIdx),
            template.substring(0, templateIdx))) {
            return template.substring(templateIdx).equals(
                        name.substring(nameIdx));
        } else {
            return false;
        }
    }
    private static boolean matchWildCards(String name, String template) {
        int wildcardIdx = template.indexOf("*");
        if (wildcardIdx == -1)
            return name.equals(template);
        boolean isBeginning = true;
        String beforeWildcard = "";
        String afterWildcard = template;
        while (wildcardIdx != -1) {
            beforeWildcard = afterWildcard.substring(0, wildcardIdx);
            afterWildcard = afterWildcard.substring(wildcardIdx + 1);
            int beforeStartIdx = name.indexOf(beforeWildcard);
            if ((beforeStartIdx == -1) ||
                        (isBeginning && beforeStartIdx != 0)) {
                return false;
            }
            isBeginning = false;
            name = name.substring(beforeStartIdx + beforeWildcard.length());
            wildcardIdx = afterWildcard.indexOf("*");
        }
        return name.endsWith(afterWildcard);
    }
}
