public class VMDeathEventSet extends AbstractEventSet {
    private static final long serialVersionUID = 1163097303940092229L;
    VMDeathEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }
   @Override
    public void notify(JDIListener listener) {
        listener.vmDeath(this);
    }
}
