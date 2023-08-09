public class ProgramRaster extends BaseObj {
    boolean mPointSmooth;
    boolean mLineSmooth;
    boolean mPointSprite;
    float mPointSize;
    float mLineWidth;
    Element mIn;
    Element mOut;
    ProgramRaster(int id, RenderScript rs) {
        super(rs);
        mID = id;
        mPointSize = 1.0f;
        mLineWidth = 1.0f;
        mPointSmooth = false;
        mLineSmooth = false;
        mPointSprite = false;
    }
    public void setLineWidth(float w) {
        mRS.validate();
        mLineWidth = w;
        mRS.nProgramRasterSetLineWidth(mID, w);
    }
    public void setPointSize(float s) {
        mRS.validate();
        mPointSize = s;
        mRS.nProgramRasterSetPointSize(mID, s);
    }
    void internalInit() {
        int inID = 0;
        int outID = 0;
        if (mIn != null) {
            inID = mIn.mID;
        }
        if (mOut != null) {
            outID = mOut.mID;
        }
        mID = mRS.nProgramRasterCreate(inID, outID, mPointSmooth, mLineSmooth, mPointSprite);
    }
    public static class Builder {
        RenderScript mRS;
        ProgramRaster mPR;
        public Builder(RenderScript rs, Element in, Element out) {
            mRS = rs;
            mPR = new ProgramRaster(0, rs);
        }
        public void setPointSpriteEnable(boolean enable) {
            mPR.mPointSprite = enable;
        }
        public void setPointSmoothEnable(boolean enable) {
            mPR.mPointSmooth = enable;
        }
        public void setLineSmoothEnable(boolean enable) {
            mPR.mLineSmooth = enable;
        }
        static synchronized ProgramRaster internalCreate(RenderScript rs, Builder b) {
            b.mPR.internalInit();
            ProgramRaster pr = b.mPR;
            b.mPR = new ProgramRaster(0, b.mRS);
            return pr;
        }
        public ProgramRaster create() {
            mRS.validate();
            return internalCreate(mRS, this);
        }
    }
}
