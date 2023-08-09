public abstract class ChatGroupManager {
    protected HashMap<Address, ChatGroup> mGroups;
    protected HashMap<String, Invitation> mInvitations;
    protected CopyOnWriteArrayList<GroupListener> mGroupListeners;
    protected InvitationListener mInvitationListener;
    protected ChatGroupManager() {
        mGroups = new HashMap<Address, ChatGroup>();
        mInvitations = new HashMap<String, Invitation>();
        mGroupListeners = new CopyOnWriteArrayList<GroupListener>();
    }
    public void addGroupListener(GroupListener listener) {
        mGroupListeners.add(listener);
    }
    public void removeGroupListener(GroupListener listener) {
        mGroupListeners.remove(listener);
    }
    public synchronized void setInvitationListener(InvitationListener listener) {
        mInvitationListener = listener;
    }
    public abstract void createChatGroupAsync(String name);
    public abstract void deleteChatGroupAsync(ChatGroup group);
    protected abstract void addGroupMemberAsync(ChatGroup group, Contact contact);
    protected abstract void removeGroupMemberAsync(ChatGroup group, Contact contact);
    public abstract void joinChatGroupAsync(Address address);
    public abstract void leaveChatGroupAsync(ChatGroup group);
    public abstract void inviteUserAsync(ChatGroup group, Contact invitee);
    public abstract void acceptInvitationAsync(Invitation invitation);
    public void acceptInvitationAsync(String inviteId) {
        Invitation invitation = mInvitations.remove(inviteId);
        if (invitation != null) {
            acceptInvitationAsync(invitation);
        }
    }
    public void rejectInvitationAsync(String inviteId) {
        Invitation invitation = mInvitations.remove(inviteId);
        if (invitation != null) {
            rejectInvitationAsync(invitation);
        }
    }
    public abstract void rejectInvitationAsync(Invitation invitation);
    public ChatGroup getChatGroup(Address address) {
        return mGroups.get(address);
    }
    protected void notifyGroupChanged(Address groupAddress, ArrayList<Contact> joined,
            ArrayList<Contact> left) {
        ChatGroup group = mGroups.get(groupAddress);
        if (group == null) {
            group = new ChatGroup(groupAddress, groupAddress.getScreenName(), this);
            mGroups.put(groupAddress, group);
        }
        if (joined != null) {
            for (Contact contact : joined) {
                notifyMemberJoined(group, contact);
            }
        }
        if (left != null) {
            for (Contact contact : left) {
                notifyMemberLeft(group, contact);
            }
        }
    }
    protected synchronized void notifyGroupCreated(ChatGroup group) {
        mGroups.put(group.getAddress(), group);
        for (GroupListener listener : mGroupListeners) {
            listener.onGroupCreated(group);
        }
    }
    protected synchronized void notifyGroupDeleted(ChatGroup group) {
        mGroups.remove(group.getAddress());
        for (GroupListener listener : mGroupListeners) {
            listener.onGroupDeleted(group);
        }
    }
    protected synchronized void notifyJoinedGroup(ChatGroup group) {
        mGroups.put(group.getAddress(), group);
        for (GroupListener listener : mGroupListeners) {
            listener.onJoinedGroup(group);
        }
    }
    protected synchronized void notifyLeftGroup(ChatGroup group) {
        mGroups.remove(group.getAddress());
        for (GroupListener listener : mGroupListeners) {
            listener.onLeftGroup(group);
        }
    }
    protected synchronized void notifyGroupError(int errorType, String groupName, ImErrorInfo error) {
        for (GroupListener listener : mGroupListeners) {
            listener.onGroupError(errorType, groupName, error);
        }
    }
    protected synchronized void notifyGroupInvitation(Invitation invitation) {
        mInvitations.put(invitation.getInviteID(), invitation);
        if (mInvitationListener != null) {
            mInvitationListener.onGroupInvitation(invitation);
        }
    }
    protected void notifyMemberJoined(ChatGroup group, Contact contact) {
        group.notifyMemberJoined(contact);
    }
    protected void notifyMemberLeft(ChatGroup group, Contact contact) {
        group.notifyMemberLeft(contact);
    }
    protected void notifyGroupMemberError(ChatGroup group, ImErrorInfo error) {
        group.notifyGroupMemberError(error);
    }
}
