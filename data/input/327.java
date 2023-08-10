public class SizeCommand extends Command {
    public SizeCommand(String evtnm, int flags) {
        super(evtnm, flags);
    }
    protected void process(AuRequest request) {
        final Component comp = request.getComponent();
        if (comp == null) throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
        final String[] data = request.getData();
        if (data == null || data.length != 3) throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] { Objects.toString(data), this });
        final Sizable size = (Sizable) ((ComponentCtrl) comp).getExtraCtrl();
        size.setWidthByClient(data[0]);
        size.setHeightByClient(data[1]);
        Events.postEvent(new SizeEvent(getId(), comp, data[0], data[1], Commands.parseKeys(data[2])));
    }
}
