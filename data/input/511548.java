public abstract class OrientationListener implements SensorListener {
    private OrientationEventListener mOrientationEventLis;
    public static final int ORIENTATION_UNKNOWN = OrientationEventListener.ORIENTATION_UNKNOWN;
    public OrientationListener(Context context) {
        mOrientationEventLis = new OrientationEventListenerInternal(context);
    }
    public OrientationListener(Context context, int rate) {
        mOrientationEventLis = new OrientationEventListenerInternal(context, rate);
    }
    class OrientationEventListenerInternal extends OrientationEventListener {
        OrientationEventListenerInternal(Context context) {
            super(context);
        }
        OrientationEventListenerInternal(Context context, int rate) {
            super(context, rate);
            registerListener(OrientationListener.this);
        }
        public void onOrientationChanged(int orientation) {
            OrientationListener.this.onOrientationChanged(orientation);
        }
    }
    public void enable() {
        mOrientationEventLis.enable();
    }
    public void disable() {
        mOrientationEventLis.disable();
    }
    public void onAccuracyChanged(int sensor, int accuracy) {
    }
    public void onSensorChanged(int sensor, float[] values) {
    }
    abstract public void onOrientationChanged(int orientation);
}
