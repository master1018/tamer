public class DistributionPointName {
    private static final byte TAG_FULL_NAME = 0;
    private static final byte TAG_RELATIVE_NAME = 1;
    private GeneralNames fullName = null;
    private RDN relativeName = null;
    private volatile int hashCode;
    public DistributionPointName(GeneralNames fullName) {
        if (fullName == null) {
            throw new IllegalArgumentException("fullName must not be null");
        }
        this.fullName = fullName;
    }
    public DistributionPointName(RDN relativeName) {
        if (relativeName == null) {
            throw new IllegalArgumentException("relativeName must not be null");
        }
        this.relativeName = relativeName;
    }
    public DistributionPointName(DerValue encoding) throws IOException {
        if (encoding.isContextSpecific(TAG_FULL_NAME) &&
            encoding.isConstructed()) {
            encoding.resetTag(DerValue.tag_Sequence);
            fullName = new GeneralNames(encoding);
        } else if (encoding.isContextSpecific(TAG_RELATIVE_NAME) &&
            encoding.isConstructed()) {
            encoding.resetTag(DerValue.tag_Set);
            relativeName = new RDN(encoding);
        } else {
            throw new IOException("Invalid encoding for DistributionPointName");
        }
    }
    public GeneralNames getFullName() {
        return fullName;
    }
    public RDN getRelativeName() {
        return relativeName;
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream theChoice = new DerOutputStream();
        if (fullName != null) {
            fullName.encode(theChoice);
            out.writeImplicit(
                DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_FULL_NAME),
                theChoice);
        } else {
            relativeName.encode(theChoice);
            out.writeImplicit(
                DerValue.createTag(DerValue.TAG_CONTEXT, true,
                    TAG_RELATIVE_NAME),
                theChoice);
        }
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DistributionPointName == false) {
            return false;
        }
        DistributionPointName other = (DistributionPointName)obj;
        return equals(this.fullName, other.fullName) &&
               equals(this.relativeName, other.relativeName);
    }
    public int hashCode() {
        int hash = hashCode;
        if (hash == 0) {
            hash = 1;
            if (fullName != null) {
                hash += fullName.hashCode();
            } else {
                hash += relativeName.hashCode();
            }
            hashCode = hash;
        }
        return hash;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fullName != null) {
            sb.append("DistributionPointName:\n     " + fullName + "\n");
        } else {
            sb.append("DistributionPointName:\n     " + relativeName + "\n");
        }
        return sb.toString();
    }
    private static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
