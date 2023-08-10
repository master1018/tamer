public class SBResourceLoader implements ResourceLoader {
    private SBContext context;
    public SBResourceLoader(SBContext context) {
        this.context = context;
    }
    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }
    public URL getCatalogFile() throws MalformedURLException {
        return getResource("/WEB-INF/jax-ws-catalog.xml");
    }
    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }
}
