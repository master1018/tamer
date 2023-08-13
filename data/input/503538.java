public class ChatListenerAdapter extends IChatListener.Stub {
    private static final String TAG = ImApp.LOG_TAG;
    public void onContactJoined(IChatSession ses, Contact contact) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onContactJoined(" + ses + ", " + contact + ")");
        }
    }
    public void onContactLeft(IChatSession ses, Contact contact) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onContactLeft(" + ses + ", " + contact + ")");
        }
    }
    public void onIncomingMessage(IChatSession ses, Message msg) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onIncomingMessage(" + ses + ", " + msg + ")");
        }
    }
    public void onSendMessageError(IChatSession ses, Message msg,
            ImErrorInfo error) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onSendMessageError(" + ses + ", " + msg + ", " + error + ")");
        }
    }
    public void onInviteError(IChatSession ses, ImErrorInfo error) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onInviteError(" + ses + ", " + error + ")");
        }
    }
    public void onConvertedToGroupChat(IChatSession ses) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConvertedToGroupChat(" + ses + ")");
        }
    }
}
