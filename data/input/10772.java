public class HTMLFrameHyperlinkEvent extends HyperlinkEvent {
    public HTMLFrameHyperlinkEvent(Object source, EventType type, URL targetURL,
                                   String targetFrame) {
        super(source, type, targetURL);
        this.targetFrame = targetFrame;
    }
    public HTMLFrameHyperlinkEvent(Object source, EventType type, URL targetURL, String desc,
                                   String targetFrame) {
        super(source, type, targetURL, desc);
        this.targetFrame = targetFrame;
    }
    public HTMLFrameHyperlinkEvent(Object source, EventType type, URL targetURL,
                                   Element sourceElement, String targetFrame) {
        super(source, type, targetURL, null, sourceElement);
        this.targetFrame = targetFrame;
    }
    public HTMLFrameHyperlinkEvent(Object source, EventType type, URL targetURL, String desc,
                                   Element sourceElement, String targetFrame) {
        super(source, type, targetURL, desc, sourceElement);
        this.targetFrame = targetFrame;
    }
    public HTMLFrameHyperlinkEvent(Object source, EventType type, URL targetURL,
                                   String desc, Element sourceElement,
                                   InputEvent inputEvent, String targetFrame) {
        super(source, type, targetURL, desc, sourceElement, inputEvent);
        this.targetFrame = targetFrame;
    }
    public String getTarget() {
        return targetFrame;
    }
    private String targetFrame;
}
