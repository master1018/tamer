public class GeneralNames {
    private final List<GeneralName> names;
    public GeneralNames(DerValue derVal) throws IOException {
        this();
        if (derVal.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for GeneralNames.");
        }
        if (derVal.data.available() == 0) {
            throw new IOException("No data available in "
                                      + "passed DER encoded value.");
        }
        while (derVal.data.available() != 0) {
            DerValue encName = derVal.data.getDerValue();
            GeneralName name = new GeneralName(encName);
            add(name);
        }
    }
    public GeneralNames() {
        names = new ArrayList<GeneralName>();
    }
    public GeneralNames add(GeneralName name) {
        if (name == null) {
            throw new NullPointerException();
        }
        names.add(name);
        return this;
    }
    public GeneralName get(int index) {
        return names.get(index);
    }
    public boolean isEmpty() {
        return names.isEmpty();
    }
    public int size() {
        return names.size();
    }
    public Iterator<GeneralName> iterator() {
        return names.iterator();
    }
    public List<GeneralName> names() {
        return names;
    }
    public void encode(DerOutputStream out) throws IOException {
        if (isEmpty()) {
            return;
        }
        DerOutputStream temp = new DerOutputStream();
        for (GeneralName gn : names) {
            gn.encode(temp);
        }
        out.write(DerValue.tag_Sequence, temp);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof GeneralNames == false) {
            return false;
        }
        GeneralNames other = (GeneralNames)obj;
        return this.names.equals(other.names);
    }
    public int hashCode() {
        return names.hashCode();
    }
    public String toString() {
        return names.toString();
    }
}
