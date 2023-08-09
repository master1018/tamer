public class DomainNameValidator {
    private final static String TAG = "DomainNameValidator";
    private static final boolean DEBUG = false;
    private static final boolean LOG_ENABLED = DEBUG ? Config.LOGD : Config.LOGV;
    private static Pattern QUICK_IP_PATTERN;
    static {
        try {
            QUICK_IP_PATTERN = Pattern.compile("^[a-f0-9\\.:]+$");
        } catch (PatternSyntaxException e) {}
    }
    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;
    public static boolean match(X509Certificate certificate, String thisDomain) {
        if (certificate == null || thisDomain == null || thisDomain.length() == 0) {
            return false;
        }
        thisDomain = thisDomain.toLowerCase();
        if (!isIpAddress(thisDomain)) {
            return matchDns(certificate, thisDomain);
        } else {
            return matchIpAddress(certificate, thisDomain);
        }
    }
    private static boolean isIpAddress(String domain) {
        boolean rval = (domain != null && domain.length() != 0);
        if (rval) {
            try {
                rval = QUICK_IP_PATTERN.matcher(domain).matches();
                if (rval) {
                    rval = domain.equals(
                        InetAddress.getByName(domain).getHostAddress());
                }
            } catch (UnknownHostException e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                  errorMessage = "unknown host exception";
                }
                if (LOG_ENABLED) {
                    Log.v(TAG, "DomainNameValidator.isIpAddress(): " + errorMessage);
                }
                rval = false;
            }
        }
        return rval;
    }
    private static boolean matchIpAddress(X509Certificate certificate, String thisDomain) {
        if (LOG_ENABLED) {
            Log.v(TAG, "DomainNameValidator.matchIpAddress(): this domain: " + thisDomain);
        }
        try {
            Collection subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                Iterator i = subjectAltNames.iterator();
                while (i.hasNext()) {
                    List altNameEntry = (List)(i.next());
                    if (altNameEntry != null && 2 <= altNameEntry.size()) {
                        Integer altNameType = (Integer)(altNameEntry.get(0));
                        if (altNameType != null) {
                            if (altNameType.intValue() == ALT_IPA_NAME) {
                                String altName = (String)(altNameEntry.get(1));
                                if (altName != null) {
                                    if (LOG_ENABLED) {
                                        Log.v(TAG, "alternative IP: " + altName);
                                    }
                                    if (thisDomain.equalsIgnoreCase(altName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {}
        return false;
    }
    private static boolean matchDns(X509Certificate certificate, String thisDomain) {
        boolean hasDns = false;
        try {
            Collection subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                Iterator i = subjectAltNames.iterator();
                while (i.hasNext()) {
                    List altNameEntry = (List)(i.next());
                    if (altNameEntry != null && 2 <= altNameEntry.size()) {
                        Integer altNameType = (Integer)(altNameEntry.get(0));
                        if (altNameType != null) {
                            if (altNameType.intValue() == ALT_DNS_NAME) {
                                hasDns = true;
                                String altName = (String)(altNameEntry.get(1));
                                if (altName != null) {
                                    if (matchDns(thisDomain, altName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "failed to parse certificate";
            }
            Log.w(TAG, "DomainNameValidator.matchDns(): " + errorMessage);
            return false;
        }
        if (!hasDns) {
            final String cn = new DNParser(certificate.getSubjectX500Principal())
                    .find("cn");
            if (LOG_ENABLED) {
                Log.v(TAG, "Validating subject: DN:"
                        + certificate.getSubjectX500Principal().getName(X500Principal.CANONICAL)
                        + "  CN:" + cn);
            }
            if (cn != null) {
                return matchDns(thisDomain, cn);
            }
        }
        return false;
    }
    public static boolean matchDns(String thisDomain, String thatDomain) {
        if (LOG_ENABLED) {
            Log.v(TAG, "DomainNameValidator.matchDns():" +
                      " this domain: " + thisDomain +
                      " that domain: " + thatDomain);
        }
        if (thisDomain == null || thisDomain.length() == 0 ||
            thatDomain == null || thatDomain.length() == 0) {
            return false;
        }
        thatDomain = thatDomain.toLowerCase();
        boolean rval = thisDomain.equals(thatDomain);
        if (!rval) {
            String[] thisDomainTokens = thisDomain.split("\\.");
            String[] thatDomainTokens = thatDomain.split("\\.");
            int thisDomainTokensNum = thisDomainTokens.length;
            int thatDomainTokensNum = thatDomainTokens.length;
            if (thisDomainTokensNum >= thatDomainTokensNum) {
                for (int i = thatDomainTokensNum - 1; i >= 0; --i) {
                    rval = thisDomainTokens[i].equals(thatDomainTokens[i]);
                    if (!rval) {
                        rval = (i == 0 && thisDomainTokensNum == thatDomainTokensNum);
                        if (rval) {
                            rval = thatDomainTokens[0].equals("*");
                            if (!rval) {
                                rval = domainTokenMatch(
                                    thisDomainTokens[0], thatDomainTokens[0]);
                            }
                        }
                        break;
                    }
                }
            } else {
              rval = thatDomain.equals("*." + thisDomain);
            }
        }
        return rval;
    }
    private static boolean domainTokenMatch(String thisDomainToken, String thatDomainToken) {
        if (thisDomainToken != null && thatDomainToken != null) {
            int starIndex = thatDomainToken.indexOf('*');
            if (starIndex >= 0) {
                if (thatDomainToken.length() - 1 <= thisDomainToken.length()) {
                    String prefix = thatDomainToken.substring(0,  starIndex);
                    String suffix = thatDomainToken.substring(starIndex + 1);
                    return thisDomainToken.startsWith(prefix) && thisDomainToken.endsWith(suffix);
                }
            }
        }
        return false;
    }
}
