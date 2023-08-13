public class X509CRLSelector implements CRLSelector {
    static {
        CertPathHelperImpl.initialize();
    }
    private static final Debug debug = Debug.getInstance("certpath");
    private HashSet<Object> issuerNames;
    private HashSet<X500Principal> issuerX500Principals;
    private BigInteger minCRL;
    private BigInteger maxCRL;
    private Date dateAndTime;
    private X509Certificate certChecking;
    private long skew = 0;
    public X509CRLSelector() {}
    public void setIssuers(Collection<X500Principal> issuers) {
        if ((issuers == null) || issuers.isEmpty()) {
            issuerNames = null;
            issuerX500Principals = null;
        } else {
            issuerX500Principals = new HashSet<X500Principal>(issuers);
            issuerNames = new HashSet<Object>();
            for (X500Principal p : issuerX500Principals) {
                issuerNames.add(p.getEncoded());
            }
        }
    }
    public void setIssuerNames(Collection<?> names) throws IOException {
        if (names == null || names.size() == 0) {
            issuerNames = null;
            issuerX500Principals = null;
        } else {
            HashSet<Object> tempNames = cloneAndCheckIssuerNames(names);
            issuerX500Principals = parseIssuerNames(tempNames);
            issuerNames = tempNames;
        }
    }
    public void addIssuer(X500Principal issuer) {
        addIssuerNameInternal(issuer.getEncoded(), issuer);
    }
    public void addIssuerName(String name) throws IOException {
        addIssuerNameInternal(name, new X500Name(name).asX500Principal());
    }
    public void addIssuerName(byte[] name) throws IOException {
        addIssuerNameInternal(name.clone(), new X500Name(name).asX500Principal());
    }
    private void addIssuerNameInternal(Object name, X500Principal principal) {
        if (issuerNames == null) {
            issuerNames = new HashSet<Object>();
        }
        if (issuerX500Principals == null) {
            issuerX500Principals = new HashSet<X500Principal>();
        }
        issuerNames.add(name);
        issuerX500Principals.add(principal);
    }
    private static HashSet<Object> cloneAndCheckIssuerNames(Collection<?> names)
        throws IOException
    {
        HashSet<Object> namesCopy = new HashSet<Object>();
        Iterator<?> i = names.iterator();
        while (i.hasNext()) {
            Object nameObject = i.next();
            if (!(nameObject instanceof byte []) &&
                !(nameObject instanceof String))
                throw new IOException("name not byte array or String");
            if (nameObject instanceof byte [])
                namesCopy.add(((byte []) nameObject).clone());
            else
                namesCopy.add(nameObject);
        }
        return(namesCopy);
    }
    private static HashSet<Object> cloneIssuerNames(Collection<Object> names) {
        try {
            return cloneAndCheckIssuerNames(names);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    private static HashSet<X500Principal> parseIssuerNames(Collection<Object> names)
    throws IOException {
        HashSet<X500Principal> x500Principals = new HashSet<X500Principal>();
        for (Iterator<Object> t = names.iterator(); t.hasNext(); ) {
            Object nameObject = t.next();
            if (nameObject instanceof String) {
                x500Principals.add(new X500Name((String)nameObject).asX500Principal());
            } else {
                try {
                    x500Principals.add(new X500Principal((byte[])nameObject));
                } catch (IllegalArgumentException e) {
                    throw (IOException)new IOException("Invalid name").initCause(e);
                }
            }
        }
        return x500Principals;
    }
    public void setMinCRLNumber(BigInteger minCRL) {
        this.minCRL = minCRL;
    }
    public void setMaxCRLNumber(BigInteger maxCRL) {
        this.maxCRL = maxCRL;
    }
    public void setDateAndTime(Date dateAndTime) {
        if (dateAndTime == null)
            this.dateAndTime = null;
        else
            this.dateAndTime = new Date(dateAndTime.getTime());
        this.skew = 0;
    }
    void setDateAndTime(Date dateAndTime, long skew) {
        this.dateAndTime =
            (dateAndTime == null ? null : new Date(dateAndTime.getTime()));
        this.skew = skew;
    }
    public void setCertificateChecking(X509Certificate cert) {
        certChecking = cert;
    }
    public Collection<X500Principal> getIssuers() {
        if (issuerX500Principals == null) {
            return null;
        }
        return Collections.unmodifiableCollection(issuerX500Principals);
    }
    public Collection<Object> getIssuerNames() {
        if (issuerNames == null) {
            return null;
        }
        return cloneIssuerNames(issuerNames);
    }
    public BigInteger getMinCRL() {
        return minCRL;
    }
    public BigInteger getMaxCRL() {
        return maxCRL;
    }
    public Date getDateAndTime() {
        if (dateAndTime == null)
            return null;
        return (Date) dateAndTime.clone();
    }
    public X509Certificate getCertificateChecking() {
        return certChecking;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("X509CRLSelector: [\n");
        if (issuerNames != null) {
            sb.append("  IssuerNames:\n");
            Iterator<Object> i = issuerNames.iterator();
            while (i.hasNext())
                sb.append("    " + i.next() + "\n");
        }
        if (minCRL != null)
            sb.append("  minCRLNumber: " + minCRL + "\n");
        if (maxCRL != null)
            sb.append("  maxCRLNumber: " + maxCRL + "\n");
        if (dateAndTime != null)
            sb.append("  dateAndTime: " + dateAndTime + "\n");
        if (certChecking != null)
            sb.append("  Certificate being checked: " + certChecking + "\n");
        sb.append("]");
        return sb.toString();
    }
    public boolean match(CRL crl) {
        if (!(crl instanceof X509CRL)) {
            return false;
        }
        X509CRL xcrl = (X509CRL)crl;
        if (issuerNames != null) {
            X500Principal issuer = xcrl.getIssuerX500Principal();
            Iterator<X500Principal> i = issuerX500Principals.iterator();
            boolean found = false;
            while (!found && i.hasNext()) {
                if (i.next().equals(issuer)) {
                    found = true;
                }
            }
            if (!found) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: issuer DNs "
                        + "don't match");
                }
                return false;
            }
        }
        if ((minCRL != null) || (maxCRL != null)) {
            byte[] crlNumExtVal = xcrl.getExtensionValue("2.5.29.20");
            if (crlNumExtVal == null) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: no CRLNumber");
                }
            }
            BigInteger crlNum;
            try {
                DerInputStream in = new DerInputStream(crlNumExtVal);
                byte[] encoded = in.getOctetString();
                CRLNumberExtension crlNumExt =
                    new CRLNumberExtension(Boolean.FALSE, encoded);
                crlNum = (BigInteger)crlNumExt.get(CRLNumberExtension.NUMBER);
            } catch (IOException ex) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: exception in "
                        + "decoding CRL number");
                }
                return false;
            }
            if (minCRL != null) {
                if (crlNum.compareTo(minCRL) < 0) {
                    if (debug != null) {
                        debug.println("X509CRLSelector.match: CRLNumber too small");
                    }
                    return false;
                }
            }
            if (maxCRL != null) {
                if (crlNum.compareTo(maxCRL) > 0) {
                    if (debug != null) {
                        debug.println("X509CRLSelector.match: CRLNumber too large");
                    }
                    return false;
                }
            }
        }
        if (dateAndTime != null) {
            Date crlThisUpdate = xcrl.getThisUpdate();
            Date nextUpdate = xcrl.getNextUpdate();
            if (nextUpdate == null) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: nextUpdate null");
                }
                return false;
            }
            Date nowPlusSkew = dateAndTime;
            Date nowMinusSkew = dateAndTime;
            if (skew > 0) {
                nowPlusSkew = new Date(dateAndTime.getTime() + skew);
                nowMinusSkew = new Date(dateAndTime.getTime() - skew);
            }
            if (nowMinusSkew.after(nextUpdate)
                || nowPlusSkew.before(crlThisUpdate)) {
                if (debug != null) {
                    debug.println("X509CRLSelector.match: update out of range");
                }
                return false;
            }
        }
        return true;
    }
    public Object clone() {
        try {
            X509CRLSelector copy = (X509CRLSelector)super.clone();
            if (issuerNames != null) {
                copy.issuerNames =
                        new HashSet<Object>(issuerNames);
                copy.issuerX500Principals =
                        new HashSet<X500Principal>(issuerX500Principals);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
