public class SensorManager
{
    private static final String TAG = "SensorManager";
    private static final float[] mTempMatrix = new float[16];
    @Deprecated
    public static final int SENSOR_ORIENTATION = 1 << 0;
    @Deprecated
    public static final int SENSOR_ACCELEROMETER = 1 << 1;
    @Deprecated
    public static final int SENSOR_TEMPERATURE = 1 << 2;
    @Deprecated
    public static final int SENSOR_MAGNETIC_FIELD = 1 << 3;
    @Deprecated
    public static final int SENSOR_LIGHT = 1 << 4;
    @Deprecated
    public static final int SENSOR_PROXIMITY = 1 << 5;
    @Deprecated
    public static final int SENSOR_TRICORDER = 1 << 6;
    @Deprecated
    public static final int SENSOR_ORIENTATION_RAW = 1 << 7;
    @Deprecated
    public static final int SENSOR_ALL = 0x7F;
    @Deprecated
    public static final int SENSOR_MIN = SENSOR_ORIENTATION;
    @Deprecated
    public static final int SENSOR_MAX = ((SENSOR_ALL + 1)>>1);
    @Deprecated
    public static final int DATA_X = 0;
    @Deprecated
    public static final int DATA_Y = 1;
    @Deprecated
    public static final int DATA_Z = 2;
    @Deprecated
    public static final int RAW_DATA_INDEX = 3;
    @Deprecated
    public static final int RAW_DATA_X = 3;
    @Deprecated
    public static final int RAW_DATA_Y = 4;
    @Deprecated
    public static final int RAW_DATA_Z = 5;
    public static final float STANDARD_GRAVITY = 9.80665f;
    public static final float GRAVITY_SUN             = 275.0f;
    public static final float GRAVITY_MERCURY         = 3.70f;
    public static final float GRAVITY_VENUS           = 8.87f;
    public static final float GRAVITY_EARTH           = 9.80665f;
    public static final float GRAVITY_MOON            = 1.6f;
    public static final float GRAVITY_MARS            = 3.71f;
    public static final float GRAVITY_JUPITER         = 23.12f;
    public static final float GRAVITY_SATURN          = 8.96f;
    public static final float GRAVITY_URANUS          = 8.69f;
    public static final float GRAVITY_NEPTUNE         = 11.0f;
    public static final float GRAVITY_PLUTO           = 0.6f;
    public static final float GRAVITY_DEATH_STAR_I    = 0.000000353036145f;
    public static final float GRAVITY_THE_ISLAND      = 4.815162342f;
    public static final float MAGNETIC_FIELD_EARTH_MAX = 60.0f;
    public static final float MAGNETIC_FIELD_EARTH_MIN = 30.0f;
    public static final float LIGHT_SUNLIGHT_MAX = 120000.0f;
    public static final float LIGHT_SUNLIGHT     = 110000.0f;
    public static final float LIGHT_SHADE        = 20000.0f;
    public static final float LIGHT_OVERCAST     = 10000.0f;
    public static final float LIGHT_SUNRISE      = 400.0f;
    public static final float LIGHT_CLOUDY       = 100.0f;
    public static final float LIGHT_FULLMOON     = 0.25f;
    public static final float LIGHT_NO_MOON      = 0.001f;
    public static final int SENSOR_DELAY_FASTEST = 0;
    public static final int SENSOR_DELAY_GAME = 1;
    public static final int SENSOR_DELAY_UI = 2;
    public static final int SENSOR_DELAY_NORMAL = 3;
    public static final int SENSOR_STATUS_UNRELIABLE = 0;
    public static final int SENSOR_STATUS_ACCURACY_LOW = 1;
    public static final int SENSOR_STATUS_ACCURACY_MEDIUM = 2;
    public static final int SENSOR_STATUS_ACCURACY_HIGH = 3;
    public static final int AXIS_X = 1;
    public static final int AXIS_Y = 2;
    public static final int AXIS_Z = 3;
    public static final int AXIS_MINUS_X = AXIS_X | 0x80;
    public static final int AXIS_MINUS_Y = AXIS_Y | 0x80;
    public static final int AXIS_MINUS_Z = AXIS_Z | 0x80;
    private ISensorService mSensorService;
    Looper mMainLooper;
    @SuppressWarnings("deprecation")
    private HashMap<SensorListener, LegacyListener> mLegacyListenersMap =
        new HashMap<SensorListener, LegacyListener>();
    private static final int SENSOR_DISABLE = -1;
    private static boolean sSensorModuleInitialized = false;
    private static ArrayList<Sensor> sFullSensorsList = new ArrayList<Sensor>();
    private static SparseArray<List<Sensor>> sSensorListByType = new SparseArray<List<Sensor>>();
    private static IWindowManager sWindowManager;
    private static int sRotation = Surface.ROTATION_0;
    private static SensorThread sSensorThread;
    static SparseArray<Sensor> sHandleToSensor = new SparseArray<Sensor>();
    static final ArrayList<ListenerDelegate> sListeners =
        new ArrayList<ListenerDelegate>();
    static private class SensorThread {
        Thread mThread;
        boolean mSensorsReady;
        SensorThread() {
            sensors_data_init();
        }
        @Override
        protected void finalize() {
            sensors_data_uninit();
        }
        boolean startLocked(ISensorService service) {
            try {
                if (mThread == null) {
                    Bundle dataChannel = service.getDataChannel();
                    if (dataChannel != null) {
                        mSensorsReady = false;
                        SensorThreadRunnable runnable = new SensorThreadRunnable(dataChannel);
                        Thread thread = new Thread(runnable, SensorThread.class.getName());
                        thread.start();
                        synchronized (runnable) {
                            while (mSensorsReady == false) {
                                runnable.wait();
                            }
                        }
                        mThread = thread;
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in startLocked: ", e);
            } catch (InterruptedException e) {
            }
            return mThread == null ? false : true;
        }
        private class SensorThreadRunnable implements Runnable {
            private Bundle mDataChannel;
            SensorThreadRunnable(Bundle dataChannel) {
                mDataChannel = dataChannel;
            }
            private boolean open() {
                Parcelable[] pfds = mDataChannel.getParcelableArray("fds");
                FileDescriptor[] fds;
                if (pfds != null) {
                    int length = pfds.length;
                    fds = new FileDescriptor[length];
                    for (int i = 0; i < length; i++) {
                        ParcelFileDescriptor pfd = (ParcelFileDescriptor)pfds[i];
                        fds[i] = pfd.getFileDescriptor();
                    }
                } else {
                    fds = null;
                }
                int[] ints = mDataChannel.getIntArray("ints");
                sensors_data_open(fds, ints);
                if (pfds != null) {
                    try {
                        for (int i = pfds.length - 1; i >= 0; i--) {
                            ParcelFileDescriptor pfd = (ParcelFileDescriptor)pfds[i];
                            pfd.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IOException: ", e);
                    }
                }
                mDataChannel = null;
                return true;
            }
            public void run() {
                final float[] values = new float[3];
                final int[] status = new int[1];
                final long timestamp[] = new long[1];
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
                if (!open()) {
                    return;
                }
                synchronized (this) {
                    mSensorsReady = true;
                    this.notify();
                }
                while (true) {
                    final int sensor = sensors_data_poll(values, status, timestamp);
                    int accuracy = status[0];
                    synchronized (sListeners) {
                        if (sensor == -1 || sListeners.isEmpty()) {
                            if (sensor == -1) {
                                Log.d(TAG, "_sensors_data_poll() failed, we bail out.");
                            }
                            sensors_data_close();
                            mThread = null;
                            break;
                        }
                        final Sensor sensorObject = sHandleToSensor.get(sensor);
                        if (sensorObject != null) {
                            final int size = sListeners.size();
                            for (int i=0 ; i<size ; i++) {
                                ListenerDelegate listener = sListeners.get(i);
                                if (listener.hasSensor(sensorObject)) {
                                    listener.onSensorChangedLocked(sensorObject,
                                            values, timestamp, accuracy);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private class ListenerDelegate extends Binder {
        final SensorEventListener mSensorEventListener;
        private final ArrayList<Sensor> mSensorList = new ArrayList<Sensor>();
        private final Handler mHandler;
        private SensorEvent mValuesPool;
        public int mSensors;
        ListenerDelegate(SensorEventListener listener, Sensor sensor, Handler handler) {
            mSensorEventListener = listener;
            Looper looper = (handler != null) ? handler.getLooper() : mMainLooper;
            mHandler = new Handler(looper) {
                @Override
                public void handleMessage(Message msg) {
                    SensorEvent t = (SensorEvent)msg.obj;
                    if (t.accuracy >= 0) {
                        mSensorEventListener.onAccuracyChanged(t.sensor, t.accuracy);
                    }
                    mSensorEventListener.onSensorChanged(t);
                    returnToPool(t);
                }
            };
            addSensor(sensor);
        }
        protected SensorEvent createSensorEvent() {
            return new SensorEvent(3);
        }
        protected SensorEvent getFromPool() {
            SensorEvent t = null;
            synchronized (this) {
                t = mValuesPool;
                mValuesPool = null;
            }
            if (t == null) {
                t = createSensorEvent();
            }
            return t;
        }
        protected void returnToPool(SensorEvent t) {
            synchronized (this) {
                if (mValuesPool == null) {
                    mValuesPool = t;
                }
            }
        }
        Object getListener() {
            return mSensorEventListener;
        }
        int addSensor(Sensor sensor) {
            mSensors |= 1<<sensor.getHandle();
            mSensorList.add(sensor);
            return mSensors;
        }
        int removeSensor(Sensor sensor) {
            mSensors &= ~(1<<sensor.getHandle());
            mSensorList.remove(sensor);
            return mSensors;
        }
        boolean hasSensor(Sensor sensor) {
            return ((mSensors & (1<<sensor.getHandle())) != 0);
        }
        List<Sensor> getSensors() {
            return mSensorList;
        }
        void onSensorChangedLocked(Sensor sensor, float[] values, long[] timestamp, int accuracy) {
            SensorEvent t = getFromPool();
            final float[] v = t.values;
            v[0] = values[0];
            v[1] = values[1];
            v[2] = values[2];
            t.timestamp = timestamp[0];
            t.accuracy = accuracy;
            t.sensor = sensor;
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = t;
            mHandler.sendMessage(msg);
        }
    }
    public SensorManager(Looper mainLooper) {
        mSensorService = ISensorService.Stub.asInterface(
                ServiceManager.getService(Context.SENSOR_SERVICE));
        mMainLooper = mainLooper;
        synchronized(sListeners) {
            if (!sSensorModuleInitialized) {
                sSensorModuleInitialized = true;
                nativeClassInit();
                sWindowManager = IWindowManager.Stub.asInterface(
                        ServiceManager.getService("window"));
                if (sWindowManager != null) {
                    try {
                        sRotation = sWindowManager.watchRotation(
                            new IRotationWatcher.Stub() {
                                public void onRotationChanged(int rotation) {
                                    SensorManager.this.onRotationChanged(rotation);
                                }
                            }
                        );
                    } catch (RemoteException e) {
                    }
                }
                sensors_module_init();
                final ArrayList<Sensor> fullList = sFullSensorsList;
                int i = 0;
                do {
                    Sensor sensor = new Sensor();
                    i = sensors_module_get_next_sensor(sensor, i);
                    if (i>=0) {
                        sensor.setLegacyType(getLegacySensorType(sensor.getType()));
                        fullList.add(sensor);
                        sHandleToSensor.append(sensor.getHandle(), sensor);
                    }
                } while (i>0);
                sSensorThread = new SensorThread();
            }
        }
    }
    private int getLegacySensorType(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return SENSOR_ACCELEROMETER;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return SENSOR_MAGNETIC_FIELD;
            case Sensor.TYPE_ORIENTATION:
                return SENSOR_ORIENTATION_RAW;
            case Sensor.TYPE_TEMPERATURE:
                return SENSOR_TEMPERATURE;
        }
        return 0;
    }
    @Deprecated
    public int getSensors() {
        int result = 0;
        final ArrayList<Sensor> fullList = sFullSensorsList;
        for (Sensor i : fullList) {
            switch (i.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    result |= SensorManager.SENSOR_ACCELEROMETER;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    result |= SensorManager.SENSOR_MAGNETIC_FIELD;
                    break;
                case Sensor.TYPE_ORIENTATION:
                    result |= SensorManager.SENSOR_ORIENTATION |
                                    SensorManager.SENSOR_ORIENTATION_RAW;
                    break;
            }
        }
        return result;
    }
    public List<Sensor> getSensorList(int type) {
        List<Sensor> list;
        final ArrayList<Sensor> fullList = sFullSensorsList;
        synchronized(fullList) {
            list = sSensorListByType.get(type);
            if (list == null) {
                if (type == Sensor.TYPE_ALL) {
                    list = fullList;
                } else {
                    list = new ArrayList<Sensor>();
                    for (Sensor i : fullList) {
                        if (i.getType() == type)
                            list.add(i);
                    }
                }
                list = Collections.unmodifiableList(list);
                sSensorListByType.append(type, list);
            }
        }
        return list;
    }
    public Sensor getDefaultSensor(int type) {
        List<Sensor> l = getSensorList(type);
        return l.isEmpty() ? null : l.get(0);
    }
    @Deprecated
    public boolean registerListener(SensorListener listener, int sensors) {
        return registerListener(listener, sensors, SENSOR_DELAY_NORMAL);
    }
    @Deprecated
    public boolean registerListener(SensorListener listener, int sensors, int rate) {
        if (listener == null) {
            return false;
        }
        boolean result = false;
        result = registerLegacyListener(SENSOR_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER,
                listener, sensors, rate) || result;
        result = registerLegacyListener(SENSOR_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD,
                listener, sensors, rate) || result;
        result = registerLegacyListener(SENSOR_ORIENTATION_RAW, Sensor.TYPE_ORIENTATION,
                listener, sensors, rate) || result;
        result = registerLegacyListener(SENSOR_ORIENTATION, Sensor.TYPE_ORIENTATION,
                listener, sensors, rate) || result;
        result = registerLegacyListener(SENSOR_TEMPERATURE, Sensor.TYPE_TEMPERATURE,
                listener, sensors, rate) || result;
        return result;
    }
    @SuppressWarnings("deprecation")
    private boolean registerLegacyListener(int legacyType, int type,
            SensorListener listener, int sensors, int rate)
    {
        if (listener == null) {
            return false;
        }
        boolean result = false;
        if ((sensors & legacyType) != 0) {
            Sensor sensor = getDefaultSensor(type);
            if (sensor != null) {
                LegacyListener legacyListener = null;
                synchronized (mLegacyListenersMap) {
                    legacyListener = mLegacyListenersMap.get(listener);
                    if (legacyListener == null) {
                        legacyListener = new LegacyListener(listener);
                        mLegacyListenersMap.put(listener, legacyListener);
                    }
                }
                legacyListener.registerSensor(legacyType);
                result = registerListener(legacyListener, sensor, rate);
            }
        }
        return result;
    }
    @Deprecated
    public void unregisterListener(SensorListener listener, int sensors) {
        unregisterLegacyListener(SENSOR_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER,
                listener, sensors);
        unregisterLegacyListener(SENSOR_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD,
                listener, sensors);
        unregisterLegacyListener(SENSOR_ORIENTATION_RAW, Sensor.TYPE_ORIENTATION,
                listener, sensors);
        unregisterLegacyListener(SENSOR_ORIENTATION, Sensor.TYPE_ORIENTATION,
                listener, sensors);
        unregisterLegacyListener(SENSOR_TEMPERATURE, Sensor.TYPE_TEMPERATURE,
                listener, sensors);
    }
    @SuppressWarnings("deprecation")
    private void unregisterLegacyListener(int legacyType, int type,
            SensorListener listener, int sensors)
    {
        if (listener == null) {
            return;
        }
        LegacyListener legacyListener = null;
        synchronized (mLegacyListenersMap) {
            legacyListener = mLegacyListenersMap.get(listener);
        }
        if (legacyListener != null) {
            if ((sensors & legacyType) != 0) {
                Sensor sensor = getDefaultSensor(type);
                if (sensor != null) {
                    if (legacyListener.unregisterSensor(legacyType)) {
                        unregisterListener(legacyListener, sensor);
                        synchronized(sListeners) {
                            boolean found = false;
                            for (ListenerDelegate i : sListeners) {
                                if (i.getListener() == legacyListener) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                synchronized (mLegacyListenersMap) {
                                    mLegacyListenersMap.remove(listener);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    @Deprecated
    public void unregisterListener(SensorListener listener) {
        unregisterListener(listener, SENSOR_ALL | SENSOR_ORIENTATION_RAW);
    }
    public void unregisterListener(SensorEventListener listener, Sensor sensor) {
        unregisterListener((Object)listener, sensor);
    }
    public void unregisterListener(SensorEventListener listener) {
        unregisterListener((Object)listener);
    }
    public boolean registerListener(SensorEventListener listener, Sensor sensor, int rate) {
        return registerListener(listener, sensor, rate, null);
    }
    public boolean registerListener(SensorEventListener listener, Sensor sensor, int rate,
            Handler handler) {
        if (listener == null || sensor == null) {
            return false;
        }
        boolean result;
        int delay = -1;
        switch (rate) {
            case SENSOR_DELAY_FASTEST:
                delay = 0;
                break;
            case SENSOR_DELAY_GAME:
                delay = 20;
                break;
            case SENSOR_DELAY_UI:
                delay = 60;
                break;
            case SENSOR_DELAY_NORMAL:
                delay = 200;
                break;
            default:
                return false;
        }
        try {
            synchronized (sListeners) {
                ListenerDelegate l = null;
                for (ListenerDelegate i : sListeners) {
                    if (i.getListener() == listener) {
                        l = i;
                        break;
                    }
                }
                String name = sensor.getName();
                int handle = sensor.getHandle();
                if (l == null) {
                    result = false;
                    l = new ListenerDelegate(listener, sensor, handler);
                    sListeners.add(l);
                    if (!sListeners.isEmpty()) {
                        result = sSensorThread.startLocked(mSensorService);
                        if (result) {
                            result = mSensorService.enableSensor(l, name, handle, delay);
                            if (!result) {
                                sListeners.remove(l);
                            }
                        }
                    }
                } else {
                    result = mSensorService.enableSensor(l, name, handle, delay);
                    if (result) {
                        l.addSensor(sensor);
                    }
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in registerListener: ", e);
            result = false;
        }
        return result;
    }
    private void unregisterListener(Object listener, Sensor sensor) {
        if (listener == null || sensor == null) {
            return;
        }
        try {
            synchronized (sListeners) {
                final int size = sListeners.size();
                for (int i=0 ; i<size ; i++) {
                    ListenerDelegate l = sListeners.get(i);
                    if (l.getListener() == listener) {
                        String name = sensor.getName();
                        int handle = sensor.getHandle();
                        mSensorService.enableSensor(l, name, handle, SENSOR_DISABLE);
                        if (l.removeSensor(sensor) == 0) {
                            sListeners.remove(i);
                        }
                        break;
                    }
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in unregisterListener: ", e);
        }
    }
    private void unregisterListener(Object listener) {
        if (listener == null) {
            return;
        }
        try {
            synchronized (sListeners) {
                final int size = sListeners.size();
                for (int i=0 ; i<size ; i++) {
                    ListenerDelegate l = sListeners.get(i);
                    if (l.getListener() == listener) {
                        for (Sensor sensor : l.getSensors()) {
                            String name = sensor.getName();
                            int handle = sensor.getHandle();
                            mSensorService.enableSensor(l, name, handle, SENSOR_DISABLE);
                        }
                        sListeners.remove(i);
                        break;
                    }
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in unregisterListener: ", e);
        }
    }
    public static boolean getRotationMatrix(float[] R, float[] I,
            float[] gravity, float[] geomagnetic) {
        float Ax = gravity[0];
        float Ay = gravity[1];
        float Az = gravity[2];
        final float Ex = geomagnetic[0];
        final float Ey = geomagnetic[1];
        final float Ez = geomagnetic[2];
        float Hx = Ey*Az - Ez*Ay;
        float Hy = Ez*Ax - Ex*Az;
        float Hz = Ex*Ay - Ey*Ax;
        final float normH = (float)Math.sqrt(Hx*Hx + Hy*Hy + Hz*Hz);
        if (normH < 0.1f) {
            return false;
        }
        final float invH = 1.0f / normH;
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;
        final float invA = 1.0f / (float)Math.sqrt(Ax*Ax + Ay*Ay + Az*Az);
        Ax *= invA;
        Ay *= invA;
        Az *= invA;
        final float Mx = Ay*Hz - Az*Hy;
        final float My = Az*Hx - Ax*Hz;
        final float Mz = Ax*Hy - Ay*Hx;
        if (R != null) {
            if (R.length == 9) {
                R[0] = Hx;     R[1] = Hy;     R[2] = Hz;
                R[3] = Mx;     R[4] = My;     R[5] = Mz;
                R[6] = Ax;     R[7] = Ay;     R[8] = Az;
            } else if (R.length == 16) {
                R[0]  = Hx;    R[1]  = Hy;    R[2]  = Hz;   R[3]  = 0;
                R[4]  = Mx;    R[5]  = My;    R[6]  = Mz;   R[7]  = 0;
                R[8]  = Ax;    R[9]  = Ay;    R[10] = Az;   R[11] = 0;
                R[12] = 0;     R[13] = 0;     R[14] = 0;    R[15] = 1;
            }
        }
        if (I != null) {
            final float invE = 1.0f / (float)Math.sqrt(Ex*Ex + Ey*Ey + Ez*Ez);
            final float c = (Ex*Mx + Ey*My + Ez*Mz) * invE;
            final float s = (Ex*Ax + Ey*Ay + Ez*Az) * invE;
            if (I.length == 9) {
                I[0] = 1;     I[1] = 0;     I[2] = 0;
                I[3] = 0;     I[4] = c;     I[5] = s;
                I[6] = 0;     I[7] =-s;     I[8] = c;
            } else if (I.length == 16) {
                I[0] = 1;     I[1] = 0;     I[2] = 0;
                I[4] = 0;     I[5] = c;     I[6] = s;
                I[8] = 0;     I[9] =-s;     I[10]= c;
                I[3] = I[7] = I[11] = I[12] = I[13] = I[14] = 0;
                I[15] = 1;
            }
        }
        return true;
    }
    public static float getInclination(float[] I) {
        if (I.length == 9) {
            return (float)Math.atan2(I[5], I[4]);
        } else {
            return (float)Math.atan2(I[6], I[5]);            
        }
    }
    public static boolean remapCoordinateSystem(float[] inR, int X, int Y,
            float[] outR)
    {
        if (inR == outR) {
            final float[] temp = mTempMatrix;
            synchronized(temp) {
                if (remapCoordinateSystemImpl(inR, X, Y, temp)) {
                    final int size = outR.length;
                    for (int i=0 ; i<size ; i++)
                        outR[i] = temp[i];
                    return true;
                }
            }
        }
        return remapCoordinateSystemImpl(inR, X, Y, outR);
    }
    private static boolean remapCoordinateSystemImpl(float[] inR, int X, int Y,
            float[] outR)
    {
        final int length = outR.length;
        if (inR.length != length)
            return false;   
        if ((X & 0x7C)!=0 || (Y & 0x7C)!=0)
            return false;   
        if (((X & 0x3)==0) || ((Y & 0x3)==0))
            return false;   
        if ((X & 0x3) == (Y & 0x3))
            return false;   
        int Z = X ^ Y;
        final int x = (X & 0x3)-1;
        final int y = (Y & 0x3)-1;
        final int z = (Z & 0x3)-1;
        final int axis_y = (z+1)%3;
        final int axis_z = (z+2)%3;
        if (((x^axis_y)|(y^axis_z)) != 0)
            Z ^= 0x80;
        final boolean sx = (X>=0x80);
        final boolean sy = (Y>=0x80);
        final boolean sz = (Z>=0x80);
        final int rowLength = ((length==16)?4:3);
        for (int j=0 ; j<3 ; j++) {
            final int offset = j*rowLength;
            for (int i=0 ; i<3 ; i++) {
                if (x==i)   outR[offset+i] = sx ? -inR[offset+0] : inR[offset+0];
                if (y==i)   outR[offset+i] = sy ? -inR[offset+1] : inR[offset+1];
                if (z==i)   outR[offset+i] = sz ? -inR[offset+2] : inR[offset+2];
            }
        }
        if (length == 16) {
            outR[3] = outR[7] = outR[11] = outR[12] = outR[13] = outR[14] = 0;
            outR[15] = 1;
        }
        return true;
    }
    public static float[] getOrientation(float[] R, float values[]) {
        if (R.length == 9) {
            values[0] = (float)Math.atan2(R[1], R[4]);
            values[1] = (float)Math.asin(-R[7]);
            values[2] = (float)Math.atan2(-R[6], R[8]);
        } else {
            values[0] = (float)Math.atan2(R[1], R[5]);
            values[1] = (float)Math.asin(-R[9]);
            values[2] = (float)Math.atan2(-R[8], R[10]);
        }
        return values;
    }
    public void onRotationChanged(int rotation) {
        synchronized(sListeners) {
            sRotation  = rotation;
        }
    }
    static int getRotation() {
        synchronized(sListeners) {
            return sRotation;
        }
    }
    private class LegacyListener implements SensorEventListener {
        private float mValues[] = new float[6];
        @SuppressWarnings("deprecation")
        private SensorListener mTarget;
        private int mSensors;
        private final LmsFilter mYawfilter = new LmsFilter();
        @SuppressWarnings("deprecation")
        LegacyListener(SensorListener target) {
            mTarget = target;
            mSensors = 0;
        }
        void registerSensor(int legacyType) {
            mSensors |= legacyType;
        }
        boolean unregisterSensor(int legacyType) {
            mSensors &= ~legacyType;
            int mask = SENSOR_ORIENTATION|SENSOR_ORIENTATION_RAW;
            if (((legacyType&mask)!=0) && ((mSensors&mask)!=0)) {
                return false;
            }
            return true;
        }
        @SuppressWarnings("deprecation")
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            try {
                mTarget.onAccuracyChanged(sensor.getLegacyType(), accuracy);
            } catch (AbstractMethodError e) {
            }
        }
        @SuppressWarnings("deprecation")
        public void onSensorChanged(SensorEvent event) {
            final float v[] = mValues;
            v[0] = event.values[0];
            v[1] = event.values[1];
            v[2] = event.values[2];
            int legacyType = event.sensor.getLegacyType();
            mapSensorDataToWindow(legacyType, v, SensorManager.getRotation());
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                if ((mSensors & SENSOR_ORIENTATION_RAW)!=0) {
                    mTarget.onSensorChanged(SENSOR_ORIENTATION_RAW, v);
                }
                if ((mSensors & SENSOR_ORIENTATION)!=0) {
                    v[0] = mYawfilter.filter(event.timestamp, v[0]);
                    mTarget.onSensorChanged(SENSOR_ORIENTATION, v);
                }
            } else {
                mTarget.onSensorChanged(legacyType, v);
            }
        }
        private void mapSensorDataToWindow(int sensor,
                float[] values, int orientation) {
            float x = values[0];
            float y = values[1];
            float z = values[2];
            switch (sensor) {
                case SensorManager.SENSOR_ORIENTATION:
                case SensorManager.SENSOR_ORIENTATION_RAW:
                    z = -z;
                    break;
                case SensorManager.SENSOR_ACCELEROMETER:
                    x = -x;
                    y = -y;
                    z = -z;
                    break;
                case SensorManager.SENSOR_MAGNETIC_FIELD:
                    x = -x;
                    y = -y;
                    break;
            }
            values[0] = x;
            values[1] = y;
            values[2] = z;
            values[3] = x;
            values[4] = y;
            values[5] = z;
            if ((orientation & Surface.ROTATION_90) != 0) {
                switch (sensor) {
                    case SENSOR_ACCELEROMETER:
                    case SENSOR_MAGNETIC_FIELD:
                        values[0] =-y;
                        values[1] = x;
                        values[2] = z;
                        break;
                    case SENSOR_ORIENTATION:
                    case SENSOR_ORIENTATION_RAW:
                        values[0] = x + ((x < 270) ? 90 : -270);
                        values[1] = z;
                        values[2] = y;
                        break;
                }
            }
            if ((orientation & Surface.ROTATION_180) != 0) {
                x = values[0];
                y = values[1];
                z = values[2];
                switch (sensor) {
                    case SENSOR_ACCELEROMETER:
                    case SENSOR_MAGNETIC_FIELD:
                        values[0] =-x;
                        values[1] =-y;
                        values[2] = z;
                        break;
                    case SENSOR_ORIENTATION:
                    case SENSOR_ORIENTATION_RAW:
                        values[0] = (x >= 180) ? (x - 180) : (x + 180);
                        values[1] =-y;
                        values[2] =-z;
                        break;
                }
            }
        }
    }
    class LmsFilter {
        private static final int SENSORS_RATE_MS = 20;
        private static final int COUNT = 12;
        private static final float PREDICTION_RATIO = 1.0f/3.0f;
        private static final float PREDICTION_TIME = (SENSORS_RATE_MS*COUNT/1000.0f)*PREDICTION_RATIO;
        private float mV[] = new float[COUNT*2];
        private float mT[] = new float[COUNT*2];
        private int mIndex;
        public LmsFilter() {
            mIndex = COUNT;
        }
        public float filter(long time, float in) {
            float v = in;
            final float ns = 1.0f / 1000000000.0f;
            final float t = time*ns;
            float v1 = mV[mIndex];
            if ((v-v1) > 180) {
                v -= 360;
            } else if ((v1-v) > 180) {
                v += 360;
            }
            mIndex++;
            if (mIndex >= COUNT*2)
                mIndex = COUNT;
            mV[mIndex] = v;
            mT[mIndex] = t;
            mV[mIndex-COUNT] = v;
            mT[mIndex-COUNT] = t;
            float A, B, C, D, E;
            float a, b;
            int i;
            A = B = C = D = E = 0;
            for (i=0 ; i<COUNT-1 ; i++) {
                final int j = mIndex - 1 - i;
                final float Z = mV[j];
                final float T = 0.5f*(mT[j] + mT[j+1]) - t;
                float dT = mT[j] - mT[j+1];
                dT *= dT;
                A += Z*dT;
                B += T*(T*dT);
                C +=   (T*dT);
                D += Z*(T*dT);
                E += dT;
            }
            b = (A*B + C*D) / (E*B + C*C);
            a = (E*b - A) / C;
            float f = b + PREDICTION_TIME*a;
            f *= (1.0f / 360.0f);
            if (((f>=0)?f:-f) >= 0.5f)
                f = f - (float)Math.ceil(f + 0.5f) + 1.0f;
            if (f < 0)
                f += 1.0f;
            f *= 360.0f;
            return f;
        }
    }
    private static native void nativeClassInit();
    private static native int sensors_module_init();
    private static native int sensors_module_get_next_sensor(Sensor sensor, int next);
    static native int sensors_data_init();
    static native int sensors_data_uninit();
    static native int sensors_data_open(FileDescriptor[] fds, int[] ints);
    static native int sensors_data_close();
    static native int sensors_data_poll(float[] values, int[] status, long[] timestamp);
}
