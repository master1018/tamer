public class BrowserBackupAgent extends BackupAgent {
    static final String TAG = "BrowserBackupAgent";
    static final boolean DEBUG = false;
    static final String BOOKMARK_KEY = "_bookmarks_";
    static final int BACKUP_AGENT_VERSION = 0;
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) throws IOException {
        long savedFileSize = -1;
        long savedCrc = -1;
        int savedVersion = -1;
        DataInputStream in = new DataInputStream(
                new FileInputStream(oldState.getFileDescriptor()));
        try {
            savedFileSize = in.readLong();
            savedCrc = in.readLong();
            savedVersion = in.readInt();
        } catch (EOFException e) {
        }
        File tmpfile = File.createTempFile("bkp", null, getCacheDir());
        try {
            FileOutputStream outfstream = new FileOutputStream(tmpfile);
            long newCrc = buildBookmarkFile(outfstream);
            outfstream.close();
            if ((savedVersion != BACKUP_AGENT_VERSION)
                    || (newCrc != savedCrc)
                    || (tmpfile.length() != savedFileSize)) {
                copyFileToBackup(BOOKMARK_KEY, tmpfile, data);
            }
            writeBackupState(tmpfile.length(), newCrc, newState);
        } finally {
            tmpfile.delete();
        }
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
            ParcelFileDescriptor newState) throws IOException {
        long crc = -1;
        File tmpfile = File.createTempFile("rst", null, getFilesDir());
        try {
            while (data.readNextHeader()) {
                if (BOOKMARK_KEY.equals(data.getKey())) {
                    crc = copyBackupToFile(data, tmpfile, data.getDataSize());
                    FileInputStream infstream = new FileInputStream(tmpfile);
                    DataInputStream in = new DataInputStream(infstream);
                    try {
                        int count = in.readInt();
                        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>(count);
                        for (int i = 0; i < count; i++) {
                            Bookmark mark = new Bookmark();
                            mark.url = in.readUTF();
                            mark.visits = in.readInt();
                            mark.date = in.readLong();
                            mark.created = in.readLong();
                            mark.title = in.readUTF();
                            bookmarks.add(mark);
                        }
                        int N = bookmarks.size();
                        int nUnique = 0;
                        if (DEBUG) Log.v(TAG, "Restoring " + N + " bookmarks");
                        String[] urlCol = new String[] { BookmarkColumns.URL };
                        for (int i = 0; i < N; i++) {
                            Bookmark mark = bookmarks.get(i);
                            Cursor cursor = getContentResolver().query(Browser.BOOKMARKS_URI,
                                    urlCol,  BookmarkColumns.URL + " == '" + mark.url + "' AND " +
                                    BookmarkColumns.BOOKMARK + " == 1 ", null, null);
                            if (cursor.getCount() <= 0) {
                                if (DEBUG) Log.v(TAG, "Did not see url: " + mark.url);
                                Bookmarks.addBookmark(null, getContentResolver(),
                                        mark.url, mark.title, null, false);
                                nUnique++;
                            } else {
                                if (DEBUG) Log.v(TAG, "Skipping extant url: " + mark.url);
                            }
                            cursor.close();
                        }
                        Log.i(TAG, "Restored " + nUnique + " of " + N + " bookmarks");
                    } catch (IOException ioe) {
                        Log.w(TAG, "Bad backup data; not restoring");
                        crc = -1;
                    }
                }
                writeBackupState(tmpfile.length(), crc, newState);
            }
        } finally {
            tmpfile.delete();
        }
    }
    class Bookmark {
        public String url;
        public int visits;
        public long date;
        public long created;
        public String title;
    }
    private long buildBookmarkFile(FileOutputStream outfstream) throws IOException {
        CRC32 crc = new CRC32();
        ByteArrayOutputStream bufstream = new ByteArrayOutputStream(512);
        DataOutputStream bout = new DataOutputStream(bufstream);
        Cursor cursor = getContentResolver().query(Browser.BOOKMARKS_URI,
                new String[] { BookmarkColumns.URL, BookmarkColumns.VISITS,
                BookmarkColumns.DATE, BookmarkColumns.CREATED,
                BookmarkColumns.TITLE },
                BookmarkColumns.BOOKMARK + " == 1 ", null, null);
        int count = cursor.getCount();
        if (DEBUG) Log.v(TAG, "Backing up " + count + " bookmarks");
        bout.writeInt(count);
        byte[] record = bufstream.toByteArray();
        crc.update(record);
        outfstream.write(record);
        for (int i = 0; i < count; i++) {
            cursor.moveToNext();
            String url = cursor.getString(0);
            int visits = cursor.getInt(1);
            long date = cursor.getLong(2);
            long created = cursor.getLong(3);
            String title = cursor.getString(4);
            bufstream.reset();
            bout.writeUTF(url);
            bout.writeInt(visits);
            bout.writeLong(date);
            bout.writeLong(created);
            bout.writeUTF(title);
            record = bufstream.toByteArray();
            crc.update(record);
            outfstream.write(record);
            if (DEBUG) Log.v(TAG, "   wrote url " + url);
        }
        cursor.close();
        return crc.getValue();
    }
    private void copyFileToBackup(String key, File file, BackupDataOutput data)
            throws IOException {
        final int CHUNK = 8192;
        byte[] buf = new byte[CHUNK];
        int toCopy = (int) file.length();
        data.writeEntityHeader(key, toCopy);
        FileInputStream in = new FileInputStream(file);
        int nRead;
        while (toCopy > 0) {
            nRead = in.read(buf, 0, CHUNK);
            data.writeEntityData(buf, nRead);
            toCopy -= nRead;
        }
        in.close();
    }
    private long copyBackupToFile(BackupDataInput data, File file, int toRead)
            throws IOException {
        final int CHUNK = 8192;
        byte[] buf = new byte[CHUNK];
        CRC32 crc = new CRC32();
        FileOutputStream out = new FileOutputStream(file);
        while (toRead > 0) {
            int numRead = data.readEntityData(buf, 0, CHUNK);
            crc.update(buf, 0, numRead);
            out.write(buf, 0, numRead);
            toRead -= numRead;
        }
        out.close();
        return crc.getValue();
    }
    private void writeBackupState(long fileSize, long crc, ParcelFileDescriptor stateFile)
            throws IOException {
        DataOutputStream out = new DataOutputStream(
                new FileOutputStream(stateFile.getFileDescriptor()));
        out.writeLong(fileSize);
        out.writeLong(crc);
        out.writeInt(BACKUP_AGENT_VERSION);
    }
}
