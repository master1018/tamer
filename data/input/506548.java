public class ExampleAgent extends BackupAgent {
    static final int AGENT_VERSION = 1;
    static final String APP_DATA_KEY = "alldata";
    boolean mAddMayo;
    boolean mAddTomato;
    int mFilling;
    File mDataFile;
    @Override
    public void onCreate() {
        mDataFile = new File(getFilesDir(), BackupRestoreActivity.DATA_FILE_NAME);
    }
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) throws IOException {
        synchronized (BackupRestoreActivity.sDataLock) {
            RandomAccessFile file = new RandomAccessFile(mDataFile, "r");
            mFilling = file.readInt();
            mAddMayo = file.readBoolean();
            mAddTomato = file.readBoolean();
        }
        boolean doBackup = (oldState == null);
        if (!doBackup) {
            doBackup = compareStateFile(oldState);
        }
        if (doBackup) {
            ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
            DataOutputStream outWriter = new DataOutputStream(bufStream);
            outWriter.writeInt(mFilling);
            outWriter.writeBoolean(mAddMayo);
            outWriter.writeBoolean(mAddTomato);
            byte[] buffer = bufStream.toByteArray();
            int len = buffer.length;
            data.writeEntityHeader(APP_DATA_KEY, len);
            data.writeEntityData(buffer, len);
        }
        writeStateFile(newState);
    }
    boolean compareStateFile(ParcelFileDescriptor oldState) {
        FileInputStream instream = new FileInputStream(oldState.getFileDescriptor());
        DataInputStream in = new DataInputStream(instream);
        try {
            int stateVersion = in.readInt();
            if (stateVersion > AGENT_VERSION) {
                return true;
            }
            int lastFilling = in.readInt();
            boolean lastMayo = in.readBoolean();
            boolean lastTomato = in.readBoolean();
            return (lastFilling != mFilling)
                    || (lastTomato != mAddTomato)
                    || (lastMayo != mAddMayo);
        } catch (IOException e) {
            return true;
        }
    }
    void writeStateFile(ParcelFileDescriptor stateFile) throws IOException {
        FileOutputStream outstream = new FileOutputStream(stateFile.getFileDescriptor());
        DataOutputStream out = new DataOutputStream(outstream);
        out.writeInt(AGENT_VERSION);
        out.writeInt(mFilling);
        out.writeBoolean(mAddMayo);
        out.writeBoolean(mAddTomato);
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
            ParcelFileDescriptor newState) throws IOException {
        while (data.readNextHeader()) {
            String key = data.getKey();
            int dataSize = data.getDataSize();
            if (APP_DATA_KEY.equals(key)) {
                byte[] dataBuf = new byte[dataSize];
                data.readEntityData(dataBuf, 0, dataSize);
                ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
                DataInputStream in = new DataInputStream(baStream);
                mFilling = in.readInt();
                mAddMayo = in.readBoolean();
                mAddTomato = in.readBoolean();
                synchronized (BackupRestoreActivity.sDataLock) {
                    RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
                    file.setLength(0L);
                    file.writeInt(mFilling);
                    file.writeBoolean(mAddMayo);
                    file.writeBoolean(mAddTomato);
                }
            } else {
                data.skipEntityData();
            }
        }
        writeStateFile(newState);
    }
}
