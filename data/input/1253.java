public class JFSExternalFileProducerFactory extends JFSFileProducerFactory {
    private HashMap<String, JFSExternalFileProducer> producers = new HashMap<String, JFSExternalFileProducer>();
    public final void resetProducers() {
        producers.clear();
    }
    public final JFSFileProducer createProducer(String uri) {
        JFSExternalFileProducer p = new JFSExternalFileProducer(uri);
        producers.put(uri, p);
        return p;
    }
    public final void shutDownProducer(String uri) {
        JFSExternalFileProducer p = producers.get(uri);
        if (p != null && JFSConfig.getInstance().getServerShutDown()) {
            JFSServerAccess sa = JFSServerAccess.getInstance(p.getHost(), p.getPort(), p.getRootPath());
            sa.shutDown();
        }
    }
    public final void cancelProducer(String uri) {
        JFSExternalFileProducer p = producers.get(uri);
        if (p != null) {
            JFSServerAccess sa = JFSServerAccess.getInstance(p.getHost(), p.getPort(), p.getRootPath());
            sa.cancel();
        }
    }
}
