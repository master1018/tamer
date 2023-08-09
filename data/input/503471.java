public final class MotionEvent implements Parcelable {
    static final boolean DEBUG_POINTERS = false;
    public static final int ACTION_MASK             = 0xff;
    public static final int ACTION_DOWN             = 0;
    public static final int ACTION_UP               = 1;
    public static final int ACTION_MOVE             = 2;
    public static final int ACTION_CANCEL           = 3;
    public static final int ACTION_OUTSIDE          = 4;
    public static final int ACTION_POINTER_DOWN     = 5;
    public static final int ACTION_POINTER_UP       = 6;
    public static final int ACTION_POINTER_INDEX_MASK  = 0xff00;
    public static final int ACTION_POINTER_INDEX_SHIFT = 8;
    @Deprecated
    public static final int ACTION_POINTER_1_DOWN   = ACTION_POINTER_DOWN | 0x0000;
    @Deprecated
    public static final int ACTION_POINTER_2_DOWN   = ACTION_POINTER_DOWN | 0x0100;
    @Deprecated
    public static final int ACTION_POINTER_3_DOWN   = ACTION_POINTER_DOWN | 0x0200;
    @Deprecated
    public static final int ACTION_POINTER_1_UP     = ACTION_POINTER_UP | 0x0000;
    @Deprecated
    public static final int ACTION_POINTER_2_UP     = ACTION_POINTER_UP | 0x0100;
    @Deprecated
    public static final int ACTION_POINTER_3_UP     = ACTION_POINTER_UP | 0x0200;
    @Deprecated
    public static final int ACTION_POINTER_ID_MASK  = 0xff00;
    @Deprecated
    public static final int ACTION_POINTER_ID_SHIFT = 8;
    private static final boolean TRACK_RECYCLED_LOCATION = false;
    public static final int EDGE_TOP = 0x00000001;
    public static final int EDGE_BOTTOM = 0x00000002;
    public static final int EDGE_LEFT = 0x00000004;
    public static final int EDGE_RIGHT = 0x00000008;
    static public final int SAMPLE_X = 0;
    static public final int SAMPLE_Y = 1;
    static public final int SAMPLE_PRESSURE = 2;
    static public final int SAMPLE_SIZE = 3;
    static public final int NUM_SAMPLE_DATA = 4;
    static public final int BASE_AVAIL_POINTERS = 5;
    static private final int BASE_AVAIL_SAMPLES = 8;
    static private final int MAX_RECYCLED = 10;
    static private Object gRecyclerLock = new Object();
    static private int gRecyclerUsed = 0;
    static private MotionEvent gRecyclerTop = null;
    private long mDownTime;
    private long mEventTimeNano;
    private int mAction;
    private float mRawX;
    private float mRawY;
    private float mXPrecision;
    private float mYPrecision;
    private int mDeviceId;
    private int mEdgeFlags;
    private int mMetaState;
    private int mNumPointers;
    private int mNumSamples;
    private int[] mPointerIdentifiers;
    private float[] mDataSamples;
    private long[] mTimeSamples;
    private MotionEvent mNext;
    private RuntimeException mRecycledLocation;
    private boolean mRecycled;
    private MotionEvent() {
        mPointerIdentifiers = new int[BASE_AVAIL_POINTERS];
        mDataSamples = new float[BASE_AVAIL_POINTERS*BASE_AVAIL_SAMPLES*NUM_SAMPLE_DATA];
        mTimeSamples = new long[BASE_AVAIL_SAMPLES];
    }
    static private MotionEvent obtain() {
        synchronized (gRecyclerLock) {
            if (gRecyclerTop == null) {
                return new MotionEvent();
            }
            MotionEvent ev = gRecyclerTop;
            gRecyclerTop = ev.mNext;
            gRecyclerUsed--;
            ev.mRecycledLocation = null;
            ev.mRecycled = false;
            return ev;
        }
    }
    static public MotionEvent obtainNano(long downTime, long eventTime, long eventTimeNano,
            int action, int pointers, int[] inPointerIds, float[] inData, int metaState,
            float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        MotionEvent ev = obtain();
        ev.mDeviceId = deviceId;
        ev.mEdgeFlags = edgeFlags;
        ev.mDownTime = downTime;
        ev.mEventTimeNano = eventTimeNano;
        ev.mAction = action;
        ev.mMetaState = metaState;
        ev.mRawX = inData[SAMPLE_X];
        ev.mRawY = inData[SAMPLE_Y];
        ev.mXPrecision = xPrecision;
        ev.mYPrecision = yPrecision;
        ev.mNumPointers = pointers;
        ev.mNumSamples = 1;
        int[] pointerIdentifiers = ev.mPointerIdentifiers;
        if (pointerIdentifiers.length < pointers) {
            ev.mPointerIdentifiers = pointerIdentifiers = new int[pointers];
        }
        System.arraycopy(inPointerIds, 0, pointerIdentifiers, 0, pointers);
        final int ND = pointers * NUM_SAMPLE_DATA;
        float[] dataSamples = ev.mDataSamples;
        if (dataSamples.length < ND) {
            ev.mDataSamples = dataSamples = new float[ND];
        }
        System.arraycopy(inData, 0, dataSamples, 0, ND);
        ev.mTimeSamples[0] = eventTime;
        if (DEBUG_POINTERS) {
            StringBuilder sb = new StringBuilder(128);
            sb.append("New:");
            for (int i=0; i<pointers; i++) {
                sb.append(" #");
                sb.append(ev.mPointerIdentifiers[i]);
                sb.append("(");
                sb.append(ev.mDataSamples[(i*NUM_SAMPLE_DATA) + SAMPLE_X]);
                sb.append(",");
                sb.append(ev.mDataSamples[(i*NUM_SAMPLE_DATA) + SAMPLE_Y]);
                sb.append(")");
            }
            Log.v("MotionEvent", sb.toString());
        }
        return ev;
    }
    static public MotionEvent obtain(long downTime, long eventTime, int action,
            float x, float y, float pressure, float size, int metaState,
            float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        MotionEvent ev = obtain();
        ev.mDeviceId = deviceId;
        ev.mEdgeFlags = edgeFlags;
        ev.mDownTime = downTime;
        ev.mEventTimeNano = eventTime * 1000000;
        ev.mAction = action;
        ev.mMetaState = metaState;
        ev.mXPrecision = xPrecision;
        ev.mYPrecision = yPrecision;
        ev.mNumPointers = 1;
        ev.mNumSamples = 1;
        int[] pointerIds = ev.mPointerIdentifiers;
        pointerIds[0] = 0;
        float[] data = ev.mDataSamples;
        data[SAMPLE_X] = ev.mRawX = x;
        data[SAMPLE_Y] = ev.mRawY = y;
        data[SAMPLE_PRESSURE] = pressure;
        data[SAMPLE_SIZE] = size;
        ev.mTimeSamples[0] = eventTime;
        return ev;
    }
    static public MotionEvent obtain(long downTime, long eventTime, int action,
            int pointers, float x, float y, float pressure, float size, int metaState,
            float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        MotionEvent ev = obtain();
        ev.mDeviceId = deviceId;
        ev.mEdgeFlags = edgeFlags;
        ev.mDownTime = downTime;
        ev.mEventTimeNano = eventTime * 1000000;
        ev.mAction = action;
        ev.mNumPointers = pointers;
        ev.mMetaState = metaState;
        ev.mXPrecision = xPrecision;
        ev.mYPrecision = yPrecision;
        ev.mNumPointers = 1;
        ev.mNumSamples = 1;
        int[] pointerIds = ev.mPointerIdentifiers;
        pointerIds[0] = 0;
        float[] data = ev.mDataSamples;
        data[SAMPLE_X] = ev.mRawX = x;
        data[SAMPLE_Y] = ev.mRawY = y;
        data[SAMPLE_PRESSURE] = pressure;
        data[SAMPLE_SIZE] = size;
        ev.mTimeSamples[0] = eventTime;
        return ev;
    }
    static public MotionEvent obtain(long downTime, long eventTime, int action,
            float x, float y, int metaState) {
        MotionEvent ev = obtain();
        ev.mDeviceId = 0;
        ev.mEdgeFlags = 0;
        ev.mDownTime = downTime;
        ev.mEventTimeNano = eventTime * 1000000;
        ev.mAction = action;
        ev.mNumPointers = 1;
        ev.mMetaState = metaState;
        ev.mXPrecision = 1.0f;
        ev.mYPrecision = 1.0f;
        ev.mNumPointers = 1;
        ev.mNumSamples = 1;
        int[] pointerIds = ev.mPointerIdentifiers;
        pointerIds[0] = 0;
        float[] data = ev.mDataSamples;
        data[SAMPLE_X] = ev.mRawX = x;
        data[SAMPLE_Y] = ev.mRawY = y;
        data[SAMPLE_PRESSURE] = 1.0f;
        data[SAMPLE_SIZE] = 1.0f;
        ev.mTimeSamples[0] = eventTime;
        return ev;
    }
    public void scale(float scale) {
        mRawX *= scale;
        mRawY *= scale;
        mXPrecision *= scale;
        mYPrecision *= scale;
        float[] history = mDataSamples;
        final int length = mNumPointers * mNumSamples * NUM_SAMPLE_DATA;
        for (int i = 0; i < length; i += NUM_SAMPLE_DATA) {
            history[i + SAMPLE_X] *= scale;
            history[i + SAMPLE_Y] *= scale;
            history[i + SAMPLE_SIZE] *= scale;    
        }
    }
    static public MotionEvent obtain(MotionEvent o) {
        MotionEvent ev = obtain();
        ev.mDeviceId = o.mDeviceId;
        ev.mEdgeFlags = o.mEdgeFlags;
        ev.mDownTime = o.mDownTime;
        ev.mEventTimeNano = o.mEventTimeNano;
        ev.mAction = o.mAction;
        ev.mNumPointers = o.mNumPointers;
        ev.mRawX = o.mRawX;
        ev.mRawY = o.mRawY;
        ev.mMetaState = o.mMetaState;
        ev.mXPrecision = o.mXPrecision;
        ev.mYPrecision = o.mYPrecision;
        final int NS = ev.mNumSamples = o.mNumSamples;
        if (ev.mTimeSamples.length >= NS) {
            System.arraycopy(o.mTimeSamples, 0, ev.mTimeSamples, 0, NS);
        } else {
            ev.mTimeSamples = (long[])o.mTimeSamples.clone();
        }
        final int NP = (ev.mNumPointers=o.mNumPointers);
        if (ev.mPointerIdentifiers.length >= NP) {
            System.arraycopy(o.mPointerIdentifiers, 0, ev.mPointerIdentifiers, 0, NP);
        } else {
            ev.mPointerIdentifiers = (int[])o.mPointerIdentifiers.clone();
        }
        final int ND = NP * NS * NUM_SAMPLE_DATA;
        if (ev.mDataSamples.length >= ND) {
            System.arraycopy(o.mDataSamples, 0, ev.mDataSamples, 0, ND);
        } else {
            ev.mDataSamples = (float[])o.mDataSamples.clone();
        }
        return ev;
    }
    static public MotionEvent obtainNoHistory(MotionEvent o) {
        MotionEvent ev = obtain();
        ev.mDeviceId = o.mDeviceId;
        ev.mEdgeFlags = o.mEdgeFlags;
        ev.mDownTime = o.mDownTime;
        ev.mEventTimeNano = o.mEventTimeNano;
        ev.mAction = o.mAction;
        ev.mNumPointers = o.mNumPointers;
        ev.mRawX = o.mRawX;
        ev.mRawY = o.mRawY;
        ev.mMetaState = o.mMetaState;
        ev.mXPrecision = o.mXPrecision;
        ev.mYPrecision = o.mYPrecision;
        ev.mNumSamples = 1;
        ev.mTimeSamples[0] = o.mTimeSamples[0];
        final int NP = (ev.mNumPointers=o.mNumPointers);
        if (ev.mPointerIdentifiers.length >= NP) {
            System.arraycopy(o.mPointerIdentifiers, 0, ev.mPointerIdentifiers, 0, NP);
        } else {
            ev.mPointerIdentifiers = (int[])o.mPointerIdentifiers.clone();
        }
        final int ND = NP * NUM_SAMPLE_DATA;
        if (ev.mDataSamples.length >= ND) {
            System.arraycopy(o.mDataSamples, 0, ev.mDataSamples, 0, ND);
        } else {
            ev.mDataSamples = (float[])o.mDataSamples.clone();
        }
        return ev;
    }
    public void recycle() {
        if (TRACK_RECYCLED_LOCATION) {
            if (mRecycledLocation != null) {
                throw new RuntimeException(toString() + " recycled twice!", mRecycledLocation);
            }
            mRecycledLocation = new RuntimeException("Last recycled here");
        } else if (mRecycled) {
            throw new RuntimeException(toString() + " recycled twice!");
        }
        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < MAX_RECYCLED) {
                gRecyclerUsed++;
                mNumSamples = 0;
                mNext = gRecyclerTop;
                gRecyclerTop = this;
            }
        }
    }
    public final int getAction() {
        return mAction;
    }
    public final int getActionMasked() {
        return mAction & ACTION_MASK;
    }
    public final int getActionIndex() {
        return (mAction & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;
    }
    public final long getDownTime() {
        return mDownTime;
    }
    public final long getEventTime() {
        return mTimeSamples[0];
    }
    public final long getEventTimeNano() {
        return mEventTimeNano;
    }
    public final float getX() {
        return mDataSamples[SAMPLE_X];
    }
    public final float getY() {
        return mDataSamples[SAMPLE_Y];
    }
    public final float getPressure() {
        return mDataSamples[SAMPLE_PRESSURE];
    }
    public final float getSize() {
        return mDataSamples[SAMPLE_SIZE];
    }
    public final int getPointerCount() {
        return mNumPointers;
    }
    public final int getPointerId(int pointerIndex) {
        return mPointerIdentifiers[pointerIndex];
    }
    public final int findPointerIndex(int pointerId) {
        int i = mNumPointers;
        while (i > 0) {
            i--;
            if (mPointerIdentifiers[i] == pointerId) {
                return i;
            }
        }
        return -1;
    }
    public final float getX(int pointerIndex) {
        return mDataSamples[(pointerIndex*NUM_SAMPLE_DATA) + SAMPLE_X];
    }
    public final float getY(int pointerIndex) {
        return mDataSamples[(pointerIndex*NUM_SAMPLE_DATA) + SAMPLE_Y];
    }
    public final float getPressure(int pointerIndex) {
        return mDataSamples[(pointerIndex*NUM_SAMPLE_DATA) + SAMPLE_PRESSURE];
    }
    public final float getSize(int pointerIndex) {
        return mDataSamples[(pointerIndex*NUM_SAMPLE_DATA) + SAMPLE_SIZE];
    }
    public final int getMetaState() {
        return mMetaState;
    }
    public final float getRawX() {
        return mRawX;
    }
    public final float getRawY() {
        return mRawY;
    }
    public final float getXPrecision() {
        return mXPrecision;
    }
    public final float getYPrecision() {
        return mYPrecision;
    }
    public final int getHistorySize() {
        return mNumSamples - 1;
    }
    public final long getHistoricalEventTime(int pos) {
        return mTimeSamples[pos + 1];
    }
    public final float getHistoricalX(int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers) + SAMPLE_X];
    }
    public final float getHistoricalY(int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers) + SAMPLE_Y];
    }
    public final float getHistoricalPressure(int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers) + SAMPLE_PRESSURE];
    }
    public final float getHistoricalSize(int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers) + SAMPLE_SIZE];
    }
    public final float getHistoricalX(int pointerIndex, int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers)
                            + (pointerIndex * NUM_SAMPLE_DATA) + SAMPLE_X];
    }
    public final float getHistoricalY(int pointerIndex, int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers)
                            + (pointerIndex * NUM_SAMPLE_DATA) + SAMPLE_Y];
    }
    public final float getHistoricalPressure(int pointerIndex, int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers)
                            + (pointerIndex * NUM_SAMPLE_DATA) + SAMPLE_PRESSURE];
    }
    public final float getHistoricalSize(int pointerIndex, int pos) {
        return mDataSamples[((pos + 1) * NUM_SAMPLE_DATA * mNumPointers)
                            + (pointerIndex * NUM_SAMPLE_DATA) + SAMPLE_SIZE];
    }
    public final int getDeviceId() {
        return mDeviceId;
    }
    public final int getEdgeFlags() {
        return mEdgeFlags;
    }
    public final void setEdgeFlags(int flags) {
        mEdgeFlags = flags;
    }
    public final void setAction(int action) {
        mAction = action;
    }
    public final void offsetLocation(float deltaX, float deltaY) {
        final int N = mNumPointers*mNumSamples*4;
        final float[] pos = mDataSamples;
        for (int i=0; i<N; i+=NUM_SAMPLE_DATA) {
            pos[i+SAMPLE_X] += deltaX;
            pos[i+SAMPLE_Y] += deltaY;
        }
    }
    public final void setLocation(float x, float y) {
        float deltaX = x-mDataSamples[SAMPLE_X];
        float deltaY = y-mDataSamples[SAMPLE_Y];
        if (deltaX != 0 || deltaY != 0) {
            offsetLocation(deltaX, deltaY);
        }
    }
    public final void addBatch(long eventTime, float x, float y,
            float pressure, float size, int metaState) {
        float[] data = mDataSamples;
        long[] times = mTimeSamples;
        final int NP = mNumPointers;
        final int NS = mNumSamples;
        final int NI = NP*NS;
        final int ND = NI * NUM_SAMPLE_DATA;
        if (data.length <= ND) {
            final int NEW_ND = ND + (NP * (BASE_AVAIL_SAMPLES * NUM_SAMPLE_DATA));
            float[] newData = new float[NEW_ND];
            System.arraycopy(data, 0, newData, 0, ND);
            mDataSamples = data = newData;
        }
        if (times.length <= NS) {
            final int NEW_NS = NS + BASE_AVAIL_SAMPLES;
            long[] newHistoryTimes = new long[NEW_NS];
            System.arraycopy(times, 0, newHistoryTimes, 0, NS);
            mTimeSamples = times = newHistoryTimes;
        }
        times[NS] = times[0];
        times[0] = eventTime;
        final int pos = NS*NUM_SAMPLE_DATA;
        data[pos+SAMPLE_X] = data[SAMPLE_X];
        data[pos+SAMPLE_Y] = data[SAMPLE_Y];
        data[pos+SAMPLE_PRESSURE] = data[SAMPLE_PRESSURE];
        data[pos+SAMPLE_SIZE] = data[SAMPLE_SIZE];
        data[SAMPLE_X] = x;
        data[SAMPLE_Y] = y;
        data[SAMPLE_PRESSURE] = pressure;
        data[SAMPLE_SIZE] = size;
        mNumSamples = NS+1;
        mRawX = x;
        mRawY = y;
        mMetaState |= metaState;
    }
    public final void addBatch(long eventTime, float[] inData, int metaState) {
        float[] data = mDataSamples;
        long[] times = mTimeSamples;
        final int NP = mNumPointers;
        final int NS = mNumSamples;
        final int NI = NP*NS;
        final int ND = NI * NUM_SAMPLE_DATA;
        if (data.length < (ND+(NP*NUM_SAMPLE_DATA))) {
            final int NEW_ND = ND + (NP * (BASE_AVAIL_SAMPLES * NUM_SAMPLE_DATA));
            float[] newData = new float[NEW_ND];
            System.arraycopy(data, 0, newData, 0, ND);
            mDataSamples = data = newData;
        }
        if (times.length < (NS+1)) {
            final int NEW_NS = NS + BASE_AVAIL_SAMPLES;
            long[] newHistoryTimes = new long[NEW_NS];
            System.arraycopy(times, 0, newHistoryTimes, 0, NS);
            mTimeSamples = times = newHistoryTimes;
        }
        times[NS] = times[0];
        times[0] = eventTime;
        System.arraycopy(data, 0, data, ND, mNumPointers*NUM_SAMPLE_DATA);
        System.arraycopy(inData, 0, data, 0, mNumPointers*NUM_SAMPLE_DATA);
        mNumSamples = NS+1;
        mRawX = inData[SAMPLE_X];
        mRawY = inData[SAMPLE_Y];
        mMetaState |= metaState;
        if (DEBUG_POINTERS) {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Add:");
            for (int i=0; i<mNumPointers; i++) {
                sb.append(" #");
                sb.append(mPointerIdentifiers[i]);
                sb.append("(");
                sb.append(mDataSamples[(i*NUM_SAMPLE_DATA) + SAMPLE_X]);
                sb.append(",");
                sb.append(mDataSamples[(i*NUM_SAMPLE_DATA) + SAMPLE_Y]);
                sb.append(")");
            }
            Log.v("MotionEvent", sb.toString());
        }
    }
    @Override
    public String toString() {
        return "MotionEvent{" + Integer.toHexString(System.identityHashCode(this))
            + " action=" + mAction + " x=" + getX()
            + " y=" + getY() + " pressure=" + getPressure() + " size=" + getSize() + "}";
    }
    public static final Parcelable.Creator<MotionEvent> CREATOR
            = new Parcelable.Creator<MotionEvent>() {
        public MotionEvent createFromParcel(Parcel in) {
            MotionEvent ev = obtain();
            ev.readFromParcel(in);
            return ev;
        }
        public MotionEvent[] newArray(int size) {
            return new MotionEvent[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mDownTime);
        out.writeLong(mEventTimeNano);
        out.writeInt(mAction);
        out.writeInt(mMetaState);
        out.writeFloat(mRawX);
        out.writeFloat(mRawY);
        final int NP = mNumPointers;
        out.writeInt(NP);
        final int NS = mNumSamples;
        out.writeInt(NS);
        final int NI = NP*NS;
        if (NI > 0) {
            int i;
            int[] state = mPointerIdentifiers;
            for (i=0; i<NP; i++) {
                out.writeInt(state[i]);
            }
            final int ND = NI*NUM_SAMPLE_DATA;
            float[] history = mDataSamples;
            for (i=0; i<ND; i++) {
                out.writeFloat(history[i]);
            }
            long[] times = mTimeSamples;
            for (i=0; i<NS; i++) {
                out.writeLong(times[i]);
            }
        }
        out.writeFloat(mXPrecision);
        out.writeFloat(mYPrecision);
        out.writeInt(mDeviceId);
        out.writeInt(mEdgeFlags);
    }
    private void readFromParcel(Parcel in) {
        mDownTime = in.readLong();
        mEventTimeNano = in.readLong();
        mAction = in.readInt();
        mMetaState = in.readInt();
        mRawX = in.readFloat();
        mRawY = in.readFloat();
        final int NP = in.readInt();
        mNumPointers = NP;
        final int NS = in.readInt();
        mNumSamples = NS;
        final int NI = NP*NS;
        if (NI > 0) {
            int[] ids = mPointerIdentifiers;
            if (ids.length < NP) {
                mPointerIdentifiers = ids = new int[NP];
            }
            for (int i=0; i<NP; i++) {
                ids[i] = in.readInt();
            }
            float[] history = mDataSamples;
            final int ND = NI*NUM_SAMPLE_DATA;
            if (history.length < ND) {
                mDataSamples = history = new float[ND];
            }
            for (int i=0; i<ND; i++) {
                history[i] = in.readFloat();
            }
            long[] times = mTimeSamples;
            if (times == null || times.length < NS) {
                mTimeSamples = times = new long[NS];
            }
            for (int i=0; i<NS; i++) {
                times[i] = in.readLong();
            }
        }
        mXPrecision = in.readFloat();
        mYPrecision = in.readFloat();
        mDeviceId = in.readInt();
        mEdgeFlags = in.readInt();
    }
}
