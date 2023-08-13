public class DropBoxManager {
    private static final String TAG = "DropBoxManager";
    private final IDropBoxManagerService mService;
    public static final int IS_EMPTY = 1;
    public static final int IS_TEXT = 2;
    public static final int IS_GZIPPED = 4;
    public static class Entry implements Parcelable {
        private final String mTag;
        private final long mTimeMillis;
        private final byte[] mData;
        private final ParcelFileDescriptor mFileDescriptor;
        private final int mFlags;
        public Entry(String tag, long millis) {
            this(tag, millis, (Object) null, IS_EMPTY);
        }
        public Entry(String tag, long millis, String text) {
            this(tag, millis, (Object) text.getBytes(), IS_TEXT);
        }
        public Entry(String tag, long millis, byte[] data, int flags) {
            this(tag, millis, (Object) data, flags);
        }
        public Entry(String tag, long millis, ParcelFileDescriptor data, int flags) {
            this(tag, millis, (Object) data, flags);
        }
        public Entry(String tag, long millis, File data, int flags) throws IOException {
            this(tag, millis, (Object) ParcelFileDescriptor.open(
                    data, ParcelFileDescriptor.MODE_READ_ONLY), flags);
        }
        private Entry(String tag, long millis, Object value, int flags) {
            if (tag == null) throw new NullPointerException();
            if (((flags & IS_EMPTY) != 0) != (value == null)) throw new IllegalArgumentException();
            mTag = tag;
            mTimeMillis = millis;
            mFlags = flags;
            if (value == null) {
                mData = null;
                mFileDescriptor = null;
            } else if (value instanceof byte[]) {
                mData = (byte[]) value;
                mFileDescriptor = null;
            } else if (value instanceof ParcelFileDescriptor) {
                mData = null;
                mFileDescriptor = (ParcelFileDescriptor) value;
            } else {
                throw new IllegalArgumentException();
            }
        }
        public void close() {
            try { if (mFileDescriptor != null) mFileDescriptor.close(); } catch (IOException e) { }
        }
        public String getTag() { return mTag; }
        public long getTimeMillis() { return mTimeMillis; }
        public int getFlags() { return mFlags & ~IS_GZIPPED; }  
        public String getText(int maxBytes) {
            if ((mFlags & IS_TEXT) == 0) return null;
            if (mData != null) return new String(mData, 0, Math.min(maxBytes, mData.length));
            InputStream is = null;
            try {
                is = getInputStream();
                byte[] buf = new byte[maxBytes];
                return new String(buf, 0, Math.max(0, is.read(buf)));
            } catch (IOException e) {
                return null;
            } finally {
                try { if (is != null) is.close(); } catch (IOException e) {}
            }
        }
        public InputStream getInputStream() throws IOException {
            InputStream is;
            if (mData != null) {
                is = new ByteArrayInputStream(mData);
            } else if (mFileDescriptor != null) {
                is = new ParcelFileDescriptor.AutoCloseInputStream(mFileDescriptor);
            } else {
                return null;
            }
            return (mFlags & IS_GZIPPED) != 0 ? new GZIPInputStream(is) : is;
        }
        public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator() {
            public Entry[] newArray(int size) { return new Entry[size]; }
            public Entry createFromParcel(Parcel in) {
                return new Entry(
                        in.readString(), in.readLong(), in.readValue(null), in.readInt());
            }
        };
        public int describeContents() {
            return mFileDescriptor != null ? Parcelable.CONTENTS_FILE_DESCRIPTOR : 0;
        }
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mTag);
            out.writeLong(mTimeMillis);
            if (mFileDescriptor != null) {
                out.writeValue(mFileDescriptor);
            } else {
                out.writeValue(mData);
            }
            out.writeInt(mFlags);
        }
    }
    public DropBoxManager(IDropBoxManagerService service) { mService = service; }
    protected DropBoxManager() { mService = null; }
    public void addText(String tag, String data) {
        try { mService.add(new Entry(tag, 0, data)); } catch (RemoteException e) {}
    }
    public void addData(String tag, byte[] data, int flags) {
        if (data == null) throw new NullPointerException();
        try { mService.add(new Entry(tag, 0, data, flags)); } catch (RemoteException e) {}
    }
    public void addFile(String tag, File file, int flags) throws IOException {
        if (file == null) throw new NullPointerException();
        Entry entry = new Entry(tag, 0, file, flags);
        try {
            mService.add(new Entry(tag, 0, file, flags));
        } catch (RemoteException e) {
        } finally {
            entry.close();
        }
    }
    public boolean isTagEnabled(String tag) {
        try { return mService.isTagEnabled(tag); } catch (RemoteException e) { return false; }
    }
    public Entry getNextEntry(String tag, long msec) {
        try { return mService.getNextEntry(tag, msec); } catch (RemoteException e) { return null; }
    }
}
