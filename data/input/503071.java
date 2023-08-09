public class ChatSession {
    private ImEntity mParticipant;
    private ChatSessionManager mManager;
    private CopyOnWriteArrayList<MessageListener> mListeners;
    private Vector<Message> mHistoryMessages;
    ChatSession(ImEntity participant, ChatSessionManager manager) {
        mParticipant = participant;
        mManager = manager;
        mListeners = new CopyOnWriteArrayList<MessageListener>();
        mHistoryMessages = new Vector<Message>();
    }
    public ImEntity getParticipant() {
        return mParticipant;
    }
    public void setParticipant(ImEntity participant) {
        mParticipant = participant;
    }
    public synchronized void addMessageListener(MessageListener listener) {
        if ((listener != null) && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    public synchronized void removeMessageListener(MessageListener listener) {
        mListeners.remove(listener);
    }
    public void sendMessageAsync(String text) {
        Message message = new Message(text);
        sendMessageAsync(message);
    }
    public void sendMessageAsync(Message msg) {
        msg.setTo(mParticipant.getAddress());
        mHistoryMessages.add(msg);
        mManager.sendMessageAsync(this, msg);
    }
    public void onReceiveMessage(Message msg) {
        mHistoryMessages.add(msg);
        for (MessageListener listener : mListeners) {
            listener.onIncomingMessage(this, msg);
        }
    }
    public void onSendMessageError(Message message, ImErrorInfo error) {
        for (MessageListener listener : mListeners) {
            listener.onSendMessageError(this, message, error);
        }
    }
    public void onSendMessageError(String msgId, ImErrorInfo error) {
        for(Message msg : mHistoryMessages){
            if(msgId.equals(msg.getID())){
                onSendMessageError(msg, error);
                return;
            }
        }
        Log.i("ChatSession", "Message has been removed when we get delivery error:"
                + error);
    }
    public List<Message> getHistoryMessages() {
        return Collections.unmodifiableList(mHistoryMessages);
    }
}
