class GTView extends SurfaceView implements SurfaceHolder.Callback {
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("utc");
    private static final float[] SUNLIGHT_COLOR = {
        1.0f, 0.9375f, 0.91015625f, 1.0f
    };
    private static final float EARTH_INCLINATION = 23.45f * Shape.PI / 180.0f;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;
    private static final boolean PERFORM_DEPTH_TEST= false;
    private static final boolean USE_RAW_OFFSETS = true;
    private static final Annulus ATMOSPHERE =
        new Annulus(0.0f, 0.0f, 1.75f, 0.9f, 1.08f, 0.4f, 0.4f, 0.8f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 50);
    private static final int SPHERE_LATITUDES = 25;
    private static int SPHERE_LONGITUDES = 25;
    private static Sphere worldFlat = new LatLongSphere(0.0f, 0.0f, 0.0f, 1.0f,
        SPHERE_LATITUDES, SPHERE_LONGITUDES,
        0.0f, 360.0f, true, true, false, true);
    private Object3D mWorld;
    private PointCloud mLights;
    boolean mInitialized = false;
    private boolean mAlphaKeySet = false;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;
    private EGLDisplay mEGLDisplay;
    private EGLConfig  mEGLConfig;
    GLView  mGLView;
    private float mRotAngle = 0.0f;
    private float mTiltAngle = 0.0f;
    private float mRotVelocity = 1.0f;
    private float mWrapX =  0.0f;
    private float  mWrapVelocity =  0.0f;
    private float mWrapVelocityFactor =  0.01f;
    private boolean mDisplayAtmosphere = true;
    private boolean mDisplayClock = false;
    private boolean mClockShowing = false;
    private boolean mDisplayLights = false;
    private boolean mDisplayWorld = true;
    private boolean mDisplayWorldFlat = false;
    private boolean mSmoothShading = true;
    private String mCityName = "";
    private List<City> mClockCities;
    private List<City> mCityNameMatches = new ArrayList<City>();
    private List<City> mCities;
    private long mClockFadeTime;
    private Interpolator mClockSizeInterpolator =
        new DecelerateInterpolator(1.0f);
    private int mCityIndex;
    private Clock mClock;
    private boolean mFlyToCity = false;
    private long mCityFlyStartTime;
    private float mCityFlightTime;
    private float mRotAngleStart, mRotAngleDest;
    private float mTiltAngleStart, mTiltAngleDest;
    private Interpolator mFlyToCityInterpolator =
        new AccelerateDecelerateInterpolator();
    private static int sNumLights;
    private static int[] sLightCoords;
    private float[] mClipPlaneEquation = new float[4];
    private float[] mLightDir = new float[4];
    Calendar mSunCal = Calendar.getInstance(UTC_TIME_ZONE);
    private int mNumTriangles;
    private long startTime;
    private static final int MOTION_NONE = 0;
    private static final int MOTION_X = 1;
    private static final int MOTION_Y = 2;
    private static final int MIN_MANHATTAN_DISTANCE = 20;
    private static final float ROTATION_FACTOR = 1.0f / 30.0f;
    private static final float TILT_FACTOR = 0.35f;
    private float mMotionStartX;
    private float mMotionStartY;
    private float mMotionStartRotVelocity;
    private float mMotionStartTiltAngle;
    private int mMotionDirection;
    private boolean mPaused = true;
    private boolean mHaveSurface = false;
    private boolean mStartAnimating = false;
    public void surfaceCreated(SurfaceHolder holder) {
        mHaveSurface = true;
        startEGL();
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHaveSurface = false;
        stopEGL();
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }
    public GTView(Context context) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
        startTime = System.currentTimeMillis();
        mClock = new Clock();
        startEGL();
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }
    private void startEGL() {
        EGL10 egl = (EGL10)EGLContext.getEGL();
        if (mEGLContext == null) {
            EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            int[] version = new int[2];
            egl.eglInitialize(dpy, version);
            int[] configSpec = {
                    EGL10.EGL_DEPTH_SIZE,   16,
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] num_config = new int[1];
            egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
            mEGLConfig = configs[0];
            mEGLContext = egl.eglCreateContext(dpy, mEGLConfig, 
                    EGL10.EGL_NO_CONTEXT, null);
            mEGLDisplay = dpy;
            AssetManager am = mContext.getAssets();
            try {
                loadAssets(am);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new RuntimeException(ioe);
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                aioobe.printStackTrace();
                throw new RuntimeException(aioobe);
            }
        }
        if (mEGLSurface == null && !mPaused && mHaveSurface) {
            mEGLSurface = egl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, 
                    this, null);
            egl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, 
                    mEGLContext);
            mInitialized = false;
            if (mStartAnimating) {
                startAnimating();
                mStartAnimating = false;
            }
        }
    }
    private void stopEGL() {
        EGL10 egl = (EGL10)EGLContext.getEGL();
        if (mEGLSurface != null) {
            egl.eglMakeCurrent(mEGLDisplay, 
                    egl.EGL_NO_SURFACE, egl.EGL_NO_SURFACE, egl.EGL_NO_CONTEXT);
            egl.eglDestroySurface(mEGLDisplay, mEGLSurface);
            mEGLSurface = null;
        }
        if (mEGLContext != null) {
            egl.eglDestroyContext(mEGLDisplay, mEGLContext);
            egl.eglTerminate(mEGLDisplay);
            mEGLContext = null;
            mEGLDisplay = null;
            mEGLConfig = null;
        }
    }
    public void onPause() {
        mPaused = true;
        stopAnimating();
        stopEGL();
    }
    public void onResume() {
        mPaused = false;
        startEGL();
    }
    public void destroy() {
        stopAnimating();
        stopEGL();
    }
    public void startAnimating() {
        if (mEGLSurface == null) {
            mStartAnimating = true; 
        } else {
            mHandler.sendEmptyMessage(INVALIDATE);
        }
    }
    public void stopAnimating() {
        mHandler.removeMessages(INVALIDATE);
    }
    private int readInt16(InputStream is) throws IOException {
        int lo = is.read();
        int hi = is.read();
        return (hi << 8) | lo;
    }
    private static float getOffset(City c) {
        return USE_RAW_OFFSETS ? c.getRawOffset() : c.getOffset();
    }
    private InputStream cache(InputStream is) throws IOException {
        int nbytes = is.available();
        byte[] data = new byte[nbytes];
        int nread = 0;
        while (nread < nbytes) {
            nread += is.read(data, nread, nbytes - nread);
        }
        return new ByteArrayInputStream(data);
    }
    private void loadAssets(final AssetManager am) throws IOException {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry();
        InputStream cis = null;
        try {
            cis = am.open("cities_" + language + "_" + country + ".dat");
        } catch (FileNotFoundException e1) {
            try {
                cis = am.open("cities_" + language + ".dat");
            } catch (FileNotFoundException e2) {
                try {
                    cis = am.open("cities_en.dat");
                } catch (FileNotFoundException e3) {
                    throw e3;
                }
            }
        }
        cis = cache(cis);
        City.loadCities(cis);
        City[] cities;
        if (USE_RAW_OFFSETS) {
            cities = City.getCitiesByRawOffset();
        } else {
            cities = City.getCitiesByOffset();
        }
        mClockCities = new ArrayList<City>(cities.length);
        for (int i = 0; i < cities.length; i++) {
            mClockCities.add(cities[i]);
        }
        mCities = mClockCities;
        mCityIndex = 0;
        this.mWorld = new Object3D() {
                @Override
                public InputStream readFile(String filename)
                    throws IOException {
                    return cache(am.open(filename));
                }
            };
        mWorld.load("world.gles");
        InputStream lis = am.open("lights.dat");
        lis = cache(lis);
        int lightWidth = readInt16(lis);
        int lightHeight = readInt16(lis);
        sNumLights = readInt16(lis);
        sLightCoords = new int[3 * sNumLights];
        int lidx = 0;
        float lightRadius = 1.009f;
        float lightScale = 65536.0f * lightRadius;
        float[] cosTheta = new float[lightWidth];
        float[] sinTheta = new float[lightWidth];
        float twoPi = (float) (2.0 * Math.PI);
        float scaleW = twoPi / lightWidth;
        for (int i = 0; i < lightWidth; i++) {
            float theta = twoPi - i * scaleW;
            cosTheta[i] = (float)Math.cos(theta);
            sinTheta[i] = (float)Math.sin(theta);
        }
        float[] cosPhi = new float[lightHeight];
        float[] sinPhi = new float[lightHeight];
        float scaleH = (float) (Math.PI / lightHeight);
        for (int j = 0; j < lightHeight; j++) {
            float phi = j * scaleH;
            cosPhi[j] = (float)Math.cos(phi);
            sinPhi[j] = (float)Math.sin(phi);
        }
        int nbytes = 4 * sNumLights;
        byte[] ilights = new byte[nbytes];
        int nread = 0;
        while (nread < nbytes) {
            nread += lis.read(ilights, nread, nbytes - nread);
        }
        int idx = 0;
        for (int i = 0; i < sNumLights; i++) {
            int lx = (((ilights[idx + 1] & 0xff) << 8) |
                       (ilights[idx    ] & 0xff));
            int ly = (((ilights[idx + 3] & 0xff) << 8) |
                       (ilights[idx + 2] & 0xff));
            idx += 4;
            float sin = sinPhi[ly];
            float x = cosTheta[lx]*sin;
            float y = cosPhi[ly];
            float z = sinTheta[lx]*sin;
            sLightCoords[lidx++] = (int) (x * lightScale);
            sLightCoords[lidx++] = (int) (y * lightScale);
            sLightCoords[lidx++] = (int) (z * lightScale);
        }
        mLights = new PointCloud(sLightCoords);
    }
    private boolean tzEqual(float o1, float o2) {
        return Math.abs(o1 - o2) < 0.001;
    }
    private void shiftTimeZone(int incr) {
        if (mCities.size() <= 1) {
            return;
        }
        float offset = getOffset(mCities.get(mCityIndex));
        do {
            mCityIndex = (mCityIndex + mCities.size() + incr) % mCities.size();
        } while (tzEqual(getOffset(mCities.get(mCityIndex)), offset));
        offset = getOffset(mCities.get(mCityIndex));
        locateCity(true, offset);
        goToCity();
    }
    private boolean atEndOfTimeZone(int incr) {
        if (mCities.size() <= 1) {
            return true;
        }
        float offset = getOffset(mCities.get(mCityIndex));
        int nindex = (mCityIndex + mCities.size() + incr) % mCities.size();
        if (tzEqual(getOffset(mCities.get(nindex)), offset)) {
            return false;
        }
        return true;
    }
    private void shiftWithinTimeZone(int incr) {
        float offset = getOffset(mCities.get(mCityIndex));
        int nindex = (mCityIndex + mCities.size() + incr) % mCities.size();
        if (tzEqual(getOffset(mCities.get(nindex)), offset)) {
            mCityIndex = nindex;
            goToCity();
        }
    }
    private boolean nameMatches(City city, String prefix) {
        String cityName = city.getName().replaceAll("[ ]", "");
        return prefix.regionMatches(true, 0,
                                    cityName, 0,
                                    prefix.length());
    }
    private boolean hasMatches(String prefix) {
        for (int i = 0; i < mClockCities.size(); i++) {
            City city = mClockCities.get(i);
            if (nameMatches(city, prefix)) {
                return true;
            }
        }
        return false;
    }
    private void shiftByName() {
        City finalCity = null;
        City currCity = mCities.get(mCityIndex);
        if (nameMatches(currCity, mCityName)) {
            finalCity = currCity;
        }
        mCityNameMatches.clear();
        for (int i = 0; i < mClockCities.size(); i++) {
            City city = mClockCities.get(i);
            if (nameMatches(city, mCityName)) {
                mCityNameMatches.add(city);
            }
        }
        mCities = mCityNameMatches;
        if (finalCity != null) {
            for (int i = 0; i < mCityNameMatches.size(); i++) {
                if (mCityNameMatches.get(i) == finalCity) {
                    mCityIndex = i;
                    break;
                }
            }
        } else {
            locateCity(false, 0.0f);
        }
        goToCity();
    }
    private void incrementRotationalVelocity(float incr) {
        if (mDisplayWorldFlat) {
            mWrapVelocity -= incr;
        } else {
            mRotVelocity -= incr;
        }
    }
    private void clearCityMatches() {
        if (mCityNameMatches.size() > 0) {
            City city = mCityNameMatches.get(mCityIndex);
            for (int i = 0; i < mClockCities.size(); i++) {
                City ncity = mClockCities.get(i);
                if (city.equals(ncity)) {
                    mCityIndex = i;
                    break;
                }
            }
        }
        mCityName = "";
        mCityNameMatches.clear();
        mCities = mClockCities;
        goToCity();
    }
    private void enableClock(boolean enabled) {
        mClockFadeTime = System.currentTimeMillis();
        mDisplayClock = enabled;
        mClockShowing = true;
        mAlphaKeySet = enabled;
        if (enabled) {
            locateCity(false, 0.0f);
        }
        clearCityMatches();
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMotionStartX = event.getX();
                mMotionStartY = event.getY();
                mMotionStartRotVelocity = mDisplayWorldFlat ?
                    mWrapVelocity : mRotVelocity;
                mMotionStartTiltAngle = mTiltAngle;
                if (mDisplayWorldFlat) {
                    mWrapVelocity = 0.0f;
                } else {
                    mRotVelocity = 0.0f;
                }
                mMotionDirection = MOTION_NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mMotionStartX;
                float dy = event.getY() - mMotionStartY;
                float delx = Math.abs(dx);
                float dely = Math.abs(dy);
                if ((mMotionDirection == MOTION_NONE) &&
                    (delx + dely > MIN_MANHATTAN_DISTANCE)) {
                    if (delx > dely) {
                        mMotionDirection = MOTION_X;
                    } else {
                        mMotionDirection = MOTION_Y;
                    }
                }
                if (!mDisplayClock) {
                    if (mMotionDirection == MOTION_X) {
                        if (mDisplayWorldFlat) {
                            mWrapVelocity = mMotionStartRotVelocity +
                                dx * ROTATION_FACTOR;
                        } else {
                            mRotVelocity = mMotionStartRotVelocity +
                                dx * ROTATION_FACTOR;
                        }
                        mClock.setCity(null);
                    } else if (mMotionDirection == MOTION_Y &&
                        !mDisplayWorldFlat) {
                        mTiltAngle = mMotionStartTiltAngle + dy * TILT_FACTOR;
                        if (mTiltAngle < -90.0f) {
                            mTiltAngle = -90.0f;
                        }
                        if (mTiltAngle > 90.0f) {
                            mTiltAngle = 90.0f;
                        }
                        mClock.setCity(null);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mMotionDirection = MOTION_NONE;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTiltAngle = mMotionStartTiltAngle;
                if (mDisplayWorldFlat) {
                    mWrapVelocity = mMotionStartRotVelocity;
                } else {
                    mRotVelocity = mMotionStartRotVelocity;
                }
                mMotionDirection = MOTION_NONE;
                break;
        }
        return true;
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mInitialized && mGLView.processKey(keyCode)) {
            boolean drawing = (mClockShowing || mGLView.hasMessages());
            this.setWillNotDraw(!drawing);
            return true;
        }
        boolean handled = false;
        if (!mAlphaKeySet) {
            char numChar = event.getNumber();
            if (numChar >= '0' && numChar <= '9') {
                keyCode = KeyEvent.KEYCODE_0 + (numChar - '0');
            }
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_SPACE:
            mAlphaKeySet = !mAlphaKeySet;
            enableClock(mAlphaKeySet);
            handled = true;
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (mDisplayClock) {
                shiftTimeZone(-1);
            } else {
                mClock.setCity(null);
                incrementRotationalVelocity(1.0f);
            }
            handled = true;
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (mDisplayClock) {
                shiftTimeZone(1);
            } else {
                mClock.setCity(null);
                incrementRotationalVelocity(-1.0f);
            }
            handled = true;
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
            if (mDisplayClock) {
                shiftWithinTimeZone(-1);
            } else {
                mClock.setCity(null);
                if (!mDisplayWorldFlat) {
                    mTiltAngle += 360.0f / 48.0f;
                }
            }
            handled = true;
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            if (mDisplayClock) {
                shiftWithinTimeZone(1);
            } else {
                mClock.setCity(null);
                if (!mDisplayWorldFlat) {
                    mTiltAngle -= 360.0f / 48.0f;
                }
            }
            handled = true;
            break;
        case KeyEvent.KEYCODE_DPAD_CENTER:
            if ((!mDisplayWorldFlat && mRotVelocity == 0.0f) ||
                (mDisplayWorldFlat && mWrapVelocity == 0.0f)) {
                mDisplayWorldFlat = !mDisplayWorldFlat;
            } else {
                if (mDisplayWorldFlat) {
                    mWrapVelocity = 0.0f;
                } else {
                    mRotVelocity = 0.0f;
                }
            }
            handled = true;
            break;
        case KeyEvent.KEYCODE_L:
            if (!mAlphaKeySet && !mDisplayWorldFlat) {
                mDisplayLights = !mDisplayLights;
                handled = true;
            }
            break;
        case KeyEvent.KEYCODE_W:
            if (!mAlphaKeySet && !mDisplayWorldFlat) {
                mDisplayWorld = !mDisplayWorld;
                handled = true;
            }
            break;
        case KeyEvent.KEYCODE_A:
            if (!mAlphaKeySet && !mDisplayWorldFlat) {
                mDisplayAtmosphere = !mDisplayAtmosphere;
                handled = true;
            }
            break;
        case KeyEvent.KEYCODE_2:
            if (!mAlphaKeySet && !mDisplayWorldFlat) {
                mGLView.zoom(-2);
                handled = true;
            }
            break;
        case KeyEvent.KEYCODE_8:
            if (!mAlphaKeySet && !mDisplayWorldFlat) {
                mGLView.zoom(2);
                handled = true;
            }
            break;
        }
        if (!handled && mAlphaKeySet) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_B:
            case KeyEvent.KEYCODE_C:
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_E:
            case KeyEvent.KEYCODE_F:
            case KeyEvent.KEYCODE_G:
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_I:
            case KeyEvent.KEYCODE_J:
            case KeyEvent.KEYCODE_K:
            case KeyEvent.KEYCODE_L:
            case KeyEvent.KEYCODE_M:
            case KeyEvent.KEYCODE_N:
            case KeyEvent.KEYCODE_O:
            case KeyEvent.KEYCODE_P:
            case KeyEvent.KEYCODE_Q:
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_T:
            case KeyEvent.KEYCODE_U:
            case KeyEvent.KEYCODE_V:
            case KeyEvent.KEYCODE_W:
            case KeyEvent.KEYCODE_X:
            case KeyEvent.KEYCODE_Y:
            case KeyEvent.KEYCODE_Z:
                char c = (char)(keyCode - KeyEvent.KEYCODE_A + 'A');
                if (hasMatches(mCityName + c)) {
                    mCityName += c;
                    shiftByName();
                }
                handled = true;
                break;
            case KeyEvent.KEYCODE_DEL:
                if (mCityName.length() > 0) {
                    mCityName = mCityName.substring(0, mCityName.length() - 1);
                    shiftByName();
                } else {
                    clearCityMatches();
                }
                handled = true;
                break;
            case KeyEvent.KEYCODE_ENTER:
                clearCityMatches();
                handled = true;
                break;
            }
        }
        boolean drawing = (mClockShowing ||
            ((mGLView != null) && (mGLView.hasMessages())));
        this.setWillNotDraw(!drawing);
        if (!handled) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }
    private synchronized void init(GL10 gl) {
        mGLView = new GLView();
        mGLView.setNearFrustum(5.0f);
        mGLView.setFarFrustum(50.0f);
        mGLView.setLightModelAmbientIntensity(0.225f);
        mGLView.setAmbientIntensity(0.0f);
        mGLView.setDiffuseIntensity(1.5f);
        mGLView.setDiffuseColor(SUNLIGHT_COLOR);
        mGLView.setSpecularIntensity(0.0f);
        mGLView.setSpecularColor(SUNLIGHT_COLOR);
        if (PERFORM_DEPTH_TEST) {
            gl.glEnable(GL10.GL_DEPTH_TEST);
        }
        gl.glDisable(GL10.GL_SCISSOR_TEST);
        gl.glClearColor(0, 0, 0, 1);
        gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
        mInitialized = true;
    }
    private void computeSunDirection() {
        mSunCal.setTimeInMillis(System.currentTimeMillis());
        int day = mSunCal.get(Calendar.DAY_OF_YEAR);
        int seconds = 3600 * mSunCal.get(Calendar.HOUR_OF_DAY) +
            60 * mSunCal.get(Calendar.MINUTE) + mSunCal.get(Calendar.SECOND);
        day += (float) seconds / SECONDS_PER_DAY;
        float decl = (float) (EARTH_INCLINATION *
            Math.cos(Shape.TWO_PI * (day + 10) / 365.0));
        float phi = decl + Shape.PI_OVER_TWO;
        float theta = Shape.TWO_PI * seconds / SECONDS_PER_DAY;
        float sinPhi = (float) Math.sin(phi);
        float cosPhi = (float) Math.cos(phi);
        float sinTheta = (float) Math.sin(theta);
        float cosTheta = (float) Math.cos(theta);
        float x = cosTheta * sinPhi;
        float y = cosPhi;
        float z = sinTheta * sinPhi;
        mLightDir[0] = x;
        mLightDir[1] = y;
        mLightDir[2] = z;
        mLightDir[3] = 0.0f;
    }
    private float distance(float lat1, float lon1,
                           float lat2, float lon2) {
        lat1 *= Shape.DEGREES_TO_RADIANS;
        lat2 *= Shape.DEGREES_TO_RADIANS;
        lon1 *= Shape.DEGREES_TO_RADIANS;
        lon2 *= Shape.DEGREES_TO_RADIANS;
        float r = 6371.0f; 
        float dlat = lat2 - lat1;
        float dlon = lon2 - lon1;
        double sinlat2 = Math.sin(dlat / 2.0f);
        sinlat2 *= sinlat2;
        double sinlon2 = Math.sin(dlon / 2.0f);
        sinlon2 *= sinlon2;
        double a = sinlat2 + Math.cos(lat1) * Math.cos(lat2) * sinlon2;
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) (r * c);
    }
    private void locateCity(boolean useOffset, float offset) {
        float mindist = Float.MAX_VALUE;
        int minidx = -1;
        for (int i = 0; i < mCities.size(); i++) {
            City city = mCities.get(i);
            if (useOffset && !tzEqual(getOffset(city), offset)) {
                continue;
            }
            float dist = distance(city.getLatitude(), city.getLongitude(),
                mTiltAngle, mRotAngle - 90.0f);
            if (dist < mindist) {
                mindist = dist;
                minidx = i;
            }
        }
        mCityIndex = minidx;
    }
    private void goToCity() {
        City city = mCities.get(mCityIndex);
        float dist = distance(city.getLatitude(), city.getLongitude(),
            mTiltAngle, mRotAngle - 90.0f);
        mFlyToCity = true;
        mCityFlyStartTime = System.currentTimeMillis();
        mCityFlightTime = dist / 5.0f; 
        mRotAngleStart = mRotAngle;
        mRotAngleDest = city.getLongitude() + 90;
        if (mRotAngleDest - mRotAngleStart > 180.0f) {
            mRotAngleDest -= 360.0f;
        } else if (mRotAngleStart - mRotAngleDest > 180.0f) {
            mRotAngleDest += 360.0f;
        }
        mTiltAngleStart = mTiltAngle;
        mTiltAngleDest = city.getLatitude();
        mRotVelocity = 0.0f;
    }
    private float lerp(float a, float b, float lerp) {
        return a + (b - a)*lerp;
    }
    private void drawCityLights(GL10 gl, float brightness) {
        gl.glEnable(GL10.GL_POINT_SMOOTH);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glDisable(GL10.GL_DITHER);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glPointSize(1.0f);
        float ls = lerp(0.8f, 0.3f, brightness);
        gl.glColor4f(ls * 1.0f, ls * 1.0f, ls * 0.8f, 1.0f);
        if (mDisplayWorld) {
            mClipPlaneEquation[0] = -mLightDir[0];
            mClipPlaneEquation[1] = -mLightDir[1];
            mClipPlaneEquation[2] = -mLightDir[2];
            mClipPlaneEquation[3] = 0.0f;
            ((GL11) gl).glClipPlanef(GL11.GL_CLIP_PLANE0,
                mClipPlaneEquation, 0);
            gl.glEnable(GL11.GL_CLIP_PLANE0);
        }
        mLights.draw(gl);
        if (mDisplayWorld) {
            gl.glDisable(GL11.GL_CLIP_PLANE0);
        }
        mNumTriangles += mLights.getNumTriangles()*2;
    }
    private void drawAtmosphere(GL10 gl) {
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glShadeModel(mSmoothShading ? GL10.GL_SMOOTH : GL10.GL_FLAT);
        float tx = mGLView.getTranslateX();
        float ty = mGLView.getTranslateY();
        float tz = mGLView.getTranslateZ();
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(tx, ty, tz);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        ATMOSPHERE.draw(gl);
        mNumTriangles += ATMOSPHERE.getNumTriangles();
    }
    private void drawWorldFlat(GL10 gl) {
        gl.glDisable(GL10.GL_BLEND);
        gl.glEnable(GL10.GL_DITHER);
        gl.glShadeModel(mSmoothShading ? GL10.GL_SMOOTH : GL10.GL_FLAT);
        gl.glTranslatef(mWrapX - 2, 0.0f, 0.0f);
        worldFlat.draw(gl);
        gl.glTranslatef(2.0f, 0.0f, 0.0f);
        worldFlat.draw(gl);
        mNumTriangles += worldFlat.getNumTriangles() * 2;
        mWrapX += mWrapVelocity * mWrapVelocityFactor;
        while (mWrapX < 0.0f) {
            mWrapX += 2.0f;
        }
        while (mWrapX > 2.0f) {
            mWrapX -= 2.0f;
        }
    }
    private void drawWorldRound(GL10 gl) {
        gl.glDisable(GL10.GL_BLEND);
        gl.glEnable(GL10.GL_DITHER);
        gl.glShadeModel(mSmoothShading ? GL10.GL_SMOOTH : GL10.GL_FLAT);
        mWorld.draw(gl);
        mNumTriangles += mWorld.getNumTriangles();
    }
    private void drawClock(Canvas canvas,
                           long now,
                           int w, int h,
                           float lerp) {
        float clockAlpha = lerp(0.0f, 0.8f, lerp);
        mClockShowing = clockAlpha > 0.0f;
        if (clockAlpha > 0.0f) {
            City city = mCities.get(mCityIndex);
            mClock.setCity(city);
            mClock.setTime(now);
            float cx = w / 2.0f;
            float cy = h / 2.0f;
            float smallRadius = 18.0f;
            float bigRadius = 0.75f * 0.5f * Math.min(w, h);
            float radius = lerp(smallRadius, bigRadius, lerp);
            boolean scrollingByName =
                (mCityName.length() > 0) && (mCities.size() > 1);
            mClock.drawClock(canvas, cx, cy, radius,
                             clockAlpha,
                             1.0f,
                             lerp == 1.0f, lerp == 1.0f,
                             !atEndOfTimeZone(-1),
                             !atEndOfTimeZone(1),
                             scrollingByName,
                             mCityName.length());
        }
    }
    @Override protected void onDraw(Canvas canvas) {
        long now = System.currentTimeMillis();
        if (startTime != -1) {
            startTime = -1;
        }
        int w = getWidth();
        int h = getHeight();
        float lerp = Math.min((now - mClockFadeTime)/1000.0f, 1.0f);
        if (!mDisplayClock) {
            lerp = 1.0f - lerp;
        }
        lerp = mClockSizeInterpolator.getInterpolation(lerp);
        drawClock(canvas, now, w, h, lerp);
        mGLView.showMessages(canvas);
        mGLView.showStatistics(canvas, w);
    }
    protected void drawOpenGLScene() {
        long now = System.currentTimeMillis();
        mNumTriangles = 0;
        EGL10 egl = (EGL10)EGLContext.getEGL();
        GL10 gl = (GL10)mEGLContext.getGL();
        if (!mInitialized) {
            init(gl);
        }
        int w = getWidth();
        int h = getHeight();
        gl.glViewport(0, 0, w, h);
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CCW);
        float ratio = (float) w / h;
        mGLView.setAspectRatio(ratio);
        mGLView.setTextureParameters(gl);
        if (PERFORM_DEPTH_TEST) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        } else {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        }
        if (mDisplayWorldFlat) {
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-1.0f, 1.0f, -1.0f / ratio, 1.0f / ratio, 1.0f, 2.0f);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, 0.0f, -1.0f);
        } else {
            mGLView.setProjection(gl);
            mGLView.setView(gl);
        }
        if (!mDisplayWorldFlat) {
            if (mFlyToCity) {
                float lerp = (now - mCityFlyStartTime)/mCityFlightTime;
                if (lerp >= 1.0f) {
                    mFlyToCity = false;
                }
                lerp = Math.min(lerp, 1.0f);
                lerp = mFlyToCityInterpolator.getInterpolation(lerp);
                mRotAngle = lerp(mRotAngleStart, mRotAngleDest, lerp);
                mTiltAngle = lerp(mTiltAngleStart, mTiltAngleDest, lerp);
            }
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glRotatef(mTiltAngle, 1, 0, 0);
            gl.glRotatef(mRotAngle, 0, 1, 0);
            mRotAngle += mRotVelocity;
            if (mRotAngle < 0.0f) {
                mRotAngle += 360.0f;
            }
            if (mRotAngle > 360.0f) {
                mRotAngle -= 360.0f;
            }
        }
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, mLightDir, 0);
        mGLView.setLights(gl, GL10.GL_LIGHT0);
        if (mDisplayWorldFlat) {
            drawWorldFlat(gl);
        } else if (mDisplayWorld) {
            drawWorldRound(gl);
        }
        if (mDisplayLights && !mDisplayWorldFlat) {
            float lerp = Math.min((now - mClockFadeTime)/1000.0f, 1.0f);
            if (!mDisplayClock) {
                lerp = 1.0f - lerp;
            }
            lerp = mClockSizeInterpolator.getInterpolation(lerp);
            drawCityLights(gl, lerp);
        }
        if (mDisplayAtmosphere && !mDisplayWorldFlat) {
            drawAtmosphere(gl);
        }
        mGLView.setNumTriangles(mNumTriangles);
        egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
            Context c = getContext();
            if (c instanceof Activity) {
                ((Activity)c).finish();
            }
        }
    }
    private static final int INVALIDATE = 1;
    private static final int ONE_MINUTE = 60000;
    private final Handler mHandler = new Handler() {
        private long mLastSunPositionTime = 0;
        @Override public void handleMessage(Message msg) {
            if (msg.what == INVALIDATE) {
                if ((msg.getWhen() - mLastSunPositionTime) >= ONE_MINUTE) {
                    computeSunDirection();
                    mLastSunPositionTime = msg.getWhen();
                }
                drawOpenGLScene();
                if (mInitialized &&
                                (mClockShowing || mGLView.hasMessages())) {
                    invalidate();
                }
                sendEmptyMessage(INVALIDATE);
            }
        }
    };
}
public class GlobalTime extends Activity {
    GTView gtView = null;
    @Override protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        gtView = new GTView(this);
        setContentView(gtView);
    }
    @Override protected void onResume() {
        super.onResume();
        gtView.onResume();
        Looper.myQueue().addIdleHandler(new Idler());
    }
    @Override protected void onPause() {
        super.onPause();
        gtView.onPause();
    }
    @Override protected void onStop() {
        super.onStop();
        gtView.destroy();
        gtView = null;
    }
    class Idler implements MessageQueue.IdleHandler {
        public Idler() {
            super();
        }
        public final boolean queueIdle() {
            if (gtView != null) {
                gtView.startAnimating();
            }
            return false;
        }
    }
}
