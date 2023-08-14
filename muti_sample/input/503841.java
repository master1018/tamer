public final class StdAttributeList extends FixedSizeList
        implements AttributeList {
    public StdAttributeList(int size) {
        super(size);
    }
    public Attribute get(int n) {
        return (Attribute) get0(n);
    }
    public int byteLength() {
        int sz = size();
        int result = 2; 
        for (int i = 0; i < sz; i++) {
            result += get(i).byteLength();
        }
        return result;
    }
    public Attribute findFirst(String name) {
        int sz = size();
        for (int i = 0; i < sz; i++) {
            Attribute att = get(i);
            if (att.getName().equals(name)) {
                return att;
            }
        }
        return null;
    }
    public Attribute findNext(Attribute attrib) {
        int sz = size();
        int at;
        outer: {
            for (at = 0; at < sz; at++) {
                Attribute att = get(at);
                if (att == attrib) {
                    break outer;
                }
            }
            return null;
        }
        String name = attrib.getName();
        for (at++; at < sz; at++) {
            Attribute att = get(at);
            if (att.getName().equals(name)) {
                return att;
            }
        }
        return null;
    }
    public void set(int n, Attribute attribute) {
        set0(n, attribute);
    }
}
