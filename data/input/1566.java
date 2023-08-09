public class GeneralSubtrees implements Cloneable {
    private final List<GeneralSubtree> trees;
    private static final int NAME_DIFF_TYPE = GeneralNameInterface.NAME_DIFF_TYPE;
    private static final int NAME_MATCH = GeneralNameInterface.NAME_MATCH;
    private static final int NAME_NARROWS = GeneralNameInterface.NAME_NARROWS;
    private static final int NAME_WIDENS = GeneralNameInterface.NAME_WIDENS;
    private static final int NAME_SAME_TYPE = GeneralNameInterface.NAME_SAME_TYPE;
    public GeneralSubtrees() {
        trees = new ArrayList<GeneralSubtree>();
    }
    private GeneralSubtrees(GeneralSubtrees source) {
        trees = new ArrayList<GeneralSubtree>(source.trees);
    }
    public GeneralSubtrees(DerValue val) throws IOException {
        this();
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding of GeneralSubtrees.");
        }
        while (val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            GeneralSubtree tree = new GeneralSubtree(opt);
            add(tree);
        }
    }
    public GeneralSubtree get(int index) {
        return trees.get(index);
    }
    public void remove(int index) {
        trees.remove(index);
    }
    public void add(GeneralSubtree tree) {
        if (tree == null) {
            throw new NullPointerException();
        }
        trees.add(tree);
    }
    public boolean contains(GeneralSubtree tree) {
        if (tree == null) {
            throw new NullPointerException();
        }
        return trees.contains(tree);
    }
    public int size() {
        return trees.size();
    }
    public Iterator<GeneralSubtree> iterator() {
        return trees.iterator();
    }
    public List<GeneralSubtree> trees() {
        return trees;
    }
    public Object clone() {
        return new GeneralSubtrees(this);
    }
    public String toString() {
        String s = "   GeneralSubtrees:\n" + trees.toString() + "\n";
        return s;
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream seq = new DerOutputStream();
        for (int i = 0, n = size(); i < n; i++) {
            get(i).encode(seq);
        }
        out.write(DerValue.tag_Sequence, seq);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof GeneralSubtrees == false) {
            return false;
        }
        GeneralSubtrees other = (GeneralSubtrees)obj;
        return this.trees.equals(other.trees);
    }
    public int hashCode() {
        return trees.hashCode();
    }
    private GeneralNameInterface getGeneralNameInterface(int ndx) {
        return getGeneralNameInterface(get(ndx));
    }
    private static GeneralNameInterface getGeneralNameInterface(GeneralSubtree gs) {
        GeneralName gn = gs.getName();
        GeneralNameInterface gni = gn.getName();
        return gni;
    }
    private void minimize() {
        for (int i = 0; i < size(); i++) {
            GeneralNameInterface current = getGeneralNameInterface(i);
            boolean remove1 = false;
            for (int j = i + 1; j < size(); j++) {
                GeneralNameInterface subsequent = getGeneralNameInterface(j);
                switch (current.constrains(subsequent)) {
                    case GeneralNameInterface.NAME_DIFF_TYPE:
                        continue;
                    case GeneralNameInterface.NAME_MATCH:
                        remove1 = true;
                        break;
                    case GeneralNameInterface.NAME_NARROWS:
                        remove(j);
                        j--; 
                        continue;
                    case GeneralNameInterface.NAME_WIDENS:
                        remove1 = true;
                        break;
                    case GeneralNameInterface.NAME_SAME_TYPE:
                        continue;
                }
                break;
            } 
            if (remove1) {
                remove(i);
                i--; 
            }
        }
    }
    private GeneralSubtree createWidestSubtree(GeneralNameInterface name) {
        try {
            GeneralName newName;
            switch (name.getType()) {
            case GeneralNameInterface.NAME_ANY:
                ObjectIdentifier otherOID = ((OtherName)name).getOID();
                newName = new GeneralName(new OtherName(otherOID, null));
                break;
            case GeneralNameInterface.NAME_RFC822:
                newName = new GeneralName(new RFC822Name(""));
                break;
            case GeneralNameInterface.NAME_DNS:
                newName = new GeneralName(new DNSName(""));
                break;
            case GeneralNameInterface.NAME_X400:
                newName = new GeneralName(new X400Address((byte[])null));
                break;
            case GeneralNameInterface.NAME_DIRECTORY:
                newName = new GeneralName(new X500Name(""));
                break;
            case GeneralNameInterface.NAME_EDI:
                newName = new GeneralName(new EDIPartyName(""));
                break;
            case GeneralNameInterface.NAME_URI:
                newName = new GeneralName(new URIName(""));
                break;
            case GeneralNameInterface.NAME_IP:
                newName = new GeneralName(new IPAddressName((byte[])null));
                break;
            case GeneralNameInterface.NAME_OID:
                newName = new GeneralName
                    (new OIDName(new ObjectIdentifier((int[])null)));
                break;
            default:
                throw new IOException
                    ("Unsupported GeneralNameInterface type: " + name.getType());
            }
            return new GeneralSubtree(newName, 0, -1);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error: " + e, e);
        }
    }
    public GeneralSubtrees intersect(GeneralSubtrees other) {
        if (other == null) {
            throw new NullPointerException("other GeneralSubtrees must not be null");
        }
        GeneralSubtrees newThis = new GeneralSubtrees();
        GeneralSubtrees newExcluded = null;
        if (size() == 0) {
            union(other);
            return null;
        }
        this.minimize();
        other.minimize();
        for (int i = 0; i < size(); i++) {
            GeneralNameInterface thisEntry = getGeneralNameInterface(i);
            boolean removeThisEntry = false;
            boolean sameType = false;
            for (int j = 0; j < other.size(); j++) {
                GeneralSubtree otherEntryGS = other.get(j);
                GeneralNameInterface otherEntry =
                    getGeneralNameInterface(otherEntryGS);
                switch (thisEntry.constrains(otherEntry)) {
                    case NAME_NARROWS:
                        remove(i);
                        i--;
                        newThis.add(otherEntryGS);
                        sameType = false;
                        break;
                    case NAME_SAME_TYPE:
                        sameType = true;
                        continue;
                    case NAME_MATCH:
                    case NAME_WIDENS:
                        sameType = false;
                        break;
                    case NAME_DIFF_TYPE:
                    default:
                        continue;
                }
                break;
            }
            if (sameType) {
                boolean intersection = false;
                for (int j = 0; j < size(); j++) {
                    GeneralNameInterface thisAltEntry = getGeneralNameInterface(j);
                    if (thisAltEntry.getType() == thisEntry.getType()) {
                        for (int k = 0; k < other.size(); k++) {
                            GeneralNameInterface othAltEntry =
                                other.getGeneralNameInterface(k);
                            int constraintType =
                                thisAltEntry.constrains(othAltEntry);
                            if (constraintType == NAME_MATCH ||
                                constraintType == NAME_WIDENS ||
                                constraintType == NAME_NARROWS) {
                                intersection = true;
                                break;
                            }
                        }
                    }
                }
                if (intersection == false) {
                    if (newExcluded == null) {
                        newExcluded = new GeneralSubtrees();
                    }
                    GeneralSubtree widestSubtree =
                         createWidestSubtree(thisEntry);
                    if (!newExcluded.contains(widestSubtree)) {
                        newExcluded.add(widestSubtree);
                    }
                }
                remove(i);
                i--;
            }
        }
        if (newThis.size() > 0) {
            union(newThis);
        }
        for (int i = 0; i < other.size(); i++) {
            GeneralSubtree otherEntryGS = other.get(i);
            GeneralNameInterface otherEntry = getGeneralNameInterface(otherEntryGS);
            boolean diffType = false;
            for (int j = 0; j < size(); j++) {
                GeneralNameInterface thisEntry = getGeneralNameInterface(j);
                switch (thisEntry.constrains(otherEntry)) {
                    case NAME_DIFF_TYPE:
                        diffType = true;
                        continue;
                    case NAME_NARROWS:
                    case NAME_SAME_TYPE:
                    case NAME_MATCH:
                    case NAME_WIDENS:
                        diffType = false; 
                        break;
                    default:
                        continue;
                }
                break;
            }
            if (diffType) {
                add(otherEntryGS);
            }
        }
        return newExcluded;
    }
    public void union(GeneralSubtrees other) {
        if (other != null) {
            for (int i = 0, n = other.size(); i < n; i++) {
                add(other.get(i));
            }
            minimize();
        }
    }
    public void reduce(GeneralSubtrees excluded) {
        if (excluded == null) {
            return;
        }
        for (int i = 0, n = excluded.size(); i < n; i++) {
            GeneralNameInterface excludedName = excluded.getGeneralNameInterface(i);
            for (int j = 0; j < size(); j++) {
                GeneralNameInterface permitted = getGeneralNameInterface(j);
                switch (excludedName.constrains(permitted)) {
                case GeneralNameInterface.NAME_DIFF_TYPE:
                    break;
                case GeneralNameInterface.NAME_MATCH:
                    remove(j);
                    j--;
                    break;
                case GeneralNameInterface.NAME_NARROWS:
                    remove(j);
                    j--;
                    break;
                case GeneralNameInterface.NAME_WIDENS:
                    break;
                case GeneralNameInterface.NAME_SAME_TYPE:
                    break;
                }
            } 
        } 
    }
}
