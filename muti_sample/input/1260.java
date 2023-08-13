public class FilteredMemberList extends AbstractList<Symbol> {
    private final Scope scope;
    public FilteredMemberList(Scope scope) {
        this.scope = scope;
    }
    public int size() {
        int cnt = 0;
        for (Scope.Entry e = scope.elems; e != null; e = e.sibling) {
            if (!unwanted(e.sym))
                cnt++;
        }
        return cnt;
    }
    public Symbol get(int index) {
        for (Scope.Entry e = scope.elems; e != null; e = e.sibling) {
            if (!unwanted(e.sym) && (index-- == 0))
                return e.sym;
        }
        throw new IndexOutOfBoundsException();
    }
    public Iterator<Symbol> iterator() {
        return new Iterator<Symbol>() {
            private Scope.Entry nextEntry = scope.elems;
            private boolean hasNextForSure = false;
            public boolean hasNext() {
                if (hasNextForSure) {
                    return true;
                }
                while (nextEntry != null && unwanted(nextEntry.sym)) {
                    nextEntry = nextEntry.sibling;
                }
                hasNextForSure = (nextEntry != null);
                return hasNextForSure;
            }
            public Symbol next() {
                if (hasNext()) {
                    Symbol result = nextEntry.sym;
                    nextEntry = nextEntry.sibling;
                    hasNextForSure = false;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    private static boolean unwanted(Symbol s) {
        return s == null  ||  (s.flags() & SYNTHETIC) != 0;
    }
}
