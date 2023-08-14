public class ProgramFragment extends Program {
    ProgramFragment(int id, RenderScript rs) {
        super(id, rs);
    }
    public static class ShaderBuilder extends BaseProgramBuilder {
        public ShaderBuilder(RenderScript rs) {
            super(rs);
        }
        public ProgramFragment create() {
            mRS.validate();
            int[] tmp = new int[(mInputCount + mOutputCount + mConstantCount + 1) * 2];
            int idx = 0;
            for (int i=0; i < mInputCount; i++) {
                tmp[idx++] = 0;
                tmp[idx++] = mInputs[i].mID;
            }
            for (int i=0; i < mOutputCount; i++) {
                tmp[idx++] = 1;
                tmp[idx++] = mOutputs[i].mID;
            }
            for (int i=0; i < mConstantCount; i++) {
                tmp[idx++] = 2;
                tmp[idx++] = mConstants[i].mID;
            }
            tmp[idx++] = 3;
            tmp[idx++] = mTextureCount;
            int id = mRS.nProgramFragmentCreate2(mShader, tmp);
            ProgramFragment pf = new ProgramFragment(id, mRS);
            initProgram(pf);
            return pf;
        }
    }
    public static class Builder {
        public static final int MAX_TEXTURE = 2;
        RenderScript mRS;
        boolean mPointSpriteEnable;
        public enum EnvMode {
            REPLACE (1),
            MODULATE (2),
            DECAL (3);
            int mID;
            EnvMode(int id) {
                mID = id;
            }
        }
        public enum Format {
            ALPHA (1),
            LUMINANCE_ALPHA (2),
            RGB (3),
            RGBA (4);
            int mID;
            Format(int id) {
                mID = id;
            }
        }
        private class Slot {
            EnvMode env;
            Format format;
            Slot(EnvMode _env, Format _fmt) {
                env = _env;
                format = _fmt;
            }
        }
        Slot[] mSlots;
        public Builder(RenderScript rs) {
            mRS = rs;
            mSlots = new Slot[MAX_TEXTURE];
            mPointSpriteEnable = false;
        }
        public void setTexture(EnvMode env, Format fmt, int slot)
            throws IllegalArgumentException {
            if((slot < 0) || (slot >= MAX_TEXTURE)) {
                throw new IllegalArgumentException("MAX_TEXTURE exceeded.");
            }
            mSlots[slot] = new Slot(env, fmt);
        }
        public void setPointSpriteTexCoordinateReplacement(boolean enable) {
            mPointSpriteEnable = enable;
        }
        public ProgramFragment create() {
            mRS.validate();
            int[] tmp = new int[MAX_TEXTURE * 2 + 1];
            if (mSlots[0] != null) {
                tmp[0] = mSlots[0].env.mID;
                tmp[1] = mSlots[0].format.mID;
            }
            if (mSlots[1] != null) {
                tmp[2] = mSlots[1].env.mID;
                tmp[3] = mSlots[1].format.mID;
            }
            tmp[4] = mPointSpriteEnable ? 1 : 0;
            int id = mRS.nProgramFragmentCreate(tmp);
            ProgramFragment pf = new ProgramFragment(id, mRS);
            pf.mTextureCount = MAX_TEXTURE;
            return pf;
        }
    }
}
