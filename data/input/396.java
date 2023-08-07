public class TimerCommand extends Command {
    public TimerCommand(String evtnm, int flags) {
        super(evtnm, flags);
    }
    protected void process(AuRequest request) {
        final Component comp = request.getComponent();
        if (comp == null) throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
        final Object xc = ((ComponentCtrl) comp).getExtraCtrl();
        if (xc instanceof Timer) ((Timer) xc).onTimer();
        final String[] data = request.getData();
        Events.postEvent(data == null || data.length == 0 ? new Event(getId(), comp) : data.length == 1 ? new Event(getId(), comp, data[0]) : new Event(getId(), comp, data));
    }
}
