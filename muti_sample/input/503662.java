public class Sampler extends BaseObj {
    public enum Value {
        NEAREST (0),
        LINEAR (1),
        LINEAR_MIP_LINEAR (2),
        WRAP (3),
        CLAMP (4);
        int mID;
        Value(int id) {
            mID = id;
        }
    }
    Sampler(int id, RenderScript rs) {
        super(rs);
        mID = id;
    }
    public static class Builder {
        RenderScript mRS;
        Value mMin;
        Value mMag;
        Value mWrapS;
        Value mWrapT;
        Value mWrapR;
        public Builder(RenderScript rs) {
            mRS = rs;
            mMin = Value.NEAREST;
            mMag = Value.NEAREST;
            mWrapS = Value.WRAP;
            mWrapT = Value.WRAP;
            mWrapR = Value.WRAP;
        }
        public void setMin(Value v) {
            if (v == Value.NEAREST ||
                v == Value.LINEAR ||
                v == Value.LINEAR_MIP_LINEAR) {
                mMin = v;
            } else {
                throw new IllegalArgumentException("Invalid value");
            }
        }
        public void setMag(Value v) {
            if (v == Value.NEAREST || v == Value.LINEAR) {
                mMag = v;
            } else {
                throw new IllegalArgumentException("Invalid value");
            }
        }
        public void setWrapS(Value v) {
            if (v == Value.WRAP || v == Value.CLAMP) {
                mWrapS = v;
            } else {
                throw new IllegalArgumentException("Invalid value");
            }
        }
        public void setWrapT(Value v) {
            if (v == Value.WRAP || v == Value.CLAMP) {
                mWrapT = v;
            } else {
                throw new IllegalArgumentException("Invalid value");
            }
        }
        public void setWrapR(Value v) {
            if (v == Value.WRAP || v == Value.CLAMP) {
                mWrapR = v;
            } else {
                throw new IllegalArgumentException("Invalid value");
            }
        }
        static synchronized Sampler internalCreate(RenderScript rs, Builder b) {
            rs.nSamplerBegin();
            rs.nSamplerSet(0, b.mMin.mID);
            rs.nSamplerSet(1, b.mMag.mID);
            rs.nSamplerSet(2, b.mWrapS.mID);
            rs.nSamplerSet(3, b.mWrapT.mID);
            rs.nSamplerSet(4, b.mWrapR.mID);
            int id = rs.nSamplerCreate();
            return new Sampler(id, rs);
        }
        public Sampler create() {
            mRS.validate();
            return internalCreate(mRS, this);
        }
    }
}
