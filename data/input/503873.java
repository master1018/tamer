public abstract class ChatSessionManager {
    private CopyOnWriteArrayList<ChatSessionListener> mListeners;
    protected Vector<ChatSession> mSessions;
    protected ChatSessionManager() {
        mListeners = new CopyOnWriteArrayList<ChatSessionListener>();
        mSessions = new Vector<ChatSession>();
    }
    public synchronized void addChatSessionListener(ChatSessionListener listener) {
        if ((listener != null) && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    public synchronized void removeChatSessionListener(ChatSessionListener listener) {
        mListeners.remove(listener);
    }
    public synchronized ChatSession createChatSession(ImEntity participant) {
        for(ChatSession session : mSessions) {
            if(session.getParticipant().equals(participant)) {
                return session;
            }
        }
        ChatSession session = new ChatSession(participant, this);
        for (ChatSessionListener listener : mListeners) {
            listener.onChatSessionCreated(session);
        }
        mSessions.add(session);
        return session;
    }
    public void closeChatSession(ChatSession session) {
        mSessions.remove(session);
    }
    protected abstract void sendMessageAsync(ChatSession session, Message message);
}
