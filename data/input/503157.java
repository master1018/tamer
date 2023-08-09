public class ContactListListenerAdapter extends IContactListListener.Stub {
    private static final String TAG = ImApp.LOG_TAG;
    private final SimpleAlertHandler mHandler;
    public ContactListListenerAdapter(SimpleAlertHandler handler) {
        mHandler = handler;
    }
    public void onContactChange(int type, IContactList list,
            Contact contact) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onContactListChanged(" + type + ", " + list + ", "
                    + contact + ")");
        }
    }
    public void onAllContactListsLoaded() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onAllContactListsLoaded");
        }
    }
    public void onContactsPresenceUpdate(Contact[] contacts) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onContactsPresenceUpdate(" + contacts.length + ")");
        }
    }
    public void onContactError(int errorType, ImErrorInfo error,
            String listName, Contact contact) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onContactError(" + errorType + ", " + error + ", "
                    + listName + ", " + contact + ")");
        }
        mHandler.showContactError(errorType, error, listName, contact);
    }
}
