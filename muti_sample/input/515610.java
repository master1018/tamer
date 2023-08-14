public class ChatSessionListenerAdapter extends IChatSessionListener.Stub {
    private static final String TAG = ImApp.LOG_TAG;
    public void onChatSessionCreated(IChatSession session) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "notifyChatSessionCreated(" + session + ")");
        }
    }
    public void onChatSessionCreateError(String name, ImErrorInfo error) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "notifyChatSessionCreateError(" + name + ", " + error + ")");
        }
    }
}
