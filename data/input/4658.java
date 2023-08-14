public class X509CertInfo implements CertAttrSet<String> {
    public static final String IDENT = "x509.info";
    public static final String NAME = "info";
    public static final String VERSION = CertificateVersion.NAME;
    public static final String SERIAL_NUMBER = CertificateSerialNumber.NAME;
    public static final String ALGORITHM_ID = CertificateAlgorithmId.NAME;
    public static final String ISSUER = CertificateIssuerName.NAME;
    public static final String VALIDITY = CertificateValidity.NAME;
    public static final String SUBJECT = CertificateSubjectName.NAME;
    public static final String KEY = CertificateX509Key.NAME;
    public static final String ISSUER_ID = CertificateIssuerUniqueIdentity.NAME;
    public static final String SUBJECT_ID = CertificateSubjectUniqueIdentity.NAME;
    public static final String EXTENSIONS = CertificateExtensions.NAME;
    protected CertificateVersion version = new CertificateVersion();
    protected CertificateSerialNumber   serialNum = null;
    protected CertificateAlgorithmId    algId = null;
    protected CertificateIssuerName     issuer = null;
    protected CertificateValidity       interval = null;
    protected CertificateSubjectName    subject = null;
    protected CertificateX509Key        pubKey = null;
    protected CertificateIssuerUniqueIdentity   issuerUniqueId = null;
    protected CertificateSubjectUniqueIdentity  subjectUniqueId = null;
    protected CertificateExtensions     extensions = null;
    private static final int ATTR_VERSION = 1;
    private static final int ATTR_SERIAL = 2;
    private static final int ATTR_ALGORITHM = 3;
    private static final int ATTR_ISSUER = 4;
    private static final int ATTR_VALIDITY = 5;
    private static final int ATTR_SUBJECT = 6;
    private static final int ATTR_KEY = 7;
    private static final int ATTR_ISSUER_ID = 8;
    private static final int ATTR_SUBJECT_ID = 9;
    private static final int ATTR_EXTENSIONS = 10;
    private byte[]      rawCertInfo = null;
    private static final Map<String,Integer> map = new HashMap<String,Integer>();
    static {
        map.put(VERSION, Integer.valueOf(ATTR_VERSION));
        map.put(SERIAL_NUMBER, Integer.valueOf(ATTR_SERIAL));
        map.put(ALGORITHM_ID, Integer.valueOf(ATTR_ALGORITHM));
        map.put(ISSUER, Integer.valueOf(ATTR_ISSUER));
        map.put(VALIDITY, Integer.valueOf(ATTR_VALIDITY));
        map.put(SUBJECT, Integer.valueOf(ATTR_SUBJECT));
        map.put(KEY, Integer.valueOf(ATTR_KEY));
        map.put(ISSUER_ID, Integer.valueOf(ATTR_ISSUER_ID));
        map.put(SUBJECT_ID, Integer.valueOf(ATTR_SUBJECT_ID));
        map.put(EXTENSIONS, Integer.valueOf(ATTR_EXTENSIONS));
    }
    public X509CertInfo() { }
    public X509CertInfo(byte[] cert) throws CertificateParsingException {
        try {
            DerValue    in = new DerValue(cert);
            parse(in);
        } catch (IOException e) {
            CertificateParsingException parseException =
                        new CertificateParsingException(e.toString());
            parseException.initCause(e);
            throw parseException;
        }
    }
    public X509CertInfo(DerValue derVal) throws CertificateParsingException {
        try {
            parse(derVal);
        } catch (IOException e) {
            CertificateParsingException parseException =
                        new CertificateParsingException(e.toString());
            parseException.initCause(e);
            throw parseException;
        }
    }
    public void encode(OutputStream out)
    throws CertificateException, IOException {
        if (rawCertInfo == null) {
            DerOutputStream tmp = new DerOutputStream();
            emit(tmp);
            rawCertInfo = tmp.toByteArray();
        }
        out.write(rawCertInfo.clone());
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(VERSION);
        elements.addElement(SERIAL_NUMBER);
        elements.addElement(ALGORITHM_ID);
        elements.addElement(ISSUER);
        elements.addElement(VALIDITY);
        elements.addElement(SUBJECT);
        elements.addElement(KEY);
        elements.addElement(ISSUER_ID);
        elements.addElement(SUBJECT_ID);
        elements.addElement(EXTENSIONS);
        return elements.elements();
    }
    public String getName() {
        return(NAME);
    }
    public byte[] getEncodedInfo() throws CertificateEncodingException {
        try {
            if (rawCertInfo == null) {
                DerOutputStream tmp = new DerOutputStream();
                emit(tmp);
                rawCertInfo = tmp.toByteArray();
            }
            return rawCertInfo.clone();
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        } catch (CertificateException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }
    public boolean equals(Object other) {
        if (other instanceof X509CertInfo) {
            return equals((X509CertInfo) other);
        } else {
            return false;
        }
    }
    public boolean equals(X509CertInfo other) {
        if (this == other) {
            return(true);
        } else if (rawCertInfo == null || other.rawCertInfo == null) {
            return(false);
        } else if (rawCertInfo.length != other.rawCertInfo.length) {
            return(false);
        }
        for (int i = 0; i < rawCertInfo.length; i++) {
            if (rawCertInfo[i] != other.rawCertInfo[i]) {
                return(false);
            }
        }
        return(true);
    }
    public int hashCode() {
        int     retval = 0;
        for (int i = 1; i < rawCertInfo.length; i++) {
            retval += rawCertInfo[i] * i;
        }
        return(retval);
    }
    public String toString() {
        if (subject == null || pubKey == null || interval == null
            || issuer == null || algId == null || serialNum == null) {
                throw new NullPointerException("X.509 cert is incomplete");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("  " + version.toString() + "\n");
        sb.append("  Subject: " + subject.toString() + "\n");
        sb.append("  Signature Algorithm: " + algId.toString() + "\n");
        sb.append("  Key:  " + pubKey.toString() + "\n");
        sb.append("  " + interval.toString() + "\n");
        sb.append("  Issuer: " + issuer.toString() + "\n");
        sb.append("  " + serialNum.toString() + "\n");
        if (issuerUniqueId != null) {
            sb.append("  Issuer Id:\n" + issuerUniqueId.toString() + "\n");
        }
        if (subjectUniqueId != null) {
            sb.append("  Subject Id:\n" + subjectUniqueId.toString() + "\n");
        }
        if (extensions != null) {
            Collection allExts = extensions.getAllExtensions();
            Object[] objs = allExts.toArray();
            sb.append("\nCertificate Extensions: " + objs.length);
            for (int i = 0; i < objs.length; i++) {
                sb.append("\n[" + (i+1) + "]: ");
                Extension ext = (Extension)objs[i];
                try {
                    if (OIDMap.getClass(ext.getExtensionId()) == null) {
                        sb.append(ext.toString());
                        byte[] extValue = ext.getExtensionValue();
                        if (extValue != null) {
                            DerOutputStream out = new DerOutputStream();
                            out.putOctetString(extValue);
                            extValue = out.toByteArray();
                            HexDumpEncoder enc = new HexDumpEncoder();
                            sb.append("Extension unknown: "
                                      + "DER encoded OCTET string =\n"
                                      + enc.encodeBuffer(extValue) + "\n");
                        }
                    } else
                        sb.append(ext.toString()); 
                } catch (Exception e) {
                    sb.append(", Error parsing this extension");
                }
            }
            Map<String,Extension> invalid = extensions.getUnparseableExtensions();
            if (invalid.isEmpty() == false) {
                sb.append("\nUnparseable certificate extensions: " + invalid.size());
                int i = 1;
                for (Extension ext : invalid.values()) {
                    sb.append("\n[" + (i++) + "]: ");
                    sb.append(ext);
                }
            }
        }
        sb.append("\n]");
        return sb.toString();
    }
    public void set(String name, Object val)
    throws CertificateException, IOException {
        X509AttributeName attrName = new X509AttributeName(name);
        int attr = attributeMap(attrName.getPrefix());
        if (attr == 0) {
            throw new CertificateException("Attribute name not recognized: "
                                           + name);
        }
        rawCertInfo = null;
        String suffix = attrName.getSuffix();
        switch (attr) {
        case ATTR_VERSION:
            if (suffix == null) {
                setVersion(val);
            } else {
                version.set(suffix, val);
            }
            break;
        case ATTR_SERIAL:
            if (suffix == null) {
                setSerialNumber(val);
            } else {
                serialNum.set(suffix, val);
            }
            break;
        case ATTR_ALGORITHM:
            if (suffix == null) {
                setAlgorithmId(val);
            } else {
                algId.set(suffix, val);
            }
            break;
        case ATTR_ISSUER:
            if (suffix == null) {
                setIssuer(val);
            } else {
                issuer.set(suffix, val);
            }
            break;
        case ATTR_VALIDITY:
            if (suffix == null) {
                setValidity(val);
            } else {
                interval.set(suffix, val);
            }
            break;
        case ATTR_SUBJECT:
            if (suffix == null) {
                setSubject(val);
            } else {
                subject.set(suffix, val);
            }
            break;
        case ATTR_KEY:
            if (suffix == null) {
                setKey(val);
            } else {
                pubKey.set(suffix, val);
            }
            break;
        case ATTR_ISSUER_ID:
            if (suffix == null) {
                setIssuerUniqueId(val);
            } else {
                issuerUniqueId.set(suffix, val);
            }
            break;
        case ATTR_SUBJECT_ID:
            if (suffix == null) {
                setSubjectUniqueId(val);
            } else {
                subjectUniqueId.set(suffix, val);
            }
            break;
        case ATTR_EXTENSIONS:
            if (suffix == null) {
                setExtensions(val);
            } else {
                if (extensions == null)
                    extensions = new CertificateExtensions();
                extensions.set(suffix, val);
            }
            break;
        }
    }
    public void delete(String name)
    throws CertificateException, IOException {
        X509AttributeName attrName = new X509AttributeName(name);
        int attr = attributeMap(attrName.getPrefix());
        if (attr == 0) {
            throw new CertificateException("Attribute name not recognized: "
                                           + name);
        }
        rawCertInfo = null;
        String suffix = attrName.getSuffix();
        switch (attr) {
        case ATTR_VERSION:
            if (suffix == null) {
                version = null;
            } else {
                version.delete(suffix);
            }
            break;
        case (ATTR_SERIAL):
            if (suffix == null) {
                serialNum = null;
            } else {
                serialNum.delete(suffix);
            }
            break;
        case (ATTR_ALGORITHM):
            if (suffix == null) {
                algId = null;
            } else {
                algId.delete(suffix);
            }
            break;
        case (ATTR_ISSUER):
            if (suffix == null) {
                issuer = null;
            } else {
                issuer.delete(suffix);
            }
            break;
        case (ATTR_VALIDITY):
            if (suffix == null) {
                interval = null;
            } else {
                interval.delete(suffix);
            }
            break;
        case (ATTR_SUBJECT):
            if (suffix == null) {
                subject = null;
            } else {
                subject.delete(suffix);
            }
            break;
        case (ATTR_KEY):
            if (suffix == null) {
                pubKey = null;
            } else {
                pubKey.delete(suffix);
            }
            break;
        case (ATTR_ISSUER_ID):
            if (suffix == null) {
                issuerUniqueId = null;
            } else {
                issuerUniqueId.delete(suffix);
            }
            break;
        case (ATTR_SUBJECT_ID):
            if (suffix == null) {
                subjectUniqueId = null;
            } else {
                subjectUniqueId.delete(suffix);
            }
            break;
        case (ATTR_EXTENSIONS):
            if (suffix == null) {
                extensions = null;
            } else {
                if (extensions != null)
                   extensions.delete(suffix);
            }
            break;
        }
    }
    public Object get(String name)
    throws CertificateException, IOException {
        X509AttributeName attrName = new X509AttributeName(name);
        int attr = attributeMap(attrName.getPrefix());
        if (attr == 0) {
            throw new CertificateParsingException(
                          "Attribute name not recognized: " + name);
        }
        String suffix = attrName.getSuffix();
        switch (attr) { 
        case (ATTR_EXTENSIONS):
            if (suffix == null) {
                return(extensions);
            } else {
                if (extensions == null) {
                    return null;
                } else {
                    return(extensions.get(suffix));
                }
            }
        case (ATTR_SUBJECT):
            if (suffix == null) {
                return(subject);
            } else {
                return(subject.get(suffix));
            }
        case (ATTR_ISSUER):
            if (suffix == null) {
                return(issuer);
            } else {
                return(issuer.get(suffix));
            }
        case (ATTR_KEY):
            if (suffix == null) {
                return(pubKey);
            } else {
                return(pubKey.get(suffix));
            }
        case (ATTR_ALGORITHM):
            if (suffix == null) {
                return(algId);
            } else {
                return(algId.get(suffix));
            }
        case (ATTR_VALIDITY):
            if (suffix == null) {
                return(interval);
            } else {
                return(interval.get(suffix));
            }
        case (ATTR_VERSION):
            if (suffix == null) {
                return(version);
            } else {
                return(version.get(suffix));
            }
        case (ATTR_SERIAL):
            if (suffix == null) {
                return(serialNum);
            } else {
                return(serialNum.get(suffix));
            }
        case (ATTR_ISSUER_ID):
            if (suffix == null) {
                return(issuerUniqueId);
            } else {
                if (issuerUniqueId == null)
                    return null;
                else
                    return(issuerUniqueId.get(suffix));
            }
        case (ATTR_SUBJECT_ID):
            if (suffix == null) {
                return(subjectUniqueId);
            } else {
                if (subjectUniqueId == null)
                    return null;
                else
                    return(subjectUniqueId.get(suffix));
            }
        }
        return null;
    }
    private void parse(DerValue val)
    throws CertificateParsingException, IOException {
        DerInputStream  in;
        DerValue        tmp;
        if (val.tag != DerValue.tag_Sequence) {
            throw new CertificateParsingException("signed fields invalid");
        }
        rawCertInfo = val.toByteArray();
        in = val.data;
        tmp = in.getDerValue();
        if (tmp.isContextSpecific((byte)0)) {
            version = new CertificateVersion(tmp);
            tmp = in.getDerValue();
        }
        serialNum = new CertificateSerialNumber(tmp);
        algId = new CertificateAlgorithmId(in);
        issuer = new CertificateIssuerName(in);
        X500Name issuerDN = (X500Name)issuer.get(CertificateIssuerName.DN_NAME);
        if (issuerDN.isEmpty()) {
            throw new CertificateParsingException(
                "Empty issuer DN not allowed in X509Certificates");
        }
        interval = new CertificateValidity(in);
        subject = new CertificateSubjectName(in);
        X500Name subjectDN = (X500Name)subject.get(CertificateSubjectName.DN_NAME);
        if ((version.compare(CertificateVersion.V1) == 0) &&
                subjectDN.isEmpty()) {
            throw new CertificateParsingException(
                      "Empty subject DN not allowed in v1 certificate");
        }
        pubKey = new CertificateX509Key(in);
        if (in.available() != 0) {
            if (version.compare(CertificateVersion.V1) == 0) {
                throw new CertificateParsingException(
                          "no more data allowed for version 1 certificate");
            }
        } else {
            return;
        }
        tmp = in.getDerValue();
        if (tmp.isContextSpecific((byte)1)) {
            issuerUniqueId = new CertificateIssuerUniqueIdentity(tmp);
            if (in.available() == 0)
                return;
            tmp = in.getDerValue();
        }
        if (tmp.isContextSpecific((byte)2)) {
            subjectUniqueId = new CertificateSubjectUniqueIdentity(tmp);
            if (in.available() == 0)
                return;
            tmp = in.getDerValue();
        }
        if (version.compare(CertificateVersion.V3) != 0) {
            throw new CertificateParsingException(
                      "Extensions not allowed in v2 certificate");
        }
        if (tmp.isConstructed() && tmp.isContextSpecific((byte)3)) {
            extensions = new CertificateExtensions(tmp.data);
        }
        verifyCert(subject, extensions);
    }
    private void verifyCert(CertificateSubjectName subject,
        CertificateExtensions extensions)
        throws CertificateParsingException, IOException {
        X500Name subjectDN = (X500Name)subject.get(CertificateSubjectName.DN_NAME);
        if (subjectDN.isEmpty()) {
            if (extensions == null) {
                throw new CertificateParsingException("X.509 Certificate is " +
                        "incomplete: subject field is empty, and certificate " +
                        "has no extensions");
            }
            SubjectAlternativeNameExtension subjectAltNameExt = null;
            SubjectAlternativeNameExtension extValue = null;
            GeneralNames names = null;
            try {
                subjectAltNameExt = (SubjectAlternativeNameExtension)
                        extensions.get(SubjectAlternativeNameExtension.NAME);
                names = (GeneralNames) subjectAltNameExt.get
                        (SubjectAlternativeNameExtension.SUBJECT_NAME);
            } catch (IOException e) {
                throw new CertificateParsingException("X.509 Certificate is " +
                        "incomplete: subject field is empty, and " +
                        "SubjectAlternativeName extension is absent");
            }
            if (names == null || names.isEmpty()) {
                throw new CertificateParsingException("X.509 Certificate is " +
                        "incomplete: subject field is empty, and " +
                        "SubjectAlternativeName extension is empty");
            } else if (subjectAltNameExt.isCritical() == false) {
                throw new CertificateParsingException("X.509 Certificate is " +
                        "incomplete: SubjectAlternativeName extension MUST " +
                        "be marked critical when subject field is empty");
            }
        }
    }
    private void emit(DerOutputStream out)
    throws CertificateException, IOException {
        DerOutputStream tmp = new DerOutputStream();
        version.encode(tmp);
        serialNum.encode(tmp);
        algId.encode(tmp);
        if ((version.compare(CertificateVersion.V1) == 0) &&
            (issuer.toString() == null))
            throw new CertificateParsingException(
                      "Null issuer DN not allowed in v1 certificate");
        issuer.encode(tmp);
        interval.encode(tmp);
        if ((version.compare(CertificateVersion.V1) == 0) &&
            (subject.toString() == null))
            throw new CertificateParsingException(
                      "Null subject DN not allowed in v1 certificate");
        subject.encode(tmp);
        pubKey.encode(tmp);
        if (issuerUniqueId != null) {
            issuerUniqueId.encode(tmp);
        }
        if (subjectUniqueId != null) {
            subjectUniqueId.encode(tmp);
        }
        if (extensions != null) {
            extensions.encode(tmp);
        }
        out.write(DerValue.tag_Sequence, tmp);
    }
    private int attributeMap(String name) {
        Integer num = map.get(name);
        if (num == null) {
            return 0;
        }
        return num.intValue();
    }
    private void setVersion(Object val) throws CertificateException {
        if (!(val instanceof CertificateVersion)) {
            throw new CertificateException("Version class type invalid.");
        }
        version = (CertificateVersion)val;
    }
    private void setSerialNumber(Object val) throws CertificateException {
        if (!(val instanceof CertificateSerialNumber)) {
            throw new CertificateException("SerialNumber class type invalid.");
        }
        serialNum = (CertificateSerialNumber)val;
    }
    private void setAlgorithmId(Object val) throws CertificateException {
        if (!(val instanceof CertificateAlgorithmId)) {
            throw new CertificateException(
                             "AlgorithmId class type invalid.");
        }
        algId = (CertificateAlgorithmId)val;
    }
    private void setIssuer(Object val) throws CertificateException {
        if (!(val instanceof CertificateIssuerName)) {
            throw new CertificateException(
                             "Issuer class type invalid.");
        }
        issuer = (CertificateIssuerName)val;
    }
    private void setValidity(Object val) throws CertificateException {
        if (!(val instanceof CertificateValidity)) {
            throw new CertificateException(
                             "CertificateValidity class type invalid.");
        }
        interval = (CertificateValidity)val;
    }
    private void setSubject(Object val) throws CertificateException {
        if (!(val instanceof CertificateSubjectName)) {
            throw new CertificateException(
                             "Subject class type invalid.");
        }
        subject = (CertificateSubjectName)val;
    }
    private void setKey(Object val) throws CertificateException {
        if (!(val instanceof CertificateX509Key)) {
            throw new CertificateException(
                             "Key class type invalid.");
        }
        pubKey = (CertificateX509Key)val;
    }
    private void setIssuerUniqueId(Object val) throws CertificateException {
        if (version.compare(CertificateVersion.V2) < 0) {
            throw new CertificateException("Invalid version");
        }
        if (!(val instanceof CertificateIssuerUniqueIdentity)) {
            throw new CertificateException(
                             "IssuerUniqueId class type invalid.");
        }
        issuerUniqueId = (CertificateIssuerUniqueIdentity)val;
    }
    private void setSubjectUniqueId(Object val) throws CertificateException {
        if (version.compare(CertificateVersion.V2) < 0) {
            throw new CertificateException("Invalid version");
        }
        if (!(val instanceof CertificateSubjectUniqueIdentity)) {
            throw new CertificateException(
                             "SubjectUniqueId class type invalid.");
        }
        subjectUniqueId = (CertificateSubjectUniqueIdentity)val;
    }
    private void setExtensions(Object val) throws CertificateException {
        if (version.compare(CertificateVersion.V3) < 0) {
            throw new CertificateException("Invalid version");
        }
        if (!(val instanceof CertificateExtensions)) {
          throw new CertificateException(
                             "Extensions class type invalid.");
        }
        extensions = (CertificateExtensions)val;
    }
}
