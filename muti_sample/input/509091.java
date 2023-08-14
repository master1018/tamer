public class Light extends BaseObj {
    Light(int id, RenderScript rs) {
        super(rs);
        mID = id;
    }
    public void setColor(float r, float g, float b) {
        mRS.validate();
        mRS.nLightSetColor(mID, r, g, b);
    }
    public void setPosition(float x, float y, float z) {
        mRS.validate();
        mRS.nLightSetPosition(mID, x, y, z);
    }
    public static class Builder {
        RenderScript mRS;
        boolean mIsMono;
        boolean mIsLocal;
        public Builder(RenderScript rs) {
            mRS = rs;
            mIsMono = false;
            mIsLocal = false;
        }
        public void lightSetIsMono(boolean isMono) {
            mIsMono = isMono;
        }
        public void lightSetIsLocal(boolean isLocal) {
            mIsLocal = isLocal;
        }
        static synchronized Light internalCreate(RenderScript rs, Builder b) {
            rs.nSamplerBegin();
            rs.nLightSetIsMono(b.mIsMono);
            rs.nLightSetIsLocal(b.mIsLocal);
            int id = rs.nLightCreate();
            return new Light(id, rs);
        }
        public Light create() {
            mRS.validate();
            return internalCreate(mRS, this);
        }
    }
}
