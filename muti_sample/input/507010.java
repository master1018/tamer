public class ChatSessionManagerAdapter extends IChatSessionManager.Stub {
    static final String TAG = RemoteImService.TAG;
    ImConnectionAdapter mConnection;
    ChatSessionManager mSessionManager;
    ChatGroupManager mGroupManager;
    HashMap<String, ChatSessionAdapter> mActiveSessions;
    ChatSessionListenerAdapter mSessionListenerAdapter;
    final RemoteCallbackList<IChatSessionListener> mRemoteListeners
            = new RemoteCallbackList<IChatSessionListener>();
    public ChatSessionManagerAdapter(ImConnectionAdapter connection) {
        mConnection = connection;
        ImConnection connAdaptee = connection.getAdaptee();
        mSessionManager = connAdaptee.getChatSessionManager();
        mActiveSessions = new HashMap<String, ChatSessionAdapter>();
        mSessionListenerAdapter = new ChatSessionListenerAdapter();
        mSessionManager.addChatSessionListener(mSessionListenerAdapter);
        if((connAdaptee.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0) {
            mGroupManager = connAdaptee.getChatGroupManager();
            mGroupManager.addGroupListener(new ChatGroupListenerAdpater());
        }
    }
    public IChatSession createChatSession(String contactAddress) {
        ContactListManagerAdapter listManager =
            (ContactListManagerAdapter) mConnection.getContactListManager();
        Contact contact = listManager.getContactByAddress(contactAddress);
        if(contact == null) {
            try {
                contact = listManager.createTemporaryContact(contactAddress);
            } catch (IllegalArgumentException e) {
                mSessionListenerAdapter.notifyChatSessionCreateFailed(contactAddress,
                        new ImErrorInfo(ImErrorInfo.ILLEGAL_CONTACT_ADDRESS,
                                "Invalid contact address:" + contactAddress));
                return null;
            }
        }
        ChatSession session = mSessionManager.createChatSession(contact);
        return getChatSessionAdapter(session);
    }
    public void closeChatSession(ChatSessionAdapter adapter) {
        synchronized (mActiveSessions) {
            ChatSession session = adapter.getAdaptee();
            mSessionManager.closeChatSession(session);
            mActiveSessions.remove(adapter.getAddress());
        }
    }
    public void closeAllChatSessions() {
        synchronized (mActiveSessions) {
            ArrayList<ChatSessionAdapter> sessions =
                new ArrayList<ChatSessionAdapter>(mActiveSessions.values());
            for (ChatSessionAdapter ses : sessions) {
                ses.leave();
            }
        }
    }
    public void updateChatSession(String oldAddress, ChatSessionAdapter adapter) {
        synchronized (mActiveSessions) {
            mActiveSessions.remove(oldAddress);
            mActiveSessions.put(adapter.getAddress(), adapter);
        }
    }
    public IChatSession getChatSession(String address) {
        synchronized (mActiveSessions) {
            return mActiveSessions.get(address);
        }
    }
    public List getActiveChatSessions() {
        synchronized (mActiveSessions) {
            return new ArrayList<ChatSessionAdapter>(mActiveSessions.values());
        }
    }
    public int getChatSessionCount() {
        synchronized (mActiveSessions) {
            return mActiveSessions.size();
        }
    }
    public void registerChatSessionListener(IChatSessionListener listener) {
        if (listener != null) {
            mRemoteListeners.register(listener);
        }
    }
    public void unregisterChatSessionListener(IChatSessionListener listener) {
        if (listener != null) {
            mRemoteListeners.unregister(listener);
        }
    }
    ChatSessionAdapter getChatSessionAdapter(ChatSession session) {
        synchronized (mActiveSessions) {
            Address participantAddress = session.getParticipant().getAddress();
            String key = participantAddress.getFullName();
            ChatSessionAdapter adapter = mActiveSessions.get(key);
            if (adapter == null) {
                adapter = new ChatSessionAdapter(session, mConnection);
                mActiveSessions.put(key, adapter);
            }
            return adapter;
        }
    }
    class ChatSessionListenerAdapter implements ChatSessionListener {
        public void onChatSessionCreated(ChatSession session) {
            final IChatSession sessionAdapter = getChatSessionAdapter(session);
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IChatSessionListener listener = mRemoteListeners.getBroadcastItem(i);
                try {
                    listener.onChatSessionCreated(sessionAdapter);
                } catch (RemoteException e) {
                }
            }
            mRemoteListeners.finishBroadcast();
        }
        public void notifyChatSessionCreateFailed(final String name, final ImErrorInfo error) {
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IChatSessionListener listener = mRemoteListeners.getBroadcastItem(i);
                try {
                    listener.onChatSessionCreateError(name, error);
                } catch (RemoteException e) {
                }
            }
            mRemoteListeners.finishBroadcast();
        }
    }
    class ChatGroupListenerAdpater implements GroupListener {
        public void onGroupCreated(ChatGroup group) {
        }
        public void onGroupDeleted(ChatGroup group) {
            closeSession(group);
        }
        public void onGroupError(int errorType, String name, ImErrorInfo error) {
            if(errorType == ERROR_CREATING_GROUP) {
                mSessionListenerAdapter.notifyChatSessionCreateFailed(name, error);
            }
        }
        public void onJoinedGroup(ChatGroup group) {
            mSessionManager.createChatSession(group);
        }
        public void onLeftGroup(ChatGroup group) {
            closeSession(group);
        }
        private void closeSession(ChatGroup group) {
            String address = group.getAddress().getFullName();
            IChatSession session = getChatSession(address);
            if(session != null) {
                closeChatSession((ChatSessionAdapter) session);
            }
        }
    }
}
