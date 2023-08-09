public final class AccelerometerListener {
    private static final String TAG = "AccelerometerListener";
    private static final boolean DEBUG = true;
    private static final boolean VDEBUG = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mOrientation;
    private int mPendingOrientation;
    private OrientationListener mListener;
    public static final int ORIENTATION_UNKNOWN = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    public static final int ORIENTATION_HORIZONTAL = 2;
    private static final int ORIENTATION_CHANGED = 1234;
    private static final int VERTICAL_DEBOUNCE = 100;
    private static final int HORIZONTAL_DEBOUNCE = 500;
    private static final double VERTICAL_ANGLE = 50.0;
    public interface OrientationListener {
        public void orientationChanged(int orientation);
    }
    public AccelerometerListener(Context context, OrientationListener listener) {
        mListener = listener;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    public void enable(boolean enable) {
        if (DEBUG) Log.d(TAG, "enable(" + enable + ")");
        synchronized (this) {
            if (enable) {
                mOrientation = ORIENTATION_UNKNOWN;
                mPendingOrientation = ORIENTATION_UNKNOWN;
                mSensorManager.registerListener(mSensorListener, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                mSensorManager.unregisterListener(mSensorListener);
                mHandler.removeMessages(ORIENTATION_CHANGED);
            }
        }
    }
    private void setOrientation(int orientation) {
        synchronized (this) {
            if (mPendingOrientation == orientation) {
                return;
            }
            mHandler.removeMessages(ORIENTATION_CHANGED);
            if (mOrientation != orientation) {
                mPendingOrientation = orientation;
                Message m = mHandler.obtainMessage(ORIENTATION_CHANGED);
                int delay = (orientation == ORIENTATION_VERTICAL ? VERTICAL_DEBOUNCE
                                                                 : HORIZONTAL_DEBOUNCE);
                mHandler.sendMessageDelayed(m, delay);
            } else {
                mPendingOrientation = ORIENTATION_UNKNOWN;
            }
        }
    }
    private void onSensorEvent(double x, double y, double z) {
        if (VDEBUG) Log.d(TAG, "onSensorEvent(" + x + ", " + y + ", " + z + ")");
        if (x == 0.0 || y == 0.0 || z == 0.0) return;
        double xy = Math.sqrt(x*x + y*y);
        double angle = Math.atan2(xy, z);
        angle = angle * 180.0 / Math.PI;
        int orientation = (angle >  VERTICAL_ANGLE ? ORIENTATION_VERTICAL : ORIENTATION_HORIZONTAL);
        if (VDEBUG) Log.d(TAG, "angle: " + angle + " orientation: " + orientation);
        setOrientation(orientation);
    }
    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            onSensorEvent(event.values[0], event.values[1], event.values[2]);
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ORIENTATION_CHANGED:
                synchronized (this) {
                    mOrientation = mPendingOrientation;
                    if (DEBUG) {
                        Log.d(TAG, "orientation: " +
                            (mOrientation == ORIENTATION_HORIZONTAL ? "horizontal"
                                : (mOrientation == ORIENTATION_VERTICAL ? "vertical"
                                    : "unknown")));
                    }
                    mListener.orientationChanged(mOrientation);
                }
                break;
            }
        }
    };
}
