public final class SchemeRegistry {
    private final Map<String,Scheme> registeredSchemes;
    public SchemeRegistry() {
        super();
        registeredSchemes = new LinkedHashMap<String,Scheme>();
    }
    public synchronized final Scheme getScheme(String name) {
        Scheme found = get(name);
        if (found == null) {
            throw new IllegalStateException
                ("Scheme '"+name+"' not registered.");
        }
        return found;
    }
    public synchronized final Scheme getScheme(HttpHost host) {
        if (host == null) {
            throw new IllegalArgumentException("Host must not be null.");
        }
        return getScheme(host.getSchemeName());
    }
    public synchronized final Scheme get(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name must not be null.");
        Scheme found = registeredSchemes.get(name);
        return found;
    }
    public synchronized final Scheme register(Scheme sch) {
        if (sch == null)
            throw new IllegalArgumentException("Scheme must not be null.");
        Scheme old = registeredSchemes.put(sch.getName(), sch);
        return old;
    }
    public synchronized final Scheme unregister(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name must not be null.");
        Scheme gone = registeredSchemes.remove(name);
        return gone;
    }
    public synchronized final List<String> getSchemeNames() {
        return new ArrayList<String>(registeredSchemes.keySet());
    }
    public synchronized void setItems(final Map<String, Scheme> map) {
        if (map == null) {
            return;
        }
        registeredSchemes.clear();
        registeredSchemes.putAll(map);
    }
} 
