public class EventQueueItem {
    public AWTEvent event;
    public EventQueueItem next;
    public EventQueueItem(AWTEvent evt) {
        event = evt;
    }
}
