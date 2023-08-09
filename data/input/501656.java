public class MeetingResponseRequest extends Request {
    public int mResponse;
    MeetingResponseRequest(long messageId, int response) {
        mMessageId = messageId;
        mResponse = response;
    }
}
