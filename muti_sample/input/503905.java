public class MockHandler extends Handler {
    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        dispatchMessage(msg);
        return true;
    }
}
