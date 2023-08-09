public class ProgramStore extends BaseObj {
        public enum DepthFunc {
        ALWAYS (0),
        LESS (1),
        LEQUAL (2),
        GREATER (3),
        GEQUAL (4),
        EQUAL (5),
        NOTEQUAL (6);
        int mID;
        DepthFunc(int id) {
            mID = id;
        }
    }
    public enum BlendSrcFunc {
        ZERO (0),
        ONE (1),
        DST_COLOR (2),
        ONE_MINUS_DST_COLOR (3),
        SRC_ALPHA (4),
        ONE_MINUS_SRC_ALPHA (5),
        DST_ALPHA (6),
        ONE_MINUS_DST_ALPHA (7),
        SRC_ALPHA_SATURATE (8);
        int mID;
        BlendSrcFunc(int id) {
            mID = id;
        }
    }
    public enum BlendDstFunc {
        ZERO (0),
        ONE (1),
        SRC_COLOR (2),
        ONE_MINUS_SRC_COLOR (3),
        SRC_ALPHA (4),
        ONE_MINUS_SRC_ALPHA (5),
        DST_ALPHA (6),
        ONE_MINUS_DST_ALPHA (7);
        int mID;
        BlendDstFunc(int id) {
            mID = id;
        }
    }
    ProgramStore(int id, RenderScript rs) {
        super(rs);
        mID = id;
    }
    public static class Builder {
        RenderScript mRS;
        Element mIn;
        Element mOut;
        DepthFunc mDepthFunc;
        boolean mDepthMask;
        boolean mColorMaskR;
        boolean mColorMaskG;
        boolean mColorMaskB;
        boolean mColorMaskA;
        BlendSrcFunc mBlendSrc;
        BlendDstFunc mBlendDst;
        boolean mDither;
        public Builder(RenderScript rs, Element in, Element out) {
            mRS = rs;
            mIn = in;
            mOut = out;
            mDepthFunc = DepthFunc.ALWAYS;
            mDepthMask = false;
            mColorMaskR = true;
            mColorMaskG = true;
            mColorMaskB = true;
            mColorMaskA = true;
            mBlendSrc = BlendSrcFunc.ONE;
            mBlendDst = BlendDstFunc.ZERO;
        }
        public void setDepthFunc(DepthFunc func) {
            mDepthFunc = func;
        }
        public void setDepthMask(boolean enable) {
            mDepthMask = enable;
        }
        public void setColorMask(boolean r, boolean g, boolean b, boolean a) {
            mColorMaskR = r;
            mColorMaskG = g;
            mColorMaskB = b;
            mColorMaskA = a;
        }
        public void setBlendFunc(BlendSrcFunc src, BlendDstFunc dst) {
            mBlendSrc = src;
            mBlendDst = dst;
        }
        public void setDitherEnable(boolean enable) {
            mDither = enable;
        }
        static synchronized ProgramStore internalCreate(RenderScript rs, Builder b) {
            int inID = 0;
            int outID = 0;
            if (b.mIn != null) {
                inID = b.mIn.mID;
            }
            if (b.mOut != null) {
                outID = b.mOut.mID;
            }
            rs.nProgramFragmentStoreBegin(inID, outID);
            rs.nProgramFragmentStoreDepthFunc(b.mDepthFunc.mID);
            rs.nProgramFragmentStoreDepthMask(b.mDepthMask);
            rs.nProgramFragmentStoreColorMask(b.mColorMaskR,
                                              b.mColorMaskG,
                                              b.mColorMaskB,
                                              b.mColorMaskA);
            rs.nProgramFragmentStoreBlendFunc(b.mBlendSrc.mID, b.mBlendDst.mID);
            rs.nProgramFragmentStoreDither(b.mDither);
            int id = rs.nProgramFragmentStoreCreate();
            return new ProgramStore(id, rs);
        }
        public ProgramStore create() {
            mRS.validate();
            return internalCreate(mRS, this);
        }
    }
}
