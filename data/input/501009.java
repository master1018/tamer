public abstract class Folder {
    public enum OpenMode {
        READ_WRITE, READ_ONLY,
    }
    public enum FolderType {
        HOLDS_FOLDERS, HOLDS_MESSAGES,
    }
    public enum FolderRole {
        INBOX,      
        TRASH,
        SENT,
        DRAFTS,
        OUTBOX,     
        OTHER,      
        UNKNOWN     
    }
    public abstract void open(OpenMode mode, PersistentDataCallbacks callbacks)
            throws MessagingException;
    public abstract void close(boolean expunge) throws MessagingException;
    public abstract boolean isOpen();
    public abstract OpenMode getMode() throws MessagingException;
    public abstract boolean canCreate(FolderType type);
    public abstract boolean create(FolderType type) throws MessagingException;
    public abstract boolean exists() throws MessagingException;
    public abstract int getMessageCount() throws MessagingException;
    public abstract int getUnreadMessageCount() throws MessagingException;
    public abstract Message getMessage(String uid) throws MessagingException;
    public abstract Message[] getMessages(int start, int end, MessageRetrievalListener listener)
            throws MessagingException;
    public abstract Message[] getMessages(MessageRetrievalListener listener)
            throws MessagingException;
    public abstract Message[] getMessages(String[] uids, MessageRetrievalListener listener)
            throws MessagingException;
    public Message[] getMessages(Flag[] setFlags, Flag[] clearFlags, 
            MessageRetrievalListener listener) throws MessagingException {
        throw new MessagingException("Not implemented");
    }
    public abstract void appendMessages(Message[] messages) throws MessagingException;
    public abstract void copyMessages(Message[] msgs, Folder folder,
            MessageUpdateCallbacks callbacks) throws MessagingException;
    public abstract void setFlags(Message[] messages, Flag[] flags, boolean value)
            throws MessagingException;
    public abstract Message[] expunge() throws MessagingException;
    public abstract void fetch(Message[] messages, FetchProfile fp,
            MessageRetrievalListener listener) throws MessagingException;
    public abstract void delete(boolean recurse) throws MessagingException;
    public abstract String getName();
    public abstract Flag[] getPermanentFlags() throws MessagingException;
    public FolderRole getRole() {
        return FolderRole.UNKNOWN;
    }
    @SuppressWarnings("unused")
    public void localFolderSetupComplete(Folder localFolder) throws MessagingException {
    }
    public abstract Message createMessage(String uid) throws MessagingException;
    public interface PersistentDataCallbacks {
        public void setPersistentString(String key, String value);
        public String getPersistentString(String key, String defaultValue);
        public void setPersistentStringAndMessageFlags(String key, String value,
                Flag[] setFlags, Flag[] clearFlags) throws MessagingException;
    }
    public interface MessageUpdateCallbacks {
        public void onMessageUidChange(Message message, String newUid) throws MessagingException;
        public void onMessageNotFound(Message message) throws MessagingException;
    }
    @Override
    public String toString() {
        return getName();
    }
}
