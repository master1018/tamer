public class ImpsContactListAddress extends ImpsAddress{
    public ImpsContactListAddress() {
    }
    public ImpsContactListAddress(ImpsAddress userAddress, String name) {
        super(userAddress.getUser(), name, userAddress.getDomain());
        if(mResource == null) {
            throw new IllegalArgumentException("resource can not be null");
        }
    }
    public ImpsContactListAddress(String full, boolean verify) {
        super(full, verify);
        if(mResource == null) {
            throw new IllegalArgumentException("resource can not be null");
        }
    }
    public ImpsContactListAddress(String full) {
        this(full, false);
    }
    @Override
    public PrimitiveElement toPrimitiveElement() {
        PrimitiveElement contactList = new PrimitiveElement(ImpsTags.ContactList);
        contactList.setContents(getFullName());
        return contactList;
    }
    @Override
    public String getScreenName() {
        return getResource();
    }
    @Override
    public ImEntity getEntity(ImpsConnection connection) {
        ContactListManager manager = connection.getContactListManager();
        for(ContactList list : manager.getContactLists()) {
            if(list.getAddress().equals(this)) {
                return list;
            }
        }
        return null;
    }
}
