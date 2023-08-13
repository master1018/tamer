public class CursorWindow extends SQLiteClosable implements Parcelable {
    @SuppressWarnings("unused")
    private int nWindow;
    private int mStartPos;
    public CursorWindow(boolean localWindow) {
        mStartPos = 0;
        native_init(localWindow);
    }
    public int getStartPosition() {
        return mStartPos;
    }
    public void setStartPosition(int pos) {
        mStartPos = pos;
    }    
    public int getNumRows() {
        acquireReference();
        try {
            return getNumRows_native();
        } finally {
            releaseReference();
        }
    }
    private native int getNumRows_native();
    public boolean setNumColumns(int columnNum) {
        acquireReference();
        try {
            return setNumColumns_native(columnNum);
        } finally {
            releaseReference();
        }
    }
    private native boolean setNumColumns_native(int columnNum);
    public boolean allocRow(){
        acquireReference();
        try {
            return allocRow_native();
        } finally {
            releaseReference();
        }
    }
    private native boolean allocRow_native();    
    public void freeLastRow(){
        acquireReference();
        try {
            freeLastRow_native();
        } finally {
            releaseReference();
        }
    }
    private native void freeLastRow_native();
    public boolean putBlob(byte[] value, int row, int col) {
        acquireReference();
        try {
            return putBlob_native(value, row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean putBlob_native(byte[] value, int row, int col);    
    public boolean putString(String value, int row, int col) {
        acquireReference();
        try {
            return putString_native(value, row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean putString_native(String value, int row, int col);    
    public boolean putLong(long value, int row, int col) {
        acquireReference();
        try {
            return putLong_native(value, row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean putLong_native(long value, int row, int col);
    public boolean putDouble(double value, int row, int col) {
        acquireReference();
        try {
            return putDouble_native(value, row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean putDouble_native(double value, int row, int col);    
    public boolean putNull(int row, int col) {
        acquireReference();
        try {
            return putNull_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean putNull_native(int row, int col);
    public boolean isNull(int row, int col) {
        acquireReference();
        try {
            return isNull_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean isNull_native(int row, int col);
    public byte[] getBlob(int row, int col) {
        acquireReference();
        try {
            return getBlob_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native byte[] getBlob_native(int row, int col);
    public boolean isBlob(int row, int col) {
        acquireReference();
        try {
            return isBlob_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    public boolean isLong(int row, int col) {
        acquireReference();
        try {
            return isInteger_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    public boolean isFloat(int row, int col) {
        acquireReference();
        try {
            return isFloat_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    public boolean isString(int row, int col) {
        acquireReference();
        try {
            return isString_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native boolean isBlob_native(int row, int col);
    private native boolean isString_native(int row, int col);
    private native boolean isInteger_native(int row, int col);
    private native boolean isFloat_native(int row, int col);
    public String getString(int row, int col) {
        acquireReference();
        try {
            return getString_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native String getString_native(int row, int col);
    public void copyStringToBuffer(int row, int col, CharArrayBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("CharArrayBuffer should not be null");
        }
        if (buffer.data == null) {
            buffer.data = new char[64];
        }
        acquireReference();
        try {
            char[] newbuf = copyStringToBuffer_native(
                    row - mStartPos, col, buffer.data.length, buffer);
            if (newbuf != null) {
                buffer.data = newbuf;
            }
        } finally {
            releaseReference();
        }
    }
    private native char[] copyStringToBuffer_native(
            int row, int col, int bufferSize, CharArrayBuffer buffer);
    public long getLong(int row, int col) {
        acquireReference();
        try {
            return getLong_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native long getLong_native(int row, int col);
    public double getDouble(int row, int col) {
        acquireReference();
        try {
            return getDouble_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    private native double getDouble_native(int row, int col);
    public short getShort(int row, int col) {
        acquireReference();
        try {
            return (short) getLong_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    public int getInt(int row, int col) {
        acquireReference();
        try {
            return (int) getLong_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    }
    public float getFloat(int row, int col) {
        acquireReference();
        try {
            return (float) getDouble_native(row - mStartPos, col);
        } finally {
            releaseReference();
        }
    } 
    public void clear() {
        acquireReference();
        try {
            mStartPos = 0;        
            native_clear();
        } finally {
            releaseReference();
        }
    }
    private native void native_clear();
    public void close() {
        releaseReference();
    }
    private native void close_native();
    @Override
    protected void finalize() {
        close_native();
    }
    public static final Parcelable.Creator<CursorWindow> CREATOR
            = new Parcelable.Creator<CursorWindow>() {
        public CursorWindow createFromParcel(Parcel source) {
            return new CursorWindow(source);
        }
        public CursorWindow[] newArray(int size) {
            return new CursorWindow[size];
        }
    };
    public static CursorWindow newFromParcel(Parcel p) {
        return CREATOR.createFromParcel(p);
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(native_getBinder());
        dest.writeInt(mStartPos);
    }
    private CursorWindow(Parcel source) {
        IBinder nativeBinder = source.readStrongBinder();
        mStartPos = source.readInt();
        native_init(nativeBinder);
    }
    private native IBinder native_getBinder();
    private native void native_init(boolean localOnly);
    private native void native_init(IBinder nativeBinder);
    @Override
    protected void onAllReferencesReleased() {
        close_native();        
    }
}
