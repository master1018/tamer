public class FormSubmitEvent extends HTMLFrameHyperlinkEvent {
    public enum MethodType { GET, POST };
    FormSubmitEvent(Object source, EventType type, URL targetURL,
                   Element sourceElement, String targetFrame,
                    MethodType method, String data) {
        super(source, type, targetURL, sourceElement, targetFrame);
        this.method = method;
        this.data = data;
    }
    public MethodType getMethod() {
        return method;
    }
    public String getData() {
        return data;
    }
    private MethodType method;
    private String data;
}
