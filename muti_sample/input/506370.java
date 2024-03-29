public class ImConnectionAdapter extends IImConnection.Stub {
    private static final String TAG = RemoteImService.TAG;
    private static final String[] SESSION_COOKIE_PROJECTION = {
        Imps.SessionCookies.NAME,
        Imps.SessionCookies.VALUE,
    };
    private static final int COLUMN_SESSION_COOKIE_NAME = 0;
    private static final int COLUMN_SESSION_COOKIE_VALUE = 1;
    ImConnection mConnection;
    private ConnectionListenerAdapter mConnectionListener;
    private InvitationListenerAdapter mInvitationListener;
    final RemoteCallbackList<IConnectionListener> mRemoteConnListeners
            = new RemoteCallbackList<IConnectionListener>();
    ChatSessionManagerAdapter mChatSessionManager;
    ContactListManagerAdapter mContactListManager;
    ChatGroupManager mGroupManager;
    RemoteImService mService;
    long mProviderId = -1;
    long mAccountId = -1;
    boolean mAutoLoadContacts;
    int mConnectionState = ImConnection.DISCONNECTED;
    public ImConnectionAdapter(long providerId, ImConnection connection,
            RemoteImService service) {
        mProviderId = providerId;
        mConnection = connection;
        mService = service;
        mConnectionListener = new ConnectionListenerAdapter();
        mConnection.addConnectionListener(mConnectionListener);
        if ((connection.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0) {
            mGroupManager = mConnection.getChatGroupManager();
            mInvitationListener = new InvitationListenerAdapter();
            mGroupManager.setInvitationListener(mInvitationListener);
        }
    }
    public ImConnection getAdaptee() {
        return mConnection;
    }
    public RemoteImService getContext() {
        return mService;
    }
    public long getProviderId() {
        return mProviderId;
    }
    public long getAccountId() {
        return mAccountId;
    }
    public int[] getSupportedPresenceStatus() {
        return mConnection.getSupportedPresenceStatus();
    }
    public void networkTypeChanged() {
        mConnection.networkTypeChanged();
    }
    void reestablishSession() {
        mConnectionState = ImConnection.LOGGING_IN;
        ContentResolver cr = mService.getContentResolver();
        if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0) {
            HashMap<String, String> cookie = querySessionCookie(cr);
            if (cookie != null) {
                Log.d(TAG, "re-establish session");
                try {
                    mConnection.reestablishSessionAsync(cookie);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid session cookie, probably modified by others.");
                    clearSessionCookie(cr);
                }
            }
        }
    }
    private Uri getSessionCookiesUri() {
        Uri.Builder builder = Imps.SessionCookies.CONTENT_URI_SESSION_COOKIES_BY.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        return builder.build();
    }
    public void login(long accountId, String userName, String password,
            boolean autoLoadContacts) {
        mAccountId = accountId;
        mAutoLoadContacts = autoLoadContacts;
        mConnectionState = ImConnection.LOGGING_IN;
        mConnection.loginAsync(new LoginInfo(userName, password));
        mChatSessionManager = new ChatSessionManagerAdapter(this);
        mContactListManager = new ContactListManagerAdapter(this);
    }
    private HashMap<String, String> querySessionCookie(ContentResolver cr) {
        Cursor c = cr.query(getSessionCookiesUri(), SESSION_COOKIE_PROJECTION, null, null, null);
        if (c == null) {
            return null;
        }
        HashMap<String, String> cookie = null;
        if (c.getCount() > 0) {
            cookie = new HashMap<String, String>();
            while(c.moveToNext()) {
                cookie.put(c.getString(COLUMN_SESSION_COOKIE_NAME),
                    c.getString(COLUMN_SESSION_COOKIE_VALUE));
            }
        }
        c.close();
        return cookie;
    }
    public void logout() {
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logoutAsync();
    }
    public synchronized void cancelLogin() {
        if (mConnectionState >= ImConnection.LOGGED_IN) {
            return;
        }
        logout();
    }
    void suspend() {
        mConnectionState = ImConnection.SUSPENDING;
        mConnection.suspend();
    }
    public void registerConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            mRemoteConnListeners.register(listener);
        }
    }
    public void unregisterConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            mRemoteConnListeners.unregister(listener);
        }
    }
    public void setInvitationListener(IInvitationListener listener) {
        if(mInvitationListener != null) {
            mInvitationListener.mRemoteListener = listener;
        }
    }
    public IChatSessionManager getChatSessionManager() {
        return mChatSessionManager;
    }
    public IContactListManager getContactListManager() {
        return mContactListManager;
    }
    public int getChatSessionCount() {
        if (mChatSessionManager == null) {
            return 0;
        }
        return mChatSessionManager.getChatSessionCount();
    }
    public Contact getLoginUser() {
        return mConnection.getLoginUser();
    }
    public Presence getUserPresence() {
        return mConnection.getUserPresence();
    }
    public int updateUserPresence(Presence newPresence) {
        try {
            mConnection.updateUserPresenceAsync(newPresence);
        } catch (ImException e) {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public int getState() {
        return mConnectionState;
    }
    public void rejectInvitation(long id){
        handleInvitation(id, false);
    }
    public void acceptInvitation(long id) {
        handleInvitation(id, true);
    }
    private void handleInvitation(long id, boolean accept) {
        if(mGroupManager == null) {
            return;
        }
        ContentResolver cr = mService.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(Imps.Invitation.CONTENT_URI, id), null, null, null, null);
        if(c == null) {
            return;
        }
        if(c.moveToFirst()) {
            String inviteId = c.getString(c.getColumnIndexOrThrow(Imps.Invitation.INVITE_ID));
            int status;
            if(accept) {
                mGroupManager.acceptInvitationAsync(inviteId);
                status = Imps.Invitation.STATUS_ACCEPTED;
            } else {
                mGroupManager.rejectInvitationAsync(inviteId);
                status = Imps.Invitation.STATUS_REJECTED;
            }
            c.updateInt(c.getColumnIndexOrThrow(Imps.Invitation.STATUS), status);
            c.commitUpdates();
        }
        c.close();
    }
    void saveSessionCookie(ContentResolver cr) {
        HashMap<String, String> cookies = mConnection.getSessionContext();
        int i = 0;
        ContentValues[] valuesList = new ContentValues[cookies.size()];
        for(Map.Entry<String,String> entry : cookies.entrySet()){
            ContentValues values = new ContentValues(2);
            values.put(Imps.SessionCookies.NAME, entry.getKey());
            values.put(Imps.SessionCookies.VALUE, entry.getValue());
            valuesList[i++] = values;
        }
        cr.bulkInsert(getSessionCookiesUri(), valuesList);
    }
    void clearSessionCookie(ContentResolver cr) {
        cr.delete(getSessionCookiesUri(), null, null);
    }
    void updateAccountStatusInDb() {
        Presence p = getUserPresence();
        int presenceStatus = Imps.Presence.OFFLINE;
        int connectionStatus = convertConnStateForDb(mConnectionState);
        if (p != null) {
            presenceStatus = ContactListManagerAdapter.convertPresenceStatus(p);
        }
        ContentResolver cr = mService.getContentResolver();
        Uri uri = Imps.AccountStatus.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(Imps.AccountStatus.ACCOUNT, mAccountId);
        values.put(Imps.AccountStatus.PRESENCE_STATUS, presenceStatus);
        values.put(Imps.AccountStatus.CONNECTION_STATUS, connectionStatus);
        cr.insert(uri, values);
    }
    private static int convertConnStateForDb(int state) {
        switch (state) {
        case ImConnection.DISCONNECTED:
        case ImConnection.LOGGING_OUT:
            return Imps.ConnectionStatus.OFFLINE;
        case ImConnection.LOGGING_IN:
            return Imps.ConnectionStatus.CONNECTING;
        case ImConnection.LOGGED_IN:
            return Imps.ConnectionStatus.ONLINE;
        case ImConnection.SUSPENDED:
        case ImConnection.SUSPENDING:
            return Imps.ConnectionStatus.SUSPENDED;
        default:
            return Imps.ConnectionStatus.OFFLINE;
        }
    }
    final class ConnectionListenerAdapter implements ConnectionListener{
        public void onStateChanged(final int state, final ImErrorInfo error) {
            synchronized (this) {
                if (state == ImConnection.LOGGED_IN
                        && mConnectionState == ImConnection.LOGGING_OUT) {
                    return;
                }
                if (state != ImConnection.DISCONNECTED) {
                    mConnectionState = state;
                }
            }
            ContentResolver cr = mService.getContentResolver();
            if(state == ImConnection.LOGGED_IN) {
                if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0){
                    saveSessionCookie(cr);
                }
                if(mAutoLoadContacts && mContactListManager.getState()
                        != ContactListManager.LISTS_LOADED) {
                    mContactListManager.loadContactLists();
                }
                for (ChatSessionAdapter session : mChatSessionManager.mActiveSessions.values()) {
                    session.sendPostponedMessages();
                }
            } else if (state == ImConnection.LOGGING_OUT) {
                mService.removeConnection(ImConnectionAdapter.this);
            } else if(state == ImConnection.DISCONNECTED) {
                mService.removeConnection(ImConnectionAdapter.this);
                clearSessionCookie(cr);
                if (mContactListManager != null) {
                    mContactListManager.clearOnLogout();
                }
                if (mChatSessionManager != null) {
                    mChatSessionManager.closeAllChatSessions();
                }
                mConnectionState = state;
            } else if(state == ImConnection.SUSPENDED && error != null) {
                mService.scheduleReconnect(15000);
            }
            updateAccountStatusInDb();
            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onStateChanged(ImConnectionAdapter.this, state, error);
                } catch (RemoteException e) {
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }
        public void onUserPresenceUpdated() {
            updateAccountStatusInDb();
            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onUserPresenceUpdated(ImConnectionAdapter.this);
                } catch (RemoteException e) {
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }
        public void onUpdatePresenceError(final ImErrorInfo error) {
            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onUpdatePresenceError(ImConnectionAdapter.this, error);
                } catch (RemoteException e) {
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }
    }
    final class InvitationListenerAdapter implements InvitationListener {
        IInvitationListener mRemoteListener;
        public void onGroupInvitation(Invitation invitation) {
            String sender = invitation.getSender().getScreenName();
            ContentValues values = new ContentValues(7);
            values.put(Imps.Invitation.PROVIDER, mProviderId);
            values.put(Imps.Invitation.ACCOUNT, mAccountId);
            values.put(Imps.Invitation.INVITE_ID, invitation.getInviteID());
            values.put(Imps.Invitation.SENDER, sender);
            values.put(Imps.Invitation.GROUP_NAME, invitation.getGroupAddress().getScreenName());
            values.put(Imps.Invitation.NOTE, invitation.getReason());
            values.put(Imps.Invitation.STATUS, Imps.Invitation.STATUS_PENDING);
            ContentResolver resolver = mService.getContentResolver();
            Uri uri = resolver.insert(Imps.Invitation.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            try {
                if (mRemoteListener != null) {
                    mRemoteListener.onGroupInvitation(id);
                    return;
                }
            } catch (RemoteException e) {
                Log.i(TAG, "onGroupInvitation: dead listener "
                        + mRemoteListener +"; removing");
                mRemoteListener = null;
            }
            mService.getStatusBarNotifier().notifyGroupInvitation(mProviderId, mAccountId, id, sender);
        }
    }
}
