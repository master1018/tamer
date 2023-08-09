public class ContactListManagerAdapter extends IContactListManager.Stub {
    static final String TAG = RemoteImService.TAG;
    ImConnectionAdapter mConn;
    ContentResolver     mResolver;
    private ContactListManager          mAdaptee;
    private ContactListListenerAdapter  mContactListListenerAdapter;
    private SubscriptionRequestListenerAdapter mSubscriptionListenerAdapter;
    final RemoteCallbackList<IContactListListener> mRemoteContactListeners
            = new RemoteCallbackList<IContactListListener>();
    final RemoteCallbackList<ISubscriptionListener> mRemoteSubscriptionListeners
            = new RemoteCallbackList<ISubscriptionListener>();
    HashMap<Address, ContactListAdapter> mContactLists;
    HashMap<String, Contact> mTemporaryContacts;
    HashSet<String> mValidatedContactLists;
    HashSet<String> mValidatedContacts;
    HashSet<String> mValidatedBlockedContacts;
    private long mAccountId;
    private long mProviderId;
    private Uri mAvatarUrl;
    private Uri mContactUrl;
    static final long FAKE_TEMPORARY_LIST_ID = -1;
    static final String[] CONTACT_LIST_ID_PROJECTION  = { Imps.ContactList._ID };
    RemoteImService mContext;
    public ContactListManagerAdapter(ImConnectionAdapter conn) {
        mAdaptee  = conn.getAdaptee().getContactListManager();
        mConn     = conn;
        mContext  = conn.getContext();
        mResolver = mContext.getContentResolver();
        mContactListListenerAdapter = new ContactListListenerAdapter();
        mSubscriptionListenerAdapter = new SubscriptionRequestListenerAdapter();
        mContactLists = new HashMap<Address, ContactListAdapter>();
        mTemporaryContacts = new HashMap<String, Contact>();
        mValidatedContacts = new HashSet<String>();
        mValidatedContactLists = new HashSet<String>();
        mValidatedBlockedContacts = new HashSet<String>();
        mAdaptee.addContactListListener(mContactListListenerAdapter);
        mAdaptee.setSubscriptionRequestListener(mSubscriptionListenerAdapter);
        mAccountId  = mConn.getAccountId();
        mProviderId = mConn.getProviderId();
        Uri.Builder builder = Imps.Avatars.CONTENT_URI_AVATARS_BY.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        mAvatarUrl = builder.build();
        builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        mContactUrl = builder.build();
    }
    public int createContactList(String name, List<Contact> contacts) {
        try {
            mAdaptee.createContactListAsync(name, contacts);
        } catch (ImException e) {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public int deleteContactList(String name) {
        try {
            mAdaptee.deleteContactListAsync(name);
        } catch (ImException e) {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public List getContactLists() {
        synchronized (mContactLists) {
            return new ArrayList<ContactListAdapter>(mContactLists.values());
        }
    }
    public int removeContact(String address) {
        if(isTemporary(address)) {
            closeChatSession(address);
            String selection = Imps.Contacts.USERNAME + "=?";
            String[] selectionArgs = { address };
            mResolver.delete(mContactUrl, selection, selectionArgs);
            synchronized (mTemporaryContacts) {
                mTemporaryContacts.remove(address);
            }
        } else {
            synchronized (mContactLists) {
                for(ContactListAdapter list : mContactLists.values()) {
                    int resCode = list.removeContact(address);
                    if (ImErrorInfo.ILLEGAL_CONTACT_ADDRESS == resCode) {
                        continue;
                    }
                    if (ImErrorInfo.NO_ERROR != resCode) {
                        return resCode;
                    }
                }
            }
        }
        return ImErrorInfo.NO_ERROR;
    }
    public void approveSubscription(String address) {
        mAdaptee.approveSubscriptionRequest(address);
    }
    public void declineSubscription(String address) {
        mAdaptee.declineSubscriptionRequest(address);
    }
    public int blockContact(String address) {
        try {
            mAdaptee.blockContactAsync(address);
        } catch (ImException e) {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public int unBlockContact(String address) {
        try {
            mAdaptee.unblockContactAsync(address);
        } catch (ImException e) {
            Log.e(TAG, e.getMessage());
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public boolean isBlocked(String address) {
        try {
            return mAdaptee.isBlocked(address);
        } catch (ImException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
    public void registerContactListListener(IContactListListener listener) {
        if (listener != null) {
            mRemoteContactListeners.register(listener);
        }
    }
    public void unregisterContactListListener(IContactListListener listener) {
        if (listener != null) {
            mRemoteContactListeners.unregister(listener);
        }
    }
    public void registerSubscriptionListener(ISubscriptionListener listener) {
        if (listener != null) {
            mRemoteSubscriptionListeners.register(listener);
        }
    }
    public void unregisterSubscriptionListener(ISubscriptionListener listener) {
        if (listener != null) {
            mRemoteSubscriptionListeners.unregister(listener);
        }
    }
    public IContactList getContactList(String name) {
        return getContactListAdapter(name);
    }
    public void loadContactLists() {
        if(mAdaptee.getState() == ContactListManager.LISTS_NOT_LOADED){
            clearValidatedContactsAndLists();
            mAdaptee.loadContactListsAsync();
        }
    }
    public int getState() {
        return mAdaptee.getState();
    }
    public Contact getContactByAddress(String address) {
        Contact c = mAdaptee.getContact(address);
        if(c == null) {
            synchronized (mTemporaryContacts) {
                return mTemporaryContacts.get(address);
            }
        } else {
            return c;
        }
    }
    public Contact createTemporaryContact(String address) {
        Contact c = mAdaptee.createTemporaryContact(address);
        insertTemporary(c);
        return c;
    }
    public long queryOrInsertContact(Contact c) {
        long result;
        String username = c.getAddress().getFullName();
        String selection = Imps.Contacts.USERNAME + "=?";
        String[] selectionArgs = { username };
        String[] projection = {Imps.Contacts._ID};
        Cursor cursor = mResolver.query(mContactUrl, projection, selection,
                selectionArgs, null);
        if(cursor != null && cursor.moveToFirst()) {
            result = cursor.getLong(0);
        } else {
            result = insertTemporary(c);
        }
        if(cursor != null) {
            cursor.close();
        }
        return result;
    }
    private long insertTemporary(Contact c) {
        synchronized (mTemporaryContacts) {
            mTemporaryContacts.put(c.getAddress().getFullName(), c);
        }
        Uri uri = insertContactContent(c, FAKE_TEMPORARY_LIST_ID);
        return ContentUris.parseId(uri);
    }
    public boolean isTemporary(String address) {
        synchronized (mTemporaryContacts) {
            return mTemporaryContacts.containsKey(address);
        }
    }
    ContactListAdapter getContactListAdapter(String name) {
        synchronized (mContactLists) {
            for (ContactListAdapter list : mContactLists.values()) {
                if (name.equals(list.getName())) {
                    return list;
                }
            }
            return null;
        }
    }
    ContactListAdapter getContactListAdapter(Address address) {
        synchronized (mContactLists) {
            return mContactLists.get(address);
        }
    }
    private class Exclusion {
        private StringBuilder mSelection;
        private List mSelectionArgs;
        private String mExclusionColumn;
        Exclusion(String exclusionColumn, Collection<String> items) {
            mSelection = new StringBuilder();
            mSelectionArgs = new ArrayList();
            mExclusionColumn = exclusionColumn;
            for (String s : items) {
                add(s);
            }
        }
        public void add(String exclusionItem) {
            if (mSelection.length()==0) {
                mSelection.append(mExclusionColumn + "!=?");
            } else {
                mSelection.append(" AND " + mExclusionColumn + "!=?");
            }
            mSelectionArgs.add(exclusionItem);
        }
        public String getSelection() {
            return mSelection.toString();
        }
        public String[] getSelectionArgs() {
            return (String []) mSelectionArgs.toArray(new String[0]);
        }
    }
    private void removeObsoleteContactsAndLists() {
        Exclusion exclusion = new Exclusion(Imps.Contacts.USERNAME, mValidatedContacts);
        mResolver.delete(mContactUrl, exclusion.getSelection(), exclusion.getSelectionArgs());
        exclusion = new Exclusion(Imps.BlockedList.USERNAME, mValidatedBlockedContacts);
        Uri.Builder builder = Imps.BlockedList.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        Uri uri = builder.build();
        mResolver.delete(uri, exclusion.getSelection(), exclusion.getSelectionArgs());
        exclusion = new Exclusion(Imps.ContactList.NAME, mValidatedContactLists);
        builder = Imps.ContactList.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        uri = builder.build();
        mResolver.delete(uri, exclusion.getSelection(), exclusion.getSelectionArgs());
    }
    final class ContactListListenerAdapter implements ContactListListener {
        private boolean mAllContactsLoaded;
        private class StoredContactChange {
            int mType;
            ContactList mList;
            Contact mContact;
            StoredContactChange(int type, ContactList list, Contact contact) {
                mType = type;
                mList = list;
                mContact = contact;
            }
        }
        private Vector<StoredContactChange> mDelayedContactChanges =
                new Vector<StoredContactChange>();
        public void onContactsPresenceUpdate(final Contact[] contacts) {
            updatePresenceContent(contacts);
            final int N = mRemoteContactListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IContactListListener listener =
                        mRemoteContactListeners.getBroadcastItem(i);
                try {
                    listener.onContactsPresenceUpdate(contacts);
                } catch (RemoteException e) {
                }
            }
            mRemoteContactListeners.finishBroadcast();
        }
        public void onContactChange(final int type, final ContactList list,
                final Contact contact) {
            ContactListAdapter removed = null;
            String notificationText = null;
            switch (type) {
            case LIST_LOADED:
            case LIST_CREATED:
                addContactListContent(list);
                break;
            case LIST_DELETED:
                removed = removeContactListFromDataBase(list.getName());
                if (!mAllContactsLoaded) {
                    if (!mValidatedContactLists.contains(list.getName())) {
                        mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
                    }
                }
                break;
            case LIST_CONTACT_ADDED:
                long listId = getContactListAdapter(list.getAddress()).getDataBaseId();
                String contactAddress = contact.getAddress().getFullName();
                if(isTemporary(contactAddress)){
                    moveTemporaryContactToList(contactAddress, listId);
                } else {
                    insertContactContent(contact, listId);
                }
                notificationText = mContext.getResources().getString(
                        R.string.add_contact_success, contact.getName());
                if (!mAllContactsLoaded) {
                    if (!mValidatedContactLists.contains(list.getName())) {
                        mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
                    }
                }
                break;
            case LIST_CONTACT_REMOVED:
                deleteContactFromDataBase(contact, list);
                if (!mAllContactsLoaded) {
                    if (!mValidatedContactLists.contains(list.getName())) {
                        mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
                    }
                }
                String address = contact.getAddress().getFullName();
                closeChatSession(address);
                notificationText = mContext.getResources().getString(
                        R.string.delete_contact_success, contact.getName());
                break;
            case LIST_RENAMED:
                updateListNameInDataBase(list);
                if (!mAllContactsLoaded) {
                    if (!mValidatedContactLists.contains(list.getName())) {
                        mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
                    }
                }
                break;
            case CONTACT_BLOCKED:
                insertBlockedContactToDataBase(contact);
                address = contact.getAddress().getFullName();
                updateContactType(address, Imps.Contacts.TYPE_BLOCKED);
                closeChatSession(address);
                notificationText = mContext.getResources().getString(
                        R.string.block_contact_success, contact.getName());
                break;
            case CONTACT_UNBLOCKED:
                removeBlockedContactFromDataBase(contact);
                notificationText = mContext.getResources().getString(
                        R.string.unblock_contact_success, contact.getName());
                if (!mAllContactsLoaded) {
                    if (!mValidatedBlockedContacts.contains(contact.getName())) {
                        mDelayedContactChanges.add(new StoredContactChange(type, list, contact));
                    }
                }
                break;
            default:
                Log.e(TAG, "Unknown list update event!");
                break;
            }
            final ContactListAdapter listAdapter;
            if (type == LIST_DELETED) {
                listAdapter = removed;
            } else {
                listAdapter = (list == null) ? null
                        : getContactListAdapter(list.getAddress());
            }
            final int N = mRemoteContactListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IContactListListener listener =
                        mRemoteContactListeners.getBroadcastItem(i);
                try {
                    listener.onContactChange(type, listAdapter, contact);
                } catch (RemoteException e) {
                }
            }
            mRemoteContactListeners.finishBroadcast();
            if (mAllContactsLoaded && notificationText != null) {
                mContext.showToast(notificationText, Toast.LENGTH_SHORT);
            }
        }
        public void onContactError(final int errorType, final ImErrorInfo error,
                final String listName, final Contact contact) {
            final int N = mRemoteContactListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IContactListListener listener =
                        mRemoteContactListeners.getBroadcastItem(i);
                try {
                    listener.onContactError(errorType, error, listName, contact);
                } catch (RemoteException e) {
                }
            }
            mRemoteContactListeners.finishBroadcast();
        }
        public void handleDelayedContactChanges() {
            for (StoredContactChange change : mDelayedContactChanges) {
                onContactChange(change.mType, change.mList, change.mContact);
            }
        }
        public void onAllContactListsLoaded() {
            mAllContactsLoaded = true;
            handleDelayedContactChanges();
            removeObsoleteContactsAndLists();
            final int N = mRemoteContactListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IContactListListener listener =
                        mRemoteContactListeners.getBroadcastItem(i);
                try {
                    listener.onAllContactListsLoaded();
                } catch (RemoteException e) {
                }
            }
            mRemoteContactListeners.finishBroadcast();
        }
    }
    final class SubscriptionRequestListenerAdapter
            implements SubscriptionRequestListener {
        public void onSubScriptionRequest(final Contact from) {
            String username = from.getAddress().getFullName();
            String nickname = from.getName();
            Uri uri = insertOrUpdateSubscription(username, nickname,
                    Imps.Contacts.SUBSCRIPTION_TYPE_FROM,
                    Imps.Contacts.SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING);
            mContext.getStatusBarNotifier().notifySubscriptionRequest(mProviderId, mAccountId,
                    ContentUris.parseId(uri), username, nickname);
            final int N = mRemoteSubscriptionListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                ISubscriptionListener listener =
                    mRemoteSubscriptionListeners.getBroadcastItem(i);
                try {
                    listener.onSubScriptionRequest(from);
                } catch (RemoteException e) {
                }
            }
            mRemoteSubscriptionListeners.finishBroadcast();
        }
        public void onSubscriptionApproved(final String contact) {
            insertOrUpdateSubscription(contact, null,
                    Imps.Contacts.SUBSCRIPTION_TYPE_NONE,
                    Imps.Contacts.SUBSCRIPTION_STATUS_NONE);
            final int N = mRemoteSubscriptionListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                ISubscriptionListener listener =
                    mRemoteSubscriptionListeners.getBroadcastItem(i);
                try {
                    listener.onSubscriptionApproved(contact);
                } catch (RemoteException e) {
                }
            }
            mRemoteSubscriptionListeners.finishBroadcast();
        }
        public void onSubscriptionDeclined(final String contact) {
            insertOrUpdateSubscription(contact, null,
                    Imps.Contacts.SUBSCRIPTION_TYPE_NONE,
                    Imps.Contacts.SUBSCRIPTION_STATUS_NONE);
            final int N = mRemoteSubscriptionListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                ISubscriptionListener listener =
                    mRemoteSubscriptionListeners.getBroadcastItem(i);
                try {
                    listener.onSubscriptionDeclined(contact);
                } catch (RemoteException e) {
                }
            }
            mRemoteSubscriptionListeners.finishBroadcast();
        }
        public void onApproveSubScriptionError(final String contact, final ImErrorInfo error) {
            String displayableAddress = getDisplayableAddress(contact);
            String msg = mContext.getString(R.string.approve_subscription_error, displayableAddress);
            mContext.showToast(msg, Toast.LENGTH_SHORT);
        }
        public void onDeclineSubScriptionError(final String contact, final ImErrorInfo error) {
            String displayableAddress = getDisplayableAddress(contact);
            String msg = mContext.getString(R.string.decline_subscription_error, displayableAddress);
            mContext.showToast(msg, Toast.LENGTH_SHORT);
        }
    }
    String getDisplayableAddress(String impsAddress) {
        if (impsAddress.startsWith("wv:")) {
            return impsAddress.substring(3);
        }
        return impsAddress;
    }
    void insertBlockedContactToDataBase(Contact contact) {
        removeBlockedContactFromDataBase(contact);
        Uri.Builder builder = Imps.BlockedList.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        Uri uri = builder.build();
        String username = contact.getAddress().getFullName();
        ContentValues values = new ContentValues(2);
        values.put(Imps.BlockedList.USERNAME, username);
        values.put(Imps.BlockedList.NICKNAME, contact.getName());
        mResolver.insert(uri, values);
        mValidatedBlockedContacts.add(username);
    }
    void removeBlockedContactFromDataBase(Contact contact) {
        String address = contact.getAddress().getFullName();
        Uri.Builder builder = Imps.BlockedList.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);
        Uri uri = builder.build();
        mResolver.delete(uri, Imps.BlockedList.USERNAME + "=?", new String[]{ address });
        int type = isTemporary(address) ? Imps.Contacts.TYPE_TEMPORARY
                : Imps.Contacts.TYPE_NORMAL;
        updateContactType(address, type);
    }
    void moveTemporaryContactToList(String address, long listId) {
        synchronized (mTemporaryContacts) {
            mTemporaryContacts.remove(address);
        }
        ContentValues values = new ContentValues(2);
        values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_NORMAL);
        values.put(Imps.Contacts.CONTACTLIST, listId);
        String selection = Imps.Contacts.USERNAME + "=? AND " + Imps.Contacts.TYPE + "="
                + Imps.Contacts.TYPE_TEMPORARY;
        String[] selectionArgs = { address };
        mResolver.update(mContactUrl, values, selection, selectionArgs);
    }
    void updateContactType(String address, int type) {
        ContentValues values = new ContentValues(1);
        values.put(Imps.Contacts.TYPE, type);
        updateContact(address, values);
    }
    Uri insertOrUpdateSubscription(String username, String nickname, int subscriptionType,
            int subscriptionStatus) {
        Cursor cursor = mResolver.query(mContactUrl, new String[]{ Imps.Contacts._ID },
                Imps.Contacts.USERNAME + "=?", new String[]{username}, null);
        if (cursor == null) {
            Log.w(TAG, "query contact " + username + " failed");
            return null;
        }
        Uri uri;
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues(2);
            values.put(Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionType);
            values.put(Imps.Contacts.SUBSCRIPTION_STATUS, subscriptionStatus);
            long contactId = cursor.getLong(0);
            uri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId);
            mResolver.update(uri, values, null, null);
        } else {
            ContentValues values = new ContentValues(6);
            values.put(Imps.Contacts.USERNAME, username);
            values.put(Imps.Contacts.NICKNAME, nickname);
            values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_NORMAL);
            values.put(Imps.Contacts.CONTACTLIST, FAKE_TEMPORARY_LIST_ID);
            values.put(Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionType);
            values.put(Imps.Contacts.SUBSCRIPTION_STATUS, subscriptionStatus);
            uri = mResolver.insert(mContactUrl, values);
        }
        cursor.close();
        return uri;
    }
    void updateContact(String username, ContentValues values) {
        String selection = Imps.Contacts.USERNAME + "=?";
        String[] selectionArgs = { username };
        mResolver.update(mContactUrl, values, selection, selectionArgs);
    }
    void updatePresenceContent(Contact[] contacts) {
        ArrayList<String> usernames = new ArrayList<String>();
        ArrayList<String> statusArray = new ArrayList<String>();
        ArrayList<String> customStatusArray = new ArrayList<String>();
        ArrayList<String> clientTypeArray = new ArrayList<String>();
        for(Contact c : contacts) {
            String username = c.getAddress().getFullName();
            Presence p = c.getPresence();
            int status = convertPresenceStatus(p);
            String customStatus = p.getStatusText();
            int clientType = translateClientType(p);
            usernames.add(username);
            statusArray.add(String.valueOf(status));
            customStatusArray.add(customStatus);
            clientTypeArray.add(String.valueOf(clientType));
        }
        ContentValues values = new ContentValues();
        values.put(Imps.Contacts.ACCOUNT, mAccountId);
        values.putStringArrayList(Imps.Contacts.USERNAME, usernames);
        values.putStringArrayList(Imps.Presence.PRESENCE_STATUS, statusArray);
        values.putStringArrayList(Imps.Presence.PRESENCE_CUSTOM_STATUS, customStatusArray);
        values.putStringArrayList(Imps.Presence.CONTENT_TYPE, clientTypeArray);
        mResolver.update(Imps.Presence.BULK_CONTENT_URI, values, null, null);
    }
    void updateAvatarsContent(Contact[] contacts) {
        ArrayList<ContentValues> avatars = new ArrayList<ContentValues>();
        ArrayList<String> usernames = new ArrayList<String>();
        for (Contact contact : contacts) {
            byte[] avatarData = contact.getPresence().getAvatarData();
            if (avatarData == null) {
                continue;
            }
            String username = contact.getAddress().getFullName();
            ContentValues values = new ContentValues(2);
            values.put(Imps.Avatars.CONTACT, username);
            values.put(Imps.Avatars.DATA, avatarData);
            avatars.add(values);
            usernames.add(username);
        }
        if (avatars.size() > 0) {
            mResolver.bulkInsert(mAvatarUrl, avatars.toArray(
                    new ContentValues[avatars.size()]));
            Intent i = new Intent(ImServiceConstants.ACTION_AVATAR_CHANGED);
            i.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, usernames);
            i.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, mProviderId);
            i.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, mAccountId);
            mContext.sendBroadcast(i);
        }
    }
    ContactListAdapter removeContactListFromDataBase(String name) {
        ContactListAdapter listAdapter = getContactListAdapter(name);
        if (listAdapter == null) {
            return null;
        }
        long id = listAdapter.getDataBaseId();
        mResolver.delete(mContactUrl,
            Imps.Contacts.CONTACTLIST + "=?", new String[]{Long.toString(id)});
        mResolver.delete(ContentUris.withAppendedId(Imps.ContactList.CONTENT_URI, id), null, null);
        synchronized (mContactLists) {
            return mContactLists.remove(listAdapter.getAddress());
        }
    }
    void addContactListContent(ContactList list) {
        String selection = Imps.ContactList.NAME + "=? AND "
                + Imps.ContactList.PROVIDER + "=? AND "
                + Imps.ContactList.ACCOUNT + "=?";
        String[] selectionArgs = { list.getName(),
                Long.toString(mProviderId),
                Long.toString(mAccountId) };
        Cursor cursor = mResolver.query(Imps.ContactList.CONTENT_URI,
                                        CONTACT_LIST_ID_PROJECTION,
                                        selection,
                                        selectionArgs,
                                        null); 
        long listId = 0;
        Uri uri = null;
        try {
            if (cursor.moveToFirst()) {
                listId = cursor.getLong(0);
                uri = ContentUris.withAppendedId(Imps.ContactList.CONTENT_URI, listId);
            }
        } finally {
            cursor.close();
        }
        if (uri != null) {
            mResolver.delete(mContactUrl, Imps.Contacts.CONTACTLIST + "=?",
                    new String[]{Long.toString(listId)});
            mResolver.delete(uri, selection, selectionArgs);
        }
        ContentValues contactListValues = new ContentValues(3);
        contactListValues.put(Imps.ContactList.NAME, list.getName());
        contactListValues.put(Imps.ContactList.PROVIDER, mProviderId);
        contactListValues.put(Imps.ContactList.ACCOUNT, mAccountId);
        mValidatedContactLists.add(list.getName());
        uri = mResolver.insert(Imps.ContactList.CONTENT_URI, contactListValues);
        listId = ContentUris.parseId(uri);
        synchronized (mContactLists) {
            mContactLists.put(list.getAddress(),
                    new ContactListAdapter(list, listId));
        }
        Collection<Contact> contacts = list.getContacts();
        if (contacts == null || contacts.size() == 0) {
            return;
        }
        Iterator<Contact> iter = contacts.iterator();
        while(iter.hasNext()) {
            Contact c = iter.next();
            String address = c.getAddress().getFullName();
            if(isTemporary(address)) {
                moveTemporaryContactToList(address, listId);
                iter.remove();
            }
            mValidatedContacts.add(address);
        }
        ArrayList<String> usernames = new ArrayList<String>();
        ArrayList<String> nicknames = new ArrayList<String>();
        ArrayList<String> contactTypeArray = new ArrayList<String>();
        for (Contact c : contacts) {
            String username = c.getAddress().getFullName();
            String nickname = c.getName();
            int type = Imps.Contacts.TYPE_NORMAL;
            if(isTemporary(username)) {
                type = Imps.Contacts.TYPE_TEMPORARY;
            }
            if (isBlocked(username)) {
                type = Imps.Contacts.TYPE_BLOCKED;
            }
            usernames.add(username);
            nicknames.add(nickname);
            contactTypeArray.add(String.valueOf(type));
        }
        ContentValues values = new ContentValues(6);
        values.put(Imps.Contacts.PROVIDER, mProviderId);
        values.put(Imps.Contacts.ACCOUNT, mAccountId);
        values.put(Imps.Contacts.CONTACTLIST, listId);
        values.putStringArrayList(Imps.Contacts.USERNAME, usernames);
        values.putStringArrayList(Imps.Contacts.NICKNAME, nicknames);
        values.putStringArrayList(Imps.Contacts.TYPE, contactTypeArray);
        mResolver.insert(Imps.Contacts.BULK_CONTENT_URI, values);
    }
    void updateListNameInDataBase(ContactList list) {
        ContactListAdapter listAdapter = getContactListAdapter(list.getAddress());
        Uri uri = ContentUris.withAppendedId(Imps.ContactList.CONTENT_URI, listAdapter.getDataBaseId());
        ContentValues values = new ContentValues(1);
        values.put(Imps.ContactList.NAME, list.getName());
        mResolver.update(uri, values, null, null);
    }
    void deleteContactFromDataBase(Contact contact, ContactList list) {
        String selection = Imps.Contacts.USERNAME
                + "=? AND " + Imps.Contacts.CONTACTLIST + "=?";
        long listId = getContactListAdapter(list.getAddress()).getDataBaseId();
        String username = contact.getAddress().getFullName();
        String[] selectionArgs = {username, Long.toString(listId)};
        mResolver.delete(mContactUrl, selection, selectionArgs);
        if(mAdaptee.getContact(contact.getAddress()) == null) {
            clearHistoryMessages(username);
        }
    }
    Uri insertContactContent(Contact contact, long listId) {
        ContentValues values = getContactContentValues(contact, listId);
        Uri uri = mResolver.insert(mContactUrl, values);
        ContentValues presenceValues = getPresenceValues(ContentUris.parseId(uri),
                contact.getPresence());
        mResolver.insert(Imps.Presence.CONTENT_URI, presenceValues);
        return uri;
    }
    private ContentValues getContactContentValues(Contact contact, long listId) {
        final String username = contact.getAddress().getFullName();
        final String nickname = contact.getName();
        int type = Imps.Contacts.TYPE_NORMAL;
        if(isTemporary(username)) {
            type = Imps.Contacts.TYPE_TEMPORARY;
        }
        if (isBlocked(username)) {
            type = Imps.Contacts.TYPE_BLOCKED;
        }
        ContentValues values = new ContentValues(4);
        values.put(Imps.Contacts.USERNAME, username);
        values.put(Imps.Contacts.NICKNAME, nickname);
        values.put(Imps.Contacts.CONTACTLIST, listId);
        values.put(Imps.Contacts.TYPE, type);
        return values;
    }
    void clearHistoryMessages(String contact) {
        Uri uri = Imps.Messages.getContentUriByContact(mAccountId, contact);
        mResolver.delete(uri, null, null);
    }
    private ContentValues getPresenceValues(long contactId, Presence p) {
        ContentValues values = new ContentValues(3);
        values.put(Imps.Presence.CONTACT_ID, contactId);
        values.put(Imps.Contacts.PRESENCE_STATUS, convertPresenceStatus(p));
        values.put(Imps.Contacts.PRESENCE_CUSTOM_STATUS, p.getStatusText());
        values.put(Imps.Presence.CLIENT_TYPE, translateClientType(p));
        return values;
    }
    private int translateClientType(Presence presence) {
        int clientType = presence.getClientType();
        switch (clientType) {
            case Presence.CLIENT_TYPE_MOBILE:
                return Imps.Presence.CLIENT_TYPE_MOBILE;
            default:
                return Imps.Presence.CLIENT_TYPE_DEFAULT;
        }
    }
    public static int convertPresenceStatus(Presence presence) {
        switch (presence.getStatus()) {
        case Presence.AVAILABLE:
            return Imps.Presence.AVAILABLE;
        case Presence.IDLE:
            return Imps.Presence.IDLE;
        case Presence.AWAY:
            return Imps.Presence.AWAY;
        case Presence.DO_NOT_DISTURB:
            return Imps.Presence.DO_NOT_DISTURB;
        case Presence.OFFLINE:
            return Imps.Presence.OFFLINE;
        }
        Log.e(TAG, "Illegal presence status value " + presence.getStatus());
        return Imps.Presence.AVAILABLE;
    }
    public void clearOnLogout() {
        clearValidatedContactsAndLists();
        clearTemporaryContacts();
        clearPresence();
    }
    private void clearValidatedContactsAndLists() {
        mValidatedContacts.clear();
        mValidatedContactLists.clear();
        mValidatedBlockedContacts.clear();
    }
    private void clearTemporaryContacts() {
        String selection = Imps.Contacts.CONTACTLIST + "=" + FAKE_TEMPORARY_LIST_ID;
        mResolver.delete(mContactUrl, selection, null);
    }
    void clearPresence() {
        StringBuilder where = new StringBuilder();
        where.append(Imps.Presence.CONTACT_ID);
        where.append(" in (select _id from contacts where ");
        where.append(Imps.Contacts.ACCOUNT);
        where.append("=");
        where.append(mAccountId);
        where.append(")");
        mResolver.delete(Imps.Presence.CONTENT_URI, where.toString(), null);
    }
    void closeChatSession(String address) {
        ChatSessionManagerAdapter sessionManager =
            (ChatSessionManagerAdapter) mConn.getChatSessionManager();
        ChatSessionAdapter session =
            (ChatSessionAdapter) sessionManager.getChatSession(address);
        if(session != null) {
            session.leave();
        }
    }
    void updateChatPresence(String address, String nickname, Presence p) {
        ChatSessionManagerAdapter sessionManager =
            (ChatSessionManagerAdapter) mConn.getChatSessionManager();
        ChatSessionAdapter session =
            (ChatSessionAdapter) sessionManager.getChatSession(address);
        if(session != null) {
            session.insertPresenceUpdatesMsg(nickname, p);
        }
    }
}
