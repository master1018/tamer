public class GTabItem extends TabItem {
    private GContentController d_controller;
    private boolean d_fContentReady = false;
    public GTabItem(GContentController controller) {
        super();
        setLayout(new BorderLayout());
        d_controller = controller;
    }
    public GTabItem(GContentController controller, String strText) {
        super(strText);
        setLayout(new BorderLayout());
        d_controller = controller;
    }
    public void buildContent() {
        setContentReady(true);
        layout();
    }
    protected final boolean isContentReady() {
        return d_fContentReady;
    }
    protected final void setContentReady(boolean flag) {
        d_fContentReady = flag;
    }
    protected final GContentController getController() {
        return d_controller;
    }
}
