public class MultiRecordExampleAgent extends BackupAgent {
    static final String FILLING_KEY = "filling";
    static final String MAYO_KEY = "mayo";
    static final String TOMATO_KEY = "tomato";
    int mFilling;
    boolean mAddMayo;
    boolean mAddTomato;
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
        boolean forceBackup = (oldState == null);
        int lastFilling = 0;
        boolean lastMayo = false;
        boolean lastTomato = false;
        if (!forceBackup) {
            FileInputStream instream = new FileInputStream(oldState.getFileDescriptor());
            DataInputStream in = new DataInputStream(instream);
            try {
                lastFilling = in.readInt();
                lastMayo = in.readBoolean();
                lastTomato = in.readBoolean();
            } catch (IOException e) {
                forceBackup = true;
            }
        }
        ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bufStream);
        if (forceBackup || (mFilling != lastFilling)) {
            out.writeInt(mFilling);
            writeBackupEntity(data, bufStream, FILLING_KEY);
        }
        if (forceBackup || (mAddMayo != lastMayo)) {
            bufStream.reset();
            out.writeBoolean(mAddMayo);
            writeBackupEntity(data, bufStream, MAYO_KEY);
        }
        if (forceBackup || (mAddTomato != lastTomato)) {
            bufStream.reset();
            out.writeBoolean(mAddTomato);
            writeBackupEntity(data, bufStream, TOMATO_KEY);
        }
        writeStateFile(newState);
    }
    void writeStateFile(ParcelFileDescriptor stateFile) throws IOException {
        FileOutputStream outstream = new FileOutputStream(stateFile.getFileDescriptor());
        DataOutputStream out = new DataOutputStream(outstream);
        out.writeInt(mFilling);
        out.writeBoolean(mAddMayo);
        out.writeBoolean(mAddTomato);
    }
    void writeBackupEntity(BackupDataOutput data, ByteArrayOutputStream bufStream, String key)
            throws IOException {
        byte[] buf = bufStream.toByteArray();
        data.writeEntityHeader(key, buf.length);
        data.writeEntityData(buf, buf.length);
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
            ParcelFileDescriptor newState) throws IOException {
        while (data.readNextHeader()) {
            String key = data.getKey();
            int dataSize = data.getDataSize();
            byte[] dataBuf = new byte[dataSize];
            data.readEntityData(dataBuf, 0, dataSize);
            ByteArrayInputStream instream = new ByteArrayInputStream(dataBuf);
            DataInputStream in = new DataInputStream(instream);
            if (FILLING_KEY.equals(key)) {
                mFilling = in.readInt();
            } else if (MAYO_KEY.equals(key)) {
                mAddMayo = in.readBoolean();
            } else if (TOMATO_KEY.equals(key)) {
                mAddTomato = in.readBoolean();
            }
        }
        synchronized (BackupRestoreActivity.sDataLock) {
            RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
            file.setLength(0L);
            file.writeInt(mFilling);
            file.writeBoolean(mAddMayo);
            file.writeBoolean(mAddTomato);
        }
        writeStateFile(newState);
    }
}
