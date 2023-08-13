public class GsmCellLocation extends CellLocation {
    private int mLac;
    private int mCid;
    public GsmCellLocation() {
        mLac = -1;
        mCid = -1;
    }
    public GsmCellLocation(Bundle bundle) {
        mLac = bundle.getInt("lac", mLac);
        mCid = bundle.getInt("cid", mCid);
    }
    public int getLac() {
        return mLac;
    }
    public int getCid() {
        return mCid;
    }
    public void setStateInvalid() {
        mLac = -1;
        mCid = -1;
    }
    public void setLacAndCid(int lac, int cid) {
        mLac = lac;
        mCid = cid;
    }
    @Override
    public int hashCode() {
        return mLac ^ mCid;
    }
    @Override
    public boolean equals(Object o) {
        GsmCellLocation s;
        try {
            s = (GsmCellLocation)o;
        } catch (ClassCastException ex) {
            return false;
        }
        if (o == null) {
            return false;
        }
        return equalsHandlesNulls(mLac, s.mLac) && equalsHandlesNulls(mCid, s.mCid);
    }
    @Override
    public String toString() {
        return "["+ mLac + "," + mCid + "]";
    }
    private static boolean equalsHandlesNulls(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals (b);
    }
    public void fillInNotifierBundle(Bundle m) {
        m.putInt("lac", mLac);
        m.putInt("cid", mCid);
    }
    public boolean isEmpty() {
        return (mLac == -1 && mCid == -1);
    }
}
