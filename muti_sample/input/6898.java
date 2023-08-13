class MethodSet {
    private final Map lookupMap;
    private int count;
    private boolean frozen;
    public MethodSet() {
        frozen = false;
        lookupMap = new HashMap();
        count = 0;
    }
    public int size() {
        return count;
    }
    public void add(MemberDefinition method) {
            if (frozen) {
                throw new CompilerError("add()");
            }
            Identifier name = method.getName();
            List methodList = (List) lookupMap.get(name);
            if (methodList == null) {
                methodList = new ArrayList();
                lookupMap.put(name, methodList);
            }
            int size = methodList.size();
            for (int i = 0; i < size; i++) {
                if (((MemberDefinition) methodList.get(i))
                    .getType().equalArguments(method.getType())) {
                    throw new CompilerError("duplicate addition");
                }
            }
            methodList.add(method);
            count++;
    }
    public void replace(MemberDefinition method) {
            if (frozen) {
                throw new CompilerError("replace()");
            }
            Identifier name = method.getName();
            List methodList = (List) lookupMap.get(name);
            if (methodList == null) {
                methodList = new ArrayList();
                lookupMap.put(name, methodList);
            }
            int size = methodList.size();
            for (int i = 0; i < size; i++) {
                if (((MemberDefinition) methodList.get(i))
                    .getType().equalArguments(method.getType())) {
                    methodList.set(i, method);
                    return;
                }
            }
            methodList.add(method);
            count++;
    }
    public MemberDefinition lookupSig(Identifier name, Type type) {
        Iterator matches = lookupName(name);
        MemberDefinition candidate;
        while (matches.hasNext()) {
            candidate = (MemberDefinition) matches.next();
            if (candidate.getType().equalArguments(type)) {
                return candidate;
            }
        }
        return null;
    }
    public Iterator lookupName(Identifier name) {
        List methodList = (List) lookupMap.get(name);
        if (methodList == null) {
            return Collections.emptyIterator();
        }
        return methodList.iterator();
    }
    public Iterator iterator() {
        class MethodIterator implements Iterator {
            Iterator hashIter = lookupMap.values().iterator();
            Iterator listIter = Collections.emptyIterator();
            public boolean hasNext() {
                if (listIter.hasNext()) {
                    return true;
                } else {
                    if (hashIter.hasNext()) {
                        listIter = ((List) hashIter.next())
                            .iterator();
                        if (listIter.hasNext()) {
                            return true;
                        } else {
                            throw new
                                CompilerError("iterator() in MethodSet");
                        }
                    }
                }
                return false;
            }
            public Object next() {
                return listIter.next();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
        return new MethodIterator();
    }
    public void freeze() {
        frozen = true;
    }
    public boolean isFrozen() {
        return frozen;
    }
    public String toString() {
        int len = size();
        StringBuffer buf = new StringBuffer();
        Iterator all = iterator();
        buf.append("{");
        while (all.hasNext()) {
            buf.append(all.next().toString());
            len--;
            if (len > 0) {
                buf.append(", ");
            }
        }
        buf.append("}");
        return buf.toString();
    }
}
