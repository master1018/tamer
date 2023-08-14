public class X509CertSelector implements CertSelector {
    private X509Certificate certificateEquals;
    private BigInteger serialNumber;
    private X500Principal issuer;
    private X500Principal subject;
    private byte[] subjectKeyIdentifier;
    private byte[] authorityKeyIdentifier;
    private Date certificateValid;
    private String subjectPublicKeyAlgID;
    private Date privateKeyValid;
    private byte[] subjectPublicKey;
    private boolean[] keyUsage;
    private Set extendedKeyUsage;
    private boolean matchAllNames = true;
    private int pathLen = -1;
    private List[] subjectAltNames;
    private NameConstraints nameConstraints;
    private Set policies;
    private ArrayList pathToNames;
    private PublicKey subjectPublicKeyImpl;
    private String issuerName;
    private byte[] issuerBytes;
    public X509CertSelector() {}
    public void setCertificate(X509Certificate certificate) {
        certificateEquals = certificate;
    }
    public X509Certificate getCertificate() {
        return certificateEquals;
    }
    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }
    public BigInteger getSerialNumber() {
        return serialNumber;
    }
    public void setIssuer(X500Principal issuer) {
        this.issuer = issuer;
        this.issuerName = null;
        this.issuerBytes = null;
    }
    public X500Principal getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuerName) throws IOException {
        if (issuerName == null) {
            this.issuer = null;
            this.issuerName = null;
            this.issuerBytes = null;
            return;
        }
        try {
            this.issuer = new X500Principal(issuerName);
            this.issuerName = issuerName;
            this.issuerBytes = null;
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }
    public String getIssuerAsString() {
        if (issuer == null) {
            return null;
        }
        if (issuerName == null) {
            issuerName = issuer.getName();
        }
        return issuerName;
    }
    public void setIssuer(byte[] issuerDN) throws IOException {
        if (issuerDN == null) {
            issuer = null;
            return;
        }
        try {
            issuer = new X500Principal(issuerDN);
            this.issuerName = null;
            this.issuerBytes = new byte[issuerDN.length];
            System.arraycopy(issuerDN, 0, this.issuerBytes, 0, issuerDN.length);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }
    public byte[] getIssuerAsBytes() throws IOException {
        if (issuer == null) {
            return null;
        }
        if (issuerBytes == null) {
            issuerBytes = issuer.getEncoded();
        }
        byte[] result = new byte[issuerBytes.length];
        System.arraycopy(issuerBytes, 0, result, 0, issuerBytes.length);
        return result;
    }
    public void setSubject(X500Principal subject) {
        this.subject = subject;
    }
    public X500Principal getSubject() {
        return subject;
    }
    public void setSubject(String subjectDN) throws IOException {
        if (subjectDN == null) {
            subject = null;
            return;
        }
        try {
            subject = new X500Principal(subjectDN);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }
    public String getSubjectAsString() {
        if (subject == null) {
            return null;
        }
        return subject.getName();
    }
    public void setSubject(byte[] subjectDN) throws IOException {
        if (subjectDN == null) {
            subject = null;
            return;
        }
        try {
            subject = new X500Principal(subjectDN);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }
    public byte[] getSubjectAsBytes() throws IOException {
        if (subject == null) {
            return null;
        }
        return subject.getEncoded();
    }
    public void setSubjectKeyIdentifier(byte[] subjectKeyIdentifier) {
        if (subjectKeyIdentifier == null) {
            this.subjectKeyIdentifier = null;
            return;
        }
        this.subjectKeyIdentifier = new byte[subjectKeyIdentifier.length];
        System.arraycopy(subjectKeyIdentifier, 0, this.subjectKeyIdentifier, 0,
                         subjectKeyIdentifier.length);
    }
    public byte[] getSubjectKeyIdentifier() {
        if (subjectKeyIdentifier == null) {
            return null;
        }
        byte[] res = new byte[subjectKeyIdentifier.length];
        System.arraycopy(subjectKeyIdentifier, 0, res, 0, res.length);
        return res;
    }
    public void setAuthorityKeyIdentifier(byte[] authorityKeyIdentifier) {
        if (authorityKeyIdentifier == null) {
            this.authorityKeyIdentifier = null;
            return;
        }
        this.authorityKeyIdentifier = new byte[authorityKeyIdentifier.length];
        System.arraycopy(authorityKeyIdentifier, 0,
                         this.authorityKeyIdentifier, 0,
                         authorityKeyIdentifier.length);
    }
    public byte[] getAuthorityKeyIdentifier() {
        if (authorityKeyIdentifier == null) {
            return null;
        }
        byte[] res = new byte[authorityKeyIdentifier.length];
        System.arraycopy(authorityKeyIdentifier, 0, res, 0, res.length);
        return res;
    }
    public void setCertificateValid(Date certificateValid) {
        this.certificateValid = (certificateValid == null)
                                ? null
                                : (Date) certificateValid.clone();
    }
    public Date getCertificateValid() {
        return (certificateValid == null)
                                ? null
                                : (Date) certificateValid.clone();
    }
    public void setPrivateKeyValid(Date privateKeyValid) {
        if (privateKeyValid == null) {
            this.privateKeyValid = null;
            return;
        }
        this.privateKeyValid = (Date) privateKeyValid.clone();
    }
    public Date getPrivateKeyValid() {
        if (privateKeyValid != null) {
            return (Date) privateKeyValid.clone();
        }
        return null;
    }
    private void checkOID(String oid) throws IOException {
        int beg = 0;
        int end = oid.indexOf('.', beg);
        try {
            int comp = Integer.parseInt(oid.substring(beg, end));
            beg = end + 1;
            if ((comp < 0) || (comp > 2)) {
                throw new IOException(Messages.getString("security.56", oid)); 
            }
            end = oid.indexOf('.', beg);
            comp = Integer.parseInt(oid.substring(beg, end));
            if ((comp < 0) || (comp > 39)) {
                throw new IOException(Messages.getString("security.56", oid)); 
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IOException(Messages.getString("security.56", oid)); 
        } catch (NumberFormatException e) {
            throw new IOException(Messages.getString("security.56", oid)); 
        }
    }
    public void setSubjectPublicKeyAlgID(String oid) throws IOException {
        if (oid == null) {
            subjectPublicKeyAlgID = null;
            return;
        }
        checkOID(oid);
        subjectPublicKeyAlgID = oid;
    }
    public String getSubjectPublicKeyAlgID() {
        return subjectPublicKeyAlgID;
    }
    public void setSubjectPublicKey(PublicKey key) {
        subjectPublicKey = (key == null) ? null : key.getEncoded();
        subjectPublicKeyImpl = key;
    }
    public void setSubjectPublicKey(byte[] key) throws IOException {
        if (key == null) {
            subjectPublicKey = null;
            subjectPublicKeyImpl = null;
            return;
        }
        subjectPublicKey = new byte[key.length];
        System.arraycopy(key, 0, subjectPublicKey, 0, key.length);
        subjectPublicKeyImpl = 
            ((SubjectPublicKeyInfo) SubjectPublicKeyInfo.ASN1.decode(key))
            .getPublicKey();
    }
    public PublicKey getSubjectPublicKey() {
        return subjectPublicKeyImpl;
    }
    public void setKeyUsage(boolean[] keyUsage) {
        if (keyUsage == null) {
            this.keyUsage = null;
            return;
        }
        this.keyUsage = new boolean[keyUsage.length];
        System.arraycopy(keyUsage, 0, this.keyUsage, 0, keyUsage.length);
    }
    public boolean[] getKeyUsage() {
        if (keyUsage == null) {
            return null;
        }
        boolean[] result = new boolean[keyUsage.length];
        System.arraycopy(keyUsage, 0, result, 0, keyUsage.length);
        return result;
    }
    public void setExtendedKeyUsage(Set<String> keyUsage)
                             throws IOException {
        extendedKeyUsage = null;
        if ((keyUsage == null) || (keyUsage.size() == 0)) {
            return;
        }
        HashSet key_u = new HashSet();
        Iterator it = keyUsage.iterator();
        while (it.hasNext()) {
            String usage = (String) it.next();
            checkOID(usage);
            key_u.add(usage);
        }
        extendedKeyUsage = Collections.unmodifiableSet(key_u);
    }
    public Set<String> getExtendedKeyUsage() {
        return extendedKeyUsage;
    }
    public void setMatchAllSubjectAltNames(boolean matchAllNames) {
        this.matchAllNames = matchAllNames;
    }
    public boolean getMatchAllSubjectAltNames() {
        return matchAllNames;
    }
    public void setSubjectAlternativeNames(Collection<List<?>> names)
                                    throws IOException {
        subjectAltNames = null;
        if ((names == null) || (names.size() == 0)) {
            return;
        }
        Iterator it = names.iterator();
        while (it.hasNext()) {
            List name = (List) it.next();
            int tag = ((Integer) name.get(0)).intValue();
            Object value = name.get(1);
            if (value instanceof String) {
                addSubjectAlternativeName(tag, (String) value);
            } else if (value instanceof byte[]) {
                addSubjectAlternativeName(tag, (byte[]) value);
            } else {
                throw new IOException(Messages.getString("security.57")); 
            }
        }
    }
    public void addSubjectAlternativeName(int tag, String name)
                                                       throws IOException {
        GeneralName alt_name = new GeneralName(tag, name);
        if (subjectAltNames == null) {
            subjectAltNames = new ArrayList[9];
        }
        if (subjectAltNames[tag] == null) {
            subjectAltNames[tag] = new ArrayList();
        }
        subjectAltNames[tag].add(alt_name);
    }
    public void addSubjectAlternativeName(int tag, byte[] name)
                                            throws IOException {
        GeneralName alt_name = new GeneralName(tag, name);
        if (subjectAltNames == null) {
            subjectAltNames = new ArrayList[9];
        }
        if (subjectAltNames[tag] == null) {
            subjectAltNames[tag] = new ArrayList();
        }
        subjectAltNames[tag].add(alt_name);
    }
    public Collection<List<?>> getSubjectAlternativeNames() {
        if (subjectAltNames == null) {
            return null;
        }
        ArrayList result = new ArrayList();
        for (int tag=0; tag<9; tag++) {
            if (subjectAltNames[tag] != null) {
                Integer teg = new Integer(tag);
                for (int name=0; name<subjectAltNames[tag].size(); name++) {
                    Object neim = subjectAltNames[tag].get(name);
                    if (neim instanceof byte[]) {
                        byte[] arr_neim = (byte[]) neim;
                        neim = new byte[arr_neim.length];
                        System.arraycopy(arr_neim, 0, neim, 0, arr_neim.length);
                    }
                    List list = new ArrayList(2);
                    list.add(teg);
                    list.add(neim);
                    result.add(list);
                }
            }
        }
        return result;
    }
    public void setNameConstraints(byte[] bytes) throws IOException {
        this.nameConstraints = (bytes == null)
            ? null
            : (NameConstraints) NameConstraints.ASN1.decode(bytes);
    }
    public byte[] getNameConstraints() {
        return (nameConstraints == null)
            ? null
            : nameConstraints.getEncoded();
    }
    public void setBasicConstraints(int pathLen) {
        if (pathLen < -2) {
            throw new IllegalArgumentException(Messages.getString("security.58")); 
        }
        this.pathLen = pathLen;
    }
    public int getBasicConstraints() {
        return pathLen;
    }
    public void setPolicy(Set<String> policies) throws IOException {
        if (policies == null) {
            this.policies = null;
            return;
        }
        HashSet pols = new HashSet(policies.size());
        Iterator it = policies.iterator();
        while (it.hasNext()) {
            String certPolicyId = (String) it.next();
            checkOID(certPolicyId);
            pols.add(certPolicyId);
        }
        this.policies = Collections.unmodifiableSet(pols);
    }
    public Set<String> getPolicy() {
        return policies;
    }
    public void setPathToNames(Collection<List<?>> names)
                                                        throws IOException {
        pathToNames = null;
        if ((names == null) || (names.size() == 0)) {
            return;
        }
        Iterator it = names.iterator();
        while (it.hasNext()) {
            List name = (List) it.next();
            int tag = ((Integer) name.get(0)).intValue();
            Object value = name.get(1);
            if (value instanceof String) {
                addPathToName(tag, (String) value);
            } else if (value instanceof byte[]) {
                addPathToName(tag, (byte[]) value);
            } else {
                throw new IOException(Messages.getString("security.57")); 
            }
        }
    }
    public void addPathToName(int type, String name) throws IOException {
        GeneralName path_name = new GeneralName(type, name);
        if (pathToNames == null) {
            pathToNames = new ArrayList();
        }
        pathToNames.add(path_name);
    }
    public void addPathToName(int type, byte[] name) throws IOException {
        GeneralName path_name= new GeneralName(type, name);
        if (pathToNames == null) {
            pathToNames = new ArrayList();
        }
        pathToNames.add(path_name);
    }
    public Collection<List<?>> getPathToNames() {
        if (pathToNames == null) {
            return null;
        }
        ArrayList result = new ArrayList();
        Iterator it = pathToNames.iterator();
        while (it.hasNext()) {
            GeneralName name = (GeneralName) it.next();
            result.add(name.getAsList());
        }
        return result;
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("X509CertSelector: \n["); 
        if (this.certificateEquals != null) {
            result.append("\n  certificateEquals: " + certificateEquals); 
        }
        if (this.serialNumber != null) {
        }
        if (this.issuer != null) {
            result.append("\n  issuer: " + issuer); 
        }
        if (this.subject != null) {
            result.append("\n  subject: " + subject); 
        }
        if (this.subjectKeyIdentifier != null) {
            result.append("\n  subjectKeyIdentifier: " 
                    + getBytesAsString(subjectKeyIdentifier));
        }
        if (this.authorityKeyIdentifier != null) {
            result.append("\n  authorityKeyIdentifier: " 
                    + getBytesAsString(authorityKeyIdentifier));
        }
        if (this.certificateValid != null) {
            result.append("\n  certificateValid: " + certificateValid); 
        }
        if (this.subjectPublicKeyAlgID != null) {
            result.append("\n  subjectPublicKeyAlgID: " 
                    + subjectPublicKeyAlgID);
        }
        if (this.privateKeyValid != null) {
            result.append("\n  privateKeyValid: " + privateKeyValid); 
        }
        if (this.subjectPublicKey != null) {
            result.append("\n  subjectPublicKey: " 
                    + getBytesAsString(subjectPublicKey));
        }
        if (this.keyUsage != null) {
            result.append("\n  keyUsage: \n  ["); 
            String[] kuNames = new String[] {
                "digitalSignature", "nonRepudiation", "keyEncipherment", 
                "dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign", 
                "encipherOnly", "decipherOnly" 
            };
            for (int i=0; i<9; i++) {
                if (keyUsage[i]) {
                    result.append("\n    " + kuNames[i]); 
                }
            }
            result.append("\n  ]"); 
        }
        if (this.extendedKeyUsage != null) {
            result.append("\n  extendedKeyUsage: " 
                    + extendedKeyUsage.toString());
        }
        result.append("\n  matchAllNames: " + matchAllNames); 
        result.append("\n  pathLen: " + pathLen); 
        if (this.subjectAltNames != null) {
            result.append("\n  subjectAltNames:  \n  ["); 
            for (int i=0; i<9; i++) {
                List names = this.subjectAltNames[i];
                if (names != null) {
                    int size = names.size();
                    for (int j=0; j<size; j++) {
                        result.append("\n    " 
                            + ((GeneralName)names.get(j)).toString());
                    }
                }
            }
            result.append("\n  ]"); 
        }
        if (this.nameConstraints != null) {
        }
        if (this.policies != null) {
            result.append("\n  policies: " + policies.toString()); 
        }
        if (this.pathToNames != null) {
            result.append("\n  pathToNames:  \n  ["); 
            int size = pathToNames.size();
            for (int i = 0; i < size; i++) {
                result.append("\n    " 
                    + ((GeneralName)pathToNames.get(i)).toString());
            }
        }
        result.append("\n]"); 
        return result.toString();
    }
    private String getBytesAsString(byte[] data) {
        String result = ""; 
        for (int i=0; i<data.length; i++) {
            String tail = Integer.toHexString(0x00ff & data[i]);
            if (tail.length() == 1) {
                tail = "0" + tail; 
            }
            result += tail + " "; 
        }
        return result;
    }
    private byte[] getExtensionValue(X509Certificate cert, String oid) {
        try {
            byte[] bytes = cert.getExtensionValue(oid);
            if (bytes == null) {
                return null;
            }
            return (byte[]) ASN1OctetString.getInstance().decode(bytes);
        } catch (IOException e) {
            return null;
        }
    }
    public boolean match(Certificate certificate) {
        if (! (certificate instanceof X509Certificate)) {
            return false;
        }
        X509Certificate cert = (X509Certificate) certificate;
        if ((certificateEquals != null) &&
            !certificateEquals.equals(cert)) {
            return false;
        }
        if ((serialNumber != null) &&
            !serialNumber.equals(cert.getSerialNumber())) {
            return false;
        }
        if ((issuer != null) &&
            !issuer.equals(cert.getIssuerX500Principal())) {
            return false;
        }
        if ((subject != null) &&
            !subject.equals(cert.getSubjectX500Principal())) {
            return false;
        }
        if ((subjectKeyIdentifier != null) &&
            !Arrays.equals(subjectKeyIdentifier,
                           getExtensionValue(cert, "2.5.29.14"))) { 
            return false;
        }
        if ((authorityKeyIdentifier != null) &&
            !Arrays.equals(authorityKeyIdentifier,
                           getExtensionValue(cert, "2.5.29.35"))) { 
            return false;
        }
        if (certificateValid != null) {
            try {
                cert.checkValidity(certificateValid);
            } catch(CertificateExpiredException e) {
                return false;
            } catch(CertificateNotYetValidException e) {
                return false;
            }
        }
        if (privateKeyValid != null) {
            try {
                byte[] bytes = getExtensionValue(cert, "2.5.29.16"); 
                if (bytes == null) {
                    return false;
                }
                PrivateKeyUsagePeriod pkup = (PrivateKeyUsagePeriod) 
                                    PrivateKeyUsagePeriod.ASN1.decode(bytes);
                Date notBefore = pkup.getNotBefore();
                Date notAfter = pkup.getNotAfter();
                if ((notBefore == null) && (notAfter == null)) {
                    return false;
                }
                if ((notBefore != null)
                    && notBefore.compareTo(privateKeyValid) > 0) {
                    return false;
                }
                if ((notAfter != null)
                    && notAfter.compareTo(privateKeyValid) < 0) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }
        if (subjectPublicKeyAlgID  != null) {
            try {
                byte[] encoding = cert.getPublicKey().getEncoded();
                AlgorithmIdentifier ai = ((SubjectPublicKeyInfo) 
                        SubjectPublicKeyInfo.ASN1.decode(encoding))
                        .getAlgorithmIdentifier();
                if (!subjectPublicKeyAlgID.equals(ai.getAlgorithm())) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (subjectPublicKey != null) {
            if (!Arrays.equals(subjectPublicKey,
                               cert.getPublicKey().getEncoded())) {
                return false;
            }
        }
        if (keyUsage != null) {
            boolean[] ku = cert.getKeyUsage();
            if (ku != null) {
                int i = 0;
                int min_length = (ku.length < keyUsage.length) ? ku.length
                        : keyUsage.length;
                for (; i < min_length; i++) {
                    if (keyUsage[i] && !ku[i]) {
                        return false;
                    }
                }
                for (; i<keyUsage.length; i++) {
                    if (keyUsage[i]) {
                        return false;
                    }
                }
            }
        }
        if (extendedKeyUsage != null) {
            try {
                List keyUsage = cert.getExtendedKeyUsage();
                if (keyUsage != null) {
                    if (!keyUsage.containsAll(extendedKeyUsage)) {
                        return false;
                    }
                }
            } catch (CertificateParsingException e) {
                return false;
            }
        }
        if (pathLen != -1) {
            int p_len = cert.getBasicConstraints();
            if ((pathLen < 0) && (p_len >= 0)) {
                return false;
            }
            if ((pathLen > 0) && (pathLen > p_len)) {
                return false;
            }
        }
        if (subjectAltNames != null) {
            PASSED:
            try {
                byte[] bytes = getExtensionValue(cert, "2.5.29.17"); 
                if (bytes == null) {
                    return false;
                }
                List sans = ((GeneralNames) GeneralNames.ASN1.decode(bytes))
                            .getNames();
                if ((sans == null) || (sans.size() == 0)) {
                    return false;
                }
                boolean[][] map = new boolean[9][];
                for (int i=0; i<9; i++) {
                    map[i] = (subjectAltNames[i] == null)
                                ? new boolean[0]
                                : new boolean[subjectAltNames[i].size()];
                }
                Iterator it = sans.iterator();
                while (it.hasNext()) {
                    GeneralName name = (GeneralName) it.next();
                    int tag = name.getTag();
                    for (int i=0; i<map[tag].length; i++) {
                        if (((GeneralName) subjectAltNames[tag].get(i))
                                                            .equals(name)) {
                            if (!matchAllNames) {
                                break PASSED;
                            }
                            map[tag][i] = true;
                        }
                    }
                }
                if (!matchAllNames) {
                    return false;
                }
                for (int tag=0; tag<9; tag++) {
                    for (int name=0; name<map[tag].length; name++) {
                        if (!map[tag][name]) {
                            return false;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (nameConstraints != null) {
            if (!nameConstraints.isAcceptable(cert)) {
                return false;
            }
        }
        if (policies != null) {
            byte[] bytes = getExtensionValue(cert, "2.5.29.32"); 
            if (bytes == null) {
                return false;
            }
            if (policies.size() == 0) {
                return true;
            }
            PASSED:
            try {
                List policyInformations = ((CertificatePolicies) 
                        CertificatePolicies.ASN1.decode(bytes))
                        .getPolicyInformations();
                Iterator it = policyInformations.iterator();
                while (it.hasNext()) {
                    if (policies.contains(((PolicyInformation) it.next())
                                          .getPolicyIdentifier())) {
                        break PASSED;
                    }
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        if (pathToNames != null) {
            byte[] bytes = getExtensionValue(cert, "2.5.29.30"); 
            if (bytes != null) {
                NameConstraints nameConstraints;
                try {
                    nameConstraints =
                        (NameConstraints) NameConstraints.ASN1.decode(bytes);
                } catch (IOException e) {
                    return false;
                }
                if (!nameConstraints.isAcceptable(pathToNames)) {
                    return false;
                }
            }
        }
        return true;
    }
    public Object clone() {
        X509CertSelector result;
		try {
			result = (X509CertSelector) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
        if (this.subjectKeyIdentifier != null) {
            result.subjectKeyIdentifier =
                new byte[this.subjectKeyIdentifier.length];
            System.arraycopy(this.subjectKeyIdentifier, 0,
                    result.subjectKeyIdentifier, 0,
                    this.subjectKeyIdentifier.length);
        }
        if (this.authorityKeyIdentifier != null) {
            result.authorityKeyIdentifier =
                new byte[this.authorityKeyIdentifier.length];
            System.arraycopy(this.authorityKeyIdentifier, 0,
                    result.authorityKeyIdentifier, 0,
                    this.authorityKeyIdentifier.length);
        }
        if (this.subjectPublicKey != null) {
            result.subjectPublicKey = new byte[this.subjectPublicKey.length];
            System.arraycopy(this.subjectPublicKey, 0, result.subjectPublicKey,
                    0, this.subjectPublicKey.length);
        }
        if (this.keyUsage != null) {
            result.keyUsage = new boolean[this.keyUsage.length];
            System.arraycopy(this.keyUsage, 0, result.keyUsage, 0,
                    this.keyUsage.length);
        }
        result.extendedKeyUsage = (this.extendedKeyUsage == null)
            ? null
            : new HashSet(this.extendedKeyUsage);
        if (this.subjectAltNames != null) {
            result.subjectAltNames = new ArrayList[9];
            for (int i=0; i<9; i++) {
                if (this.subjectAltNames[i] != null) {
                    result.subjectAltNames[i] =
                        new ArrayList(this.subjectAltNames[i]);
                }
            }
        }
        result.policies = (this.policies == null)
            ? null
            : new HashSet(this.policies);
        result.pathToNames = (this.pathToNames == null)
            ? null
            : new ArrayList(this.pathToNames);
        return result;
    }
}
