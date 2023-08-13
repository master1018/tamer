public class ConversationListItemData {
    private Conversation mConversation;
    private long mThreadId;
    private String mSubject;
    private String mDate;
    private boolean mHasAttachment;
    private boolean mIsRead;
    private boolean mHasError;
    private boolean mHasDraft;
    private int mMessageCount;
    private ContactList mRecipients;
    private String mRecipientString;
    private int mPresenceResId;
    public ConversationListItemData(Context context, Conversation conv) {
        mConversation = conv;
        mThreadId = conv.getThreadId();
        mPresenceResId = 0;
        mSubject = conv.getSnippet();
        mDate = MessageUtils.formatTimeStampString(context, conv.getDate());
        mIsRead = !conv.hasUnreadMessages();
        mHasError = conv.hasError();
        mHasDraft = conv.hasDraft();
        mMessageCount = conv.getMessageCount();
        mHasAttachment = conv.hasAttachment();
        updateRecipients();
    }
    public void updateRecipients() {
        mRecipients = mConversation.getRecipients();
        mRecipientString = mRecipients.formatNames(", ");
    }
    public long getThreadId() {
        return mThreadId;
    }
    public String getDate() {
        return mDate;
    }
    public String getFrom() {
        return mRecipientString;
    }
    public ContactList getContacts() {
        return mRecipients;
    }
    public int getPresenceResourceId() {
        return mPresenceResId;
    }
    public String getSubject() {
        return mSubject;
    }
    public boolean hasAttachment() {
        return mHasAttachment;
    }
    public boolean isRead() {
        return mIsRead;
    }
    public boolean hasError() {
        return mHasError;
    }
    public boolean hasDraft() {
        return mHasDraft;
    }
    public int getMessageCount() {
        return mMessageCount;
    }
    @Override
    public String toString() {
        return "[ConversationHeader from:" + getFrom() + " subject:" + getSubject()
        + "]";
    }
}
