public final class VelocityTracker implements Poolable<VelocityTracker> {
    static final String TAG = "VelocityTracker";
    static final boolean DEBUG = false;
    static final boolean localLOGV = DEBUG || Config.LOGV;
    static final int NUM_PAST = 10;
    static final int LONGEST_PAST_TIME = 200;
    static final VelocityTracker[] mPool = new VelocityTracker[1];
    private static final Pool<VelocityTracker> sPool = Pools.synchronizedPool(
            Pools.finitePool(new PoolableManager<VelocityTracker>() {
                public VelocityTracker newInstance() {
                    return new VelocityTracker();
                }
                public void onAcquired(VelocityTracker element) {
                    element.clear();
                }
                public void onReleased(VelocityTracker element) {
                }
            }, 2));
    final float mPastX[][] = new float[MotionEvent.BASE_AVAIL_POINTERS][NUM_PAST];
    final float mPastY[][] = new float[MotionEvent.BASE_AVAIL_POINTERS][NUM_PAST];
    final long mPastTime[][] = new long[MotionEvent.BASE_AVAIL_POINTERS][NUM_PAST];
    float mYVelocity[] = new float[MotionEvent.BASE_AVAIL_POINTERS];
    float mXVelocity[] = new float[MotionEvent.BASE_AVAIL_POINTERS];
    int mLastTouch;
    private VelocityTracker mNext;
    static public VelocityTracker obtain() {
        return sPool.acquire();
    }
    public void recycle() {
        sPool.release(this);
    }
    public void setNextPoolable(VelocityTracker element) {
        mNext = element;
    }
    public VelocityTracker getNextPoolable() {
        return mNext;
    }
    private VelocityTracker() {
        clear();
    }
    public void clear() {
        final long[][] pastTime = mPastTime;
        for (int p = 0; p < MotionEvent.BASE_AVAIL_POINTERS; p++) {
            for (int i = 0; i < NUM_PAST; i++) {
                pastTime[p][i] = Long.MIN_VALUE;
            }
        }
    }
    public void addMovement(MotionEvent ev) {
        final int N = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        int touchIndex = (mLastTouch + 1) % NUM_PAST;
        for (int i=0; i<N; i++) {
            for (int id = 0; id < MotionEvent.BASE_AVAIL_POINTERS; id++) {
                mPastTime[id][touchIndex] = Long.MIN_VALUE;
            }
            for (int p = 0; p < pointerCount; p++) {
                int id = ev.getPointerId(p);
                mPastX[id][touchIndex] = ev.getHistoricalX(p, i);
                mPastY[id][touchIndex] = ev.getHistoricalY(p, i);
                mPastTime[id][touchIndex] = ev.getHistoricalEventTime(i);
            }
            touchIndex = (touchIndex + 1) % NUM_PAST;
        }
        for (int id = 0; id < MotionEvent.BASE_AVAIL_POINTERS; id++) {
            mPastTime[id][touchIndex] = Long.MIN_VALUE;
        }
        final long time = ev.getEventTime();
        for (int p = 0; p < pointerCount; p++) {
            int id = ev.getPointerId(p);
            mPastX[id][touchIndex] = ev.getX(p);
            mPastY[id][touchIndex] = ev.getY(p);
            mPastTime[id][touchIndex] = time;
        }
        mLastTouch = touchIndex;
    }
    public void computeCurrentVelocity(int units) {
        computeCurrentVelocity(units, Float.MAX_VALUE);
    }
    public void computeCurrentVelocity(int units, float maxVelocity) {
        for (int pos = 0; pos < MotionEvent.BASE_AVAIL_POINTERS; pos++) {
            final float[] pastX = mPastX[pos];
            final float[] pastY = mPastY[pos];
            final long[] pastTime = mPastTime[pos];
            final int lastTouch = mLastTouch;
            int oldestTouch = lastTouch;
            if (pastTime[lastTouch] != Long.MIN_VALUE) { 
                final float acceptableTime = pastTime[lastTouch] - LONGEST_PAST_TIME;
                int nextOldestTouch = (NUM_PAST + oldestTouch - 1) % NUM_PAST;
                while (pastTime[nextOldestTouch] >= acceptableTime &&
                        nextOldestTouch != lastTouch) {
                    oldestTouch = nextOldestTouch;
                    nextOldestTouch = (NUM_PAST + oldestTouch - 1) % NUM_PAST;
                }
            }
            final float oldestX = pastX[oldestTouch];
            final float oldestY = pastY[oldestTouch];
            final long oldestTime = pastTime[oldestTouch];
            float accumX = 0;
            float accumY = 0;
            float N = (lastTouch - oldestTouch + NUM_PAST) % NUM_PAST + 1;
            if (N > 3) N--;
            for (int i=1; i < N; i++) {
                final int j = (oldestTouch + i) % NUM_PAST;
                final int dur = (int)(pastTime[j] - oldestTime);
                if (dur == 0) continue;
                float dist = pastX[j] - oldestX;
                float vel = (dist/dur) * units;   
                accumX = (accumX == 0) ? vel : (accumX + vel) * .5f;
                dist = pastY[j] - oldestY;
                vel = (dist/dur) * units;   
                accumY = (accumY == 0) ? vel : (accumY + vel) * .5f;
            }
            mXVelocity[pos] = accumX < 0.0f ? Math.max(accumX, -maxVelocity)
                    : Math.min(accumX, maxVelocity);
            mYVelocity[pos] = accumY < 0.0f ? Math.max(accumY, -maxVelocity)
                    : Math.min(accumY, maxVelocity);
            if (localLOGV) Log.v(TAG, "Y velocity=" + mYVelocity +" X velocity="
                    + mXVelocity + " N=" + N);
        }
    }
    public float getXVelocity() {
        return mXVelocity[0];
    }
    public float getYVelocity() {
        return mYVelocity[0];
    }
    public float getXVelocity(int id) {
        return mXVelocity[id];
    }
    public float getYVelocity(int id) {
        return mYVelocity[id];
    }
}
