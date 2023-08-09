public abstract class OrientationEventListener {
    private static final String TAG = "OrientationEventListener";
    private static final boolean DEBUG = false;
    private static final boolean localLOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private int mOrientation = ORIENTATION_UNKNOWN;
    private SensorManager mSensorManager;
    private boolean mEnabled = false;
    private int mRate;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private OrientationListener mOldListener;
    public static final int ORIENTATION_UNKNOWN = -1;
    public OrientationEventListener(Context context) {
        this(context, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public OrientationEventListener(Context context, int rate) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mRate = rate;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mSensor != null) {
            mSensorEventListener = new SensorEventListenerImpl();
        }
    }
    void registerListener(OrientationListener lis) {
        mOldListener = lis;
    }
    public void enable() {
        if (mSensor == null) {
            Log.w(TAG, "Cannot detect sensors. Not enabled");
            return;
        }
        if (mEnabled == false) {
            if (localLOGV) Log.d(TAG, "OrientationEventListener enabled");
            mSensorManager.registerListener(mSensorEventListener, mSensor, mRate);
            mEnabled = true;
        }
    }
    public void disable() {
        if (mSensor == null) {
            Log.w(TAG, "Cannot detect sensors. Invalid disable");
            return;
        }
        if (mEnabled == true) {
            if (localLOGV) Log.d(TAG, "OrientationEventListener disabled");
            mSensorManager.unregisterListener(mSensorEventListener);
            mEnabled = false;
        }
    }
    class SensorEventListenerImpl implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];        
            float magnitude = X*X + Y*Y;
            if (magnitude * 4 >= Z*Z) {
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float)Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int)Math.round(angle);
                while (orientation >= 360) {
                    orientation -= 360;
                } 
                while (orientation < 0) {
                    orientation += 360;
                }
            }
            if (mOldListener != null) {
                mOldListener.onSensorChanged(Sensor.TYPE_ACCELEROMETER, event.values);
            }
            if (orientation != mOrientation) {
                mOrientation = orientation;
                onOrientationChanged(orientation);
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    public boolean canDetectOrientation() {
        return mSensor != null;
    }
    abstract public void onOrientationChanged(int orientation);
}
