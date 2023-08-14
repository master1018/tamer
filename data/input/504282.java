public class ContactListAdapter extends IContactList.Stub {
    private ContactList mAdaptee;
    private long mDataBaseId;
    public ContactListAdapter(ContactList adaptee, long dataBaseId) {
        mAdaptee = adaptee;
        mDataBaseId = dataBaseId;
    }
    public long getDataBaseId() {
        return mDataBaseId;
    }
    public Address getAddress() {
        return mAdaptee.getAddress();
    }
    public int addContact(String address) {
        if (address == null) {
            Log.e(RemoteImService.TAG, "Address can't be null!");
            return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;
        }
        try {
            mAdaptee.addContact(address);
        } catch (IllegalArgumentException e) {
            return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;
        } catch (ImException e) {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public String getName() {
        return mAdaptee.getName();
    }
    public int removeContact(String address) {
        Contact contact = mAdaptee.getContact(address);
        if (contact == null) {
            return ImErrorInfo.ILLEGAL_CONTACT_ADDRESS;
        }
        try {
            mAdaptee.removeContact(contact);
        } catch (ImException e) {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }
    public void setDefault(boolean isDefault) {
        mAdaptee.setDefault(isDefault);
    }
    public boolean isDefault() {
        return mAdaptee.isDefault();
    }
    public void setName(String name) {
        if (name == null) {
            Log.e(RemoteImService.TAG, "Name can't be null!");
            return;
        }
        mAdaptee.setName(name);
    }
}
