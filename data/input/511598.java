class Visualization5RS extends RenderScriptScene {
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
        public float mRotate;
        public float mTilt;
        public int   mIdle;
        public int   mWaveCounter;
    }
    WorldState mWorldState = new WorldState();
    private Type mStateType;
    private Allocation mState;
    private ProgramStore mPfsBackground;
    private ProgramFragment mPfBackgroundMip;
    private ProgramFragment mPfBackgroundNoMip;
    private Sampler mSamplerMip;
    private Sampler mSamplerNoMip;
    private Allocation[] mTextures;
    private ProgramVertex mPVBackground;
    private ProgramVertex.MatrixAllocation mPVAlloc;
    private SimpleMesh mCubeMesh;
    protected Allocation mPointAlloc;
    protected float [] mPointData = new float[256*8];
    private Allocation mLineIdxAlloc;
    private short [] mIndexData = new short[256*2];
    private short [] mVizData = new short[1024];
    private static final int RSID_STATE = 0;
    private static final int RSID_POINTS = 1;
    private static final int RSID_LINES = 2;
    private static final int RSID_PROGRAMVERTEX = 3;
    private float mTouchY;
    Visualization5RS(int width, int height) {
        super(width, height);
        mWidth = width;
        mHeight = height;
        int outlen = mPointData.length / 8;
        int half = outlen / 2;
        for(int i = 0; i < outlen; i++) {
            mPointData[i*8]   = i - half;          
            mPointData[i*8+2] = 0;                 
            mPointData[i*8+3] = 0;                 
            mPointData[i*8+4] = i - half;          
            mPointData[i*8+6] = 1.0f;              
            mPointData[i*8+7] = 0f;                
        }
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (mPVAlloc != null) {
            mPVAlloc.setupProjectionNormalized(width, height);
        }
        mWorldState.mTilt = -20;
    }
    @Override
    public void onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = event.getY() - mTouchY;
                mTouchY += dy;
                dy /= 10;
                dy += mWorldState.mTilt;
                if (dy > 0) {
                    dy = 0;
                } else if (dy < -45) {
                    dy = -45;
                }
                mWorldState.mTilt = dy;
                mState.data(mWorldState);
        }
    }
    @Override
    public void setOffset(float xOffset, float yOffset,
            float xStep, float yStep, int xPixels, int yPixels) {
        mWorldState.mRotate = (xOffset - 0.5f) * 90;
        mState.data(mWorldState);
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
        mTextures = new Allocation[8];
        mTextures[0] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.background, Element.RGBA_8888(mRS), true);
        mTextures[0].setName("Tvumeter_background");
        mTextures[1] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.frame, Element.RGBA_8888(mRS), true);
        mTextures[1].setName("Tvumeter_frame");
        mTextures[2] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.peak_on, Element.RGBA_8888(mRS), true);
        mTextures[2].setName("Tvumeter_peak_on");
        mTextures[3] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.peak_off, Element.RGBA_8888(mRS), true);
        mTextures[3].setName("Tvumeter_peak_off");
        mTextures[4] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.needle, Element.RGBA_8888(mRS), true);
        mTextures[4].setName("Tvumeter_needle");
        mTextures[5] = Allocation.createFromBitmapResourceBoxed(mRS, mResources, R.drawable.black, Element.RGB_565(mRS), false);
        mTextures[5].setName("Tvumeter_black");
        mTextures[6] = Allocation.createFromBitmapResource(mRS, mResources, R.drawable.albumart, Element.RGBA_8888(mRS), true);
        mTextures[6].setName("Tvumeter_album");
        mTextures[7] = Allocation.createFromBitmapResource(mRS, mResources, R.drawable.fire, Element.RGB_565(mRS), false);
        mTextures[7].setName("Tlinetexture");
        final int count = mTextures.length;
        for (int i = 0; i < count; i++) {
            mTextures[i].uploadToTexture(0);
        }
        {
            Sampler.Builder builder = new Sampler.Builder(mRS);
            builder.setMin(Value.LINEAR);
            builder.setMag(Value.LINEAR);
            builder.setWrapS(Value.WRAP);
            builder.setWrapT(Value.WRAP);
            mSamplerNoMip = builder.create();
        }
        {
            Sampler.Builder builder = new Sampler.Builder(mRS);
            builder.setMin(Value.LINEAR_MIP_LINEAR);
            builder.setMag(Value.LINEAR);
            builder.setWrapS(Value.WRAP);
            builder.setWrapT(Value.WRAP);
            mSamplerMip = builder.create();
        }
        {
            ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
            builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                               ProgramFragment.Builder.Format.RGBA, 0);
            mPfBackgroundNoMip = builder.create();
            mPfBackgroundNoMip.setName("PFBackgroundNoMip");
            mPfBackgroundNoMip.bindSampler(mSamplerNoMip, 0);
        }
        {
            ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
            builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                               ProgramFragment.Builder.Format.RGBA, 0);
            mPfBackgroundMip = builder.create();
            mPfBackgroundMip.setName("PFBackgroundMip");
            mPfBackgroundMip.bindSampler(mSamplerMip, 0);
        }
        {
            ProgramStore.Builder builder = new ProgramStore.Builder(mRS, null, null);
            builder.setDepthFunc(ProgramStore.DepthFunc.EQUAL);
            builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ONE_MINUS_SRC_ALPHA);
            builder.setDitherEnable(true); 
            builder.setDepthMask(false);
            mPfsBackground = builder.create();
            mPfsBackground.setName("PFSBackground");
        }
        final SimpleMesh.Builder meshBuilder = new SimpleMesh.Builder(mRS);
        Builder elementBuilder = new Builder(mRS);
        elementBuilder.add(Element.ATTRIB_POSITION_2(mRS), "position");
        elementBuilder.add(Element.ATTRIB_TEXTURE_2(mRS), "texture");
        final Element vertexElement = elementBuilder.create();
        final int vertexSlot = meshBuilder.addVertexType(vertexElement, mPointData.length / 4);
        meshBuilder.setIndexType(Element.INDEX_16(mRS), mIndexData.length);
        meshBuilder.setPrimitive(Primitive.LINE);
        mCubeMesh = meshBuilder.create();
        mCubeMesh.setName("CubeMesh");
        mPointAlloc = mCubeMesh.createVertexAllocation(vertexSlot);
        mPointAlloc.setName("PointBuffer");
        mLineIdxAlloc = mCubeMesh.createIndexAllocation();
        mCubeMesh.bindVertexAllocation(mPointAlloc, 0);
        mCubeMesh.bindIndexAllocation(mLineIdxAlloc);
        updateWave();
        for(int i = 0; i < mIndexData.length; i ++) {
            mIndexData[i] = (short) i;
        }
        mPointAlloc.data(mPointData);
        mPointAlloc.uploadToBufferObject();
        mLineIdxAlloc.data(mIndexData);
        mLineIdxAlloc.uploadToBufferObject();
        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setScript(mResources, R.raw.many);
        sb.setRoot(true);
        ScriptC script = sb.create();
        script.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        script.setTimeZone(TimeZone.getDefault().getID());
        script.bindAllocation(mState, RSID_STATE);
        script.bindAllocation(mPointAlloc, RSID_POINTS);
        script.bindAllocation(mLineIdxAlloc, RSID_LINES);
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
        if (len == 0) {
            if (mWorldState.mIdle == 0) {
                mWorldState.mIdle = 1;
            }
        } else {
            if (mWorldState.mIdle != 0) {
                mWorldState.mIdle = 0;
            }
            int outlen = mPointData.length / 8;
            len /= 4;
            if (len > outlen) len = outlen;
            for(int i = 0; i < len; i++) {
                int amp = (mVizData[i*4]  + mVizData[i*4+1] + mVizData[i*4+2] + mVizData[i*4+3]);
                mPointData[i*8+1] = amp;
                mPointData[i*8+5] = -amp;
            }
            mPointAlloc.data(mPointData);
            mWorldState.mWaveCounter++;
        }
        mState.data(mWorldState);
    }
}
