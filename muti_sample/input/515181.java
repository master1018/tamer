public final class CookieSpecRegistry {
    private final Map<String,CookieSpecFactory> registeredSpecs;
    public CookieSpecRegistry() {
        super();
        this.registeredSpecs = new LinkedHashMap<String,CookieSpecFactory>();
    }
    public synchronized void register(final String name, final CookieSpecFactory factory) {
         if (name == null) {
             throw new IllegalArgumentException("Name may not be null");
         }
        if (factory == null) {
            throw new IllegalArgumentException("Cookie spec factory may not be null");
        }
        registeredSpecs.put(name.toLowerCase(Locale.ENGLISH), factory);
    }
    public synchronized void unregister(final String id) {
         if (id == null) {
             throw new IllegalArgumentException("Id may not be null");
         }
         registeredSpecs.remove(id.toLowerCase(Locale.ENGLISH));
    }
    public synchronized CookieSpec getCookieSpec(final String name, final HttpParams params) 
        throws IllegalStateException {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        CookieSpecFactory factory = registeredSpecs.get(name.toLowerCase(Locale.ENGLISH));
        if (factory != null) {
            return factory.newInstance(params);
        } else {
            throw new IllegalStateException("Unsupported cookie spec: " + name);
        }
    } 
    public synchronized CookieSpec getCookieSpec(final String name) 
        throws IllegalStateException {
        return getCookieSpec(name, null);
    } 
    public synchronized List<String> getSpecNames(){
        return new ArrayList<String>(registeredSpecs.keySet()); 
    }
    public synchronized void setItems(final Map<String, CookieSpecFactory> map) {
        if (map == null) {
            return;
        }
        registeredSpecs.clear();
        registeredSpecs.putAll(map);
    }
}
