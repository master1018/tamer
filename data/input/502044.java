public class LocalsArraySet extends LocalsArray {
    private final OneLocalsArray primary;
    private final ArrayList<LocalsArray> secondaries;
    public LocalsArraySet(int maxLocals) {
        super(maxLocals != 0);
        primary = new OneLocalsArray(maxLocals);
        secondaries = new ArrayList();
    }
    public LocalsArraySet(OneLocalsArray primary,
            ArrayList<LocalsArray> secondaries) {
        super(primary.getMaxLocals() > 0);
        this.primary = primary;
        this.secondaries = secondaries;        
    }
    private LocalsArraySet(LocalsArraySet toCopy) {
        super(toCopy.getMaxLocals() > 0);
        primary = toCopy.primary.copy();
        secondaries = new ArrayList(toCopy.secondaries.size());
        int sz = toCopy.secondaries.size();
        for (int i = 0; i < sz; i++) {
            LocalsArray la = toCopy.secondaries.get(i);
            if (la == null) {
                secondaries.add(null);
            } else {
                secondaries.add(la.copy());
            }
        }
    }
    @Override
    public void setImmutable() {
        primary.setImmutable();
        for (LocalsArray la : secondaries) {
            if (la != null) {
                la.setImmutable();
            }
        }
        super.setImmutable();
    }
    @Override
    public LocalsArray copy() {
        return new LocalsArraySet(this);
    }
    @Override
    public void annotate(ExceptionWithContext ex) {
        ex.addContext("(locals array set; primary)");
        primary.annotate(ex);
        int sz = secondaries.size();
        for (int label = 0; label < sz; label++) {
            LocalsArray la = secondaries.get(label);
            if (la != null) {
                ex.addContext("(locals array set: primary for caller "
                        + Hex.u2(label) + ')');
                la.getPrimary().annotate(ex);
            }
        }
    }
    public String toHuman() {
        StringBuilder sb = new StringBuilder();
        sb.append("(locals array set; primary)\n");
        sb.append(getPrimary().toHuman());
        sb.append('\n');
        int sz = secondaries.size();
        for (int label = 0; label < sz; label++) {
            LocalsArray la = secondaries.get(label);
            if (la != null) {
                sb.append("(locals array set: primary for caller "
                        + Hex.u2(label) + ")\n");
                sb.append(la.getPrimary().toHuman());
                sb.append('\n');
            }
        }
        return sb.toString();
    }
    @Override
    public void makeInitialized(Type type) {
        int len = primary.getMaxLocals();
        if (len == 0) {
            return;
        }
        throwIfImmutable();
        primary.makeInitialized(type);
        for (LocalsArray la : secondaries) {
            if (la != null) {
                la.makeInitialized(type);
            }
        }
    }
    @Override
    public int getMaxLocals() {
        return primary.getMaxLocals();
    }
    @Override
    public void set(int idx, TypeBearer type) {
        throwIfImmutable();
        primary.set(idx, type);
        for (LocalsArray la : secondaries) {
            if (la != null) {
                la.set(idx, type);
            }
        }
    }
    @Override
    public void set(RegisterSpec spec) {
        set(spec.getReg(), spec);
    }
    @Override
    public void invalidate(int idx) {
        throwIfImmutable();
        primary.invalidate(idx);
        for (LocalsArray la : secondaries) {
            if (la != null) {
                la.invalidate(idx);
            }
        }
    }
    @Override
    public TypeBearer getOrNull(int idx) {
        return primary.getOrNull(idx);
    }
    @Override
    public TypeBearer get(int idx) {
        return primary.get(idx);
    }
    @Override
    public TypeBearer getCategory1(int idx) {
        return primary.getCategory1(idx);
    }
    @Override
    public TypeBearer getCategory2(int idx) {
        return primary.getCategory2(idx);
    }
    private LocalsArraySet mergeWithSet(LocalsArraySet other) {
        OneLocalsArray newPrimary;
        ArrayList<LocalsArray> newSecondaries;
        boolean secondariesChanged = false;
        newPrimary = primary.merge(other.getPrimary());
        int sz1 = secondaries.size();
        int sz2 = other.secondaries.size();
        int sz = Math.max(sz1, sz2);
        newSecondaries = new ArrayList(sz);
        for (int i = 0; i < sz; i++) {
            LocalsArray la1 = (i < sz1 ? secondaries.get(i) : null);
            LocalsArray la2 = (i < sz2 ? other.secondaries.get(i) : null);
            LocalsArray resultla = null;
            if (la1 == la2) {
                resultla = la1;
            } else if (la1 == null) {
                resultla = la2;
            } else if (la2 == null) {
                resultla = la1;
            } else {
                try {
                    resultla = la1.merge(la2);
                } catch (SimException ex) {
                    ex.addContext(
                            "Merging locals set for caller block " + Hex.u2(i));
                }
            }
            secondariesChanged = secondariesChanged || (la1 != resultla);
            newSecondaries.add(resultla);
        }
        if ((primary == newPrimary) && ! secondariesChanged ) {
            return this;
        }
        return new LocalsArraySet(newPrimary, newSecondaries);
    }
    private LocalsArraySet mergeWithOne(OneLocalsArray other) {
        OneLocalsArray newPrimary;
        ArrayList<LocalsArray> newSecondaries;
        boolean secondariesChanged = false;
        newPrimary = primary.merge(other.getPrimary());
        newSecondaries = new ArrayList(secondaries.size());
        int sz = secondaries.size();
        for (int i = 0; i < sz; i++) {
            LocalsArray la = secondaries.get(i);
            LocalsArray resultla = null;
            if (la != null) {
                try {
                    resultla = la.merge(other);
                } catch (SimException ex) {
                    ex.addContext("Merging one locals against caller block "
                                    + Hex.u2(i));
                }
            }
            secondariesChanged = secondariesChanged || (la != resultla);
            newSecondaries.add(resultla);
        }
        if ((primary == newPrimary) && ! secondariesChanged ) {
            return this;
        }
        return new LocalsArraySet(newPrimary, newSecondaries);
    }
    @Override
    public LocalsArraySet merge(LocalsArray other) {
        LocalsArraySet result;
        try {
            if (other instanceof LocalsArraySet) {
                result = mergeWithSet((LocalsArraySet) other);
            } else {
                result = mergeWithOne((OneLocalsArray) other);
            }
        } catch (SimException ex) {
            ex.addContext("underlay locals:");
            annotate(ex);
            ex.addContext("overlay locals:");
            other.annotate(ex);
            throw ex;
        }
        result.setImmutable();
        return result;
    }
    private LocalsArray getSecondaryForLabel(int label) {
        if (label >= secondaries.size()) {
            return null;
        }
        return secondaries.get(label);
    }
    @Override
    public LocalsArraySet mergeWithSubroutineCaller
            (LocalsArray other, int predLabel) {
        LocalsArray mine = getSecondaryForLabel(predLabel);
        LocalsArray newSecondary;
        OneLocalsArray newPrimary;
        newPrimary = primary.merge(other.getPrimary());
        if (mine == other) {
            newSecondary = mine;
        } else if (mine == null) {
            newSecondary = other;
        } else {
            newSecondary = mine.merge(other);
        }
        if ((newSecondary == mine) && (newPrimary == primary)) {
            return this;
        } else {
            newPrimary = null;
            int szSecondaries = secondaries.size();
            int sz = Math.max(predLabel + 1, szSecondaries);
            ArrayList<LocalsArray> newSecondaries = new ArrayList(sz);
            for (int i = 0; i < sz; i++) {
                LocalsArray la = null;
                if (i == predLabel) {
                    la = newSecondary;
                } else if (i < szSecondaries) {
                    la = secondaries.get(i);
                }
                if (la != null) {
                    if (newPrimary == null) {
                        newPrimary = la.getPrimary();
                    } else {
                        newPrimary = newPrimary.merge(la.getPrimary());
                    }
                }
                newSecondaries.add(la);
            }
            LocalsArraySet result
                    = new LocalsArraySet(newPrimary, newSecondaries);
            result.setImmutable();
            return result;
        }
    }
    public LocalsArray subArrayForLabel(int subLabel) {
        LocalsArray result = getSecondaryForLabel(subLabel);
        return result;
    }
    @Override
    protected OneLocalsArray getPrimary() {
        return primary;
    }
}
