public class DistributionPoint {
    public final static int KEY_COMPROMISE         = 1;
    public final static int CA_COMPROMISE          = 2;
    public final static int AFFILIATION_CHANGED    = 3;
    public final static int SUPERSEDED             = 4;
    public final static int CESSATION_OF_OPERATION = 5;
    public final static int CERTIFICATE_HOLD       = 6;
    public final static int PRIVILEGE_WITHDRAWN    = 7;
    public final static int AA_COMPROMISE          = 8;
    private static final String[] REASON_STRINGS = {
        null,
        "key compromise",
        "CA compromise",
        "affiliation changed",
        "superseded",
        "cessation of operation",
        "certificate hold",
        "privilege withdrawn",
        "AA compromise",
    };
    private static final byte TAG_DIST_PT = 0;
    private static final byte TAG_REASONS = 1;
    private static final byte TAG_ISSUER = 2;
    private static final byte TAG_FULL_NAME = 0;
    private static final byte TAG_REL_NAME = 1;
    private GeneralNames fullName;
    private RDN relativeName;
    private boolean[] reasonFlags;
    private GeneralNames crlIssuer;
    private volatile int hashCode;
    public DistributionPoint(GeneralNames fullName, boolean[] reasonFlags,
            GeneralNames crlIssuer) {
        if ((fullName == null) && (crlIssuer == null)) {
            throw new IllegalArgumentException
                        ("fullName and crlIssuer may not both be null");
        }
        this.fullName = fullName;
        this.reasonFlags = reasonFlags;
        this.crlIssuer = crlIssuer;
    }
    public DistributionPoint(RDN relativeName, boolean[] reasonFlags,
            GeneralNames crlIssuer) {
        if ((relativeName == null) && (crlIssuer == null)) {
            throw new IllegalArgumentException
                        ("relativeName and crlIssuer may not both be null");
        }
        this.relativeName = relativeName;
        this.reasonFlags = reasonFlags;
        this.crlIssuer = crlIssuer;
    }
    public DistributionPoint(DerValue val) throws IOException {
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding of DistributionPoint.");
        }
        while ((val.data != null) && (val.data.available() != 0)) {
            DerValue opt = val.data.getDerValue();
            if (opt.isContextSpecific(TAG_DIST_PT) && opt.isConstructed()) {
                if ((fullName != null) || (relativeName != null)) {
                    throw new IOException("Duplicate DistributionPointName in "
                                          + "DistributionPoint.");
                }
                DerValue distPnt = opt.data.getDerValue();
                if (distPnt.isContextSpecific(TAG_FULL_NAME)
                        && distPnt.isConstructed()) {
                    distPnt.resetTag(DerValue.tag_Sequence);
                    fullName = new GeneralNames(distPnt);
                } else if (distPnt.isContextSpecific(TAG_REL_NAME)
                        && distPnt.isConstructed()) {
                    distPnt.resetTag(DerValue.tag_Set);
                    relativeName = new RDN(distPnt);
                } else {
                    throw new IOException("Invalid DistributionPointName in "
                                          + "DistributionPoint");
                }
            } else if (opt.isContextSpecific(TAG_REASONS)
                                                && !opt.isConstructed()) {
                if (reasonFlags != null) {
                    throw new IOException("Duplicate Reasons in " +
                                          "DistributionPoint.");
                }
                opt.resetTag(DerValue.tag_BitString);
                reasonFlags = (opt.getUnalignedBitString()).toBooleanArray();
            } else if (opt.isContextSpecific(TAG_ISSUER)
                                                && opt.isConstructed()) {
                if (crlIssuer != null) {
                    throw new IOException("Duplicate CRLIssuer in " +
                                          "DistributionPoint.");
                }
                opt.resetTag(DerValue.tag_Sequence);
                crlIssuer = new GeneralNames(opt);
            } else {
                throw new IOException("Invalid encoding of " +
                                      "DistributionPoint.");
            }
        }
        if ((crlIssuer == null) && (fullName == null) && (relativeName == null)) {
            throw new IOException("One of fullName, relativeName, "
                + " and crlIssuer has to be set");
        }
    }
    public GeneralNames getFullName() {
        return fullName;
    }
    public RDN getRelativeName() {
        return relativeName;
    }
    public boolean[] getReasonFlags() {
        return reasonFlags;
    }
    public GeneralNames getCRLIssuer() {
        return crlIssuer;
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tagged = new DerOutputStream();
        if ((fullName != null) || (relativeName != null)) {
            DerOutputStream distributionPoint = new DerOutputStream();
            if (fullName != null) {
                DerOutputStream derOut = new DerOutputStream();
                fullName.encode(derOut);
                distributionPoint.writeImplicit(
                    DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_FULL_NAME),
                    derOut);
            } else if (relativeName != null) {
                DerOutputStream derOut = new DerOutputStream();
                relativeName.encode(derOut);
                distributionPoint.writeImplicit(
                    DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_REL_NAME),
                    derOut);
            }
            tagged.write(
                DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_DIST_PT),
                distributionPoint);
        }
        if (reasonFlags != null) {
            DerOutputStream reasons = new DerOutputStream();
            BitArray rf = new BitArray(reasonFlags);
            reasons.putTruncatedUnalignedBitString(rf);
            tagged.writeImplicit(
                DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_REASONS),
                reasons);
        }
        if (crlIssuer != null) {
            DerOutputStream issuer = new DerOutputStream();
            crlIssuer.encode(issuer);
            tagged.writeImplicit(
                DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_ISSUER),
                issuer);
        }
        out.write(DerValue.tag_Sequence, tagged);
    }
    private static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DistributionPoint == false) {
            return false;
        }
        DistributionPoint other = (DistributionPoint)obj;
        boolean equal = equals(this.fullName, other.fullName)
                     && equals(this.relativeName, other.relativeName)
                     && equals(this.crlIssuer, other.crlIssuer)
                     && Arrays.equals(this.reasonFlags, other.reasonFlags);
        return equal;
    }
    public int hashCode() {
        int hash = hashCode;
        if (hash == 0) {
            hash = 1;
            if (fullName != null) {
                hash += fullName.hashCode();
            }
            if (relativeName != null) {
                hash += relativeName.hashCode();
            }
            if (crlIssuer != null) {
                hash += crlIssuer.hashCode();
            }
            if (reasonFlags != null) {
                for (int i = 0; i < reasonFlags.length; i++) {
                    if (reasonFlags[i]) {
                        hash += i;
                    }
                }
            }
            hashCode = hash;
        }
        return hash;
    }
    private static String reasonToString(int reason) {
        if ((reason > 0) && (reason < REASON_STRINGS.length)) {
            return REASON_STRINGS[reason];
        }
        return "Unknown reason " + reason;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fullName != null) {
            sb.append("DistributionPoint:\n     " + fullName + "\n");
        }
        if (relativeName != null) {
            sb.append("DistributionPoint:\n     " + relativeName + "\n");
        }
        if (reasonFlags != null) {
            sb.append("   ReasonFlags:\n");
            for (int i = 0; i < reasonFlags.length; i++) {
                if (reasonFlags[i]) {
                    sb.append("    " + reasonToString(i) + "\n");
                }
            }
        }
        if (crlIssuer != null) {
            sb.append("   CRLIssuer:" + crlIssuer + "\n");
        }
        return sb.toString();
    }
}
