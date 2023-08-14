public class Pool {
    public static final int MAX_ENTRIES = 0xFFFF;
    public static final int MAX_STRING_LENGTH = 0xFFFF;
    int pp;
    Object[] pool;
    Map<Object,Integer> indices;
    public Pool(int pp, Object[] pool) {
        this.pp = pp;
        this.pool = pool;
        this.indices = new HashMap<Object,Integer>(pool.length);
        for (int i = 1; i < pp; i++) {
            if (pool[i] != null) indices.put(pool[i], i);
        }
    }
    public Pool() {
        this(1, new Object[64]);
    }
    public int numEntries() {
        return pp;
    }
    public void reset() {
        pp = 1;
        indices.clear();
    }
    private void doublePool() {
        Object[] newpool = new Object[pool.length * 2];
        System.arraycopy(pool, 0, newpool, 0, pool.length);
        pool = newpool;
    }
    public int put(Object value) {
        if (value instanceof MethodSymbol)
            value = new Method((MethodSymbol)value);
        else if (value instanceof VarSymbol)
            value = new Variable((VarSymbol)value);
        Integer index = indices.get(value);
        if (index == null) {
            index = pp;
            indices.put(value, index);
            if (pp == pool.length) doublePool();
            pool[pp++] = value;
            if (value instanceof Long || value instanceof Double) {
                if (pp == pool.length) doublePool();
                pool[pp++] = null;
            }
        }
        return index.intValue();
    }
    public int get(Object o) {
        Integer n = indices.get(o);
        return n == null ? -1 : n.intValue();
    }
    static class Method extends DelegatedSymbol {
        MethodSymbol m;
        Method(MethodSymbol m) {
            super(m);
            this.m = m;
        }
        public boolean equals(Object other) {
            if (!(other instanceof Method)) return false;
            MethodSymbol o = ((Method)other).m;
            return
                o.name == m.name &&
                o.owner == m.owner &&
                o.type.equals(m.type);
        }
        public int hashCode() {
            return
                m.name.hashCode() * 33 +
                m.owner.hashCode() * 9 +
                m.type.hashCode();
        }
    }
    static class Variable extends DelegatedSymbol {
        VarSymbol v;
        Variable(VarSymbol v) {
            super(v);
            this.v = v;
        }
        public boolean equals(Object other) {
            if (!(other instanceof Variable)) return false;
            VarSymbol o = ((Variable)other).v;
            return
                o.name == v.name &&
                o.owner == v.owner &&
                o.type.equals(v.type);
        }
        public int hashCode() {
            return
                v.name.hashCode() * 33 +
                v.owner.hashCode() * 9 +
                v.type.hashCode();
        }
    }
}
