class Visualization4RS extends RenderScriptScene {
    private final Handler mHandler = new Handler();
    private final Runnable mDrawCube = new Runnable() {
        public void run() {
            updateWave();
        }
    };
    private boolean mVisible;
    private int mNeedlePos = 0;
    private int mNeedleSpeed = 0;
    private int mNeedleMass = 10;
    private int mSpringForceAtOrigin = 200;
    static class WorldState {
        public float mAngle;
        public int   mPeak;
    }
    WorldState mWorldState = new WorldState();
    private Type mStateType;
    private Allocation mState;
    private ProgramStore mPfsBackground;
    private ProgramFragment mPfBackground;
    private Sampler mSampler;
    private Allocation[] mTextures;
    private ProgramVertex mPVBackground;
    private ProgramVertex.MatrixAllocation mPVAlloc;
    private short [] mVizData = new short[1024];
    private static final int RSID_STATE = 0;
    private static final int RSID_POINTS = 1;
    private static final int RSID_LINES = 2;
    private static final int RSID_PROGRAMVERTEX = 3;
    Visualization4RS(int width, int height) {
        super(width, height);
        mWidth = width;
        mHeight = height;
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (mPVAlloc != null) {
            mPVAlloc.setupProjectionNormalized(width, height);
        }
    }
    @Override
    protected ScriptC createScript() {
        mStateType = Type.createFromClass(mRS, WorldState.class, 1, "WorldState");
        mState = Allocation.createTyped(mRS, mStateType);
        ProgramVertex.Builder pvb = new ProgramVertex.Builder(mRS, null, null);
        mPVBackground = pvb.create();
        mPVBackground.setName("PVBackground");
        mPVAlloc = new ProgramVertex.MatrixAllocation(mRS);
        mPVBackground.bindAllocation(mPVAlloc);
        mPVAlloc.setupProjectionNormalized(mWidth, mHeight);
        updateWave();
        mTextures = new Allocation[6];
        mTextures[0] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.background, Element.RGBA_8888(mRS), false);
        mTextures[0].setName("Tvumeter_background");
        mTextures[1] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.frame, Element.RGBA_8888(mRS), false);
        mTextures[1].setName("Tvumeter_frame");
        mTextures[2] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.peak_on, Element.RGBA_8888(mRS), false);
        mTextures[2].setName("Tvumeter_peak_on");
        mTextures[3] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.peak_off, Element.RGBA_8888(mRS), false);
        mTextures[3].setName("Tvumeter_peak_off");
        mTextures[4] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.needle, Element.RGBA_8888(mRS), false);
        mTextures[4].setName("Tvumeter_needle");
        mTextures[5] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.black, Element.RGB_565(mRS), false);
        mTextures[5].setName("Tvumeter_black");
        final int count = mTextures.length;
        for (int i = 0; i < count; i++) {
            mTextures[i].uploadToTexture(0);
        }
        Sampler.Builder samplerBuilder = new Sampler.Builder(mRS);
        samplerBuilder.setMin(LINEAR);
        samplerBuilder.setMag(LINEAR);
        samplerBuilder.setWrapS(WRAP);
        samplerBuilder.setWrapT(WRAP);
        mSampler = samplerBuilder.create();
        {
            ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
            builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                               ProgramFragment.Builder.Format.RGBA, 0);
            mPfBackground = builder.create();
            mPfBackground.setName("PFBackground");
            mPfBackground.bindSampler(mSampler, 0);
        }
        {
            ProgramStore.Builder builder = new ProgramStore.Builder(mRS, null, null);
            builder.setDepthFunc(ALWAYS);
            builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ONE_MINUS_SRC_ALPHA);
            builder.setDitherEnable(true); 
            builder.setDepthMask(false);
            mPfsBackground = builder.create();
            mPfsBackground.setName("PFSBackground");
        }
        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setScript(mResources, R.raw.vu);
        sb.setRoot(true);
        ScriptC script = sb.create();
        script.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        script.setTimeZone(TimeZone.getDefault().getID());
        script.bindAllocation(mState, RSID_STATE);
        script.bindAllocation(mPVAlloc.mAlloc, RSID_PROGRAMVERTEX);
        return script;
    }
    @Override
    public void start() {
        super.start();
        mVisible = true;
        updateWave();
    }
    @Override
    public void stop() {
        super.stop();
        mVisible = false;
    }
    void updateWave() {
        mHandler.removeCallbacks(mDrawCube);
        if (!mVisible) {
            return;
        }
        mHandler.postDelayed(mDrawCube, 20);
        int len = MediaPlayer.snoop(mVizData, 0);
        int volt = 0;
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                int val = mVizData[i];
                if (val < 0) {
                    val = -val;
                }
                volt += val;
            }
            volt = volt * 4 / len; 
        }
        int netforce = volt - mNeedleSpeed * 3 - (mNeedlePos + mSpringForceAtOrigin) ;
        int acceleration = netforce / mNeedleMass;
        mNeedleSpeed += acceleration;
        mNeedlePos += mNeedleSpeed;
        if (mNeedlePos < 0) {
            mNeedlePos = 0;
            mNeedleSpeed = 0;
        } else if (mNeedlePos > 32767) {
            if (mNeedlePos > 33333) {
                 mWorldState.mPeak = 10;
            }
            mNeedlePos = 32767;
            mNeedleSpeed = 0;
        }
        if (mWorldState.mPeak > 0) {
            mWorldState.mPeak--;
        }
        mWorldState.mAngle = 131f - (mNeedlePos / 410f); 
        mState.data(mWorldState);
    }
}
