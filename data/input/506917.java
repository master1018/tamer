public class RedirectLocations {
    private final Set<URI> uris;
    public RedirectLocations() {
        super();
        this.uris = new HashSet<URI>();
    }
    public boolean contains(final URI uri) {
        return this.uris.contains(uri);
    }
    public void add(final URI uri) {
        this.uris.add(uri);
    }
    public boolean remove(final URI uri) {
        return this.uris.remove(uri);
    }
}
