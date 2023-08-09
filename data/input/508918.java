public class ImpsUserAddress extends ImpsAddress{
    public ImpsUserAddress() {
    }
    public ImpsUserAddress(String full, boolean verify) {
        super(full, verify);
        if(verify && (mUser == null || mResource != null)) {
            throw new IllegalArgumentException();
        }
    }
    public ImpsUserAddress(String full) {
        this(full, false);
    }
    public ImpsUserAddress(String user, String domain) {
        super(user, null, domain);
    }
    @Override
    public PrimitiveElement toPrimitiveElement() {
        PrimitiveElement user = new PrimitiveElement(ImpsTags.User);
        user.addChild(ImpsTags.UserID, getFullName());
        return user;
    }
    @Override
    public String getScreenName() {
        return mUser;
    }
    @Override
    public ImEntity getEntity(ImpsConnection connection) {
        ContactListManager manager = connection.getContactListManager();
        for(ContactList list : manager.getContactLists()) {
            Contact contact = list.getContact(this);
            if(contact != null) {
                return contact;
            }
        }
        return new Contact(this, this.getUser());
    }
}
