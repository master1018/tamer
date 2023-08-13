public abstract class ContactListManager {
    public static final int LISTS_NOT_LOADED = 0;
    public static final int LISTS_LOADING = 1;
    public static final int BLOCKED_LIST_LOADED = 2;
    public static final int LISTS_LOADED = 3;
    protected ContactList mDefaultContactList;
    protected Vector<ContactList> mContactLists;
    protected CopyOnWriteArrayList<ContactListListener> mContactListListeners;
    protected SubscriptionRequestListener mSubscriptionRequestListener;
    protected Vector<Contact> mBlockedList;
    private int mState;
    private Vector<String> mBlockPending;
    private Vector<Contact> mDeletePending;
    protected ContactListManager() {
        mContactLists = new Vector<ContactList>();
        mContactListListeners = new CopyOnWriteArrayList<ContactListListener>();
        mBlockedList = new Vector<Contact>();
        mBlockPending = new Vector<String>(4);
        mDeletePending = new Vector<Contact>(4);
        mState = LISTS_NOT_LOADED;
    }
    protected synchronized void setState(int state) {
        if (state < LISTS_NOT_LOADED || state > LISTS_LOADED) {
            throw new IllegalArgumentException();
        }
        mState = state;
    }
    public synchronized int getState() {
        return mState;
    }
    public synchronized void addContactListListener(ContactListListener listener) {
        if ((listener != null) && !mContactListListeners.contains(listener)) {
            mContactListListeners.add(listener);
        }
    }
    public synchronized void removeContactListListener(ContactListListener listener) {
        mContactListListeners.remove(listener);
    }
    public synchronized void setSubscriptionRequestListener(
            SubscriptionRequestListener listener) {
        mSubscriptionRequestListener = listener;
    }
    public synchronized SubscriptionRequestListener getSubscriptionRequestListener() {
        return mSubscriptionRequestListener;
    }
    public Collection<ContactList> getContactLists() {
        return Collections.unmodifiableCollection(mContactLists);
    }
    public Contact getContact(Address address) {
        return getContact(address.getFullName());
    }
    public Contact getContact(String address) {
        for (ContactList list : mContactLists) {
            Contact c = list.getContact(address);
            if( c != null) {
                return c;
            }
        }
        return null;
    }
    public abstract String normalizeAddress(String address);
    public abstract Contact createTemporaryContact(String address);
    public boolean containsContact(Contact contact) {
        for (ContactList list : mContactLists) {
            if (list.containsContact(contact)) {
                return true;
            }
        }
        return false;
    }
    public ContactList getContactList(String name) {
        for (ContactList list : mContactLists) {
            if (list.getName() != null && list.getName().equals(name)) {
                return list;
            }
        }
        return null;
    }
    public ContactList getContactList(Address address) {
        for (ContactList list : mContactLists) {
            if (list.getAddress().equals(address)) {
                return list;
            }
        }
        return null;
    }
    public ContactList getDefaultContactList() throws ImException {
        checkState();
        return mDefaultContactList;
    }
    public void createContactListAsync(String name) throws ImException {
        createContactListAsync(name, null, false);
    }
    public void createContactListAsync(String name, boolean isDefault) throws ImException {
        createContactListAsync(name, null, isDefault);
    }
    public void createContactListAsync(String name,
            Collection<Contact> contacts) throws ImException {
        createContactListAsync(name, contacts, false);
    }
    public synchronized void createContactListAsync(String name,
            Collection<Contact> contacts, boolean isDefault) throws ImException {
        checkState();
        if (getContactList(name) != null) {
            throw new ImException(ImErrorInfo.CONTACT_LIST_EXISTS,
                    "Contact list already exists");
        }
        if (mContactLists.isEmpty()) {
            isDefault = true;
        }
        doCreateContactListAsync(name, contacts, isDefault);
    }
    public void deleteContactListAsync(String name) throws ImException {
        deleteContactListAsync(getContactList(name));
    }
    public synchronized void deleteContactListAsync(ContactList list) throws ImException {
        checkState();
        if (null == list) {
            throw new ImException(ImErrorInfo.CONTACT_LIST_NOT_FOUND,
                    "Contact list doesn't exist");
        }
        doDeleteContactListAsync(list);
    }
    public void blockContactAsync(Contact contact) throws ImException {
        blockContactAsync(contact.getAddress().getFullName());
    }
    public void blockContactAsync(String address) throws ImException {
        checkState();
        if(isBlocked(address)){
            return;
        }
        if (mBlockPending.contains(address)) {
            return;
        }
        doBlockContactAsync(address, true);
    }
    public void unblockContactAsync(Contact contact) throws ImException {
        unblockContactAsync(contact.getAddress().getFullName());
    }
    public void unblockContactAsync(String address) throws ImException {
        checkState();
        if(!isBlocked(address)) {
            return;
        }
        doBlockContactAsync(address, false);
    }
    protected void addContactToListAsync(String address, ContactList list)
            throws ImException {
        checkState();
        doAddContactToListAsync(address, list);
    }
    protected void removeContactFromListAsync(Contact contact, ContactList list)
            throws ImException {
        checkState();
        if (mDeletePending.contains(contact)) {
            return;
        }
        doRemoveContactFromListAsync(contact, list);
    }
    public List<Contact> getBlockedList() throws ImException {
        checkState();
        return Collections.unmodifiableList(mBlockedList);
    }
    public boolean isBlocked(Contact contact) throws ImException {
        return isBlocked(contact.getAddress().getFullName());
    }
    public synchronized boolean isBlocked(String address) throws ImException {
        if(mState < BLOCKED_LIST_LOADED) {
            throw new ImException(ImErrorInfo.ILLEGAL_CONTACT_LIST_MANAGER_STATE,
                "Blocked list hasn't been loaded");
        }
        for(Contact c : mBlockedList) {
            if(c.getAddress().getFullName().equals(address)){
                return true;
            }
        }
        return false;
    }
    protected void checkState() throws ImException {
        if (getConnection().getState() != ImConnection.LOGGED_IN) {
            throw new ImException(ImErrorInfo.CANT_CONNECT_TO_SERVER,
                    "Can't connect to server");
        }
        if (getState() != LISTS_LOADED) {
            throw new ImException(ImErrorInfo.ILLEGAL_CONTACT_LIST_MANAGER_STATE,
                    "Illegal contact list manager state");
        }
    }
    public abstract void loadContactListsAsync();
    public abstract void approveSubscriptionRequest(String contact);
    public abstract void declineSubscriptionRequest(String contact);
    protected abstract ImConnection getConnection();
    protected abstract void doBlockContactAsync(String address, boolean block);
    protected abstract void doCreateContactListAsync(String name,
            Collection<Contact> contacts, boolean isDefault);
    protected abstract void doDeleteContactListAsync(ContactList list);
    protected void notifyContactsPresenceUpdated(Contact[] contacts) {
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactsPresenceUpdate(contacts);
        }
    }
    protected void notifyContactError(int type, ImErrorInfo error,
            String listName, Contact contact) {
        if (type == ContactListListener.ERROR_REMOVING_CONTACT) {
            mDeletePending.remove(contact);
        } else if (type == ContactListListener.ERROR_BLOCKING_CONTACT) {
            mBlockPending.remove(contact.getAddress().getFullName());
        }
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactError(type, error, listName, contact);
        }
    }
    protected void notifyContactListLoaded(ContactList list) {
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactChange(ContactListListener.LIST_LOADED,
                    list, null);
        }
    }
    protected void notifyContactListsLoaded() {
        setState(LISTS_LOADED);
        for (ContactListListener listener : mContactListListeners) {
            listener.onAllContactListsLoaded();
        }
    }
    protected void notifyContactListUpdated(ContactList list, int type,
            Contact contact) {
        synchronized (this) {
            if (type == ContactListListener.LIST_CONTACT_ADDED) {
                list.insertToCache(contact);
            } else if (type == ContactListListener.LIST_CONTACT_REMOVED) {
                list.removeFromCache(contact);
                mDeletePending.remove(contact);
            }
        }
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactChange(type, list, contact);
        }
    }
    protected void notifyContactListNameUpdated(ContactList list, String name) {
        list.mName = name;
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactChange(ContactListListener.LIST_RENAMED,
                    list, null);
        }
    }
    protected void notifyContactListCreated(ContactList list) {
        synchronized (this) {
            if (list.isDefault()) {
                for (ContactList l : mContactLists) {
                    l.setDefault(false);
                }
                mDefaultContactList = list;
            }
            mContactLists.add(list);
        }
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactChange(ContactListListener.LIST_CREATED,
                    list, null);
        }
    }
    protected void notifyContactListDeleted(ContactList list) {
        synchronized(this) {
            mContactLists.remove(list);
            if (list.isDefault() && mContactLists.size() > 0) {
                mContactLists.get(0).setDefault(true);
            }
        }
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactChange(ContactListListener.LIST_DELETED,
                    list, null);
        }
    }
    protected void notifyBlockContact(Contact contact, boolean blocked) {
        synchronized (this) {
            if (blocked) {
                mBlockedList.add(contact);
                String addr = contact.getAddress().getFullName();
                mBlockPending.remove(addr);
            } else {
                mBlockedList.remove(contact);
            }
        }
        for (ContactListListener listener : mContactListListeners) {
            listener.onContactChange(blocked ? ContactListListener.CONTACT_BLOCKED
                    : ContactListListener.CONTACT_UNBLOCKED, null, contact);
        }
    }
    protected abstract void doAddContactToListAsync(String address,
            ContactList list) throws ImException;
    protected abstract void doRemoveContactFromListAsync(Contact contact, ContactList list);
    protected abstract void setListNameAsync(String name, ContactList list);
}
