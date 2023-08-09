class NexusRS extends RenderScriptScene {
    private static final int RSID_STATE = 0;
    private static final int RSID_COMMAND = 1;
    private static final int TEXTURES_COUNT = 3;
    private final BitmapFactory.Options mOptionsARGB = new BitmapFactory.Options();
    private ProgramFragment mPfTexture;
    private ProgramFragment mPfTexture565;
    private ProgramFragment mPfColor;
    private ProgramStore mPsSolid;
    private ProgramStore mPsBlend;
    private ProgramVertex mPvOrtho;
    private ProgramVertex.MatrixAllocation mPvOrthoAlloc;
    private Sampler mClampSampler;
    private Sampler mWrapSampler;
    private Allocation mState;
    private Type mStateType;
    private WorldState mWorldState;
    private Allocation mCommandAllocation;
    private Type mCommandType;
    private CommandState mCommand;
    private Allocation[] mTextures = new Allocation[TEXTURES_COUNT];
    public NexusRS(int width, int height) {
        super(width, height);
        mOptionsARGB.inScaled = false;
        mOptionsARGB.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }
    @Override
    public void setOffset(float xOffset, float yOffset, int xPixels, int yPixels) {
        mWorldState.xOffset = xOffset;
        mState.data(mWorldState);
    }
    @Override
    public void start() {
        super.start();
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height); 
        mWorldState.width = width;
        mWorldState.height = height;
        mWorldState.rotate = width > height ? 1 : 0;
        mState.data(mWorldState);
        mPvOrthoAlloc.setupOrthoWindow(mWidth, mHeight);
    }
    @Override
    protected ScriptC createScript() {
        createProgramVertex();
        createProgramFragmentStore();
        createProgramFragment();
        createState();
        loadTextures();
        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setType(mCommandType, "Command", RSID_COMMAND);
        sb.setScript(mResources, R.raw.nexus);
        Script.Invokable invokable = sb.addInvokable("initPulses");
        sb.setRoot(true);
        ScriptC script = sb.create();
        script.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        script.setTimeZone(TimeZone.getDefault().getID());
        script.bindAllocation(mState, RSID_STATE);
        script.bindAllocation(mCommandAllocation, RSID_COMMAND);
        invokable.execute();
        return script;
    }
    static class WorldState {
        public int width;
        public int height;
        public float glWidth;
        public float glHeight;
        public int rotate;
        public int isPreview;
        public float xOffset;
        public int mode;
    }
    static class CommandState {
        public int x;
        public int y;
        public int command;
    }
    private void createState() {
        mWorldState = new WorldState();
        mWorldState.width = mWidth;
        mWorldState.height = mHeight;
        mWorldState.rotate = mWidth > mHeight ? 1 : 0;
        mWorldState.isPreview = isPreview() ? 1 : 0;
        try {
            mWorldState.mode = mResources.getInteger(R.integer.nexus_mode);
        } catch (Resources.NotFoundException exc) {
            mWorldState.mode = 0; 
        }
        mStateType = Type.createFromClass(mRS, WorldState.class, 1, "WorldState");
        mState = Allocation.createTyped(mRS, mStateType);
        mState.data(mWorldState);
        mCommand = new CommandState();
        mCommand.x = -1;
        mCommand.y = -1;
        mCommand.command = 0;
        mCommandType = Type.createFromClass(mRS, CommandState.class, 1, "DropState");
        mCommandAllocation = Allocation.createTyped(mRS, mCommandType);
        mCommandAllocation.data(mCommand);
    }
    private void loadTextures() {
        mTextures[0] = loadTexture(R.drawable.pyramid_background, "TBackground");
        mTextures[1] = loadTextureARGB(R.drawable.pulse, "TPulse");
        mTextures[2] = loadTextureARGB(R.drawable.glow, "TGlow");
        final int count = mTextures.length;
        for (int i = 0; i < count; i++) {
            mTextures[i].uploadToTexture(0);
        }
    }
    private Allocation loadTexture(int id, String name) {
        final Allocation allocation = Allocation.createFromBitmapResource(mRS, mResources,
                id, RGB_565(mRS), false);
        allocation.setName(name);
        return allocation;
    }
    private Allocation loadTextureARGB(int id, String name) {
        Bitmap b = BitmapFactory.decodeResource(mResources, id, mOptionsARGB);
        final Allocation allocation = Allocation.createFromBitmap(mRS, b, RGBA_8888(mRS), false);
        allocation.setName(name);
        return allocation;
    }
    private void createProgramFragment() {
        Sampler.Builder sampleBuilder = new Sampler.Builder(mRS);
        sampleBuilder.setMin(LINEAR);
        sampleBuilder.setMag(LINEAR);
        sampleBuilder.setWrapS(WRAP);
        sampleBuilder.setWrapT(WRAP);
        mWrapSampler = sampleBuilder.create();
        ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.MODULATE,
                           ProgramFragment.Builder.Format.RGBA, 0);
        mPfTexture = builder.create();
        mPfTexture.setName("PFTexture");
        mPfTexture.bindSampler(mWrapSampler, 0);
        builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                           ProgramFragment.Builder.Format.RGB, 0);
        mPfColor = builder.create();
        mPfColor.setName("PFColor");
        mPfColor.bindSampler(mWrapSampler, 0);
        sampleBuilder.setWrapS(CLAMP);
        sampleBuilder.setWrapT(CLAMP);
        mClampSampler = sampleBuilder.create();
        builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.MODULATE,
                           ProgramFragment.Builder.Format.RGB, 0);
        mPfTexture565 = builder.create();
        mPfTexture565.setName("PFTexture565");
        mPfTexture565.bindSampler(mClampSampler, 0);
    }
    private void createProgramFragmentStore() {
        ProgramStore.Builder builder = new ProgramStore.Builder(mRS, null, null);
        builder.setDepthFunc(ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ONE);
        builder.setDitherEnable(false);
        builder.setDepthMask(true);
        mPsSolid = builder.create();
        mPsSolid.setName("PSSolid");
        builder = new ProgramStore.Builder(mRS, null, null);
        builder.setDepthFunc(ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.SRC_ALPHA, BlendDstFunc.ONE);
        builder.setDitherEnable(false);
        builder.setDepthMask(true);
        mPsBlend = builder.create();
        mPsBlend.setName("PSBlend");
    }
    private void createProgramVertex() {
        mPvOrthoAlloc = new ProgramVertex.MatrixAllocation(mRS);
        mPvOrthoAlloc.setupOrthoWindow(mWidth, mHeight);
        ProgramVertex.Builder pvb = new ProgramVertex.Builder(mRS, null, null);
        pvb.setTextureMatrixEnable(true);
        mPvOrtho = pvb.create();
        mPvOrtho.bindAllocation(mPvOrthoAlloc);
        mPvOrtho.setName("PVOrtho");
    }
    @Override
    public Bundle onCommand(String action, int x, int y, int z, Bundle extras,
            boolean resultRequested) {
        final int dw = mWorldState.width;
        final int bw = 960; 
        if (mWorldState.rotate == 0) {
            x = (int) (x + mWorldState.xOffset * (bw-dw));
        }
        if ("android.wallpaper.tap".equals(action)) {
            sendCommand(1, x, y);
        } else if ("android.home.drop".equals(action)) {
            sendCommand(2, x, y);
        }
        return null;
    }
    private void sendCommand(int command, int x, int y) {
        mCommand.x = x;
        mCommand.y = y;
        mCommand.command = command;
        mCommandAllocation.data(mCommand);
    }
}
