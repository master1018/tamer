final public class LazySearchEnumerationImpl implements NamingEnumeration {
    private NamingEnumeration candidates;
    private SearchResult nextMatch = null;
    private SearchControls cons;
    private AttrFilter filter;
    private Context context;
    private Hashtable env;
    private boolean useFactory = true;
    public LazySearchEnumerationImpl(NamingEnumeration candidates,
        AttrFilter filter, SearchControls cons) throws NamingException {
            this.candidates = candidates;
            this.filter = filter;
            if(cons == null) {
                this.cons = new SearchControls();
            } else {
                this.cons = cons;
            }
    }
    public LazySearchEnumerationImpl(NamingEnumeration candidates,
        AttrFilter filter, SearchControls cons,
        Context ctx, Hashtable env, boolean useFactory) throws NamingException {
            this.candidates = candidates;
            this.filter = filter;
            this.env = env;
            this.context = ctx;
            this.useFactory = useFactory;
            if(cons == null) {
                this.cons = new SearchControls();
            } else {
                this.cons = cons;
            }
    }
    public LazySearchEnumerationImpl(NamingEnumeration candidates,
        AttrFilter filter, SearchControls cons,
        Context ctx, Hashtable env) throws NamingException {
            this(candidates, filter, cons, ctx, env, true);
    }
    public boolean hasMore() throws NamingException {
        return findNextMatch(false) != null;
    }
    public boolean hasMoreElements() {
        try {
            return hasMore();
        } catch (NamingException e) {
            return false;
        }
    }
    public Object nextElement() {
        try {
            return findNextMatch(true);
        } catch (NamingException e) {
            throw new NoSuchElementException(e.toString());
        }
    }
    public Object next() throws NamingException {
        return (findNextMatch(true));
    }
    public void close() throws NamingException {
        if (candidates != null) {
            candidates.close();
        }
    }
    private SearchResult findNextMatch(boolean remove) throws NamingException {
        SearchResult answer;
        if (nextMatch != null) {
            answer = nextMatch;
            if (remove) {
                nextMatch = null;
            }
            return answer;
        } else {
            Binding next;
            Object obj;
            Attributes targetAttrs;
            while (candidates.hasMore()) {
                next = (Binding)candidates.next();
                obj = next.getObject();
                if (obj instanceof DirContext) {
                    targetAttrs = ((DirContext)(obj)).getAttributes("");
                    if (filter.check(targetAttrs)) {
                        if (!cons.getReturningObjFlag()) {
                            obj = null;
                        } else if (useFactory) {
                            try {
                                Name nm = (context != null ?
                                    new CompositeName(next.getName()) : null);
                                obj = DirectoryManager.getObjectInstance(obj,
                                    nm, context, env, targetAttrs);
                            } catch (NamingException e) {
                                throw e;
                            } catch (Exception e) {
                                NamingException e2 = new NamingException(
                                    "problem generating object using object factory");
                                e2.setRootCause(e);
                                throw e2;
                            }
                        }
                        answer = new SearchResult(next.getName(),
                            next.getClassName(), obj,
                            SearchFilter.selectAttributes(targetAttrs,
                                cons.getReturningAttributes()),
                            true);
                        if (!remove)
                            nextMatch = answer;
                        return answer;
                    }
                }
            }
            return null;
        }
    }
}
