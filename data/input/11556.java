public class EDIPartyName implements GeneralNameInterface {
    private static final byte TAG_ASSIGNER = 0;
    private static final byte TAG_PARTYNAME = 1;
    private String assigner = null;
    private String party = null;
    private int myhash = -1;
    public EDIPartyName(String assignerName, String partyName) {
        this.assigner = assignerName;
        this.party = partyName;
    }
    public EDIPartyName(String partyName) {
        this.party = partyName;
    }
    public EDIPartyName(DerValue derValue) throws IOException {
        DerInputStream in = new DerInputStream(derValue.toByteArray());
        DerValue[] seq = in.getSequence(2);
        int len = seq.length;
        if (len < 1 || len > 2)
            throw new IOException("Invalid encoding of EDIPartyName");
        for (int i = 0; i < len; i++) {
            DerValue opt = seq[i];
            if (opt.isContextSpecific(TAG_ASSIGNER) &&
                !opt.isConstructed()) {
                if (assigner != null)
                    throw new IOException("Duplicate nameAssigner found in"
                                          + " EDIPartyName");
                opt = opt.data.getDerValue();
                assigner = opt.getAsString();
            }
            if (opt.isContextSpecific(TAG_PARTYNAME) &&
                !opt.isConstructed()) {
                if (party != null)
                    throw new IOException("Duplicate partyName found in"
                                          + " EDIPartyName");
                opt = opt.data.getDerValue();
                party = opt.getAsString();
            }
        }
    }
    public int getType() {
        return (GeneralNameInterface.NAME_EDI);
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tagged = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (assigner != null) {
            DerOutputStream tmp2 = new DerOutputStream();
            tmp2.putPrintableString(assigner);
            tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                 false, TAG_ASSIGNER), tmp2);
        }
        if (party == null)
            throw  new IOException("Cannot have null partyName");
        tmp.putPrintableString(party);
        tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                 false, TAG_PARTYNAME), tmp);
        out.write(DerValue.tag_Sequence, tagged);
    }
    public String getAssignerName() {
        return assigner;
    }
    public String getPartyName() {
        return party;
    }
    public boolean equals(Object other) {
        if (!(other instanceof EDIPartyName))
            return false;
        String otherAssigner = ((EDIPartyName)other).assigner;
        if (this.assigner == null) {
            if (otherAssigner != null)
                return false;
        } else {
            if (!(this.assigner.equals(otherAssigner)))
                return false;
        }
        String otherParty = ((EDIPartyName)other).party;
        if (this.party == null) {
            if (otherParty != null)
                return false;
        } else {
            if (!(this.party.equals(otherParty)))
                return false;
        }
        return true;
    }
    public int hashCode() {
        if (myhash == -1) {
            myhash = 37 + party.hashCode();
            if (assigner != null) {
                myhash = 37 * myhash + assigner.hashCode();
            }
        }
        return myhash;
    }
    public String toString() {
        return ("EDIPartyName: " +
                 ((assigner == null) ? "" :
                   ("  nameAssigner = " + assigner + ","))
                 + "  partyName = " + party);
    }
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null)
            constraintType = NAME_DIFF_TYPE;
        else if (inputName.getType() != NAME_EDI)
            constraintType = NAME_DIFF_TYPE;
        else {
            throw new UnsupportedOperationException("Narrowing, widening, and matching of names not supported for EDIPartyName");
        }
        return constraintType;
    }
    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not supported for EDIPartyName");
    }
}
