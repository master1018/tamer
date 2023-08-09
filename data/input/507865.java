public abstract class ImConnection {
    public static final int DISCONNECTED = 0;
    public static final int LOGGING_IN = 1;
    public static final int LOGGED_IN = 2;
    public static final int LOGGING_OUT = 3;
    public static final int SUSPENDING = 4;
    public static final int SUSPENDED = 5;
    public static final int CAPABILITY_GROUP_CHAT = 1;
    public static final int CAPABILITY_SESSION_REESTABLISHMENT = 2;
    protected int mState;
    protected CopyOnWriteArrayList<ConnectionListener> mConnectionListeners;
    protected Presence mUserPresence;
    protected ImConnection() {
        mConnectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
        mState = DISCONNECTED;
    }
    public void addConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            mConnectionListeners.add(listener);
        }
    }
    public void removeConnectionListener(ConnectionListener listener) {
        mConnectionListeners.remove(listener);
    }
    public abstract Contact getLoginUser();
    public String getLoginUserName() {
        Contact loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getName();
    }
    public abstract int[] getSupportedPresenceStatus();
    public Presence getUserPresence() {
        if (mState == SUSPENDING || mState == SUSPENDED) {
            return new Presence();
        }
        if (mState != LOGGED_IN) {
            return null;
        }
        return new Presence(mUserPresence);
    }
    public void updateUserPresenceAsync(Presence newPresence) throws ImException {
        if (mState != LOGGED_IN) {
            throw new ImException(ImErrorInfo.NOT_LOGGED_IN, "NOT logged in");
        }
        doUpdateUserPresenceAsync(newPresence);
    }
    public void networkTypeChanged(){
    }
    public int getState() {
        return mState;
    }
    protected void setState(int state, ImErrorInfo error) {
        if(state < DISCONNECTED || state > SUSPENDED){
            throw new IllegalArgumentException("Invalid state: " + state);
        }
        if(mState != state){
            mState = state;
            for(ConnectionListener listener : mConnectionListeners){
                listener.onStateChanged(state, error);
            }
        }
    }
    protected void notifyUserPresenceUpdated() {
        for (ConnectionListener listener : mConnectionListeners) {
            listener.onUserPresenceUpdated();
        }
    }
    protected void notifyUpdateUserPresenceError(ImErrorInfo error) {
        for (ConnectionListener listener : mConnectionListeners) {
            listener.onUpdatePresenceError(error);
        }
    }
    public abstract int getCapability();
    public abstract void loginAsync(LoginInfo loginInfo);
    public abstract void reestablishSessionAsync(HashMap<String, String> sessionContext);
    public abstract void logoutAsync();
    public abstract void suspend();
    public abstract HashMap<String, String> getSessionContext();
    public abstract ChatSessionManager getChatSessionManager();
    public abstract ContactListManager getContactListManager();
    public abstract ChatGroupManager getChatGroupManager();
    protected abstract void doUpdateUserPresenceAsync(Presence presence);
}
