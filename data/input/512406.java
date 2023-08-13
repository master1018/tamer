public class X509CRLSelector implements CRLSelector {
    private ArrayList<String> issuerNames;
    private ArrayList<X500Principal> issuerPrincipals;
    private BigInteger minCRL;
    private BigInteger maxCRL;
    private long dateAndTime = -1;
    private X509Certificate certificateChecking;
    public X509CRLSelector() { }
    public void setIssuers(Collection<X500Principal> issuers) {
        if (issuers == null) {
            issuerNames = null;
            issuerPrincipals = null;
            return;
        }
        issuerNames = new ArrayList<String>(issuers.size());
        issuerPrincipals = new ArrayList<X500Principal>(issuers);
        for (X500Principal issuer: issuers) {
            issuerNames.add(issuer.getName(X500Principal.CANONICAL));
        }
    }
    public void setIssuerNames(Collection<?> names) throws IOException {
        if (names == null) {
            issuerNames = null;
            issuerPrincipals = null;
            return;
        }
        if (names.size() == 0) {
            return;
        }
        issuerNames = new ArrayList<String>(names.size());
        for (Object name: names) {
            if (name instanceof String) {
                issuerNames.add(
                        new Name((String) name).getName(
                            X500Principal.CANONICAL));
            } else if (name instanceof byte[]) {
                issuerNames.add(
                        new Name((byte[]) name).getName(
                            X500Principal.CANONICAL));
            } else {
                throw new IOException(
                        Messages.getString("security.62")); 
            }
        }
    }
    public void addIssuer(X500Principal issuer) {
        if (issuer == null) {
            throw new NullPointerException(Messages.getString("security.61")); 
        }
        if (issuerNames == null) {
            issuerNames = new ArrayList<String>();
        }
        String name = issuer.getName(X500Principal.CANONICAL);
        if (!issuerNames.contains(name)) {
            issuerNames.add(name);
        }
        if (issuerPrincipals == null) {
            issuerPrincipals = new ArrayList<X500Principal>(issuerNames.size());
        }
        int size = issuerNames.size() - 1;
        for (int i=issuerPrincipals.size(); i<size; i++) {
            issuerPrincipals.add(new X500Principal(issuerNames.get(i)));
        }
        issuerPrincipals.add(issuer);
    }
    public void addIssuerName(String iss_name) throws IOException {
        if (issuerNames == null) {
            issuerNames = new ArrayList<String>();
        }
        if (iss_name == null) {
            iss_name = ""; 
        }
        String name = new Name(iss_name).getName(X500Principal.CANONICAL);
        if (!issuerNames.contains(name)) {
            issuerNames.add(name);
        }
    }
    public void addIssuerName(byte[] iss_name) throws IOException {
        if (iss_name == null) {
            throw new NullPointerException(Messages.getString("security.63")); 
        }
        if (issuerNames == null) {
            issuerNames = new ArrayList<String>();
        }
        String name = new Name(iss_name).getName(X500Principal.CANONICAL);
        if (!issuerNames.contains(name)) {
            issuerNames.add(name);
        }
    }
    public void setMinCRLNumber(BigInteger minCRL) {
        this.minCRL = minCRL;
    }
    public void setMaxCRLNumber(BigInteger maxCRL) {
        this.maxCRL = maxCRL;
    }
    public void setDateAndTime(Date dateAndTime) {
        if (dateAndTime == null) {
            this.dateAndTime = -1;
            return;
        }
        this.dateAndTime = dateAndTime.getTime();
    }
    public void setCertificateChecking(X509Certificate cert) {
        this.certificateChecking = cert;
    }
    public Collection<X500Principal> getIssuers() {
        if (issuerNames == null) {
            return null;
        }
        if (issuerPrincipals == null) {
            issuerPrincipals = new ArrayList<X500Principal>(issuerNames.size());
        }
        int size = issuerNames.size();
        for (int i=issuerPrincipals.size(); i<size; i++) {
            issuerPrincipals.add(new X500Principal(issuerNames.get(i)));
        }
        return Collections.unmodifiableCollection(issuerPrincipals);
    }
    public Collection<Object> getIssuerNames() {
        if (issuerNames == null) {
            return null;
        }
        return Collections.unmodifiableCollection((ArrayList<?>) issuerNames);
    }
    public BigInteger getMinCRL() {
        return minCRL;
    }
    public BigInteger getMaxCRL() {
        return maxCRL;
    }
    public Date getDateAndTime() {
        if (dateAndTime == -1) {
            return null;
        }
        return new Date(dateAndTime);
    }
    public X509Certificate getCertificateChecking() {
        return certificateChecking;
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("X509CRLSelector:\n["); 
        if (issuerNames != null) {
            result.append("\n  IssuerNames:\n  ["); 
            int size = issuerNames.size();
            for (int i=0; i<size; i++) {
                result.append("\n    " 
                    + issuerNames.get(i));
            }
            result.append("\n  ]"); 
        }
        if (minCRL != null) {
            result.append("\n  minCRL: " + minCRL); 
        }
        if (maxCRL != null) {
            result.append("\n  maxCRL: " + maxCRL); 
        }
        if (dateAndTime != -1) {
            result.append("\n  dateAndTime: " + (new Date(dateAndTime))); 
        }
        if (certificateChecking != null) {
            result.append("\n  certificateChecking: " + certificateChecking); 
        }
        result.append("\n]"); 
        return result.toString();
    }
    public boolean match(CRL crl) {
        if (!(crl instanceof X509CRL)) {
            return false;
        }
        X509CRL crlist = (X509CRL) crl;
        if ((issuerNames != null) &&
                !(issuerNames.contains(
                        crlist.getIssuerX500Principal().getName(
                            X500Principal.CANONICAL)))) {
            return false;
        }
        if ((minCRL != null) || (maxCRL != null)) {
            try {
                byte[] bytes = crlist.getExtensionValue("2.5.29.20"); 
                bytes = (byte[]) ASN1OctetString.getInstance().decode(bytes);
                BigInteger crlNumber = new BigInteger((byte[])
                        ASN1Integer.getInstance().decode(bytes));
                if ((minCRL != null) && (crlNumber.compareTo(minCRL) < 0)) {
                    return false;
                }
                if ((maxCRL != null) && (crlNumber.compareTo(maxCRL) > 0)) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }
        if (dateAndTime != -1) {
            Date thisUp = crlist.getThisUpdate();
            Date nextUp = crlist.getNextUpdate();
            if ((thisUp == null) || (nextUp == null)) {
                return false;
            }
            if ((dateAndTime < thisUp.getTime())
                                || (dateAndTime > nextUp.getTime())) {
                return false;
            }
        }
        return true;
    }
    public Object clone() {
        X509CRLSelector result;
		try {
			result = (X509CRLSelector) super.clone();
			if (issuerNames != null) {
	            result.issuerNames = new ArrayList<String>(issuerNames);
	        }
		} catch (CloneNotSupportedException e) {
			result = null;
		}        
        return result;
    }
}
