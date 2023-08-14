public class GenericWaveRS extends RenderScriptScene {
    private final Handler mHandler = new Handler();
    private final Runnable mDrawCube = new Runnable() {
        public void run() {
            updateWave();
        }
    };
    private boolean mVisible;
    private int mTexId;
    protected static class WorldState {
        public float yRotation;
        public int idle;
        public int waveCounter;
        public int width;
    }
    protected WorldState mWorldState = new WorldState();
    private Type mStateType;
    protected Allocation mState;
    private SimpleMesh mCubeMesh;
    protected Allocation mPointAlloc;
    protected float [] mPointData = new float[1024*8];
    private Allocation mLineIdxAlloc;
    private short [] mIndexData = new short[1024*2];
    private ProgramVertex mPVBackground;
    private ProgramVertex.MatrixAllocation mPVAlloc;
    protected short [] mVizData = new short[1024];
    private ProgramFragment mPfBackground;
    private Sampler mSampler;
    private Allocation mTexture;
    private static final int RSID_STATE = 0;
    private static final int RSID_POINTS = 1;
    private static final int RSID_LINES = 2;
    private static final int RSID_PROGRAMVERTEX = 3;
    protected GenericWaveRS(int width, int height, int texid) {
        super(width, height);
        mTexId = texid;
        mWidth = width;
        mHeight = height;
        int outlen = mPointData.length / 8;
        int half = outlen / 2;
        for(int i = 0; i < outlen; i++) {
            mPointData[i*8]   = i - half;          
            mPointData[i*8+2] = 0;                 
            mPointData[i*8+3] = 0;                 
            mPointData[i*8+4]   = i - half;        
            mPointData[i*8+6] = 1.0f;                 
            mPointData[i*8+7] = 0f;              
        }
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        mWorldState.width = width;
        if (mPVAlloc != null) {
            mPVAlloc.setupProjectionNormalized(mWidth, mHeight);
        }
    }
    @Override
    protected ScriptC createScript() {
        mStateType = Type.createFromClass(mRS, WorldState.class, 1, "WorldState");
        mState = Allocation.createTyped(mRS, mStateType);
        mWorldState.yRotation = 0.0f;
        mWorldState.width = mWidth;
        mState.data(mWorldState);
        ProgramVertex.Builder pvb = new ProgramVertex.Builder(mRS, null, null);
        mPVBackground = pvb.create();
        mPVBackground.setName("PVBackground");
        mPVAlloc = new ProgramVertex.MatrixAllocation(mRS);
        mPVBackground.bindAllocation(mPVAlloc);
        mPVAlloc.setupProjectionNormalized(mWidth, mHeight);
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
        mTexture = Allocation.createFromBitmapResourceBoxed(mRS, mResources, mTexId, RGB_565(mRS), false);
        mTexture.setName("Tlinetexture");
        mTexture.uploadToTexture(0);
        Sampler.Builder samplerBuilder = new Sampler.Builder(mRS);
        samplerBuilder.setMin(LINEAR);
        samplerBuilder.setMag(LINEAR);
        samplerBuilder.setWrapS(WRAP);
        samplerBuilder.setWrapT(WRAP);
        mSampler = samplerBuilder.create();
        ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                           ProgramFragment.Builder.Format.RGBA, 0);
        mPfBackground = builder.create();
        mPfBackground.setName("PFBackground");
        mPfBackground.bindSampler(mSampler, 0);
        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setType(mCubeMesh.getVertexType(0), "Points", RSID_POINTS);
        sb.setScript(mResources, R.raw.waveform);
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
    public void setOffset(float xOffset, float yOffset,
            float xStep, float yStep, int xPixels, int yPixels) {
        if (xStep <= 0.0f) {
            xStep = xOffset / 2; 
        }
        mWorldState.yRotation = xStep == 0.f ? 0.f : (xOffset / xStep) * 180;
        mState.data(mWorldState);
    }
    @Override
    public void start() {
        super.start();
        mVisible = true;
        MediaPlayer.snoop(mVizData, 0);
        SystemClock.sleep(200);
        updateWave();
    }
    @Override
    public void stop() {
        super.stop();
        mVisible = false;
        updateWave();
    }
    public void update() {
    }
    void updateWave() {
        mHandler.removeCallbacks(mDrawCube);
        if (!mVisible) {
            return;
        }
        mHandler.postDelayed(mDrawCube, 20);
        update();
        mWorldState.waveCounter++;
        mState.data(mWorldState);
    }
}
