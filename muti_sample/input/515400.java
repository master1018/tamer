public class MockFolder extends Folder {
    @Override
    public void appendMessages(Message[] messages) {
    }
    @Override
    public void close(boolean expunge) {
    }
    @Override
    public void copyMessages(Message[] msgs, Folder folder, 
            MessageUpdateCallbacks callbacks) {
    }
    @Override
    public boolean canCreate(FolderType type) {
        return false;
    }
    @Override
    public boolean create(FolderType type) {
        return false;
    }
    @Override
    public void delete(boolean recurse) {
    }
    @Override
    public boolean exists() {
        return false;
    }
    @Override
    public Message[] expunge() {
        return null;
    }
    @Override
    public void fetch(Message[] messages, FetchProfile fp, MessageRetrievalListener listener) {
    }
    @Override
    public Message getMessage(String uid) {
        return null;
    }
    @Override
    public int getMessageCount() {
        return 0;
    }
    @Override
    public Message[] getMessages(int start, int end, MessageRetrievalListener listener) {
        return null;
    }
    @Override
    public Message[] getMessages(MessageRetrievalListener listener) {
        return null;
    }
    @Override
    public Message[] getMessages(String[] uids, MessageRetrievalListener listener) {
        return null;
    }
    @Override
    public OpenMode getMode() {
        return null;
    }
    @Override
    public String getName() {
        return null;
    }
    @Override
    public Flag[] getPermanentFlags() {
        return null;
    }
    @Override
    public int getUnreadMessageCount() {
        return 0;
    }
    @Override
    public boolean isOpen() {
        return false;
    }
    @Override
    public void open(OpenMode mode, PersistentDataCallbacks callbacks) {
    }
    @Override
    public void setFlags(Message[] messages, Flag[] flags, boolean value) {
    }
    @Override
    public Message createMessage(String uid) {
        return null;
    }
}
