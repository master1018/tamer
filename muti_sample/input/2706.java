public class TextOutputCallback implements Callback, java.io.Serializable {
    private static final long serialVersionUID = 1689502495511663102L;
    public static final int INFORMATION         = 0;
    public static final int WARNING             = 1;
    public static final int ERROR               = 2;
    private int messageType;
    private String message;
    public TextOutputCallback(int messageType, String message) {
        if ((messageType != INFORMATION &&
                messageType != WARNING && messageType != ERROR) ||
            message == null || message.length() == 0)
            throw new IllegalArgumentException();
        this.messageType = messageType;
        this.message = message;
    }
    public int getMessageType() {
        return messageType;
    }
    public String getMessage() {
        return message;
    }
}
