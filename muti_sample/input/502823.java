public class AccountSyncAdapter extends AbstractSyncAdapter {
    public AccountSyncAdapter(Mailbox mailbox, EasSyncService service) {
        super(mailbox, service);
     }
    @Override
    public void cleanup() {
    }
    @Override
    public String getCollectionName() {
        return null;
    }
    @Override
    public boolean parse(InputStream is) throws IOException {
        return false;
    }
    @Override
    public boolean sendLocalChanges(Serializer s) throws IOException {
        return false;
    }
    @Override
    public boolean isSyncable() {
        return true;
    }
}
