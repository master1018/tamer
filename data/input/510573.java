class BaseObj {
    BaseObj(RenderScript rs) {
        rs.validate();
        mRS = rs;
        mID = 0;
        mDestroyed = false;
    }
    public int getID() {
        return mID;
    }
    int mID;
    boolean mDestroyed;
    String mName;
    RenderScript mRS;
    public void setName(String s) throws IllegalStateException, IllegalArgumentException
    {
        if(s.length() < 1) {
            throw new IllegalArgumentException("setName does not accept a zero length string.");
        }
        if(mName != null) {
            throw new IllegalArgumentException("setName object already has a name.");
        }
        try {
            byte[] bytes = s.getBytes("UTF-8");
            mRS.nAssignName(mID, bytes);
            mName = s;
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    protected void finalize() throws Throwable
    {
        if (!mDestroyed) {
            if(mID != 0 && mRS.isAlive()) {
                mRS.nObjDestroyOOB(mID);
            }
            mRS = null;
            mID = 0;
            mDestroyed = true;
        }
        super.finalize();
    }
    public void destroy() {
        if(mDestroyed) {
            throw new IllegalStateException("Object already destroyed.");
        }
        mDestroyed = true;
        mRS.nObjDestroy(mID);
    }
}
