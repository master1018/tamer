public class ImpsGroupAddress extends ImpsAddress{
    private String mScreenName;
    public ImpsGroupAddress() {
    }
    public ImpsGroupAddress(String groupId) {
        this(groupId, null);
    }
    public ImpsGroupAddress(ImpsAddress userAddress, String groupName) {
        super(userAddress.getUser(), groupName, userAddress.getDomain());
        if(mResource == null) {
            throw new IllegalArgumentException();
        }
    }
    public ImpsGroupAddress(String groupId, String screenName) {
        super(groupId);
        if(mResource == null) {
            throw new IllegalArgumentException();
        }
        mScreenName = screenName;
    }
    @Override
    public String getScreenName() {
        return mScreenName == null ? getResource() : mScreenName;
    }
    @Override
    public void writeToParcel(Parcel dest) {
        super.writeToParcel(dest);
        dest.writeString(mScreenName);
    }
    @Override
    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        mScreenName = source.readString();
    }
    @Override
    public PrimitiveElement toPrimitiveElement() {
        PrimitiveElement group = new PrimitiveElement(ImpsTags.Group);
        group.addChild(ImpsTags.GroupID, getFullName());
        return group;
    }
    @Override
    ImEntity getEntity(ImpsConnection connection) {
        ImpsChatGroupManager manager =
            (ImpsChatGroupManager) connection.getChatGroupManager();
        ChatGroup group = manager.getChatGroup(this);
        if(group == null) {
            group = manager.loadGroupMembersAsync(this);
        }
        return group;
    }
}
