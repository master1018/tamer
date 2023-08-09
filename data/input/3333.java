public class NameConstraintsExtension extends Extension
implements CertAttrSet<String>, Cloneable {
    public static final String IDENT = "x509.info.extensions.NameConstraints";
    public static final String NAME = "NameConstraints";
    public static final String PERMITTED_SUBTREES = "permitted_subtrees";
    public static final String EXCLUDED_SUBTREES = "excluded_subtrees";
    private static final byte TAG_PERMITTED = 0;
    private static final byte TAG_EXCLUDED = 1;
    private GeneralSubtrees     permitted = null;
    private GeneralSubtrees     excluded = null;
    private boolean hasMin;
    private boolean hasMax;
    private boolean minMaxValid = false;
    private void calcMinMax() throws IOException {
        hasMin = false;
        hasMax = false;
        if (excluded != null) {
            for (int i = 0; i < excluded.size(); i++) {
                GeneralSubtree subtree = excluded.get(i);
                if (subtree.getMinimum() != 0)
                    hasMin = true;
                if (subtree.getMaximum() != -1)
                    hasMax = true;
            }
        }
        if (permitted != null) {
            for (int i = 0; i < permitted.size(); i++) {
                GeneralSubtree subtree = permitted.get(i);
                if (subtree.getMinimum() != 0)
                    hasMin = true;
                if (subtree.getMaximum() != -1)
                    hasMax = true;
            }
        }
        minMaxValid = true;
    }
    private void encodeThis() throws IOException {
        minMaxValid = false;
        if (permitted == null && excluded == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tagged = new DerOutputStream();
        if (permitted != null) {
            DerOutputStream tmp = new DerOutputStream();
            permitted.encode(tmp);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                                 true, TAG_PERMITTED), tmp);
        }
        if (excluded != null) {
            DerOutputStream tmp = new DerOutputStream();
            excluded.encode(tmp);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                                 true, TAG_EXCLUDED), tmp);
        }
        seq.write(DerValue.tag_Sequence, tagged);
        this.extensionValue = seq.toByteArray();
    }
    public NameConstraintsExtension(GeneralSubtrees permitted,
                                    GeneralSubtrees excluded)
    throws IOException {
        this.permitted = permitted;
        this.excluded = excluded;
        this.extensionId = PKIXExtensions.NameConstraints_Id;
        this.critical = true;
        encodeThis();
    }
    public NameConstraintsExtension(Boolean critical, Object value)
    throws IOException {
        this.extensionId = PKIXExtensions.NameConstraints_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for" +
                                  " NameConstraintsExtension.");
        }
        if (val.data == null)
            return;
        while (val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            if (opt.isContextSpecific(TAG_PERMITTED) && opt.isConstructed()) {
                if (permitted != null) {
                    throw new IOException("Duplicate permitted " +
                         "GeneralSubtrees in NameConstraintsExtension.");
                }
                opt.resetTag(DerValue.tag_Sequence);
                permitted = new GeneralSubtrees(opt);
            } else if (opt.isContextSpecific(TAG_EXCLUDED) &&
                       opt.isConstructed()) {
                if (excluded != null) {
                    throw new IOException("Duplicate excluded " +
                             "GeneralSubtrees in NameConstraintsExtension.");
                }
                opt.resetTag(DerValue.tag_Sequence);
                excluded = new GeneralSubtrees(opt);
            } else
                throw new IOException("Invalid encoding of " +
                                      "NameConstraintsExtension.");
        }
        minMaxValid = false;
    }
    public String toString() {
        return (super.toString() + "NameConstraints: [" +
                ((permitted == null) ? "" :
                     ("\n    Permitted:" + permitted.toString())) +
                ((excluded == null) ? "" :
                     ("\n    Excluded:" + excluded.toString()))
                + "   ]\n");
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.NameConstraints_Id;
            this.critical = true;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(PERMITTED_SUBTREES)) {
            if (!(obj instanceof GeneralSubtrees)) {
                throw new IOException("Attribute value should be"
                                    + " of type GeneralSubtrees.");
            }
            permitted = (GeneralSubtrees)obj;
        } else if (name.equalsIgnoreCase(EXCLUDED_SUBTREES)) {
            if (!(obj instanceof GeneralSubtrees)) {
                throw new IOException("Attribute value should be "
                                    + "of type GeneralSubtrees.");
            }
            excluded = (GeneralSubtrees)obj;
        } else {
          throw new IOException("Attribute name not recognized by " +
                        "CertAttrSet:NameConstraintsExtension.");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(PERMITTED_SUBTREES)) {
            return (permitted);
        } else if (name.equalsIgnoreCase(EXCLUDED_SUBTREES)) {
            return (excluded);
        } else {
          throw new IOException("Attribute name not recognized by " +
                        "CertAttrSet:NameConstraintsExtension.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(PERMITTED_SUBTREES)) {
            permitted = null;
        } else if (name.equalsIgnoreCase(EXCLUDED_SUBTREES)) {
            excluded = null;
        } else {
          throw new IOException("Attribute name not recognized by " +
                        "CertAttrSet:NameConstraintsExtension.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(PERMITTED_SUBTREES);
        elements.addElement(EXCLUDED_SUBTREES);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
    public void merge(NameConstraintsExtension newConstraints)
            throws IOException {
        if (newConstraints == null) {
            return;
        }
        GeneralSubtrees newExcluded =
                        (GeneralSubtrees)newConstraints.get(EXCLUDED_SUBTREES);
        if (excluded == null) {
            excluded = (newExcluded != null) ?
                        (GeneralSubtrees)newExcluded.clone() : null;
        } else {
            if (newExcluded != null) {
                excluded.union(newExcluded);
            }
        }
        GeneralSubtrees newPermitted =
                (GeneralSubtrees)newConstraints.get(PERMITTED_SUBTREES);
        if (permitted == null) {
            permitted = (newPermitted != null) ?
                        (GeneralSubtrees)newPermitted.clone() : null;
        } else {
            if (newPermitted != null) {
                newExcluded = permitted.intersect(newPermitted);
                if (newExcluded != null) {
                    if (excluded != null) {
                        excluded.union(newExcluded);
                    } else {
                        excluded = (GeneralSubtrees)newExcluded.clone();
                    }
                }
            }
        }
        if (permitted != null) {
            permitted.reduce(excluded);
        }
        encodeThis();
    }
    public boolean verify(X509Certificate cert) throws IOException {
        if (cert == null) {
            throw new IOException("Certificate is null");
        }
        if (!minMaxValid) {
            calcMinMax();
        }
        if (hasMin) {
            throw new IOException("Non-zero minimum BaseDistance in"
                                + " name constraints not supported");
        }
        if (hasMax) {
            throw new IOException("Maximum BaseDistance in"
                                + " name constraints not supported");
        }
        X500Principal subjectPrincipal = cert.getSubjectX500Principal();
        X500Name subject = X500Name.asX500Name(subjectPrincipal);
        if (subject.isEmpty() == false) {
            if (verify(subject) == false) {
                return false;
            }
        }
        GeneralNames altNames = null;
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            SubjectAlternativeNameExtension altNameExt =
                certImpl.getSubjectAlternativeNameExtension();
            if (altNameExt != null) {
                altNames = (GeneralNames)
                            (altNameExt.get(altNameExt.SUBJECT_NAME));
            }
        } catch (CertificateException ce) {
            throw new IOException("Unable to extract extensions from " +
                        "certificate: " + ce.getMessage());
        }
        if (altNames == null) {
            return verifyRFC822SpecialCase(subject);
        }
        for (int i = 0; i < altNames.size(); i++) {
            GeneralNameInterface altGNI = altNames.get(i).getName();
            if (!verify(altGNI)) {
                return false;
            }
        }
        return true;
    }
    public boolean verify(GeneralNameInterface name) throws IOException {
        if (name == null) {
            throw new IOException("name is null");
        }
        if (excluded != null && excluded.size() > 0) {
            for (int i = 0; i < excluded.size(); i++) {
                GeneralSubtree gs = excluded.get(i);
                if (gs == null)
                    continue;
                GeneralName gn = gs.getName();
                if (gn == null)
                    continue;
                GeneralNameInterface exName = gn.getName();
                if (exName == null)
                    continue;
                switch (exName.constrains(name)) {
                case GeneralNameInterface.NAME_DIFF_TYPE:
                case GeneralNameInterface.NAME_WIDENS: 
                case GeneralNameInterface.NAME_SAME_TYPE:
                    break;
                case GeneralNameInterface.NAME_MATCH:
                case GeneralNameInterface.NAME_NARROWS: 
                    return false;
                }
            }
        }
        if (permitted != null && permitted.size() > 0) {
            boolean sameType = false;
            for (int i = 0; i < permitted.size(); i++) {
                GeneralSubtree gs = permitted.get(i);
                if (gs == null)
                    continue;
                GeneralName gn = gs.getName();
                if (gn == null)
                    continue;
                GeneralNameInterface perName = gn.getName();
                if (perName == null)
                    continue;
                switch (perName.constrains(name)) {
                case GeneralNameInterface.NAME_DIFF_TYPE:
                    continue; 
                case GeneralNameInterface.NAME_WIDENS: 
                case GeneralNameInterface.NAME_SAME_TYPE:
                    sameType = true;
                    continue; 
                case GeneralNameInterface.NAME_MATCH:
                case GeneralNameInterface.NAME_NARROWS:
                    return true; 
                }
            }
            if (sameType) {
                return false;
            }
        }
        return true;
    }
    public boolean verifyRFC822SpecialCase(X500Name subject) throws IOException {
        for (Iterator t = subject.allAvas().iterator(); t.hasNext(); ) {
            AVA ava = (AVA)t.next();
            ObjectIdentifier attrOID = ava.getObjectIdentifier();
            if (attrOID.equals(PKCS9Attribute.EMAIL_ADDRESS_OID)) {
                String attrValue = ava.getValueString();
                if (attrValue != null) {
                    RFC822Name emailName;
                    try {
                        emailName = new RFC822Name(attrValue);
                    } catch (IOException ioe) {
                        continue;
                    }
                    if (!verify(emailName)) {
                        return(false);
                    }
                }
             }
        }
        return true;
    }
    public Object clone() {
        try {
            NameConstraintsExtension newNCE =
                (NameConstraintsExtension) super.clone();
            if (permitted != null) {
                newNCE.permitted = (GeneralSubtrees) permitted.clone();
            }
            if (excluded != null) {
                newNCE.excluded = (GeneralSubtrees) excluded.clone();
            }
            return newNCE;
        } catch (CloneNotSupportedException cnsee) {
            throw new RuntimeException("CloneNotSupportedException while " +
                "cloning NameConstraintsException. This should never happen.");
        }
    }
}
