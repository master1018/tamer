public class MalformedLinkException extends LinkException {
    public MalformedLinkException(String explanation) {
        super(explanation);
    }
    public MalformedLinkException() {
        super();
    }
    private static final long serialVersionUID = -3066740437737830242L;
}
