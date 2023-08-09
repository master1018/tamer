public class ChatGroup extends ImEntity{
    private ChatGroupManager mManager;
    private Address mAddress;
    private String mName;
    private Vector<Contact> mMembers;
    private CopyOnWriteArrayList<GroupMemberListener> mMemberListeners;
    public ChatGroup(Address address, String name, ChatGroupManager manager) {
        this(address, name, null, manager);
    }
    public ChatGroup(Address address, String name, Collection<Contact> members,
            ChatGroupManager manager) {
        mAddress = address;
        mName = name;
        mManager = manager;
        mMembers = new Vector<Contact>();
        if(members != null) {
            mMembers.addAll(members);
        }
        mMemberListeners = new CopyOnWriteArrayList<GroupMemberListener>();
    }
    @Override
    public Address getAddress() {
        return mAddress;
    }
    public String getName() {
        return mName;
    }
    public void addMemberListener(GroupMemberListener listener) {
        mMemberListeners.add(listener);
    }
    public void removeMemberListener(GroupMemberListener listener) {
        mMemberListeners.remove(listener);
    }
    public List<Contact> getMembers(){
        return Collections.unmodifiableList(mMembers);
    }
    public synchronized void addMemberAsync(Contact contact) {
        mManager.addGroupMemberAsync(this, contact);
    }
    public synchronized void removeMemberAsync(Contact contact) {
        mManager.removeGroupMemberAsync(this, contact);
    }
    void notifyMemberJoined(Contact contact) {
        mMembers.add(contact);
        for(GroupMemberListener listener : mMemberListeners) {
            listener.onMemberJoined(this, contact);
        }
    }
    void notifyMemberLeft(Contact contact) {
        if(mMembers.remove(contact)) {
            for(GroupMemberListener listener : mMemberListeners) {
                listener.onMemberLeft(this, contact);
            }
        }
    }
    void notifyGroupMemberError(ImErrorInfo error) {
        for(GroupMemberListener listener : mMemberListeners) {
            listener.onError(this, error);
        }
    }
}
