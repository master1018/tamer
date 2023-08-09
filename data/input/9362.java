public class StandardMessageService implements com.message.spi.MessageService {
    @Override
    public String message() {
        return "This is a message from the standard message service";
    }
}
