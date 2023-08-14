class XAtomList {
    Set<XAtom> atoms = new HashSet<XAtom>();
    public XAtomList() {
    }
    public XAtomList(long data, int count) {
        init(data, count);
    }
    private void init(long data, int count) {
        for (int i = 0; i < count; i++) {
            add(new XAtom(XToolkit.getDisplay(), XAtom.getAtom(data+count*XAtom.getAtomSize())));
        }
    }
    public XAtomList(XAtom[] atoms) {
        init(atoms);
    }
    private void init(XAtom[] atoms) {
        for (int i = 0; i < atoms.length; i++) {
            add(atoms[i]);
        }
    }
    public XAtom[] getAtoms() {
        XAtom[] res = new XAtom[size()];
        Iterator<XAtom> iter = atoms.iterator();
        int i = 0;
        while (iter.hasNext()) {
            res[i++] = iter.next();
        }
        return res;
    }
    public long getAtomsData() {
        return XAtom.toData(getAtoms());
    }
    public boolean contains(XAtom atom) {
        return atoms.contains(atom);
    }
    public void add(XAtom atom) {
        atoms.add(atom);
    }
    public void remove(XAtom atom) {
        atoms.remove(atom);
    }
    public int size() {
        return atoms.size();
    }
    public XAtomList subset(int mask, Map<Integer, XAtom> mapping) {
        XAtomList res = new XAtomList();
        Iterator<Integer> iter = mapping.keySet().iterator();
        while (iter.hasNext()) {
            Integer bits = iter.next();
            if ( (mask & bits.intValue()) == bits.intValue() ) {
                XAtom atom = mapping.get(bits);
                if (contains(atom)) {
                    res.add(atom);
                }
            }
        }
        return res;
    }
    public Iterator<XAtom> iterator() {
        return atoms.iterator();
    }
    public void addAll(XAtomList atoms) {
        Iterator<XAtom> iter = atoms.iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        Iterator iter = atoms.iterator();
        while (iter.hasNext()) {
            buf.append(iter.next().toString());
            if (iter.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
}
