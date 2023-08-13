public class XEmbedChildProxy extends Component {
    long handle;
    XEmbeddingContainer container;
    public XEmbedChildProxy(XEmbeddingContainer container, long handle) {
        this.handle = handle;
        this.container = container;
    }
    public void addNotify() {
        synchronized(getTreeLock()) {
            if (AWTAccessor.getComponentAccessor().getPeer(this) == null) {
                AWTAccessor.getComponentAccessor().
                    setPeer(this,((XToolkit)Toolkit.getDefaultToolkit()).createEmbedProxy(this));
            }
            super.addNotify();
        }
    }
    XEmbeddingContainer getEmbeddingContainer() {
        return container;
    }
    long getHandle() {
        return handle;
    }
}
