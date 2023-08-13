public class MouseOverAction extends WidgetAction.Adapter {
    private long eventID = Integer.MIN_VALUE;
    private HoverProvider provider;
    public MouseOverAction(HoverProvider provider) {
        this.provider = provider;
    }
    @Override
    public State mouseMoved(Widget widget, WidgetMouseEvent event) {
        long id = event.getEventID();
        if (id != eventID) {
            eventID = id;
            provider.widgetHovered(widget);
        }
        return State.REJECTED;
    }
    @Override
    public State mouseExited(Widget widget, WidgetMouseEvent event) {
        provider.widgetHovered(null);
        return State.REJECTED;
    }
}
