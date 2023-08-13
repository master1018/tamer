public abstract class BeanContextEvent extends EventObject {
    private static final long serialVersionUID = 7267998073569045052L;
    protected BeanContextEvent(BeanContext bc) {
        super(bc);
    }
    public BeanContext getBeanContext() { return (BeanContext)getSource(); }
    public synchronized void setPropagatedFrom(BeanContext bc) {
        propagatedFrom = bc;
    }
    public synchronized BeanContext getPropagatedFrom() {
        return propagatedFrom;
    }
    public synchronized boolean isPropagated() {
        return propagatedFrom != null;
    }
    protected BeanContext propagatedFrom;
}
