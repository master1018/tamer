public abstract class WindowOrientationListener {
    private static final String TAG = "WindowOrientationListener";
    private static final boolean DEBUG = false;
    private static final boolean localLOGV = DEBUG || Config.DEBUG;
    private SensorManager mSensorManager;
    private boolean mEnabled = false;
    private int mRate;
    private Sensor mSensor;
    private SensorEventListenerImpl mSensorEventListener;
    public WindowOrientationListener(Context context) {
        this(context, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private WindowOrientationListener(Context context, int rate) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mRate = rate;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mSensor != null) {
            mSensorEventListener = new SensorEventListenerImpl();
        }
    }
    public void enable() {
        if (mSensor == null) {
            Log.w(TAG, "Cannot detect sensors. Not enabled");
            return;
        }
        if (mEnabled == false) {
            if (localLOGV) Log.d(TAG, "WindowOrientationListener enabled");
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
            if (localLOGV) Log.d(TAG, "WindowOrientationListener disabled");
            mSensorManager.unregisterListener(mSensorEventListener);
            mEnabled = false;
        }
    }
    public int getCurrentRotation() {
        if (mEnabled) {
            return mSensorEventListener.getCurrentRotation();
        }
        return -1;
    }
    class SensorEventListenerImpl implements SensorEventListener {
        private static final float RADIANS_TO_DEGREES = (float) (180 / Math.PI);
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;
        private static final int ROTATION_0 = 0;
        private static final int ROTATION_90 = 1;
        private static final int ROTATION_270 = 2;
        private int mRotation = ROTATION_0;
        private final int[] SURFACE_ROTATIONS = new int[] {Surface.ROTATION_0, Surface.ROTATION_90,
                Surface.ROTATION_270};
        private final int[][][] THRESHOLDS = new int[][][] {
                {{60, 180}, {180, 300}},
                {{0, 45}, {45, 165}, {330, 360}},
                {{0, 30}, {195, 315}, {315, 360}}
        };
        private final int[][] ROTATE_TO = new int[][] {
                {ROTATION_270, ROTATION_90},
                {ROTATION_0, ROTATION_270, ROTATION_0},
                {ROTATION_0, ROTATION_90, ROTATION_0}
        };
        private static final int MAX_TILT = 65;
        private final int[] MAX_TRANSITION_TILT = new int[] {MAX_TILT, MAX_TILT, MAX_TILT};
        private static final int PARTIAL_TILT = 45;
        private static final int MAX_DEVIATION_FROM_GRAVITY = 1;
        private static final int SAMPLING_PERIOD_MS = 200;
        private static final int DEFAULT_TIME_CONSTANT_MS = 200;
        private static final int TILTED_TIME_CONSTANT_MS = 600;
        private static final int ACCELERATING_TIME_CONSTANT_MS = 5000;
        private static final float DEFAULT_LOWPASS_ALPHA =
            (float) SAMPLING_PERIOD_MS / (DEFAULT_TIME_CONSTANT_MS + SAMPLING_PERIOD_MS);
        private static final float TILTED_LOWPASS_ALPHA =
            (float) SAMPLING_PERIOD_MS / (TILTED_TIME_CONSTANT_MS + SAMPLING_PERIOD_MS);
        private static final float ACCELERATING_LOWPASS_ALPHA =
            (float) SAMPLING_PERIOD_MS / (ACCELERATING_TIME_CONSTANT_MS + SAMPLING_PERIOD_MS);
        private float[] mFilteredVector = new float[] {0, 0, 0};
        int getCurrentRotation() {
            return SURFACE_ROTATIONS[mRotation];
        }
        private void calculateNewRotation(int orientation, int tiltAngle) {
            if (localLOGV) Log.i(TAG, orientation + ", " + tiltAngle + ", " + mRotation);
            int thresholdRanges[][] = THRESHOLDS[mRotation];
            int row = -1;
            for (int i = 0; i < thresholdRanges.length; i++) {
                if (orientation >= thresholdRanges[i][0] && orientation < thresholdRanges[i][1]) {
                    row = i;
                    break;
                }
            }
            if (row == -1) return; 
            int rotation = ROTATE_TO[mRotation][row];
            if (tiltAngle > MAX_TRANSITION_TILT[rotation]) {
                return;
            }
            if (localLOGV) Log.i(TAG, " new rotation = " + rotation);
            mRotation = rotation;
            onOrientationChanged(SURFACE_ROTATIONS[rotation]);
        }
        private float lowpassFilter(float newValue, float oldValue, float alpha) {
            return alpha * newValue + (1 - alpha) * oldValue;
        }
        private float vectorMagnitude(float x, float y, float z) {
            return (float) Math.sqrt(x*x + y*y + z*z);
        }
        private float tiltAngle(float z, float magnitude) {
            return Math.abs((float) Math.asin(z / magnitude) * RADIANS_TO_DEGREES);
        }
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[_DATA_X];
            float y = event.values[_DATA_Y];
            float z = event.values[_DATA_Z];
            float magnitude = vectorMagnitude(x, y, z);
            float deviation = Math.abs(magnitude - SensorManager.STANDARD_GRAVITY);
            float tiltAngle = tiltAngle(z, magnitude);
            float alpha = DEFAULT_LOWPASS_ALPHA;
            if (tiltAngle > MAX_TILT) {
                return;
            } else if (deviation > MAX_DEVIATION_FROM_GRAVITY) {
                alpha = ACCELERATING_LOWPASS_ALPHA;
            } else if (tiltAngle > PARTIAL_TILT) {
                alpha = TILTED_LOWPASS_ALPHA;
            }
            x = mFilteredVector[0] = lowpassFilter(x, mFilteredVector[0], alpha);
            y = mFilteredVector[1] = lowpassFilter(y, mFilteredVector[1], alpha);
            z = mFilteredVector[2] = lowpassFilter(z, mFilteredVector[2], alpha);
            magnitude = vectorMagnitude(x, y, z);
            tiltAngle = tiltAngle(z, magnitude);
            float orientationAngle = (float) Math.atan2(-x, y) * RADIANS_TO_DEGREES;
            int orientation = Math.round(orientationAngle);
            if (orientation < 0) {
                orientation += 360;
            }
            calculateNewRotation(orientation, Math.round(tiltAngle));
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    public boolean canDetectOrientation() {
        return mSensor != null;
    }
    abstract public void onOrientationChanged(int rotation);
}
