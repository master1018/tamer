public class TDBContainer extends JenaRDFDBContainer implements RDFDBContainer {
    public TDBContainer(JenaAnnoteaTypeFactory typeFactory, Model model, RDFDBContainerPool pool, boolean readWrite) {
        super(model, typeFactory, pool, readWrite);
    }
    @Override
    public synchronized void abort() {
        try {
            super.abort();
            sync();
        } catch (JenaException ex) {
            throw new TripleStoreException(ex);
        }
    }
    @Override
    public synchronized void commit() {
        try {
            super.commit();
            sync();
        } catch (JenaException ex) {
            throw new TripleStoreException(ex);
        }
    }
    private void sync() {
        Graph graph = getModel().getGraph();
        ((GraphTDB) graph).sync(true);
    }
}
