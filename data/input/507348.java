public class Extensions {
    private static List SUPPORTED_CRITICAL = Arrays.asList(
            new String[] {"2.5.29.15", "2.5.29.19", "2.5.29.32", "2.5.29.17",  
                "2.5.29.30", "2.5.29.36", "2.5.29.37", "2.5.29.54"}); 
    private List<Extension> extensions;
    private Set critical;
    private Set noncritical;
    private boolean hasUnsupported;
    private HashMap oidMap;
    private byte[] encoding;
    public Extensions() {}
    public Extensions(List extensions) {
        this.extensions = extensions;
    }
    public List getExtensions() {
        return extensions;
    }
    public int size() {
        return (extensions == null) 
                        ? 0
                        : extensions.size();
    }
    public Set getCriticalExtensions() {
        if (critical == null) {
            makeOidsLists();
        }
        return critical;
    }
    public Set getNonCriticalExtensions() {
        if (noncritical == null) {
            makeOidsLists();
        }
        return noncritical;
    }
    public boolean hasUnsupportedCritical() {
        if (critical == null) {
            makeOidsLists();
        }
        return hasUnsupported;
    }
    private void makeOidsLists() {
        if (extensions == null) {
            return;
        }
        int size = extensions.size();
        critical = new HashSet(size);
        noncritical = new HashSet(size);
        for (int i=0; i<size; i++) {
            Extension extn = (Extension) extensions.get(i);
            String oid = extn.getExtnID();
            if (extn.getCritical()) {
                if (!SUPPORTED_CRITICAL.contains(oid)) {
                    hasUnsupported = true;
                }
                critical.add(oid);
            } else {
                noncritical.add(oid);
            }
        }
    }
    public Extension getExtensionByOID(String oid) {
        if (extensions == null) {
            return null;
        }
        if (oidMap == null) {
            oidMap = new HashMap();
            Iterator it = extensions.iterator();
            while (it.hasNext()) {
                Extension extn = (Extension) it.next();
                oidMap.put(extn.getExtnID(), extn);
            }
        }
        return (Extension) oidMap.get(oid);
    }
    public boolean[] valueOfKeyUsage() {
        Extension extn = getExtensionByOID("2.5.29.15"); 
        KeyUsage kUsage = null;
        if ((extn == null) || ((kUsage = extn.getKeyUsageValue()) == null)) {
            return null;
        }
        return kUsage.getKeyUsage();
    }
    public List valueOfExtendedKeyUsage() throws IOException {
        Extension extn = getExtensionByOID("2.5.29.37"); 
        if (extn == null) {
            return null;
        }
        return ((ExtendedKeyUsage) 
                extn.getDecodedExtensionValue()).getExtendedKeyUsage();
    }
    public int valueOfBasicConstrains() {
        Extension extn = getExtensionByOID("2.5.29.19"); 
        BasicConstraints bc = null;
        if ((extn == null) 
                || ((bc = extn.getBasicConstraintsValue()) == null)) {
            return Integer.MAX_VALUE;
        }
        return bc.getPathLenConstraint();
    }
    public List valueOfSubjectAlternativeName() throws IOException {
        Extension extn = getExtensionByOID("2.5.29.17"); 
        if (extn == null) {
            return null;
        }
        return ((GeneralNames) GeneralNames.ASN1.decode(extn.getExtnValue()))
                .getPairsList();
    }
    public List valueOfIssuerAlternativeName() throws IOException {
        Extension extn = getExtensionByOID("2.5.29.18"); 
        if (extn == null) {
            return null;
        }
        return ((GeneralNames) 
                GeneralNames.ASN1.decode(extn.getExtnValue())).getPairsList();
    }
    public X500Principal valueOfCertificateIssuerExtension() 
                                                        throws IOException {
        Extension extn = getExtensionByOID("2.5.29.29"); 
        if (extn == null) {
            return null;
        }
        return ((CertificateIssuer) 
                extn.getDecodedExtensionValue()).getIssuer();
    }
    public void addExtension(Extension extn) {
        encoding = null;
        if (extensions == null) {
            extensions = new ArrayList();
        }
        extensions.add(extn);
        if (oidMap != null) {
            oidMap.put(extn.getExtnID(), extn);
        }
        if (critical != null) {
            String oid = extn.getExtnID();
            if (extn.getCritical()) {
                if (!SUPPORTED_CRITICAL.contains(oid)) {
                    hasUnsupported = true;
                }
                critical.add(oid);
            } else {
                noncritical.add(oid);
            }
        }
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public boolean equals(Object exts) {
        if (!(exts instanceof Extensions)) {
            return false;
        }
        Extensions extns = (Extensions) exts;
        return ((extensions == null) || (extensions.size() == 0) 
                    ? ((extns.extensions == null) 
                            || (extns.extensions.size() == 0))
                    : ((extns.extensions == null) 
                            || (extns.extensions.size() == 0))
                        ? false
                        : (extensions.containsAll(extns.extensions)
                            && (extensions.size() == extns.extensions.size()))
                );
    }
    public int hashCode() {
    	int hashcode = 0;
    	if (extensions != null) {
    		hashcode = extensions.hashCode();
    	}
    	return hashcode;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        if (extensions == null) {
            return;
        }
        int num = 1;
        for (Extension extension: extensions) {
            buffer.append('\n').append(prefix)
                .append('[').append(num++).append("]: "); 
            extension.dumpValue(buffer, prefix);
        }
    }
    public static final ASN1Type ASN1 = new ASN1SequenceOf(Extension.ASN1) {
        public Object getDecodedObject(BerInputStream in) {
            return new Extensions((List)in.content);
        }
        public Collection getValues(Object object) {
            Extensions exts = (Extensions) object;
            return (exts.extensions == null) ? new ArrayList() : exts.extensions;
        }
    };
}
